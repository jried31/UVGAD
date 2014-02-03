/*****************************************************************************
The Geogram ONE is an open source tracking device/development board based off 
the Arduino platform.  The hardware design and software files are released 
under CC-SA v3 license.
*****************************************************************************/

#include <AltSoftSerial.h>
#include <PinChangeInt.h>
#include "GeogramONE.h"
#include <EEPROM.h>
#include <I2C.h>
#include "eepromAnything.h"
#include <Time.h>

#define USESPEED			1  //set to zero to free up code space if option is not needed
#define USEMOTION			1  //set to zero to free up code space if option is not needed
#define USEHTTPPOST			0  //set to zero to free up code space if option is not needed
#define USECALL                         1  //set to zero to disable sms commands

GeogramONE ggo;
AltSoftSerial GSM;
SIM900 sim900(&GSM);
geoSmsData smsData;
PA6C gps(&Serial); 
goCoord lastValid;
geoFence fence;

volatile uint8_t call;
volatile uint8_t move;
volatile uint8_t battery = 0;
volatile uint8_t charge = 0x02; // force a read of the charger cable
volatile uint8_t d4Switch = 0x00;
volatile uint8_t d10Switch = 0x00;

uint8_t cmd0 = 0;
uint8_t cmd1 = 0;
uint8_t cmd3 = 0;
uint8_t udp = 0x00; 

//#if USEFENCE1
uint8_t fence1 = 0;
uint8_t breach1Conf = 0;

uint8_t breachSpeed = 0;
uint8_t breachReps = 0;

uint32_t smsInterval = 0;
uint32_t udpInterval = 0;
uint32_t sleepTimeOn = 0;
uint32_t sleepTimeOff = 0;
uint8_t sleepTimeConfig = 0;

uint8_t speedHyst = 0;
uint16_t speedLimit = 0;

char udpReply[11];
uint8_t smsPowerProfile = 0;
uint8_t udpPowerProfile = 0;
uint8_t smsPowerSpeed = 0;
uint8_t udpPowerSpeed = 0;

bool gsmPowerStatus = true;

void goesWhere(char *, uint8_t replyOrStored = 0);
bool engMetric;

//UV Guardian Variables
double sensorValue = 0;
const int enablePin = 4;
const int sensorPin = A6;

char uv[] = "{\"uv\":\"";
char spd[] = "\",\"spd\":\"";
char lat[] = "\",\"lat\":\"";
char lng[] = "\",\"lng\":\"";
char time[] = "\",\"time\":\"";

void setup()
{
        Serial.begin(9600);
        Serial.println("Starting...");
	ggo.init();
	gps.init(115200);
	sim900.init(9600);
	MAX17043init(7, 500);
	BMA250init(3, 500);
	attachInterrupt(0, ringIndicator, FALLING);
	attachInterrupt(1, movement, FALLING);
	PCintPort::attachInterrupt(PG_INT, &charger, CHANGE);
	PCintPort::attachInterrupt(FUELGAUGEPIN, &lowBattery, FALLING);
	goesWhere(smsData.smsNumber);
	call = sim900.checkForMessages();
	if(call == 0xFF)
		call = 0;
	battery = MAX17043getAlertFlag();

	#if USESPEED
	ggo.configureSpeed(&cmd3, &speedHyst, &speedLimit);
	#endif
        #if USEHTTPPOST
        Serial.println("Starting HTTP...");
        setupHTTP();
        #endif // USEHTTPPOST

	ggo.configureBreachParameters(&breachSpeed, &breachReps);
	ggo.configureSleepTime(&sleepTimeOn, &sleepTimeOff, &sleepTimeConfig);
	BMA250enableInterrupts();
	uint8_t swInt = EEPROM.read(IOSTATE0);
	if(swInt == 0x05)
		PCintPort::attachInterrupt(4, &d4Interrupt, RISING);
	if(swInt == 0x06)
		PCintPort::attachInterrupt(4, &d4Interrupt, FALLING);
	swInt = EEPROM.read(IOSTATE1);
	if(swInt == 0x05)
		PCintPort::attachInterrupt(10, &d10Interrupt, RISING);
	if(swInt == 0x06)
		PCintPort::attachInterrupt(10, &d10Interrupt, FALLING);

        //UV Guardian
        #if USEHTTPPOST
        pinMode(enablePin, OUTPUT);
        digitalWrite(enablePin, HIGH);
        sim900.gsmSleepMode(0);
        #endif
}

