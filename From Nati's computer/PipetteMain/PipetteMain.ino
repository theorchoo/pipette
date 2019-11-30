/**
 * ***********************************************
 *              MAIN PIPETTE FILE
 * ***********************************************
 */

// ##################### includes ######################
#include "WifiServer.h"
#include <Wire.h>
#include "Adafruit_TCS34725.h"
#include <PololuLedStrip.h>
// #####################################################


// ############### definitions & globals ###############

// ## WIFI ##
#define SERVER "192.168.0.102"
#define PORT 8080
#define ssid "Ben"
#define pass "Alice2002"
WifiClient client;
bool isConnected;

// ## Color Sensor ##
Adafruit_TCS34725 tcs = Adafruit_TCS34725(TCS34725_INTEGRATIONTIME_50MS, TCS34725_GAIN_4X);
typedef struct Color {
    float r, g, b;  
} Color;
Color activeColor = {0,0,0};

// ## Button ##
const int buttonPin = 5;
int buttonState = 0;

// ## RGB Led ##
// Create an ledStrip object and specify the pin it will use.
PololuLedStrip<8> ledStrip;
PololuLedStrip<9> ledStrip0;
PololuLedStrip<10> ledStrip1;
PololuLedStrip<11> ledStrip2;
PololuLedStrip<12> ledStrip3;
// Create a buffer for holding the colors (3 bytes per color).
#define LED_COUNT 5
rgb_color colors[LED_COUNT];

int DELAY_TIME = 350;

// #####################################################


// ################# Helper Functions ##################
void fill(int r, int g, int b){
      DELAY_TIME = 200;
      // Read the color from the sensor.
      rgb_color color;
      color.red = g;
      color.green = r;
      color.blue = b;

      // Update the colors buffer.
      for(uint16_t i = 0; i < LED_COUNT; i++)
      {
        colors[i] = color;
      }

      // Write to the LED strip.
      ledStrip.write(colors, LED_COUNT);
      delay(DELAY_TIME);
      ledStrip0.write(colors, LED_COUNT);
      delay(DELAY_TIME+=50);
      ledStrip1.write(colors, LED_COUNT);
      delay(DELAY_TIME+=75);
      ledStrip2.write(colors, LED_COUNT);
      delay(DELAY_TIME+=115);
      ledStrip3.write(colors, LED_COUNT);
}
void empty(){
      DELAY_TIME = 400;
      // Read the color from the sensor.
      rgb_color color;
      color.red = 0;
      color.green = 0;
      color.blue = 0;

      // Update the colors buffer.
      for(uint16_t i = 0; i < LED_COUNT; i++)
      {
        colors[i] = color;
      }

      // Write to the LED strip.
      ledStrip3.write(colors, LED_COUNT);
      delay(DELAY_TIME);
      ledStrip2.write(colors, LED_COUNT);
      delay(DELAY_TIME-=100);
      ledStrip1.write(colors, LED_COUNT);
      delay(DELAY_TIME-=115);
      ledStrip0.write(colors, LED_COUNT);
      delay(DELAY_TIME-=75);
      ledStrip.write(colors, LED_COUNT);
}

void getColor() {
  uint16_t clearA, red, green, blue, sumA;
  tcs.setInterrupt(false);      // turn on LED
  delay(60);  // takes 50ms to read 
  tcs.getRawData(&red, &green, &blue, &clearA);
  tcs.setInterrupt(true);  // turn off LED

  // Figure out some basic hex code for visualization
  uint32_t sum = clearA;
  float r, g, b;
  sumA = red+green+blue;
  r = red; r /= sum;
  g = green; g /= sum;
  b = blue; b /= sum;
  r *= 256; g *= 256; b *= 256;

  activeColor.r = r;
  activeColor.g = g;
  activeColor.b = b;
  
  String colorMsg = "r:";
  colorMsg += red;
  colorMsg += "g:";
  colorMsg += green;
  colorMsg += "b:";
  colorMsg += blue;
  colorMsg += "clear:";
  colorMsg += clearA;
  colorMsg += "sum:";
  colorMsg += sumA;


  /*
  Serial.print("r:");Serial.print(red); 
  Serial.print("g:");Serial.print(green);
  Serial.print("b:");Serial.print(blue);Serial.print("clear:");Serial.print(clearA);Serial.print("sum:");
  Serial.println(sumA);  
  */
  Serial.println(colorMsg);
  if (isConnected) {
      client.postMessage(colorMsg);  
  }
}
// #####################################################


// #####################################################
// ################# SETUP & LOOP ######################

void setup() {
  // Serial start
  Serial.begin(115200);
  while (!Serial) {  
    // WAIT FOR SERIAL TO START 
    }
  Serial.println("Arduino Started");

  // connect to WIFI:
  client = WifiClient();
  if (client.startServer(ssid, pass)) {
    client.setServer(SERVER,PORT);
    isConnected = true;
    Serial.print("Connected successfuly to: "); Serial.println(ssid);
  } else {
    Serial.println("Error in WIFI connection!");
    isConnected = false;
  }

  // connect to Color Sensor:
  for (int s = 0; s < 3; s++) {
      Serial.print("Connecting to Color Sensor... ("); Serial.print(s+1); Serial.println(" sec)");
      delay(1000);    
  }
  if (tcs.begin()) {
    Serial.println("Found sensor");
  } else {
    Serial.println("No TCS34725 found ... check your connections");
    while (1); // halt!
  }

  // Button init
  pinMode(buttonPin, INPUT);
}

void loop() {
  int curState;
  curState = digitalRead(buttonPin);
  
  if (curState == HIGH) {
    buttonState = curState;
  } else {
    if (buttonState == HIGH) {
        // pushed and released - do action.   

        buttonState == curState;
    }
  }

  getColor();
  fill((int) activeColor.r,(int) activeColor.g,(int) activeColor.b);
  delay(200);
}

// #####################################################
