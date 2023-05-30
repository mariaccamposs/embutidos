#include "DHT.h"

DHT dht(A1, DHT11);

const int trigPin = 9;
const int echoPin = 10;
const int Red_led1 = 7;
const int Green_led1 = 6;
const int Yellow_led1 = 3;
const int Red_led2 = 5;
const int Green_led2 = 4;
const int Yellow_led2 = 2;
const int Temp_led = 11;
int fsrPin = 0;
int fsrReading; 
// Defines variables
long duration;
String nom = "Arduino";
String msg;
String estado1 = "Lugar 1 Livre";
int distance;
int reserved_1 = 0;
String estado2 = "Lugar 2 Livre";
int reserved_2 = 0;
int fire = 0;

void setup() {
  pinMode(trigPin, OUTPUT); // Sets the trigPin as an Output
  pinMode(echoPin, INPUT); // Sets the echoPin as an Input
  pinMode(Red_led1, OUTPUT);
  pinMode(Yellow_led1, OUTPUT);
  pinMode(Green_led1, OUTPUT);
  pinMode(Red_led2, OUTPUT);
  pinMode(Yellow_led2, OUTPUT);
  pinMode(Green_led2, OUTPUT);
  pinMode(Temp_led, OUTPUT);
  Serial.begin(9600); // Starts the serial communication
  dht.begin();
}

void loop() {

  fsrReading = analogRead(fsrPin); 
  if (fsrReading < 10) {
    if(!reserved_2){
      digitalWrite(Red_led2, LOW);
      digitalWrite(Yellow_led2, LOW);
      digitalWrite(Green_led2, HIGH);
      if (estado2.equals("Lugar 2 Ocupado")) {
            Serial.println("Lugar 2 Livre");
            estado2 = "Lugar 2 Livre";
          }
    }
  } else {
    if(reserved_2) reserved_2 = 0;
    digitalWrite(Yellow_led2, LOW);
    digitalWrite(Green_led2, LOW);
    digitalWrite(Red_led2, HIGH);
    if (estado2.equals("Lugar 2 Livre")) {
      Serial.println("Lugar 2 Ocupado");
      estado2 = "Lugar 2 Ocupado";
    }
  }

  // Clears the trigPin
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);

  // Sets the trigPin on HIGH state for 10 microseconds
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  // Reads the echoPin, returns the sound wave travel time in microseconds
  duration = pulseIn(echoPin, HIGH);
  // Calculating the distance
  distance = duration * 0.034 / 2;
  // Prints the distance on the Serial Monitor
  if (distance < 15) {
    if(reserved_1) reserved_1 = 0;
    digitalWrite(Yellow_led1, LOW);
    digitalWrite(Green_led1, LOW);
    digitalWrite(Red_led1, HIGH);
    if (estado1.equals("Lugar 1 Livre")) {
      Serial.println("Lugar 1 Ocupado");
      estado1 = "Lugar 1 Ocupado";
    }
  } else {
    if(!reserved_1){
        digitalWrite(Red_led1, LOW);
        digitalWrite(Yellow_led1, LOW);
        digitalWrite(Green_led1, HIGH);
        if (estado1.equals("Lugar 1 Ocupado")) {
          Serial.println("Lugar 1 Livre");
          estado1 = "Lugar 1 Livre";
        }
    }
  }

 if (Serial.available()) {
    readSerialPort(); // Read the incoming message until the newline character
    if(strstr("1",msg.c_str())){
        reserved_1 = 1;
        digitalWrite(Red_led1, LOW);
        digitalWrite(Green_led1, LOW);
        digitalWrite(Yellow_led1, HIGH);
    }else if(strstr("2",msg.c_str())){
        reserved_2 = 1;
        digitalWrite(Red_led2, LOW);
        digitalWrite(Green_led2, LOW);
        digitalWrite(Yellow_led2, HIGH);
    }else if(strstr("3",msg.c_str())){
        reserved_1 = 0;
        digitalWrite(Red_led1, LOW);
        digitalWrite(Green_led1, HIGH);
        digitalWrite(Yellow_led1, LOW);
    }else if(strstr("4",msg.c_str())){
        reserved_2 = 0;
        digitalWrite(Red_led2, LOW);
        digitalWrite(Green_led2, HIGH);
        digitalWrite(Yellow_led2, LOW);
    }
    //Serial.print(msg);
  }
  if(dht.readTemperature()> 30){
      digitalWrite(Temp_led, HIGH);
      if(fire==0){
        Serial.println("yes");
        fire=1;
      }
      
  }else{
      digitalWrite(Temp_led, LOW);
      if(fire==1){
        Serial.println("no");
        fire=0;
      }
  }
  
  //Serial.println("Temperature = " + String(dht.readTemperature())+" °C");
  //Serial.println("Humidite = " + String(dht.readHumidity())+" %");
  delay(600);
}
void readSerialPort() {
   msg = "";
  if (Serial.available()) {
      delay(10);
      while (Serial.available() > 0) {
          msg += (char)Serial.read();
      }
      Serial.flush();
  }
}
