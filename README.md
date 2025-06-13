# Smart Dairy

Smart Dairy is an Android application designed to streamline daily milk collection and payment calculations for dairy farmers and cooperative societies. With an intuitive table-based data entry system, voice-assisted input, persistent storage, and secure access, Smart Dairy makes recording, managing, and reviewing milk entries simple and efficient.

---

## 📄 Description

Smart Dairy allows users to:

* **Record Daily Milk Entries**: Pre-populate rows for each registered member, then fill in fat percentage and milk quantity. Calculations for amount to pay are automatic based on a configurable fat rate.
* **Voice-Assisted Data Entry**: Use natural speech (e.g. `name JLSS fat 5.6 milk 8`) to fill in fields, supporting both English and Hindi numerals.
* **Historical Records**: Save each day’s full set of entries as a timestamped report. Browse past entries (morning/night), search by date, filter by shift, and view detailed tables.
* **PDF Reporting**: Generate a polished PDF of any day’s entries, including totals, user info header, and share via other apps.
* **Member Management**: Add, edit, or remove members. View individual member histories with per-member totals.
* **Security & Lock**: Protect your data with a PIN or biometric unlock on launch.
* **Resilient UX**: Unsaved draft rows are cached in local preferences so accidental closure doesn’t lose in‑progress data.

---

## 🚀 Features

* **Automatic Calculations**: Real-time summary of total milk, average fat, and total payment.
* **Voice Recognition**: Flexible parsing of key‑value speech, fuzzy name matching, multi‑language numbers.
* **Data Persistence**: Room database for entries, lists of entries, members, and fat‑rate settings.
* **Search & Filter**: Search by date text, filter morning/night shifts in history screens.
* **PDF Export & Share**: One‑tap PDF generation and sharing.
* **Offline First**: Fully functional without network.
* **Security**: PIN & biometric unlock; automatic draft saving.

---

## 🛠️ Technology Stack

* Kotlin & Jetpack Compose UI
* Android Architecture Components (ViewModel, LiveData, Flow)
* Room for local database
* DataStore for preferences & draft caching
* Android Speech Recognition API
* Android PDFDocument + FileProvider
* BiometricPrompt for secure unlock

---

## ⚙️ Installation & Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/yourusername/smart-dairy.git
   cd smart-dairy/app
   ```

2. **Open in Android Studio**

   * Import as Gradle project
   * Let Studio sync dependencies

3. **Configure Fat Rate**

   * From **Home** tab, tap **Set Fat Rate** and enter your rate per liter per % fat.

4. **Run on Device/Emulator**

   * Grant `RECORD_AUDIO` for voice input
   * Grant storage if using PDF export

---

## 📋 Usage

* **Add Members**: Navigate to **Members** and tap **Add Member**.
* **Enter Milk Data**: Go to **Add** tab. Rows preloaded with member names; fill fat & qty manually or via voice.
* **Save Entries**: Tap **Save Entries**. A new report is stored and draft table cleared.
* **View History**: Use **All** tab to browse by date & shift; open for details or export PDF.
* **Member History**: Tap a member in **Members** list to see that individual’s past entries.
* **Unlock**: On cold start, enter your PIN or use biometric.

---

## 🔒 Security

* **Secure Launch**: PIN and biometric lock protects your data on app start.
* **ProGuard**: Obfuscation enabled for release builds.

---

## ✒️ Contributing

Feel free to submit issues or pull requests. Please follow the existing code style and include tests where appropriate.

---

## 📜 License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

*Powered by JLSS*
