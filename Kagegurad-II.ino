#include <LiquidCrystal.h>

LiquidCrystal lcd(12,11,5,4,3,2);

String data;

int sensorValue;
int level;
int position;
int rain;

int temperature;
int flameLight;

int previousLevel = 0;

void setup()
{
  Serial.begin(9600);

  lcd.begin(16,2);
  lcd.print("Curtain System");
  delay(1500);
  lcd.clear();
}

void loop()
{
  if(Serial.available())
  {
    data = Serial.readStringUntil('\n');

    sscanf(data.c_str(),"S:%d,L:%d,P:%d,R:%d,T:%d,F:%d",
           &sensorValue,&level,&position,&rain,&temperature,&flameLight);

    lcd.clear();

    lcd.setCursor(0,1);
    lcd.print("S:");
    lcd.print(sensorValue);
    lcd.print(" P:");
    lcd.print(position);

    lcd.setCursor(0,0);

    if(temperature > 45 && flameLight > 650)
    {
      lcd.print("FLAME DETECTED");
      delay(1000);
      lcd.clear();
      lcd.print("Opening Curtain");
    }
    else if(rain==1)
    {
      lcd.print("Rain Detected");
      delay(1000);
      lcd.clear();
      lcd.print("Closing Curtain");
    }
    else if(level > previousLevel)
    {
      lcd.print("Light Increased");
      delay(1000);
      lcd.clear();
      lcd.print("Closing Curtain");
    }
    else if(level < previousLevel)
    {
      lcd.print("Light Decreased");
      delay(1000);
      lcd.clear();
      lcd.print("Opening Curtain");
    }
    else
    {
      lcd.print("Light Level:");
      lcd.print(level);
    }

    previousLevel = level;
  }
}