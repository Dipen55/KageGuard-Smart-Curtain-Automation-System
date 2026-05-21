const int sensorPin = A0;
const int rainPin = 8;

const int tempPin = A1;
const int flameLdrPin = A2;

const int IN1 = 7;
const int IN2 = 6;
const int EN  = 5;

int curtainPosition = 0;
int targetLevel = 0;

bool rainActive = false;
bool lastButtonState = HIGH;

const int stepTime = 500;
const int pauseTime = 800;

int lastLevel = 0;
int stableLevel = 0;
int stabilityCounter = 0;
const int stabilityRequired = 4;

int sensorValue;

int temperature;
int flameLight;
bool flameDetected=false;

void setup()
{
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  pinMode(EN, OUTPUT);

  pinMode(rainPin, INPUT_PULLUP);

  Serial.begin(9600);
}

void loop()
{
  checkRainButton();

  int tempValue = analogRead(tempPin);
  float voltage = tempValue * (5.0 / 1023.0);
  temperature = (voltage - 0.5) * 100;

  flameLight = analogRead(flameLdrPin);

  if(temperature > 45 && flameLight > 650)
    flameDetected = true;
  else
    flameDetected = false;

  sensorValue = analogRead(sensorPin);
  int level = calculateLevel(sensorValue);

  if(level == lastLevel)
    stabilityCounter++;
  else
    stabilityCounter = 0;

  if(stabilityCounter >= stabilityRequired)
    stableLevel = level;

  lastLevel = level;

  if(flameDetected)
    targetLevel = 0;
  else if(rainActive)
    targetLevel = 6;
  else
    targetLevel = stableLevel;

  moveCurtain();

  Serial.print("S:");
  Serial.print(sensorValue);
  Serial.print(",L:");
  Serial.print(targetLevel);
  Serial.print(",P:");
  Serial.print(curtainPosition);
  Serial.print(",R:");
  Serial.print(rainActive);
  Serial.print(",T:");
  Serial.print(temperature);
  Serial.print(",F:");
  Serial.println(flameLight);

  delay(200);
}

int calculateLevel(int value)
{
  if(value <= 78) return 0;
  else if(value <= 156) return 1;
  else if(value <= 234) return 2;
  else if(value <= 312) return 3;
  else if(value <= 390) return 4;
  else if(value <= 468) return 5;
  else return 6;
}

void moveCurtain()
{
  if(curtainPosition == targetLevel)
    return;

  if(curtainPosition < targetLevel)
  {
    moveClose();
    curtainPosition++;
  }
  else
  {
    moveOpen();
    curtainPosition--;
  }

  delay(pauseTime);
}

void moveClose()
{
  digitalWrite(IN1, HIGH);
  digitalWrite(IN2, LOW);
  analogWrite(EN, 200);
  delay(stepTime);
  stopMotor();
}

void moveOpen()
{
  digitalWrite(IN1, LOW);
  digitalWrite(IN2, HIGH);
  analogWrite(EN, 200);
  delay(stepTime);
  stopMotor();
}

void stopMotor()
{
  digitalWrite(IN1, LOW);
  digitalWrite(IN2, LOW);
  analogWrite(EN, 0);
}

void checkRainButton()
{
  bool buttonState = digitalRead(rainPin);

  if(buttonState == LOW && lastButtonState == HIGH)
  {
    rainActive = !rainActive;
    delay(200);
  }

  lastButtonState = buttonState;
}