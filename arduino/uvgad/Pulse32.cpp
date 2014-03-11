#include <stddef.h>
#include <stdint.h>
#include <string.h>

#include "CRC.h"
#include "ErrorCode.h"
#include "Pulse32.h"

int Pulse32::serialize(uint8_t *data, size_t &len) const
{
  if(!data || len < PKT_SIZE)
  {
    return(ErrorCode::ERR_PARAMS);
  }
  
  memcpy(data, reinterpret_cast<const uint8_t *>(&m_pkt), PKT_SIZE);
  len = PKT_SIZE;
  
  return(ErrorCode::NO_ERROR);
}

int Pulse32::parse(const uint8_t *data)
{
  if(!data)
  {
    return(ErrorCode::ERR_PARAMS);
  }
  
  memcpy(reinterpret_cast<uint8_t *>(&m_pkt), data, PKT_SIZE);
  return(ErrorCode::NO_ERROR);
}

void Pulse32::setField(unsigned int id, int32_t value)
{
  if(id > PKT_MAX_PAYLOAD_ENTRIES)
  {
    id = 0;
  }
  
  m_pkt.flags |= 0x8000 >> id;
  m_pkt.payload[id] = value;
}

int32_t Pulse32::getField(unsigned int id) const
{
  if(id > PKT_MAX_PAYLOAD_ENTRIES)
  {
    id = 0;
  }
  
  return(m_pkt.payload[id]);
}

void Pulse32::setFlags(uint16_t flags)
{
  m_pkt.flags = flags;
}

int16_t Pulse32::getFlags() const
{
  return(m_pkt.flags);
}

bool Pulse32::isFieldSet(unsigned int id) const
{
  if(id > PKT_MAX_PAYLOAD_ENTRIES)
  {
    id = 0;
  }
  
  return((m_pkt.flags & (0x8000 >> id)) ? true : false);
}

void Pulse32::clear()
{
  m_pkt.flags = 0;
  memset(m_pkt.payload, 0, sizeof(m_pkt.payload));
}
