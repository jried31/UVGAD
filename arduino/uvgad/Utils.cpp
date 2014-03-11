#include <stddef.h>
#include <stdint.h>

#include "Arduino.h"
#include "ErrorCode.h"
#include "Utils.h"

int Utils::byteToHexString(uint8_t bin, uint8_t *str, size_t len)
{
  if(str == NULL || len < 2)
  {
    return(-1);
  }
  
  uint8_t high = (bin & 0xF0) >> 4;
  uint8_t low = bin & 0x0F;
  
  if(high <= 9)
  {
    str[0] = '0' + high;
  }
  else
  {
    str[0] = 'A' + high - 10;
  }
  
  if(low <= 9)
  {
    str[1] = '0' + low;
  }
  else
  {
    str[1] = 'A' + low - 10;
  }
  
  return(0);
}

int Utils::serialRead(uint8_t *dst, size_t len)
{
  if(!dst)
  {
    return(ErrorCode::ERR_PARAMS);
  }
  
  size_t bytesRead;
  size_t totalBytesRead = 0;
  
  while(totalBytesRead < len)
  {
    while(!Serial.available())
    {
    }
    
    bytesRead = Serial.readBytes(reinterpret_cast<char *>(dst) + totalBytesRead, 
                                 len - totalBytesRead);
    
    if(bytesRead < 0)
    {
      return(ErrorCode::ERR_READ);
    }
    
    totalBytesRead += bytesRead;
  }
  
  return(totalBytesRead);
}


int Utils::serialWrite(const uint8_t *src, size_t len)
{
  if(!src)
  {
    return(ErrorCode::ERR_PARAMS);
  }
  
  size_t bytesWritten;
  size_t totalBytesWritten = 0;
  
  while(totalBytesWritten < len)
  {
    bytesWritten = Serial.write(src + totalBytesWritten, 
                                len - totalBytesWritten);
    
    if(bytesWritten <= 0)
    {
      return(ErrorCode::ERR_WRITE);
    }
    
    totalBytesWritten += bytesWritten;
  }
  
  return(totalBytesWritten);
}
