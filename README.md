# 📱 LegmaSky — Android Companion App

[![Hippocratic License HL3-CL-ECO-LAW-MIL-SV](https://img.shields.io/static/v1?label=Hippocratic%20License&message=HL3-CL-ECO-LAW-MIL-SV&labelColor=5e2751&color=bc8c3d&Style=flat)](https://firstdonoharm.dev/version/3/0/cl-eco-law-mil-sv.html)
![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Java_%2F_Kotlin-007396?style=flat&logo=openjdk&logoColor=white)
![Backend](https://img.shields.io/badge/FastAPI-Backend-009688?style=flat&logo=fastapi&logoColor=white)
![Architecture](https://img.shields.io/badge/MVVM_%2F_ViewBinding-UI-FF6F00?style=flat&logo=androidstudio&logoColor=white)
![Database](https://img.shields.io/badge/Cache-SharedPreferences_%2B_Gson-FFB300?style=flat&logo=sqlite&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-Dragonyx-181717?style=flat&logo=github&logoColor=white)
![Status](https://img.shields.io/badge/Status-Active-2ECC71?style=flat&logo=git&logoColor=white)

**LegmaSky** is the official Android companion application designed to interface with the **LegmaMiteo** open-source weather station network. The app provides real-time monitoring of environmental parameters, 5-day extended forecasts enhanced by thermal bias auto-correction logic, and an advanced offline cache system with automatic handling of expired days.

---

## 🌍 Why This Project

The **LegmaMiteo** network collects high-resolution micro-meteorological data for hyperlocal nowcasting. **LegmaSky** brings these metrics directly to mobile users, ensuring a smooth, immersive, and **fully resilient** experience even during internet outages or server downtime. 

Featuring a disk-persistent caching mechanism and a dynamic graphical layout inspired by real-time atmospheric conditions, the app guarantees maximum reliability under any circumstance.

---

## 📊 App Interface & Dynamic Design

The user interface dynamically adapts to the current time of day and season using dedicated atmospheric themes (sunrise, morning, afternoon, sunset, night) alongside edge-to-edge layouts optimized to fit seamlessly around modern smartphone camera cutouts and notches.

---

## ✨ Features

- **Real-Time Monitoring** — Instant visualization of temperature, humidity, atmospheric pressure, wind speed, rainfall, and ambient light levels detected by the station.
- **Extended Forecast (5 Days)** — Detailed daily and hourly forecasts synchronized directly with the LegmaMiteo FastAPI backend.
- **Dynamic Seasonal Backgrounds** — Fully immersive, time- and month-aware background gradients.
- **Resilient Offline Cache** — Local disk persistence (`SharedPreferences` + Gson) allowing seamless data review even when the app is restarted or the device is rebooted offline.
- **Smart "No Data" Management** — Automatic calculation of elapsed days during extended server outages, smoothly shifting timeline items and labeling uncovered periods as *"No data"*.
- **Offline Notice Banner** — An intelligent visual alert informing users whether displayed data originates from local cache and how long ago the server was last reached.

---

## 🏗️ Architecture

[LegmaSky Android App]
│
├── REST API (Retrofit) ────────▶ [FastAPI Backend]
│                                        │
└── Local Disk Cache (SharedPreferences) │
│                               ▼
[Offline Fallback]       [Open-Meteo Bias Correction]


---

## 🛠️ Technical Stack

- **UI Framework**: ViewBinding, Material Design 3, ConstraintLayout
- **Networking**: Retrofit 2, Gson
- **Local Persistence**: SharedPreferences (synchronous commit) + Gson for complex object serialization
- **Language**: Java / Kotlin

---

## 🚀 Getting Started

1. Clone the main repository:
   ```bash
   git clone [https://github.com/Dragonyx118/LegmaMiteo](https://github.com/Dragonyx118/LegmaMiteo)

    Open the Android project folder in Android Studio.

    Synchronize Gradle dependencies (Sync Project with Gradle Files).

    Configure your FastAPI server endpoint inside the application's networking layer.

    Build and run on an emulator or a physical device (Android 8.0+ recommended).

📡 Data Policy

All data displayed through the app adheres to the official LegmaMiteo network policy, released under the Creative Commons Attribution-NonCommercial 4.0 license.

Unauthorized uses (mass surveillance, military applications, AI/ML model training without explicit written permission) are strictly prohibited. See [suspicious link removed] for full details.
📄 License

This project is licensed under the terms of the Hippocratic License HL3-CL-ECO-LAW-MIL-SV — see the [suspicious link removed] file in the root of the repository.
👤 Author

Developed by Dragonyx for the LegmaMiteo ecosystem.
