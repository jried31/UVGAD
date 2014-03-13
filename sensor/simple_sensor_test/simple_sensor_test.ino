/*
  AnalogReadSerial
  Reads an analog input on pin 0, prints the result to the serial monitor.
  Attach the center pin of a potentiometer to pin A0, and the outside pins to +5V and ground.
 
 This example code is in the public domain.
 */

const int lightOutputPin = A3;
const int uvOutputPin = A2;

const int enablePin = 10;//Green Wire
const double sensorPin = A6;//Orange Wire


double uvValue,lightValue;
// the setup routine runs once when you press reset:
void setup() {
  // initialize serial communication at 9600 bits per second:
  Serial.begin(9600);
 
}

// the loop routine runs over and over again forever:
void loop() {
  // read the input on analog pin 0:
  // print out the value you read:
        Serial.println("TEsting: ");
       
  double value = (analogRead(sensorPin)-320.0)/25.0;
  Serial.print("UV: ");
  Serial.println(value);
 

 Serial.print("Light: ");
  Serial.println(analogRead(lightOutputPin));
  Serial.print("UV: ");
  Serial.println(value);
  delay(3000);        // delay in between reads for stability
}
