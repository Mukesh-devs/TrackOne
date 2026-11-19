# SMS permission and Google Play

TrackOne can detect bank transaction SMS (money debited or credited) and list them for the
user to accept or decline. This note covers what Play Console needs and what the app does,
so the declaration and the privacy policy stay accurate.

## What the app requests

- **`RECEIVE_SMS` only.** New messages arrive as a broadcast, so the app never needs
  `READ_SMS` and never reads the inbox. Do **not** declare `READ_SMS`, `SEND_SMS`,
  `RECEIVE_MMS` or any other SMS permission.
- The permission is requested only when the user turns on **Expense → Settings →
  Auto-detect bank transactions** (off by default), and only after an in-app disclosure
  explains the use. If the user turns it off or revokes the permission, the receiver is
  disabled and does nothing.

## Play policy fit

The feature fits Google Play's **"SMS-based money management"** permitted use: apps that
track and manage a user's budget from financial SMS.

It still needs a **Permissions Declaration Form** in Play Console before release. That is a
manual step for the app owner:

1. Play Console → App content → Sensitive permissions (SMS and Call Log).
2. Core functionality: *SMS-based money management*.
3. Explain the core use in a sentence or two, e.g. "TrackOne detects bank debit and credit
   alerts on the device and lets the user accept them into their expense tracker."
4. Attach a short demo video showing: the disclosure dialog, the system permission prompt,
   an incoming bank SMS appearing under the bell on the Expense home, and Accept / Decline.

## How the data is handled (for the declaration and privacy policy)

- Only messages from known bank sender IDs are looked at. Every other SMS is dropped at once,
  before anything is parsed, stored or logged.
- From a bank alert the app keeps only: **name, amount, credit/debit, and the time it was
  received**. The message body, sender, account number, balance and reference numbers are
  never stored.
- Processing happens entirely on the phone (plain Kotlin and regex, no network, no ML).
- Detected items stay in an encrypted local database (SQLCipher, key protected by the Android
  Keystore) that is excluded from cloud backup and device transfer.
- Nothing is sent anywhere unless the user taps **Accept** on an item. Accepted items are
  sent to the TrackOne backend as a normal expense or income entry, then deleted from the
  device. Declined items are deleted. Unreviewed items are deleted after 30 days.
- Non-financial SMS is never copied, stored or shared.

The privacy policy must describe this use of SMS in the same terms before the release that
ships the feature.

## Owner checklist

- [ ] Verify and extend the bank sender list in
      `feature-expense/src/main/java/com/dev/trackone/feature/expense/sms/parser/BankSenderRegistry.kt`
      with the sender IDs of real bank alerts on your own phone.
- [ ] Apply backend migration `0003_expense_idempotency_key.sql` and deploy the backend
      change from `feature/expense-idempotency-key` (the app works without it, but retries
      after a lost response could then create a duplicate).
- [ ] Submit the Permissions Declaration Form with the demo video.
- [ ] Update the privacy policy.
- [ ] Sideloaded builds on Android 13+: the SMS prompt may be blocked as a restricted
      setting until the user allows it in App info; the app shows help text for this case.

## Manual check on an emulator

```
adb emu sms send VM-HDFCBK-T "Rs.250.00 debited from a/c XX1234 to TEST SHOP"
```

The emulator delivers the sender without hyphens (`VMHDFCBKT`); the app accepts both forms.
The app must have been opened at least once since install, or Android will not deliver the
broadcast to it.