void loop()
{
	if(!gps.getCoordinates(&lastValid))
	{
		int8_t tZ = EEPROM.read(TIMEZONE);
		bool eM = EEPROM.read(ENGMETRIC);
		gps.updateRegionalSettings(tZ, eM, &lastValid);
	}
        #if USECALL
	if(call)
	{
		sim900.gsmSleepMode(0);
		char pwd[5];
		EEPROM_readAnything(PINCODE,pwd);
		if(sim900.signalQuality())
		{
			if(!sim900.getGeo(&smsData, pwd))
			{
				if(!smsData.smsPending)
					call = 0; // no more messages
				if(smsData.smsDataValid)
				{
					if(!smsData.smsCmdNum)
						cmd0 = 0x01;
					else if(smsData.smsCmdNum == 1)
						cmd1 = 0x01;
					else if(smsData.smsCmdNum == 2)
						command2();
					else if(smsData.smsCmdNum == 3)
						cmd3 = 0x01;
					else if(smsData.smsCmdNum == 4)
						command4();
					else if(smsData.smsCmdNum == 5)
						command5();
					else if(smsData.smsCmdNum == 6)
						command6();
					else if(smsData.smsCmdNum == 7)
						command7();
					else if(smsData.smsCmdNum == 8)
						command8();
					else if(smsData.smsCmdNum == 255)
					{
						sim900.gsmSleepMode(0);
						sim900.powerDownGSM();
						delay(2000);
						sim900.init(9600);
						gsmPowerStatus = true;
					}
				}
			}
		}
		sim900.gsmSleepMode(2);	
	}
        #endif
	if(cmd0)
		command0();
	#if USEMOTION
	if(cmd1)
		command1();
	#endif
	#if USESPEED
	if(cmd3)
		command3();
	#endif
      if( lastValid.signalLock ) {
        Serial.print(uv);
        Serial.print((analogRead(sensorPin)-320.0)/25.0);
        Serial.print(time);
        Serial.print(lastValid.time[0]);
	Serial.print(lastValid.time[1]);
	Serial.print(":");
	Serial.print(lastValid.time[2]);
	Serial.print(lastValid.time[3]);
	Serial.print(":");
	Serial.print(lastValid.time + 4);
        Serial.print(spd);
        Serial.print(lastValid.speed);
  
        // Latitude
        Serial.print(lat);
        
        if(lastValid.ns == 'S') { 
          Serial.print("-");
        }
        else{
          Serial.print("+");
        }
        
        double buffer = atof(lastValid.latitude + 2);
        buffer = buffer/60.0 + (lastValid.latitude[0] - '0')*10 + (lastValid.latitude[1] - '0');
        Serial.print(buffer, 6);
        
        // Longitude
        Serial.print(lng);
        
        if(lastValid.ew == 'W') { 
          Serial.print("-");
        }
        else{
          Serial.print("+");
        }
        
        buffer = atof(lastValid.longitude + 3);
        buffer = buffer/60.0 + (lastValid.longitude[0] - '0')*100 + (lastValid.longitude[1] - '0')*10 + (lastValid.longitude[2] - '0');
        Serial.print(buffer,6);  
        
        // End
        Serial.println("\"}");
        
        delay(1000);
      }
      
	if(battery)
	{
		sim900.gsmSleepMode(0);
		goesWhere(smsData.smsNumber);
		if(!sim900.prepareSMS(smsData.smsNumber))
		{
			printEEPROM(BATTERYMSG);
			if(!sim900.sendSMS())
			{
				battery = 0;
				MAX17043clearAlertFlag();
			}
		}
		sim900.gsmSleepMode(2);
	}
	if(charge & 0x02)
		chargerStatus();
	engMetric = EEPROM.read(ENGMETRIC);
} 

void printEEPROM(uint16_t eAddress)
{
	char eepChar;
	for (uint8_t ep = 0; ep < 50; ep++)
	{
		eepChar = EEPROM.read(ep + eAddress);
		if(eepChar == '\0')
			break;
		else
			GSM.print(eepChar);
	}
}

void goesWhere(char *smsAddress, uint8_t replyOrStored)
{
	if(!replyOrStored)
		EEPROM_readAnything(RETURNADDCONFIG,replyOrStored);
	if((replyOrStored == 2) || ((replyOrStored == 3) && (smsAddress[0] == NULL)))
	for(uint8_t l = 0; l < 39; l++)
	{
			smsAddress[l] = EEPROM.read(l + SMSADDRESS);
			if(smsAddress[l] == NULL)
				break;
	}
}

