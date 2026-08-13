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

That value is compiled into `BuildConfig.DEFAULT_API_BASE_URL` and held at
runtime by `ApiConfigStore`. `BaseUrlInterceptor` rewrites the origin of every
request from it; Retrofit is built with a placeholder base that is never
contacted.

Plain HTTP is allowed in debug builds only (`app/src/debug/AndroidManifest.xml`),
so the emulator can reach `wrangler dev`. Release builds stay HTTPS-only.

## Accounts

Every API call carries a **7-day** bearer token from `/auth/signin`, stored in
`EncryptedSharedPreferences` (falling back to plain prefs if the device keystore
is unavailable). The app reads the absolute `expiresAt` from the response, so
the server can change the lifetime without a client release.

`AuthInterceptor` skips the two endpoints that issue tokens — sending a stale
one while signing in would get the sign-in itself rejected. Everywhere else a
token is retired two ways, both ending with the sign-in screen:

- **Past its expiry** — dropped *before* the request goes out. The `401` is a
  foregone conclusion, and offline there would be no `401` at all, only an
  `IOException`, which would strand the dead token indefinitely.
- **A `401` came back** — server-side expiry, or `JWT_SECRET` rotated.

Cached rows carry no account marker, so **signing out clears the local
database**, and it is downloaded again on the next sign-in.

Three data-safety measures live in `CloudAccountManager`:

- **First sign-in adopts pre-cloud data.** A database that predates the API would
  otherwise be wiped the moment the server answered with an empty list, so when
  the account is empty and the device is not, the local rows are uploaded first,
  one `POST` per row. A JSON snapshot (`filesDir/pre_cloud_backup.json`) is
  written beforehand either way. A row the server rejects is logged and skipped
  rather than abandoning the rest of the adoption half-done.
- **The cache records its owner.** A session that lapses instead of signing out
  leaves the rows in place, so the owning user id is stored beside them and
  checked on every sign-in; a mismatch clears the cache first. Without it the
  next account would inherit those rows — and the adoption above would upload
  them into that account.
- **Reset-app signs out.** The "forgot PIN" escape hatch would otherwise be a way
  *around* App Lock: wipe the local data, and the account simply downloads it
  again with no PIN in the way.

## Credential passwords

`GET /credential-items` masks every password as `********` — there is no
`reveal` switch. Only `GET /credential-items/{id}` returns the real value, so
`CredentialRepositoryImpl.hydratePasswords` fetches it one row at a time before
anything reaches Room. A cache full of asterisks would be worse than no cache.

Rows whose server `updatedAt` still matches the cached copy keep the password
already on the device, so the routine background refresh costs **zero** extra
requests and only new or edited rows pay for a call. A cached mask never counts
as reusable, which heals rows written by a build that still trusted `reveal`.

A failure mid-hydration propagates instead of falling back to the mask: keeping
the previous cache always beats replacing a working password with asterisks.

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
