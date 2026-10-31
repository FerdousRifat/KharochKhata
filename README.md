# KharochKhata

A personal expense tracker for Android. Log what you spend, attach a receipt photo, see your running total, and share any expense with a friend.

- **Course:** Mobile Application and Development Lab (0613-410), Final Lab Project
- **Student:** YOUR NAME (ID: YOUR ID)
- **Tech:** Android Studio, Java + XML, Firebase Authentication, Firebase Realtime Database

## Features

All 12 required features are used for a real purpose in the app.

| # | Feature | Where in the app |
|---|---|---|
| 1 | 6 screens with navigation | Splash, Login, Register, Home, Add/Edit, Expense Details (Intents + `putExtra()`) |
| 2 | Layouts and basic views | ConstraintLayout, LinearLayout, TextView, ImageView, Button, CardView |
| 3 | Input validation | Login, Register, Add/Edit forms use `setError()`, nothing crashes on empty or wrong input |
| 4 | Model class | `Expense.java` |
| 5 | Scrolling list | Home screen, `RecyclerView` + `ExpenseAdapter` + `ViewHolder` |
| 6 | CRUD with dialog and FAB | FAB adds, tap to view, Edit button edits, `AlertDialog` confirms delete |
| 7 | Firebase login | Register, login, logout, stay logged in |
| 8 | Real-time database | Home list updates by itself when data changes (`ValueEventListener`) |
| 9 | Saved setting | "Remember me" on the Login screen (`SharedPreferences`) |
| 10 | Photo | Receipt photo from camera or gallery, shown on the form and detail screen |
| 11 | Sensor | Shake the phone on Home to open Add Expense (accelerometer) |
| 12 | Open another app | Share an expense (`ACTION_SEND`), Help menu opens a web link (`ACTION_VIEW`) |

Also: custom name and icon, Material Design 3 theme, SnackBar and Toast messages.

## How to run the app

1. Install **Android Studio** (a recent stable version). Open this folder with **File > Open**, then wait for Gradle sync. (Android Studio downloads Gradle by itself. If it offers to upgrade the Android Gradle Plugin, you can accept.)
2. Create a Firebase project at <https://console.firebase.google.com>.
3. In the Firebase project, **Add app > Android**. Use the package name `com.kharochkhata.app` exactly.
4. Download `google-services.json` and put it in the `app/` folder (next to `app/build.gradle`).
5. In Firebase, open **Build > Authentication > Sign-in method** and enable **Email/Password**.
6. In Firebase, open **Build > Realtime Database > Create database** and choose a location. Start in locked mode, then open the **Rules** tab and paste:

   ```json
   {
     "rules": {
       "users": {
         "$uid": {
           ".read": "$uid === auth.uid",
           ".write": "$uid === auth.uid"
         }
       }
     }
   }
   ```

   Press **Publish**. Each user can now read and write only their own data.
7. Run the app on an emulator or a real Android phone (Android 8.0 / API 26 or newer).

To test the shake sensor on the emulator: **... (Extended controls) > Virtual sensors > Move** and shake.

## Database structure

```
users
  └── {userId}
        └── expenses
              └── {expenseId}
                    ├── id          : "-Nxyz..."
                    ├── title       : "Lunch"
                    ├── amount      : 250.0
                    ├── category    : "Food"
                    ├── note        : "With friends"
                    ├── photo       : "/9j/4AAQ..."   (small Base64 JPEG, may be empty)
                    └── dateMillis  : 1790000000000
```

## Build the APK for the GitHub Release

1. **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. Click **locate** in the popup. The file is `app/build/outputs/apk/debug/app-debug.apk`.
3. On GitHub: **Releases > Draft a new release**, tag `v1.0`, attach the APK, and publish.

## Project structure

```
app/src/main/java/com/kharochkhata/app/
  SplashActivity.java          logo, then Home or Login
  LoginActivity.java           Firebase login, Remember me
  RegisterActivity.java        Firebase register
  MainActivity.java            list, FAB, delete dialog, shake sensor, menu
  AddEditExpenseActivity.java  form, validation, date picker, photo
  ExpenseDetailActivity.java   view, edit, delete, share
  Expense.java                 model class
  ExpenseAdapter.java          RecyclerView adapter + ViewHolder
  AppUtils.java, ImageUtils.java   small helpers
app/src/main/res/              layouts, strings, colors, theme, icons
```

## Notes

- Photos are shrunk to 480 px and saved inside the database as text, because Firebase Storage needs a paid plan. Alternative: Firebase Storage with a download URL.
- The camera button uses the camera app's preview picture, so it needs no camera permission. The picture is small, which is enough for a receipt note.
