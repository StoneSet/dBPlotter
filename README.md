# dBPlotter Application 🎶📈

![Java](https://img.shields.io/badge/Java-23-blue.svg) ![JavaFX](https://img.shields.io/badge/JavaFX-23-orange.svg) ![License](https://img.shields.io/badge/License-MIT-green.svg) ![Version](https://img.shields.io/badge/Version-0.0.1-blue.svg) 

![logo](https://github.com/user-attachments/assets/6d753fbb-23f7-41fe-a94b-e5e6d90582e1) A JavaFX-based desktop application designed to interface with the Bruel & Kjaer 2306 Level Recorder. This application processes acoustic data from CSV files (ARTA and REW formats) and sends control commands to the device over a serial connection.
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

![fefrfejava_Hsh6lzAx3E](https://github.com/user-attachments/assets/3da6ba6b-1f42-4343-961a-a76f6ec57b84)


## 🔌 Interface Overview

To interface with the Bruel & Kjaer 2306, I created a DAC interface using an Arduino.

Be aware that the B&K "Ext. port" use a 272° DIN 8 pinout (see in the service manual).

![IMG_4647](https://github.com/user-attachments/assets/dbabdb9a-cf7b-4c7c-9455-b1b4bfb38630)

#### Operation Principle

The software sends serial commands to the Arduino, which uses a 16-bit DAC to convert the digital signal into an analog output within a 0-5V range.  
The Arduino manages the paper feed motor's activation and speed.

A relay closes a connection to start the paper feed motor, while the speed is regulated by a TTL signal.
