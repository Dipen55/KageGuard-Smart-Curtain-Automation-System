# 🌌 KageGuard – Smart Curtain Automation System

KageGuard is an intelligent dual-Arduino based smart curtain automation system designed to automate curtain movement using real-time environmental monitoring. The system dynamically responds to ambient light, rain conditions, and fire hazards to improve comfort, safety, and energy efficiency.

---

# 📌 Project Overview

KageGuard eliminates the need for manual curtain control by continuously monitoring environmental conditions and automatically adjusting curtain position accordingly.

The project uses:
- **Arduino I (Master)** for sensor acquisition and motor control
- **Arduino II (Slave)** for LCD display and system monitoring

The system integrates multiple sensors with intelligent priority-based decision logic for reliable automation.

---

# 🚀 Features

✅ Automatic curtain adjustment based on ambient light  
✅ Rain detection with automatic curtain closure  
✅ Fire/flame detection with emergency curtain opening  
✅ Real-time LCD status monitoring  
✅ Dual-Arduino serial communication architecture  
✅ Smooth step-by-step motor movement  
✅ Stability filtering to prevent false triggering  
✅ Intelligent priority-based control logic  
✅ Real-time sensor value display  
✅ Expandable smart home automation platform  

---

# 🛠️ Hardware Components

| Component | Purpose |
|---|---|
| Arduino UNO ×2 | Master–Slave architecture |
| LDR Sensor | Ambient light detection |
| Flame LDR | Flame/fire light detection |
| TMP Temperature Sensor | Temperature monitoring |
| Rain Sensor / Push Button | Rain detection |
| L293D Motor Driver | Motor direction and speed control |
| DC Motor | Curtain movement |
| 16×2 LCD Display | System status monitoring |
| Potentiometer | LCD contrast control |

---

# ⚙️ System Architecture

## 🔹 Arduino I (Master)
Responsible for:
- Reading all sensor inputs
- Applying decision logic
- Controlling curtain motor movement
- Sending serial data packets

## 🔹 Arduino II (Slave)
Responsible for:
- Receiving UART serial packets
- Parsing sensor data
- Displaying system information on LCD
- Mirroring system decisions

Communication between both Arduino boards is established using:
- **UART Serial Communication**
- **9600 Baud Rate**

---

# 🧠 Working Principle

The system continuously performs the following operations:

1. Read ambient light intensity using LDR
2. Read temperature using TMP sensor
3. Detect flame intensity using flame LDR
4. Detect rain conditions
5. Apply stability filtering to remove sensor noise
6. Determine curtain target position
7. Drive motor incrementally
8. Send data to Arduino II
9. Update LCD status display

---

# 🔥 Decision Priority Logic

| Priority | Condition | Action |
|---|---|---|
| 1 | Flame detected + Temperature > 45°C | Fully Open Curtain |
| 2 | Rain detected | Fully Close Curtain |
| 3 | Normal light condition | Match curtain level to light intensity |

---

# 🌞 Light Level Mapping

| Analog Range | Level | Curtain State |
|---|---|---|
| 0 – 78 | 0 | Fully Open |
| 79 – 156 | 1 | Mostly Open |
| 157 – 234 | 2 | Partially Open |
| 235 – 312 | 3 | Half Open |
| 313 – 390 | 4 | Partially Closed |
| 391 – 468 | 5 | Mostly Closed |
| 469 – 1023 | 6 | Fully Closed |

---

# 🔌 Serial Communication Format

Arduino I transmits structured UART packets:

```cpp
S:<analog>,L:<level>,P:<position>,R:<rain>,T:<temp>,F:<flame>
```

## 📌 Example

```cpp
S:340,L:4,P:3,R:0,T:27,F:320
```

### Packet Description

| Field | Meaning |
|---|---|
| S | Raw LDR sensor value |
| L | Target curtain level |
| P | Current curtain position |
| R | Rain state |
| T | Temperature |
| F | Flame sensor reading |

---

# ⚡ Motor Control Logic

The system uses:
- **L293D Motor Driver**
- **PWM Speed Control**
- **Step-by-Step Curtain Movement**

### Motor Control Features
- Smooth incremental movement
- Controlled opening and closing
- Pause between movement steps
- Prevents mechanical overrun

---

# 📟 LCD Display Functions

The 16×2 LCD displays:
- Real-time sensor values
- Curtain position
- System decisions
- Warning conditions

### Example Messages
- `Rain Detected`
- `Closing Curtain`
- `FLAME DETECTED`
- `Opening Curtain`
- `Light Increased`
- `Light Decreased`

---

# 🧪 Testing & Validation

| Test Case | Status |
|---|---|
| Light Increase Response | ✅ Pass |
| Light Decrease Response | ✅ Pass |
| Rain Detection | ✅ Pass |
| Flame Detection | ✅ Pass |
| Stability Filtering | ✅ Pass |
| Serial Communication | ✅ Pass |
| Motor Direction Control | ✅ Pass |
| Long Duration Testing | 🔄 Ongoing |

---

# 📂 Project Structure

```bash
KageGuard/
│
├── Arduino_1/
│   └── Sensor_Motor_Controller.ino
│
├── Arduino_2/
│   └── LCD_Display_Controller.ino
│
├── Circuit_Diagram/
│   └── KageGuard_Circuit.png
│
├── Documentation/
│   └── Project_Report.pdf
│
└── README.md
```

---

# 💻 Technologies Used

- Arduino UNO
- Embedded C
- Arduino IDE
- UART Communication
- L293D Motor Driver
- LCD Interface
- Sensor Integration
- TinkerCAD Simulation

---

# 📈 Future Enhancements

🚀 ESP8266 / ESP32 Wi-Fi Integration  
🚀 Mobile App Control  
🚀 Smart Home Dashboard  
🚀 Voice Assistant Integration  
🚀 RTC-Based Scheduled Automation  
🚀 Multi-Curtain Support  
🚀 OLED Display Upgrade  
🚀 OTA Firmware Updates  
🚀 Cloud Monitoring System  

---

# 🏡 Applications

- Smart Homes
- Office Automation
- Energy Efficient Buildings
- Fire Safety Systems
- Weather Responsive Automation
- IoT-Based Home Systems

---

# 🤝 Contributors

| Name | Role | GitHub |
|---|---|---|
| [Dipendra Teli](https://github.com/Dipen55) | Project Development & Embedded Systems | [Profile](https://github.com/Dipen55) |
| [Neha Yadav](https://github.com/neha-9018) | Research, Testing & Documentation | [Profile](https://github.com/neha-9018) |

---

# 📜 License

This project is developed for educational, research, and learning purposes.

---

# ⭐ Conclusion

KageGuard demonstrates a practical implementation of smart home automation using affordable hardware and intelligent embedded control systems. The project successfully combines environmental sensing, motor automation, and real-time monitoring into a reliable and scalable smart curtain solution.

The modular architecture and future scalability make KageGuard an excellent foundation for advanced IoT-based smart home systems.

---
