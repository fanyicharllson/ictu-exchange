# ICTU-Ex: Smart Student Marketplace 🎓📱

**ICTU-Ex** is a secure, sensor-driven Android marketplace built exclusively for students of **The ICT University (ICTU), Cameroon**. The platform allows students to buy, sell, or swap academic resources — textbooks, electronics, hostel gear — within a trusted campus community.

The application directly addresses two major student pain points: the high cost of study materials and unreliable internet connectivity. It achieves this through an **offline-first architecture** and deep **hardware sensor integration**, making it practical in real campus conditions.

---

## 👥 Project Team

| Role | Name |
|------|------|
| Developer | FANYI CHARLLSON FANYI |
| Tester | TELLO KOMBOU ADRIEN NATHAN  |

---

## 🚀 Key Features

**Verified Student Marketplace**
A dedicated trading hub for academic and campus essentials, restricted to verified ICTU students.

**Biometric Authorization**
Sensitive actions — such as viewing a seller's contact or deleting a listing — are protected using Android's `BiometricPrompt` API (fingerprint authentication).

**Smart QR Transactions**
Item delivery is confirmed by scanning a unique QR code via `CameraX`, creating a digital proof of hand-to-hand exchange between students.

**Intelligent Dark Mode (Light Sensor)**
The app reads ambient light levels (in lux) using the device's light sensor and automatically switches to Dark Mode when lighting drops below a set threshold — reducing eye strain during night study sessions.

**Shake-to-Report (Accelerometer)**
A gesture-based fraud reporting tool. Shaking the device triggers an instant report dialog, allowing students to flag suspicious listings without navigating menus.

**Offline-First Data Access**
Listings browsed while online are cached locally using Room, so students can still view items even without an active internet connection.

---

## 🛠 Tech Stack & Architecture

| Component | Technology |
|-----------|------------|
| Language | Kotlin (100%) |
| UI Framework | Jetpack Compose |
| Architecture | MVVM (Model-View-ViewModel) |
| Local Database | Room Persistence Library |
| Remote Backend | Firebase (Auth, Firestore, Cloud Storage) |
| Dependency Injection | Hilt |
| Image Loading | Coil |

---

## 🧪 Testing Strategy

The project includes a comprehensive automated test suite designed to validate both UI behaviour and hardware sensor logic — since sensors cannot be physically triggered in a test environment, custom simulation techniques are used.

**Unit Testing (JUnit 5)**
Tests cover ViewModel logic and repository data handling in isolation, ensuring business rules behave correctly independent of the UI.

**UI Testing (Espresso)**
End-to-end user flows — including student login and item listing submission — are automated to catch regressions early.

**Sensor Simulation**
Because physical hardware cannot be controlled during automated tests, sensor inputs are simulated programmatically:

- *Light Sensor:* Feeds a lux value below 10 into the sensor pipeline and verifies the UI switches to Dark Theme.
- *Biometric:* Mocks both success and failure authentication callbacks to confirm the security prompt responds correctly in each case.
- *Accelerometer:* Injects coordinate change values that match a shake pattern, triggering the Shake-to-Report dialog to confirm it opens as expected.

---

## 📂 Project Structure

The codebase is organised into the following top-level packages under `com.ictu.ex`:

- **data** — handles all data operations: Room database entities and DAOs, Firebase data sources, and repository classes that the ViewModels depend on.
- **di** — contains Hilt dependency injection modules that wire together the application's components at runtime.
- **sensors** — encapsulates `SensorManager` implementations for the light sensor and accelerometer, providing clean observable interfaces to the rest of the app.
- **ui** — holds all Jetpack Compose screens, their associated ViewModels, and theme definitions (including the automatic Dark/Light theme switching logic).
- **utils** — shared helper classes for QR code generation and scanning, and the biometric authentication wrapper.

---

## ⚙️ Getting Started

1. Clone the repository.
2. Open the project in Android Studio (Hedgehog or later recommended).
3. Connect your Firebase project and place the `google-services.json` file in the `/app` directory.
4. Build and run on a physical device or emulator (API 26+).

> **Note:** Biometric and sensor features require a physical device for full functionality. The test suite uses simulated data and runs on both emulators and physical devices.
