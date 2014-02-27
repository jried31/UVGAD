#include "Constants.h"
#include "ErrorCode.h"
#include "Pulse32.h"
#include "Utils.h"

const size_t BUF_SIZE = 260;

int32_t lux = 0;
int32_t uv = 0;

void setup()
{
  Serial.begin(Constants::BAUD_RATE);
  Serial.setTimeout(0);
}

void loop()
{
  Pulse32 pkt;
  size_t bufferSize = Pulse32::PKT_SIZE;
  uint8_t buffer[Pulse32::PKT_SIZE];
  
  if(Utils::serialRead(buffer, bufferSize) != Pulse32::PKT_SIZE)
  { 
    return;
  }
  
  if(pkt.parse(buffer) != ErrorCode::NO_ERROR)
  {
    return;
  }
  
  if(pkt.isFieldSet(Constants::PULSE_ID_UV_0))
  {
    pkt.setField(Constants::PULSE_ID_UV_0, uv);
    uv++;
  }
  
  if(pkt.isFieldSet(Constants::PULSE_ID_LIGHT_0))
  {
    pkt.setField(Constants::PULSE_ID_LIGHT_0, analogRead(A0));
  }
  
  if(pkt.serialize(buffer, bufferSize) != ErrorCode::NO_ERROR)
  {
    return;
  }
  
  if(Utils::serialWrite(buffer, bufferSize) != Pulse32::PKT_SIZE)
  {
    return;
  }
}
