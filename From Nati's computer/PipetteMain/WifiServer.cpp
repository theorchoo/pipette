#include "WifiServer.h"

WifiClient::WifiClient() {

}

void WifiClient::init() {
    Serial1.begin(115200);
    Serial1.println("AT+RST");
    Serial.println("resetting Wifi");
    delay(1000);
    Serial1.println("AT+CWMODE=1");
    delay(500);
    Serial.println("Successfully Created a WifiClient object");
}

bool WifiClient::startServer(const char* ssid, const char* password) {
    String connectString = String("AT+CWJAP=\"");
    connectString += String(ssid);
    connectString += "\",\"";
    connectString += String(password);
    connectString += "\"";
    int i;
    bool success = false;
    for (i = 0; i < 4; i++) {
        Serial1.println(connectString);
        Serial1.flush();
        delay(4000);
        if (Serial1.find("OK")) {
            Serial.println("Connected Successfully");
            success = true;
            return true;
        }
        Serial.print("couldn't connect. attempt ");
        Serial.print(i + 1);
        Serial.println(" out of 4;");
    }

    return false;
}

void WifiClient::setServer(const char* server, const int port) {
    this->server = String(server);
    this->port = port;
}

bool WifiClient::postMessage(String message) {
    String postRequest =
            String("POST ") + String("/pipette") + String(" HTTP/1.0\r\n");
    postRequest += String("Host: ") + this->server + String("\r\n");
    postRequest += String("Accept: */*\r\n");
    postRequest += String("Content-Length: ") + message.length() + String("\r\n");
    postRequest += String("Content-Type: application/x-www-form-urlencoded\r\n\r\n");
    postRequest += message;

    bool sent = false;
    String connectionString = String("AT+CIPSTART=\"TCP\",");
    connectionString += this->server;
    connectionString += String("\",\"") + this->port + String("\"");

    Serial1.println(connectionString);
    delay(200);
    Serial1.println("AT+CIPSEND=" + postRequest.length());
    delay(200);
    Serial1.println(postRequest);
    Serial1.flush();
    delay(300);
    if(Serial1.find("SEND OK")) {
        Serial.println("Packet sent");
        sent = true;
    }
    Serial1.println("AT+CIPCLOSE");
    return sent;
}

bool WifiClient::closeServer(void) {
    Serial1.println("AT+CWQAP");
}
