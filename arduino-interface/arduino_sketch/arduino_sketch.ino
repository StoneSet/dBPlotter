#include <Wire.h>
#include <Adafruit_MCP4725.h>

Adafruit_MCP4725 dac;
int motorPin = 9;  // Broche PWM pour le moteur
bool isRunning = false;

void setup() {
    Serial.begin(115200);
    pinMode(motorPin, OUTPUT);
    dac.begin(0x60); // Adresse I2C du DAC
}

void loop() {
    if (Serial.available()) {
        String command = Serial.readStringUntil('\n');
        command.trim();

        if (command.startsWith("DAC ")) {
            double voltage = command.substring(4).toFloat();
            int dacValue = map(voltage * 1000, 0, 5000, 0, 4095);
            dac.setVoltage(dacValue, false);
            Serial.println("DAC Updated: " + String(voltage) + "V");
        }

        else if (command.startsWith("SET_SPEED ")) {
            int speed = command.substring(10).toInt();
            analogWrite(motorPin, map(speed, 0, 100, 0, 255));
            Serial.println("Paper speed set: " + String(speed) + " mm/s");
        }

        else if (command.startsWith("PWM_FREQ ")) {
            int frequency = command.substring(9).toInt();
            tone(motorPin, frequency);
            Serial.println("PWM Frequency set: " + String(frequency) + " Hz");
        }

        else if (command == "START_MACHINE") {
            isRunning = true;
            digitalWrite(motorPin, HIGH);
            Serial.println("Machine started");
        }

        else if (command == "STOP_MACHINE") {
            isRunning = false;
            digitalWrite(motorPin, LOW);
            Serial.println("Machine stopped");
        }

        else if (command == "PAPER_PUSH") {
            analogWrite(motorPin, 200); // Pousse un peu le papier
            delay(500);
            analogWrite(motorPin, 0);
            Serial.println("Paper pushed");
        }
    }
}
