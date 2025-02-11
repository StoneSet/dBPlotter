# DB Plotter Application 🎶📈

![Java](https://img.shields.io/badge/Java-17-blue.svg) ![JavaFX](https://img.shields.io/badge/JavaFX-17-orange.svg) ![License](https://img.shields.io/badge/License-MIT-green.svg) ![Version](https://img.shields.io/badge/Version-0.0.1-blue.svg) 

A JavaFX-based desktop application designed to interface with the Bruel & Kjaer 2306 Level Recorder. This application processes acoustic data from CSV files (ARTA and REW formats) and sends control commands to the device over a serial connection.
This useless and stupid idea allows generating report prints like in the old days. What you see in ARTA or REW will simply be printed on paper. **That's all**.

Compatible with macOS, Windows, Linux, see the release section.

### 🚀 Features

- Import CSV files from ARTA and REW.
- Display a plot of frequency vs. amplitude.
- Downsampling and smoothing for better readability.
- Automatic calculation of min/max frequencies and amplitude.
- Real-time control of the paper speed.
- Serial communication with the B&K 2306 over Arduino.

### For exemple :

![vtt2(1)](https://github.com/user-attachments/assets/ef1c7db4-19b2-415d-81fe-fb26f4bdfdc3)
#### The Bruel & Kjaer 2306 Level Recorder

![411986013-c91da920-ced7-4e5f-b778-70d37f9a10bf(1)](https://github.com/user-attachments/assets/5cf26583-5197-4dc4-ab0a-d7645ac2d9a5)

## 📸 GUI Overview

![image](https://github.com/user-attachments/assets/97562caf-53d2-4f54-a988-3f76031c11ad)
## 🔌 Interface Overview

To interface with the Bruel & Kjaer 2306, I created a DAC interface using an Arduino.

PHOTO du boitier

#### Operation Principle

The software sends serial commands to the Arduino, which uses a 16-bit DAC to convert the digital signal into an analog output within a 0-5V range.  
The Arduino manages the paper feed motor's activation and speed.

A relay closes a connection to start the paper feed motor, while the speed is regulated by a TTL signal.
