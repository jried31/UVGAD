#ifndef UTILS_H
#define UTILS_H

#include <stdint.h>
#include <stddef.h>

namespace Utils
{
  int byteToHexString(uint8_t bin, uint8_t *str, size_t len);
  int serialRead(uint8_t *dst, size_t len);
  int serialWrite(const uint8_t *src, size_t len);
}

#endif
