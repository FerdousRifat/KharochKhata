# Lab Report Outline (PDF, 10-15 pages)

Use this as your skeleton. Rewrite the explanations in your own words and add your own screenshots. The instructor expects you to understand every part, so do not paste it as is.

## Page plan

| Section | Pages | What to write |
|---|---|---|
| 1. Cover page | 1 | Course (Mobile Application and Development Lab 0613-410), project title "KharochKhata: Expense Tracker", your name, your ID |
| 2. Introduction | 1 | The problem (people forget where money goes) and the goal (a simple app to record, review and share expenses) |
| 3. Features with screenshots | 3-4 | One screenshot per screen and a 1-2 line caption each (list below) |
| 4. Components used, alternatives, and why | 4-5 | The table below, expanded: what it does in your app, alternatives, 2-3 lines of reasons. This is the main part |
| 5. Firebase database structure | 1 | The tree from `README.md` and the security rules |
| 6. GitHub link | 0.5 | Repository URL and the Release v1.0 URL |
| 7. Problems faced and how you solved them | 1 | Write real problems you met (see ideas below) |
| 8. Conclusion and references | 1 | What you learned, what you would add next, references |

## Screenshots to take (while testing)

1. Splash
2. Login (with an error shown by `setError`)
3. Register
4. Home with several expenses and the total
5. Home when empty
6. Add expense form (with photo)
7. Delete confirmation `AlertDialog`
8. Expense detail
9. Share chooser (implicit Intent)
10. Firebase console: Authentication users and Realtime Database tree
11. GitHub: commit history and the Release page

## Section 4: components table (starting point)

Include only what you really use. Everything below is used in this project.

| Component | What it does in KharochKhata | Alternatives | Why chosen |
|---|---|---|---|
| Activities + Intents | 6 screens; Home sends the expense id to Detail and Edit with `putExtra()` | Fragments + Navigation Component | Easiest to understand for a first app, each screen is one class and one layout |
| ConstraintLayout | Splash screen layout | LinearLayout, RelativeLayout | Places the logo and texts relative to each other without nesting |
| LinearLayout + ScrollView | Login, Register, Add/Edit, Detail forms | ConstraintLayout, RelativeLayout | Forms are simple vertical stacks, and ScrollView keeps them usable when the keyboard opens |
| CoordinatorLayout | Home root, so the FAB and SnackBar work together | FrameLayout | The SnackBar can move the FAB up instead of covering it |
| CardView | Summary card, list rows, detail card | MaterialCardView, plain LinearLayout with a background | Rounded corners and elevation with little code |
| TextInputLayout + TextInputEditText | All text fields, with `setError()` for validation | EditText alone | Material look, floating hint, password show/hide icon |
| Spinner | Category choice | Radio buttons, chips, AutoCompleteTextView | Eight categories fit in a drop-down without using screen space |
| DatePickerDialog | Choose the expense date | Material date picker, typing the date | Built in, no extra library, avoids wrong date formats |
| RecyclerView + Adapter + ViewHolder | Scrolling expense list | ListView + ArrayAdapter, GridView | Re-uses row views, smooth with long lists |
| Model class (`Expense`) | Holds one record, converts to and from Firebase JSON | Plain `Map` | Typed fields and getters, Firebase maps it automatically |
| FloatingActionButton | Opens the Add screen | Toolbar menu button | Standard Material pattern for the main action |
| AlertDialog | Confirm delete and logout | Bottom sheet, separate Activity | Quick yes/no without leaving the screen |
| SnackBar / Toast | Errors and success messages | Dialogs, inline text | Short messages that do not block the user |
| Firebase Authentication | Register, login, logout, stay logged in | Google Sign-In, phone OTP, own server login | No server needed, handles passwords safely and keeps the session |
| Firebase Realtime Database + `ValueEventListener` | Stores expenses per user, list updates live | Cloud Firestore, SQLite, Room | Simple tree of data and real-time updates with a few lines; Room/SQLite would not sync between devices |
| SharedPreferences | "Remember me" and saved email | SQLite, Room, text file | A single true/false value and a string, so a key-value store is enough |
| Camera and Gallery (`ActivityResultContracts`) | Receipt photo | CameraX, a third-party image picker | Uses the phone's own apps, needs no permission and little code |
| Base64 text in the database | Stores the small photo | Firebase Storage, local file path | Storage needs a paid plan; a 480 px photo is small enough for the database |
| SensorManager + accelerometer | Shake to add an expense | Proximity, light sensor, gyroscope | Shake is quick and natural for "add something"; accelerometer exists on nearly every phone |
| Implicit Intents (`ACTION_SEND`, `ACTION_VIEW`) | Share an expense, open the help web link | Explicit Intent to one app, custom share dialog | Lets the user pick any app, nothing hard-coded |
| Material Design 3 theme | Colors, buttons, text fields | AppCompat theme, custom styles | Consistent modern look with theme attributes |
| Git + GitHub (+ Release v1.0) | Version history, shared code, APK download | GitLab, Bitbucket | Free, public, and required by the course |

## Section 7: problem ideas

Write only what really happened to you. Common ones in this project:

- Gradle sync failed or was slow, solved by good internet / letting Android Studio upgrade the plugin
- `google-services.json` missing or package name mismatch
- Database permission denied until the rules were published
- App crashed on empty or wrong input, solved with validation
- Photo too large for the database, solved by resizing to 480 px and compressing
- Shake fired many times, solved with a one-second gap between triggers
- Camera does not exist on the emulator

## Viva notes: be ready to explain

1. Why does the app use `putExtra()` and what does the Detail screen do with the id?
2. How does "stay logged in" work? (Firebase keeps the user; Splash checks `getCurrentUser()`.)
3. What is a `ValueEventListener`? Why add it in `onStart()` and remove it in `onStop()`?
4. What do the Adapter and the ViewHolder each do?
5. How does shake detection work? What is the threshold, and why the 1-second gap?
6. Why is the photo stored as Base64 and what are the downsides? (Larger database, slower reads; Storage would be better in a bigger app.)
7. What is an implicit Intent? Where are two used?
8. Why are the Firebase rules written with `$uid === auth.uid`?
9. How does Add mode become Edit mode in `AddEditExpenseActivity`?
10. What does `setValue()` do when the key already exists vs a new `push()` key?

Tip: change one small thing yourself (a category, a color, the shake threshold), rebuild, and run it. It makes the viva much easier.
