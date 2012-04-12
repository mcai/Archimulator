void blink(int i) {
  for(; i > 0; i--) {
    digitalWrite(13, HIGH);
    delay(300);
    digitalWrite(13, LOW);
    delay(300);
  }
}

void setup() {
  Serial.begin(9600);
  pinMode(13, OUTPUT);
}

void loop() {
  if (Serial.available() > 0) {
    int h = Serial.read();  
    blink(h);
  }
  
  delay(100);
}
