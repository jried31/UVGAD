#include "CRC.h"
#include "ErrorCode.h"

CRC8::CRC8() : 
    m_chksum(0)
{
}

int CRC8::update(uint8_t bin)
{
  return(update(&bin, 1));
}

int CRC8::update(const void *ptr, size_t len)
{
  if(!ptr)
  {
    return(ErrorCode::ERR_PARAMS);
  }
  
  size_t bitPos;
  const uint8_t *data = reinterpret_cast<const uint8_t *>(ptr);
  
  while(len > 0)
  {
    m_chksum ^= (*data << 8);
    
    for(bitPos = 8; bitPos > 0; bitPos--)
    {
      if(m_chksum & 0x8000)
      {
        m_chksum ^= (0x1070 << 3);
      }
      
      m_chksum <<= 1;
    }
    
    data++;
    len--;
  }
  
  return(ErrorCode::NO_ERROR);
}

uint8_t CRC8::getChecksum() const
{
  return(static_cast<uint8_t>(m_chksum >> 8));
}
