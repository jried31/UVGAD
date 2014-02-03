/*****************************************************************************
The Geogram ONE is an open source tracking device/development board based off 
the Arduino platform.  The hardware design and software files are released 
under CC-SA v3 license.
*****************************************************************************/
/*****************************************************************************
WARNING: 
This file has been updated by Steven Smethurst (funvill) to ping a webserver 
with GPS location, and Sim card identity periodically. If you run this code 
unaltered, you will send your personal information to Stevens webserver. 

To learn more about these changes please visit my website. 
http://www.abluestar.com/blog/geogramone-gps-tracker-to-google-maps/

Notes: 
 * GSM modem manual (Sim900): http://garden.seeedstudio.com/images/a/a8/SIM900_AT_Command_Manual_V1.03.pdf
*****************************************************************************/
/*
#include <AltSoftSerial.h>
#include <PinChangeInt.h>
#include "GeogramONE.h"
#include <EEPROM.h>
#include <I2C.h>
*/
// You should change this to your own server. but you can use this setting 
// for a demo. 
//#define SETTING_WEBSERVER_URL       "https://ridekeepr.firebaseio.com/chase.json"
#define SETTING_WEBSERVER_URL       "http://50.23.122.235:8080/uv"
// Need to put your provider's APN here
#define SETTING_GSM_APN             "wholesale"

/**
 * HTTP is an expencive protocol that consumed a lot of bytes in the header. 
 * A HTTP packet with data will take a min of 240 BYTES up to 300 BYTES 
 * 240 == 53( TCP overhread) 120 BYTES (URL) 17 (Domain) 50 (HTTP overhead) 
 * 
 * The more frequent that you poll the server the more data you are going to 
 * use. For example if we where to send a packet with a length of 300 BYTES. 
 *      Poll Frequency  | Times in a day  | Data per month (30 days) 
 *      --------------------------------------------------
 *           10 sec     |     8640        |    75 MB
 *           60 sec     |     1440        |    12 MB
 *           15 min     |       96        |   840 KB 
 *           30 min     |       48        |    42 KB
 *            1 hr      |       24        |    21 KB 
 *
 * Only sending packets when something changes greatly reduces the amount of 
 * bandwidth needed. (normaly we are only moving 1/3 of the day max) 
 *
 * 
 */ 

#define ENABLE_DEBUG_MESSAGES       false 

#define MAX_PHONENUMBER_SIZE 25 
char phoneNumber[MAX_PHONENUMBER_SIZE];

void DebugPrint( char * msg) {
    if( ! ENABLE_DEBUG_MESSAGES ) {
        return ; 
    }    
    Serial.println( msg ); 
}

