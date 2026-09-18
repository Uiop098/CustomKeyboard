# ⌨️ CustomKeyboard — Android Input Method (IME)

A modern, lightweight, highly customizable on-screen soft keyboard for Android built with Kotlin and the Android InputMethodService API.

![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-green)
![Language](https://img.shields.io/badge/Language-Kotlin-purple)
![License](https://img.shields.io/badge/License-MIT-blue)

---

## 🚀 Features

- **Full QWERTY Keyboard:** Standard alphanumeric layout with secondary long-press symbol characters.
- **🔣 Numeric & Symbol Pads:** Dedicated numeric keypad and comprehensive punctuation screens.
- **🔊 Sound & Haptic Feedback:** Mechanical click sound effects on keypress and haptic vibration feedback.
- **🎨 Theme Customization:** Dark, Light, Cyberpunk, and Minimalist color palettes.
- **😀 Emoji Support:** Integrated emoji keyboard category selector.
- **⚡ Battery & Memory Optimized:** Native Android Service implementation with minimal memory footprint and zero background battery drain.

---

## 🛠️ Build & Installation

### Option 1: Android Studio (Recommended)
1. Clone the repository:
   ```bash
   git clone https://github.com/Uiop098/CustomKeyboard.git
   ```
2. Open the project in **Android Studio**.
3. Allow Gradle to sync dependencies.
4. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. The output APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

### Option 2: Command Line (Gradle)
```bash
./gradlew assembleDebug
```

### Enabling the Keyboard on Android
1. Install the generated APK on your device.
2. Go to **Settings > System > Languages & Input > On-screen Keyboard**.
3. Enable **Custom Keyboard** and select it as your default input method.

---

## 📜 License

MIT License. Developed by **Uiop098**.
