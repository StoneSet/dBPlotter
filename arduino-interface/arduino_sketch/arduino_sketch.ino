#include <Wire.h>
#include <Adafruit_AD569x.h>
#include <Tone.h>

// Adresse I2C du DAC AD5693R
#define DAC_ADDRESS 0x4C  

// Broches pour le contrôle moteur et PWM
#define MOTOR_PIN 2    
#define PWM_PIN 3       

bool motorRunning = false;
bool ready = false;
Tone pwmSignal;

Adafruit_AD569x dac;

void setup() {
    Serial.begin(9600);
    Wire.begin();

    pinMode(MOTOR_PIN, OUTPUT);
    pwmSignal.begin(PWM_PIN); 
    digitalWrite(MOTOR_PIN, LOW);  

    if (!dac.begin(DAC_ADDRESS, &Wire)) {
        Serial.println("ERROR: DAC_INIT_FAILED");
        while (1);
    }
    
    Serial.println("READY");
    dac.reset();
    dac.setMode(NORMAL_MODE, true, false);
    Wire.setClock(800000);
    ready = true;
}

void loop() {
    while (Serial.available()) { 
        String command = Serial.readStringUntil('\n');
        command.trim();
        if (command.length() == 0) continue;

        if (command.startsWith("STOP_MOTOR")) {
            stopMotor();
        } 
        else if (command.startsWith("START_MOTOR")) {
            int frequency = command.substring(12).toInt();
            startMotor(frequency);
        }
        else if (command.startsWith("DATA")) {
            processData(command);
        }
    }
}

/**
 * Démarre le moteur avec une fréquence définie.
 */
void startMotor(int frequency) {
    if (motorRunning) {
        Serial.println("MOTOR_ALREADY_RUNNING");
        return;
    }

    frequency = constrain(frequency, 10, 5000);

    digitalWrite(MOTOR_PIN, HIGH);
    delay(500);
    motorRunning = true;
    setPwmFrequency(frequency);
    delay(100);
    Serial.println("MOTOR_STARTED");
}

/**
 * Arrête le moteur et le signal PWM.
 */
void stopMotor() {
    digitalWrite(MOTOR_PIN, LOW);
    stopPwm();
    motorRunning = false;
    delay(100);
    Serial.println("MOTOR_STOPPED");
}

/**
 * Traite et envoie la tension au DAC.
 */
void processData(String command) {
    if (!motorRunning) {
        Serial.println("ERROR: MOTOR_NOT_RUNNING");
        return;
    }

    float voltage = command.substring(5).toFloat();
    voltage = constrain(voltage, 0.0, 5.0);  

    uint16_t dacValue = (uint16_t)((voltage / 5.0) * 65535);
    sendToDAC(dacValue);

    Serial.println("ACK: DATA_RECEIVED");
}

/**
 * Envoie une valeur au DAC via I2C.
 */
void sendToDAC(uint16_t value) {
    if (!dac.writeUpdateDAC(value)) {
        Serial.println("ERROR: DAC_WRITE_FAILED");
    }
}

/**
 * Configure une fréquence PWM continue.
 */
void setPwmFrequency(int frequency) {
    frequency = constrain(frequency, 1, 5000);
    pwmSignal.play(frequency);

    Serial.print("PWM_SET: ");
    Serial.println(frequency);
}

void stopPwm() {
    pwmSignal.stop();
    Serial.println("PWM_STOPPED");
}

