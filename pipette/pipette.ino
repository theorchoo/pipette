#include <Wire.h>
#include "SparkFunISL29125.h"

// Declare sensor object
SFE_ISL29125 RGB_sensor;
boolean filled = false;
boolean pressed = false;
String cur_color = "ffffff";

void setup()
{
  // Initialize serial communication
  Serial.begin(115200);

  // Initialize the ISL29125 with simple configuration so it starts sampling
  if (RGB_sensor.init())
  {
    Serial.println("Sensor Initialization Successful\n\r");
  }
}

// Read sensor values for each color and print them to serial monitor
String int_to_hex(i) {
  String s = String(i, HEX);
  if (s.length() < 2) {
    s = s + "0"
  }
  return s
}
String fill(r,g,b)
{
  // make new variable to hold the color data for the LED
  // change the LEDs in filling order
  String color = int_to_hex(r) + int_to_hex(g) + int_to_hex(b);
  return color;
}

void empty()
{
  // 'empty' the LEDs in the opposite order
  // send color via RF according to protocol: "EMPTY;colorinRGB\n"
  Serial.println("EMPTY;" + cur_color + "\n");
}

void loop()
{
  // Read sensor values (16 bit integers)
  unsigned int red = RGB_sensor.readRed();
  unsigned int green = RGB_sensor.readGreen();
  unsigned int blue = RGB_sensor.readBlue();

  if (digitalRead(button) == HIGH) {
    pressed = true;
  } else {
    if (pressed == true) {
      if (filled == true) {
        empty()
      } else {
        cur_color = fill(red,green,blue)
      }
      pressed = false
    }
  }
  
  // Print out readings, change HEX to DEC if you prefer decimal output
  //Serial.print("Red: "); Serial.println(red,HEX);
  //Serial.print("Green: "); Serial.println(green,HEX);
  //Serial.print("Blue: "); Serial.println(blue,HEX);
  //Serial.println();
  //delay(2000);
}
