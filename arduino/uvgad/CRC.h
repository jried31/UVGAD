#ifndef CRC_H
#define CRC_H

#include <stdint.h>
#include <stddef.h>

class CRC8
{
  public: 
    CRC8();
    
    int update(uint8_t bin);
    int update(const void *ptr, size_t len);
    
    uint8_t getChecksum() const;
  
  private: 
    uint16_t m_chksum;
};

#endif
