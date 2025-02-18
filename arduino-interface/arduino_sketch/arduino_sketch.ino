#include <Wire.h>

// Adresse I2C du DAC AD5693R
#define DAC_ADDRESS 0x4C  

// Broches de contrôle moteur et PWM
#define MOTOR_PIN 2     // Contrôle du moteur via MOSFET ou relais
#define PWM_PIN 3       // Sortie pour le signal TTL de fréquence variable

bool motorRunning = false;
bool motorReady = false;  // Sécurité pour DATA et TTL
bool ready = false;

void setup() {
    Serial.begin(115200);
    Wire.begin();

    pinMode(MOTOR_PIN, OUTPUT);
    pinMode(PWM_PIN, OUTPUT);
    digitalWrite(MOTOR_PIN, LOW);  // Moteur éteint au démarrage

    // Vérification du DAC
    sendToDAC(0); // Init avec 0V
    
    Serial.println("READY");
    ready = true;
}

void loop() {
    if (Serial.available()) {
        String command = Serial.readStringUntil('\n');  
        command.trim();  

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
 * Démarre le moteur (vérifie si ce n'est pas déjà fait)
 */
void startMotor(int frequency) {
    if (motorRunning) {
        Serial.println("MOTOR_ALREADY_RUNNING");
        return;
    }

    frequency = constrain(frequency, 10, 5000);

    digitalWrite(MOTOR_PIN, HIGH);
    delay(500);  // Petit délai pour stabiliser
    motorRunning = true;
    motorReady = true;

    setPwmFrequency(frequency);

    Serial.print("MOTOR_STARTED at ");
    Serial.print(frequency);
    Serial.println(" Hz");
}

/**
 * Arrête le moteur et sécurise les commandes
 */
void stopMotor() {
    digitalWrite(MOTOR_PIN, LOW);
    analogWrite(PWM_PIN, 0);  // Arrêter le signal PWM
    motorRunning = false;
    motorReady = false;
    Serial.println("MOTOR_STOPPED");
}

/**
 * Traite les données de tension à envoyer au DAC
 */
void processData(String command) {
    if (!motorReady) {
        Serial.println("ERROR: MOTOR_NOT_READY");
        return;
    }

    float voltage = command.substring(5).toFloat();
    voltage = constrain(voltage, 0.0, 5.0);  

    uint16_t dacValue = (uint16_t)((voltage / 5.0) * 65535);
    sendToDAC(dacValue);

    Serial.println("ACK: DATA_RECEIVED");
}

/**
 * Envoie la valeur au DAC
 */
void sendToDAC(uint16_t value) {
    Wire.beginTransmission(DAC_ADDRESS);
    Wire.write(highByte(value));  
    Wire.write(lowByte(value));   
    Wire.endTransmission();
}

/**
 * Définit une fréquence PWM continue sur le moteur
 */
void setPwmFrequency(int frequency) {
    int dutyCycle = 127;  // 50% de cycle de travail (valeur entre 0 et 255)
    int pwmValue = map(frequency, 1, 5000, 10, 255); // Conversion de la fréquence en valeur PWM

    analogWrite(PWM_PIN, pwmValue);  // Écrit un signal PWM proportionnel à la fréquence demandée
    Serial.print("PWM set to: ");
    Serial.println(pwmValue);
}