# KharochKhata: Project Plan

Course: Mobile Application and Development Lab (0613-410), Final Lab Project (individual)
App: **KharochKhata**, a personal expense tracker (Android, Java + XML, Firebase)
Plan written: Oct 25, 2026. Lab classes from this date are all project work, and the instructor checks progress every lab class.

## 1. Goal

One Android app that solves a real problem: people forget where their money goes. The app lets a user log every expense with a category, amount, date, note and receipt photo, see the running total, and share an expense with someone.

## 2. Scope: the 12 required features and where each lives

| # | Required feature | How KharochKhata covers it | File |
|---|---|---|---|
| 1 | 5+ screens with navigation | Splash, Login, Register, Home, Add/Edit, Detail (6 screens), `putExtra()` passes the expense id | all `*Activity.java` |
| 2 | Layouts and basic views | ConstraintLayout, LinearLayout, TextView, ImageView, Button, CardView | `res/layout/*` |
| 3 | Input validation | `TextInputEditText` + `setError()` on login, register, add/edit | `LoginActivity`, `RegisterActivity`, `AddEditExpenseActivity` |
| 4 | Model class | `Expense` with getters/setters | `Expense.java` |
| 5 | Scrolling list | `RecyclerView` + `ExpenseAdapter` + `ViewHolder` | `ExpenseAdapter.java` |
| 6 | CRUD with dialog and FAB | FAB adds, tap views, edit button edits, delete uses `AlertDialog` | `MainActivity`, `ExpenseDetailActivity` |
| 7 | Firebase login | Register, login, logout, stay logged in (checked in Splash) | `FirebaseAuth` |
| 8 | Firebase database, real-time list | Realtime Database + `ValueEventListener` at `users/{uid}/expenses` | `MainActivity` |
| 9 | Setting saved on phone | "Remember me" saves the email in `SharedPreferences` | `LoginActivity` |
| 10 | Take or pick a photo | Camera or Gallery for the receipt, shown in `ImageView` | `AddEditExpenseActivity` |
| 11 | One sensor | Accelerometer: shake the phone on Home to open Add Expense | `MainActivity` |
| 12 | Open another app | `ACTION_SEND` shares an expense, `ACTION_VIEW` opens a help web link from Home | `ExpenseDetailActivity`, `MainActivity` |

Also required: custom app name and icon, Material theme, SnackBar/Toast for success and error. All present.

Bonus ideas if time remains (do these only after everything above works): Room cache, navigation drawer or tabs, Google Maps for where the expense happened, a weather or currency REST API, Google Sign-In.

Out of scope: multi-currency, budgets and charts, offline-first sync, web version.

## 3. Phases

Each phase has an exit criterion. Do not start the next phase until the current one is met.

### Phase 0: Setup (Day 1)
- Install or confirm Android Studio, create the Firebase project, enable Email/Password auth and Realtime Database, download `google-services.json`.
- Create the public GitHub repo, push the empty project on day one (GitHub marks are 15).
- **Exit:** the app builds, runs on an emulator or phone, and the first commit is on GitHub.

### Phase 1: UI skeleton (Days 2-4)
- All 6 activities, layouts, theme, icon, navigation with `Intent` and `putExtra()`.
- **Exit:** you can tap through every screen with fake data.

### Phase 2: Firebase Auth (Days 5-6)
- Register, login, logout, stay logged in, validation, Remember me.
- **Exit:** a new user registers, closes the app, reopens, and lands on Home without logging in again.

### Phase 3: Data and CRUD (Days 7-10)
- `Expense` model, Realtime Database read/write, `RecyclerView`, FAB, edit, delete with `AlertDialog`.
- **Exit:** add an expense on one device and see it appear on a second device without refreshing.

### Phase 4: Device features (Days 11-13)
- Photo (camera and gallery), shake sensor, share (implicit intent).
- **Exit:** all 12 features demonstrated once, start to finish.

