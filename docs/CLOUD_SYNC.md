# Cloud-first + Room cache

The TrackOne Worker API is the source of truth. Room is an offline cache in
front of it — it is never written to directly by a feature, only by the
repository that owns it.

## Reads

```
UI ──collect──► Repository ──► Room (emits immediately)
                    └────────► API (background) ──► dao.replaceAll(...) ──► Room re-emits
```

`Flow.cachedWithRemoteRefresh` (in `:core:network`) launches the fetch in its own
coroutine rather than in `onStart`, so a slow or dead network never delays the
first cached emission. A failed fetch is swallowed by design: the user keeps
seeing cached data.

Two details worth knowing:

- **Whole-table refresh.** Even a range-scoped read (`getByDateRange`) refreshes
  every row. Fetching just the visible range and then calling `replaceAll` would
  evict every other month from the cache.
- **All pages.** `fetchAllPages` walks `limit`/`offset` until `meta.hasMore` is
  false. Without it the cache would truncate at the server's page size and the
  next `replaceAll` would delete the rows that never arrived.

`RemoteRefreshCoordinator` coalesces these background fetches: identical keys are
serialised, and a key that succeeded within 30 s is skipped. Writes call
`invalidate`, so the read right after a save always goes to the network.
Pull-to-refresh calls `repository.refreshFromCloud()`, which bypasses the window
entirely and surfaces failures.

## Writes

API first, Room second, and **nothing is written locally if the call fails** —
the exception reaches the ViewModel, which puts it in `saveError` /
`actionMessage` for the module's snackbar. Responses are written to Room verbatim
so ids and timestamps always match the server.

`DELETE` treats `404` as success: the row is gone, which is the state the caller
wanted, and failing would strand a deleted row in the cache.

## Server address

Never hardcoded. Resolution order:

1. `local.properties` → `trackone.api.baseUrl=https://…` (per machine, git-ignored)
2. `gradle.properties` / `-Ptrackone.api.baseUrl=…`
3. `http://10.0.2.2:8787/` — the emulator's view of `wrangler dev` on the host

That value is only the **seed** compiled into `BuildConfig.DEFAULT_API_BASE_URL`.
At runtime it lives in `ApiConfigStore` (SharedPreferences) and is editable from
**Settings → Cloud account → Server**, so moving the Worker to a new domain needs
neither a code change nor a rebuild. `BaseUrlInterceptor` rewrites the origin of
every request from that value; Retrofit is built with a placeholder base that is
never contacted.

Plain HTTP is allowed in debug builds only (`app/src/debug/AndroidManifest.xml`),
so the emulator can reach `wrangler dev`. Release builds stay HTTPS-only.

## Accounts

Every API call carries a 30-day bearer token from `/auth/signin`, stored in
`EncryptedSharedPreferences` (falling back to plain prefs if the device keystore
is unavailable). `AuthInterceptor` skips the two endpoints that issue tokens —
sending a stale one while signing in would get the sign-in itself rejected — and
clears the token on any other `401`.

Cached rows carry no account marker, so **signing out clears the local
database**, and it is downloaded again on the next sign-in.

Two data-safety measures live in `CloudAccountManager`:

- **First sign-in adopts pre-cloud data.** A database that predates the API would
  otherwise be wiped the moment the server answered with an empty list, so when
  the account is empty and the device is not, the local rows are uploaded first.
  A JSON snapshot (`filesDir/pre_cloud_backup.json`, same shape as the Drive
  backup) is written beforehand either way.
- **A Drive restore is pushed to the cloud.** That path writes straight to Room;
  without the push, the next background refresh would wipe the restored rows.

## Adding an endpoint

1. DTOs in `:core:network` → `model/` (`@JsonClass(generateAdapter = true)`).
   Request bodies omit `id`/`createdAt`/`updatedAt` — those are the server's.
2. Method on `TrackOneApi`. Paths are relative and carry no origin.
3. Map DTO → entity/domain in the owning feature's `data/mapper/`.
4. Call it through `apiCall {}` / `apiDelete {}` / `fetchAllPages {}` so every
   failure arrives as a `TrackOneApiException` with a user-presentable message.

Request bodies are serialised **with nulls included**, which is what lets
clearing an optional field actually clear it server-side instead of the key being
dropped and the partial update keeping the old value.
