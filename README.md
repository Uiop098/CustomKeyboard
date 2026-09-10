# Custom Keyboard Android Project

A modern, highly customizable on-screen keyboard for Android featuring sound feedback, haptic vibration, themes, and emoji support.

## 🚀 How to Build the APK

### Option 1: Android Studio (Recommended)
1. Download and extract `CustomKeyboard.zip`.
2. Open Android Studio and select **Open**, then choose the extracted folder.
3. Allow Gradle to sync dependencies.
4. Click **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. The generated `app-debug.apk` will be located in `app/build/outputs/apk/debug/`.
6. Transfer `app-debug.apk` to your Android phone via USB, Google Drive, or email, then tap to install!

### Option 2: Command Line (Gradle)
```bash
chmod +x gradlew
./gradlew assembleDebug
```

### Option 3: GitHub Actions (No Local Setup Required)
1. Push this project to your GitHub repository.
2. The included `.github/workflows/build.yml` will automatically build the APK.
3. Download the built APK directly from the **Actions** tab artifacts!

## 📲 How to Enable on Android
1. Open the **Custom Keyboard** app on your phone.
2. Tap **Step 1: Enable in Settings** and toggle **Custom Keyboard IME** to ON.
3. Tap **Step 2: Switch Input Method** and choose **Custom Keyboard**.
4. Test typing in any messaging app, notes, or browser!