### Phase 5: Polish and testing (Days 14-16)
- Empty states, wrong-input tests (nothing may crash), Snackbars, rotate the screen, no-internet behavior.
- **Exit:** a 10-minute test script runs with no crash.

### Phase 6: Release and report (Days 17-20)
- README, GitHub Release v1.0 with the signed or debug APK, 10-15 page PDF report, viva practice.
- **Exit:** APK downloads from the Release page and installs on a clean phone; report PDF is complete.

Adjust the day numbers to your real lab schedule. The instructor checks progress every lab class, so aim to have a visible milestone at each class.

## 4. Dependencies

```
Firebase project + google-services.json
        |
        v
Phase 0 setup ---> Phase 1 UI ---> Phase 2 Auth ---> Phase 3 CRUD ---> Phase 4 Device features ---> Phase 5 Polish ---> Phase 6 Release
                                         |                |
                                         |                +--> photo storing (Base64 in DB) needs the model fields from Phase 3
                                         +--> every database path uses the logged-in user id
```

Critical path: Firebase setup -> Auth -> CRUD. Everything else (photo, sensor, share) can be built in any order once CRUD works.

External dependencies:
- Internet access in the lab for Gradle sync and Firebase.
- A Google account for Firebase.
- A physical Android phone helps for camera and sensor, because emulator support is weak. Shake can be tested in the emulator's Virtual sensors panel.
- Instructor check-ins every lab class.

## 5. Risks

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Android Studio too slow or crashes on your laptop | Medium | High | Use lab PCs as the fallback, push to GitHub daily so work moves between machines |
| Build fails: missing or wrong `google-services.json` or SHA settings | High (first-time) | High | Package name must match exactly (`com.kharochkhata.app`). Re-download the file after changing it |
| Gradle sync fails (offline, version mismatch) | Medium | High | Sync once on good internet, let Android Studio upgrade the Gradle/AGP version if it offers |
| Realtime Database rules left locked or fully open | Medium | Medium | Use the rules in `README.md` (each user can only read/write their own node) |
| Firebase Storage needs a paid plan for photos | High | Medium | This project stores a small compressed Base64 photo in the database instead. Mention the alternative (Firebase Storage) in the report |
| Large photo makes the database write fail or the list slow | Medium | Medium | The code resizes to 480 px and compresses to JPEG before saving |
| Camera or sensor behaves differently on the emulator | Medium | Low | Test on a real phone, use the emulator's Virtual sensors for shake |
| Crash on empty or bad input (marks lost for no-crash rule) | Medium | High | Validation in every form, test with empty, zero, negative and very long values |
| Not able to explain code in the viva | Medium | High | Read every file, run it, change something small yourself (colors, categories, text). Use the Viva Notes in `REPORT_OUTLINE.md` |
| Running out of time | Medium | High | Finish the 12 required features first, drop all bonus items if a phase slips |
| Repo made private or deleted after submission | Low | High | Keep public, never delete |
| Report written in a rush at the end | High | Medium | Take screenshots and write the component notes as each phase finishes |

## 6. Marks map (100)

| Part | Marks | What earns it |
|---|---|---|
| Required features working | 40 | All 12 features work, about 3.3 marks each |
| UI and code quality | 15 | Consistent Material design, clear names, comments, no copy-paste duplication |
| GitHub use | 15 | Commits from day one, clear messages, README, Release v1.0 with APK |
| Lab report | 15 | 10-15 pages, components table with alternatives and reasons |
| Demo and viva | 15 | You can explain each feature and each file |

## 7. Open questions to confirm with the instructor

1. Is a debug APK acceptable for the Release, or must it be signed?
2. Is storing a compressed photo as Base64 in the database acceptable, or is Firebase Storage expected?
3. Exact date of the final lab (demo and viva), so the day numbers above can be fixed.
4. Does working on your own laptop need to be shown at each progress check to earn the bonus mark?
