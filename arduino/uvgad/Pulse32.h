#ifndef PULSE32_H
#define PULSE32_H

#include <stddef.h>
#include <stdint.h>

class Pulse32
{
  public: 
    static const size_t PKT_SIZE = 32;
    static const size_t PKT_MAX_PAYLOAD_ENTRIES = 7;
    
    int serialize(uint8_t *data, size_t &len) const;
    int parse(const uint8_t *data);
    
    void setField(unsigned int id, int32_t value);
    int32_t getField(unsigned int id) const;
    
    void setFlags(uint16_t flags);
    int16_t getFlags() const;
    
    bool isFieldSet(unsigned int id) const;
    void clear();
  
  private: 
    struct Pulse32Pkt
    {
      uint16_t flags;
      uint16_t padding;
      int32_t payload[PKT_MAX_PAYLOAD_ENTRIES];
    };
    
    Pulse32Pkt m_pkt;
};

#endif
