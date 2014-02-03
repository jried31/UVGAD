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
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
#define SETTING_WEBSERVER_URL       "http://50.23.122.235:8080/uv"
// Need to put your provider's APN here
#define SETTING_GSM_APN             "wholesale"
=======
=======
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
//#define SETTING_WEBSERVER_URL       "http://50.23.122.235:8080/uv"
//#define SETTING_WEBSERVER_URL       "http://uvg-ubuntu.cloudapp.net"
#define SETTING_WEBSERVER_URL       "http://uvgserver-53558.usw1.actionbox.io:8080"
// Need to put your provider's APN here
//#define SETTING_GSM_APN             "wholesale"
#define SETTING_GSM_APN             "epc.tmobile.com"

<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e

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

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
void httpPost(double uvValues[])
{
    // If we have GPS lock we should send the GPS data. 
    if( lastValid.signalLock ) {
      
=======
=======
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
void my_http_post(){
  GSM.println(1);
  GSM.println(2);
}

void httpPost(double uvValues[])
{
    // If we have GPS lock we should send the GPS data. 
    //if( lastValid.signalLock ) {
    if(1){  
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
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
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
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
=======
=======
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
      //char lat[] = "\",\"lat\":\"";
      //char lng[] = "\",\"lng\":\"";
      //char bat[] = "\",\"bat\":\"";
      //char sat[] = "\",\"sat\":\"";
      //char uv[] = "\",\"uv\":\"";
      
      char spd[] = "\",\"spd\":\"";
      
      GSM.print("AT+HTTPDATA="); 
      // DATA has to be at least DATASIZE bytes. If data is more than DATASIZE bytes, it is truncated. If size of data is less than DATASIZE, it cannot be sent.
      #define DATASIZE 57
      GSM.print(DATASIZE); 
      GSM.println(",10000");
      uint8_t return_value = sim900.confirmAtCommand("DOWNLOAD",5000);
      Serial.print("return_value:");
      Serial.println(return_value);
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
      
      Serial.print("Payload: "); 

      // ID - 9 Bytes
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
      GSM.print(id);
=======
      /*GSM.print(id);
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
      /*GSM.print(id);
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
      /*GSM.print(id);
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
      Serial.print(id);
      
      GSM.print(phoneNumber);
      Serial.print(phoneNumber);
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
      
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
=======
=======
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
      */
      //UV
      char uv[] = "{\"uv\":";
      char lat[] = "\"lat\":\"";
      char lng[] = "\"lng\":\"";
      
      GSM.print(uv);
      if(uvValues[0] >=0 && uvValues[0]<10){
        GSM.print("\"");
        GSM.print(" ");
        GSM.print(uvValues[0],1);
        GSM.print("\",");
        
        Serial.print("\"");
        Serial.print(" ");
        Serial.print(uvValues[0],1);
        Serial.print("\",");
      }
      else if(uvValues[0] >= 10 && uvValues[0]< 100){
        GSM.print("\"");
        GSM.print(uvValues[0],1);
        GSM.print("\",");
        
        Serial.print("\"");
        Serial.print(uvValues[0],1);
        Serial.print("\",");
      }
      else if(uvValues[0]>=100){
        GSM.print("\"99.9\",");
        Serial.print("\"99.9\",");
      }
      else{
        GSM.print(" ");
        GSM.print("\"0.0\",");
        
        Serial.print(" ");
        Serial.print("\"0.0\",");
      }
      
      //Serial.print(uv);
      //Serial.print("\"5.5\",");
      
      /*
      for(int i = 0; i < 10; i++){ 
        if(uvValues[i] >= 0){
          GSM.print(" ");
          Serial.print(" ");
        }
        GSM.print(uvValues[i],1);
        Serial.print(uvValues[i],1);
      }
      */
      
     
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
      
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
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
      GSM.print(spd);
=======
      /*GSM.print(spd);
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
      /*GSM.print(spd);
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
      /*GSM.print(spd);
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
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
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
      
=======
      */
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
      */
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
      */
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
      // Latitude
      GSM.print(lat);
      Serial.print(lat);
      
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
=======
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e

      
      double buffer = atof(lastValid.latitude + 2);
      buffer = buffer/60.0 + (lastValid.latitude[0] - '0')*10 + (lastValid.latitude[1] - '0');
      
      //double my_lat = 38; //hard code for testing
      double my_lat = buffer;
      
      if(my_lat < 10 &&  my_lat > -10){
        GSM.print(" ");
        Serial.print(" ");
      }
      
      if(my_lat < 100 && my_lat > -100){
        GSM.print(" ");
        Serial.print(" ");
      }
      
      if(my_lat <= 180 && my_lat >= -180){
        GSM.print(" ");
        Serial.print(" ");
      }
      
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
      if(lastValid.ns == 'S') { 
        GSM.print("-"); 
        Serial.print("-");
      }
      else{
        GSM.print("+"); 
        Serial.print("+");
      }
      
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
      double buffer = atof(lastValid.latitude + 2);
      buffer = buffer/60.0 + (lastValid.latitude[0] - '0')*10 + (lastValid.latitude[1] - '0');
      GSM.print(buffer, 6);
      Serial.print(buffer, 6);
      
      // Longitude
      GSM.print(lng);
      Serial.print(lng);
      
=======
=======
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
      //GSM.print(buffer, 6);
      GSM.print(my_lat, 6);
      GSM.print("\",");
      //Serial.print(buffer, 6);
      Serial.print(my_lat, 6);
      Serial.print("\",");
      
      // Longitude       
      GSM.print(lng);
      Serial.print(lng);
      

      
      buffer = atof(lastValid.longitude + 3);
      buffer = buffer/60.0 + (lastValid.longitude[0] - '0')*100 + (lastValid.longitude[1] - '0')*10 + (lastValid.longitude[2] - '0');
      
      //double my_lng = -118; // hard code for testing
      double my_lng = buffer;
	 
      if(my_lng < 10 && my_lng > -10){
        GSM.print(" ");
        Serial.print(" ");
      }
      
      if(my_lng < 100 && my_lng > -100){
        GSM.print(" ");
        Serial.print(" ");
      }
      
      if(my_lng <= 180 && my_lng >= -180){
        GSM.print("  ");
        Serial.print("  ");
      }
      
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
      if(lastValid.ew == 'W') { 
        GSM.print("-"); 
        Serial.print("-");
      }
      else{
        GSM.print("+"); 
        Serial.print("+");
      }
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
      
      buffer = atof(lastValid.longitude + 3);
      buffer = buffer/60.0 + (lastValid.longitude[0] - '0')*100 + (lastValid.longitude[1] - '0')*10 + (lastValid.longitude[2] - '0');
      GSM.print(buffer, 6);
      Serial.print(buffer,6);  
=======
=======
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
            
      //GSM.print(buffer, 6);
      GSM.print(my_lng, 6);
      //Serial.print(buffer,6); 
      Serial.print(my_lng,6); 
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
      
      // End
      GSM.println("\"}");
      Serial.println("\"}");
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
      
       sim900.confirmAtCommand("OK",5000);
      	
      Serial.println("A");
      
      	GSM.println("AT+HTTPACTION=1"); //POST the data
      	sim900.confirmAtCommand("ACTION:",5000);
      	
=======
=======
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e

      Serial.print("my_lat = ");
      Serial.println(my_lat);
      Serial.print("my_lng = ");
      Serial.println(my_lng);
              
       sim900.confirmAtCommand("OK",5000);
      
      	GSM.println("AT+HTTPACTION=1"); //POST the data
      	uint8_t return_val = sim900.confirmAtCommand("ACTION:",5000);
        Serial.print("return_val:");
        Serial.println(return_val);
        //Serial.println("Result = ");
        //Serial.println(result);    	
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
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
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
                    Serial.println(phoneNumber);
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
                    Serial.println(phoneNumber);
>>>>>>> 47b1eec80e46022b2a5bde07a77ec3053d603cd3
=======
                    Serial.println(phoneNumber);
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
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