void httpPost(double uvValues[])
{
    // If we have GPS lock we should send the GPS data. 
    if( lastValid.signalLock ) {
      
      Serial.println("HTTP Post started...");      
    
    // Wake up the modem. 
    // DebugPrint( "Waiting up the GSM modem"); 
    //sim900.gsmSleepMode(0);
    
    GSM.println("AT+SAPBR=3,1,\"Contype\",\"GPRS\"");
    sim900.confirmAtCommand("OK",5000);
	
    GSM.print("AT+SAPBR=3,1,\"APN\",\"");
    GSM.print( SETTING_GSM_APN );
    GSM.println("\""); 
    sim900.confirmAtCommand("OK",5000);
	
    GSM.println("AT+SAPBR=1,1");
    sim900.confirmAtCommand("OK",5000);// Tries to connect GPRS 
    
    //GSM.println("AT+HTTPSSL=1");
   // sim900.confirmAtCommand("OK",5000);
	
    GSM.println("AT+HTTPINIT");
    sim900.confirmAtCommand("OK",5000);
    
    GSM.println("AT+HTTPPARA=\"CID\",1");
    sim900.confirmAtCommand("OK",5000);
    
    //web address to send data to
    GSM.print("AT+HTTPPARA=\"URL\",\"");
    GSM.print(SETTING_WEBSERVER_URL);
    GSM.println("\""); 
    sim900.confirmAtCommand("OK",5000);
    
    GSM.println("AT+HTTPPARA=\"CONTENT\",\"application/json\"");
    sim900.confirmAtCommand("OK",5000);
    
      char id[] = "{\"id\":\"";
      char lat[] = "\",\"lat\":\"";
      char lng[] = "\",\"lng\":\"";
      //char bat[] = "\",\"bat\":\"";
      //char sat[] = "\",\"sat\":\"";
      char uv[] = "\",\"uv\":\"";
      char spd[] = "\",\"spd\":\"";
      
      GSM.print("AT+HTTPDATA="); 
      GSM.print(87);
      GSM.println(",10000");
      sim900.confirmAtCommand("DOWNLOAD",5000);
      
      Serial.print("Payload: "); 

      // ID - 9 Bytes
      GSM.print(id);
      Serial.print(id);
      
      GSM.print(phoneNumber);
      Serial.print(phoneNumber);
      
      //UV
      GSM.print(uv);
      Serial.print(uv);
       
      for(int i = 0; i < 10; i++){ 
        if(uvValues[i] >= 0){
          GSM.print("+");
          Serial.print("+");
        }
        GSM.print(uvValues[i],3);
        Serial.print(uvValues[i],3);
      }
      
      /*
      // Battery SOC - 3 Bytes
      GSM.print(bat);
      Serial.print(bat);
      
      int soc = MAX17043getBatterySOC()/100;
      if(soc < 10){
        GSM.print("00");
        Serial.print("00");
      }
      else if(soc < 100){
        GSM.print("0");
        Serial.print("0");
      }
      GSM.print(soc);
      Serial.print(soc);
      */
      
      // Speed
      GSM.print(spd);
      Serial.print(spd);
      
      if(lastValid.speed < 10){
        GSM.print("00");
        Serial.print("00");
      }
      else if(lastValid.speed < 100){
        GSM.print("0");
        Serial.print("0");
      }
      
      GSM.print(lastValid.speed);
      Serial.print(lastValid.speed);
      
      // Latitude
      GSM.print(lat);
      Serial.print(lat);
      
      if(lastValid.ns == 'S') { 
        GSM.print("-"); 
        Serial.print("-");
      }
      else{
        GSM.print("+"); 
        Serial.print("+");
      }
      
      double buffer = atof(lastValid.latitude + 2);
      buffer = buffer/60.0 + (lastValid.latitude[0] - '0')*10 + (lastValid.latitude[1] - '0');
      GSM.print(buffer, 6);
      Serial.print(buffer, 6);
      
      // Longitude
      GSM.print(lng);
      Serial.print(lng);
      
      if(lastValid.ew == 'W') { 
        GSM.print("-"); 
        Serial.print("-");
      }
      else{
        GSM.print("+"); 
        Serial.print("+");
      }
      
      buffer = atof(lastValid.longitude + 3);
      buffer = buffer/60.0 + (lastValid.longitude[0] - '0')*100 + (lastValid.longitude[1] - '0')*10 + (lastValid.longitude[2] - '0');
      GSM.print(buffer, 6);
      Serial.print(buffer,6);  
      
      // End
      GSM.println("\"}");
      Serial.println("\"}");
      
       sim900.confirmAtCommand("OK",5000);
      	
      Serial.println("A");
      
      	GSM.println("AT+HTTPACTION=1"); //POST the data
      	sim900.confirmAtCommand("ACTION:",5000);
      	
        delay (1000); 
        
          
      	GSM.println("AT+HTTPTERM"); //terminate http
      	sim900.confirmAtCommand("OK",5000);
      	
      	GSM.println("AT+SAPBR=0,1");// Disconnect GPRS
      	sim900.confirmAtCommand("OK",5000);
      	sim900.confirmAtCommand("DEACT",5000);
      	
      // Put the modem to sleep.
      	//sim900.gsmSleepMode(2);
    } 
    else{
      Serial.println("No signal.");
    }
}

/**
 * Gets the phone number from the device if possible
 * 
 * Request:
 *          AT+CNUM
 *
 * Response: 
 *          +CNUM: "","+11234567890",145,7,4
 *          OK
 */ 
uint8_t GetPhoneNumber() {

    // Request the phone number from the sim card 
    GSM.println("AT+CNUM");
    
    // Wait for a response. 
	if( sim900.confirmAtCommand("OK",5000) == 0 ) {
        // Extract Phone number from the response. 
        // Search for the start of the string. 
        char * startOfPhoneNumber = strstr( sim900.atRxBuffer, "\",\"" ); 
        if( startOfPhoneNumber != NULL ) { 
            // Found the start of the string 
            startOfPhoneNumber += 3 ; // Move past the header. 
            if( startOfPhoneNumber[0] == '+' ) {
                startOfPhoneNumber++; // Move past the plus
            }
            char * endOfPhoneNumber = strstr( startOfPhoneNumber, "\"" ); 
            if( endOfPhoneNumber != NULL ) {
                // Found the end of the string. 
                if( endOfPhoneNumber - startOfPhoneNumber < MAX_PHONENUMBER_SIZE-1 ) {                
                    // Fits in the buffer 
                    strncpy( phoneNumber, startOfPhoneNumber, endOfPhoneNumber - startOfPhoneNumber ) ;                
                    phoneNumber[ endOfPhoneNumber - startOfPhoneNumber ] = 0 ; 
                    return 1; 
                }
            }
        }
    }
    return 0; 
}

void setupHTTP() {
    // Get the phone number from the simcard
    GetPhoneNumber(); 
}


