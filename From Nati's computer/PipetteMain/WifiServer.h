//
// Created by Or Dagan on 31.8.2016.
//

#ifndef PIPETTE_WIFISERVER_H
#define PIPETTE_WIFISERVER_H

#include "Arduino.h"

class WifiClient
{
public:
    bool startServer(const char* ssid, const char* password);
    void setServer(const char* server, const int port);
    bool postMessage(String message);
    bool closeServer();
    WifiClient();
    void init();

private:
    String server = String("");
    int port = 80;
};

#endif //PIPETTE_WIFISERVER_H
