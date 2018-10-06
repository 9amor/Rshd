#include<ESP8266WiFi.h>

const char* ssid     = "AndroidSamar";
const char* password = "bssb6229";

int onOff;
int target = D1;

const char* serialNumber = "121";

int count1;

int count2;
const char* host = "endothelial-scratch.000webhostapp.com";
const char* streamId   = "http://endothelial-scratch.000webhostapp.com/update2.php";

unsigned long wifi_time = 0;
uint32_t tsLastReport = 0;

void setup() {
  pinMode(target, INPUT);
  Serial.begin(115200);
  delay(10);

    // We start by connecting to a WiFi network
  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");  
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
 
 

}

void loop() {
onOff = digitalRead(target);
if ( onOff == HIGH&& count1==0 && count2<=1){
  count1++;
  senddata(1);
   Serial.print(1);
   count2=0;
   

}else if ( onOff ==LOW&& count1<=1&& count2==0){
  senddata(0);
  Serial.print(0);
  count2++;
  count1 =0;
}
}


void senddata(int onOffState)
{
         WiFiClient client;
        const int httpPort = 80;
        if (!client.connect(host, httpPort)) {
          Serial.println("connection failed");
          return;
        }
 
        // We now create a URL for the request
        String url = "";
        url += streamId;
        url += "?id=";
        url += serialNumber;
        url += "&onOffState=";
        url+= onOffState;
  Serial.println(url);
  
        // This will send the request to the server
        client.print(String("GET ") + url + " HTTP/1.1\r\n" +
               "Host: " + host + "\r\n" + 
               "Connection: close\r\n\r\n");
        unsigned long timeout = millis();
        while (client.available() == 0) {
          if (millis() - timeout > 5000) {
            Serial.println(">>> Client Timeout !");
            client.stop();
            return;
          }
        }
} 
