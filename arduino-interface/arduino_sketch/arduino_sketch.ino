#include <Wire.h>

// Adresse I2C du DAC AD5693R
#define DAC_ADDRESS 0x4C  

// Broches de contrôle moteur et PWM
#define MOTOR_PIN 2     // Contrôle du moteur via MOSFET ou relais
#define PWM_PIN 3       // Sortie pour le signal TTL de fréquence variable

bool motorRunning = false;
bool ready = false;

void setup() {
    Serial.begin(115200);

    Wire.begin();

    pinMode(MOTOR_PIN, OUTPUT);
    pinMode(PWM_PIN, OUTPUT);
    digitalWrite(MOTOR_PIN, LOW);  // Moteur éteint au démarrage

    // Vérification du DAC (Test simple)
    sendToDAC(0); // Init avec 0V
    
    // Envoyer le message "READY" lorsque l'interface est prête
    Serial.println("READY");
    ready = true;
}

void loop() {
    if (Serial.available()) {
        String command = Serial.readStringUntil('\n');  // Lire la commande
        command.trim();  // Nettoyer les espaces ou retours à la ligne

        if (command.startsWith("START_MOTOR")) {
            startMotor();
        } 
        else if (command.startsWith("STOP_MOTOR")) {
            stopMotor();
        } 
        else if (command.startsWith("DATA")) {
            startMotor();
            handleData(command);
        }
        else if (command.startsWith("TTL_FREQ")) {
            handleTTL(command);
        }
    }
}

/**
 * Démarre le moteur
 */
void startMotor() {
    digitalWrite(MOTOR_PIN, HIGH);
    motorRunning = true;
    Serial.println("MOTOR_STARTED");
}

/**
 * Arrête le moteur
 */
void stopMotor() {
    digitalWrite(MOTOR_PIN, LOW);
    analogWrite(PWM_PIN, 0);  // Désactiver le signal TTL
    motorRunning = false;
    Serial.println("MOTOR_STOPPED");
}

/**
 * Gère l'envoi de la tension au DAC AD5693R
 * Commande reçue: "DATA 2.34"
 */
void handleData(String command) {
    if (!motorRunning) {
        Serial.println("ERROR: MOTOR_NOT_RUNNING");
        return;
    }

    // Extraire la valeur de tension
    float voltage = command.substring(5).toFloat();
    voltage = constrain(voltage, 0.0, 5.0);  // Limite de 0V à 5V

    // Convertir en 16-bit et envoyer au DAC
    uint16_t dacValue = (uint16_t)((voltage / 5.0) * 65535);
    sendToDAC(dacValue);

    Serial.println("ACK: DATA_RECEIVED");
}

/**
 * Envoie une valeur au DAC AD5693R
 */
void sendToDAC(uint16_t value) {
    Wire.beginTransmission(DAC_ADDRESS);
    Wire.write(highByte(value));  // Byte de poids fort
    Wire.write(lowByte(value));   // Byte de poids faible
    Wire.endTransmission();
}

/**
 * Gère la fréquence TTL du moteur
 * Commande reçue: "TTL_FREQ 1200"
 */
void handleTTL(String command) {
    if (!motorRunning) {
        Serial.println("ERROR: MOTOR_NOT_RUNNING");
        return;
    }

    int frequency = command.substring(9).toInt();
    frequency = constrain(frequency, 1, 5000);  // Limite de 1 Hz à 5 kHz

    int dutyCycle = 127;  // 50% de PWM
    int period = 1000000 / frequency; // Microsecondes
    int halfPeriod = period / 2;

    // Génération manuelle de la fréquence TTL
    for (int i = 0; i < 100; i++) { // Génération temporaire
        digitalWrite(PWM_PIN, HIGH);
        delayMicroseconds(halfPeriod);
        digitalWrite(PWM_PIN, LOW);
        delayMicroseconds(halfPeriod);
    }

    Serial.println("ACK: TTL_FREQ_SET");
}
