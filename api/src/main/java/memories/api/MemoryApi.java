/*
 * SPDX-FileCopyrightText: 2020-2021 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package memories.api;

import memories.spi.Memory;
import memories.spi.MemoryAllocator;
import memories.spi.exception.MemoryAccessException;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

class MemoryApi implements Memory {

  static final boolean BE;

  private static final int BYTE_SIZE = 1;
  private static final int SHORT_SIZE = 2;
  private static final int INT_SIZE = 4;
  private static final int LONG_SIZE = 8;

  static {
    BE = MemoryAllocatorApi.NativeMemoryAllocator.nativeByteOrderIsBE();
  }

  private final MemoryAllocator allocator;
  protected Object buffer;
  protected Thread ownerThread;
  protected long address;
  protected long capacity;
  protected long readerIndex;
  protected long writerIndex;
  protected long markedReaderIndex;
  protected long markedWriterIndex;

  private boolean bigEndian;
  private PhantomCleaner phantomCleaner;

  MemoryApi(Object buffer,
      Thread ownerThread,
      long address,
      long capacity,
      ByteOrder byteOrder,
      MemoryAllocator allocator) {
    this.buffer = buffer;
    this.ownerThread = ownerThread;
    this.address = address;
    this.capacity = capacity;
    this.allocator = allocator;
    this.bigEndian = byteOrder == ByteOrder.BIG_ENDIAN;
  }

  private static short shortReverseBytes(short value) {
    return (short) (((value & 0xFF00) >> 8) | (value << 8));
  }

  private static int intReverseBytes(int value) {
    return (value << 24) | ((value & 0xff00) << 8) | ((value >>> 8) & 0xff00) | (value >>> 24);
  }

  private static long longReverseBytes(long value) {
    long i = (value & 0x00ff00ff00ff00ffL) << 8 | (value >>> 8) & 0x00ff00ff00ff00ffL;
    return (i << 48) | ((i & 0xffff0000L) << 16) | ((i >>> 16) & 0xffff0000L) | (i >>> 48);
  }

  static int pickPos(int top, int pos) {
    return BE ? top - pos : pos;
  }

  static byte pick(byte le, byte be) {
    return BE ? be : le;
  }

  static short pick(short le, short be) {
    return BE ? be : le;
  }

  static int pick(int le, int be) {
    return BE ? be : le;
  }

  public long capacity() {
    return capacity;
  }

  public Memory capacity(long newCapacity) {
    if (newCapacity <= 0) {
      throw new IllegalArgumentException(
          "newCapacity: " + newCapacity + " (expected: newCapacity(" + newCapacity + ") > 0)");
    }
    long oldAddress = address;
    long newAddress =
        MemoryAllocatorApi.NativeMemoryAllocator.nativeRealloc(oldAddress, newCapacity);
    this.phantomCleaner.address = newAddress;
    this.address = newAddress;
    this.capacity = newCapacity;
    return this;
  }

  // @Override
  public long readerIndex() {
    return readerIndex;
  }

  // @Override
  public Memory readerIndex(long readerIndex) {
    if (readerIndex < 0 || readerIndex > writerIndex) {
      throw new IndexOutOfBoundsException(
          "readerIndex: "
              + readerIndex
              + " (expected: 0 <= readerIndex <= writerIndex("
              + writerIndex
              + "))");
    }
    this.readerIndex = readerIndex;
    return this;
  }

  // @Override
  public long writerIndex() {
    return writerIndex;
  }

  // @Override
  public Memory writerIndex(long writerIndex) {
    if (writerIndex < readerIndex || writerIndex > capacity) {
      throw new IndexOutOfBoundsException(
          "writerIndex: "
              + writerIndex
              + " (expected: readerIndex("
              + readerIndex
              + ") <= writerIndex("
              + writerIndex
              + ") <= capacity("
              + capacity
              + "))");
    }
    this.writerIndex = writerIndex;
    return this;
  }

  // @Override
  public Memory setIndex(long readerIndex, long writerIndex) {
    if (readerIndex < 0 || readerIndex > writerIndex || writerIndex > capacity) {
      throw new IndexOutOfBoundsException(
          "readerIndex: "
              + readerIndex
              + ", writerIndex: "
              + writerIndex
              + " (expected: 0 <= readerIndex <= writerIndex <= capacity("
              + capacity
              + "))");
    }
    this.readerIndex = readerIndex;
    this.writerIndex = writerIndex;
    return this;
  }

  // @Override
  public long readableBytes() {
    return writerIndex - readerIndex;
  }

  // @Override
  public long writableBytes() {
    return capacity - writerIndex;
  }

  // @Override
  public boolean isReadable() {
    return writerIndex > readerIndex;
  }

  // @Override
  public boolean isReadable(long numBytes) {
    return numBytes > 0 && writerIndex - readerIndex >= numBytes;
  }

  // @Override
  public boolean isWritable() {
    return capacity > writerIndex;
  }

  // @Override
  public boolean isWritable(long numBytes) {
    return numBytes > 0 && capacity - writerIndex >= numBytes;
  }

  // @Override
  public Memory clear() {
    writerIndex = readerIndex = 0;
    return this;
  }

  // @Override
  public Memory markReaderIndex() {
    markedReaderIndex = readerIndex;
    return this;
  }

  // @Override
  public Memory resetReaderIndex() {
    readerIndex = markedReaderIndex;
    return this;
  }

  // @Override
  public Memory markWriterIndex() {
    markedWriterIndex = writerIndex;
    return this;
  }

  // @Override
  public Memory resetWriterIndex() {
    writerIndex = markedWriterIndex;
    return this;
  }

  // @Override
  public Memory ensureWritable(long minWritableBytes) {
    if (minWritableBytes < 0) {
      throw new IllegalArgumentException(
          "minWritableBytes: " + minWritableBytes + " (expected: >= 0)");
    }
    checkWritableBytes(minWritableBytes);
    return this;
  }

  // @Override
  public boolean getBoolean(long index) {
    return getByte(index) > 0;
  }

  // @Override
  public short getUnsignedByte(long index) {
    return (short) (getByte(index) & 0xFF);
  }

  // @Override
  public short getShortRE(long index) {
    return shortReverseBytes(getShort(index));
  }

  // @Override
  public int getUnsignedShort(long index) {
    return getShort(index) & 0xFFFF;
  }

  // @Override
  public int getUnsignedShortRE(long index) {
    return shortReverseBytes((short) getUnsignedShort(index)) & 0xFFFF;
  }

  // @Override
  public int getIntRE(long index) {
    return intReverseBytes(getInt(index));
  }

  // @Override
  public long getUnsignedInt(long index) {
    return getInt(index) & 0xFFFFFFFFL;
  }

  // @Override
  public long getUnsignedIntRE(long index) {
    return intReverseBytes((int) getUnsignedInt(index)) & 0xFFFFFFFFL;
  }

  // @Override
  public long getLongRE(long index) {
    return longReverseBytes(getLong(index));
  }

  // @Override
  public float getFloat(long index) {
    return Float.intBitsToFloat(getInt(index));
  }

  // @Override
  public float getFloatRE(long index) {
    return Float.intBitsToFloat(getIntRE(index));
  }

  // @Override
  public double getDouble(long index) {
    return Double.longBitsToDouble(getLong(index));
  }

  // @Override
  public double getDoubleRE(long index) {
    return Double.longBitsToDouble(getLongRE(index));
  }

  // @Override
  public Memory getBytes(long index, Memory dst) {
    return getBytes(index, dst, dst.writableBytes());
  }

  // @Override
  public Memory getBytes(long index, Memory dst, long length) {
    getBytes(index, dst, dst.writerIndex(), length);
    dst.writerIndex(dst.writerIndex() + length);
    return this;
  }

  // @Override
  public Memory getBytes(long index, byte[] dst) {
    return getBytes(index, dst, 0, dst.length);
  }

  // @Override
  public Memory setBoolean(long index, boolean value) {
    return setByte(index, value ? 1 : 0);
  }

  // @Override
  public Memory setShortRE(long index, int value) {
    return setShort(index, shortReverseBytes((short) (value & 0xFFFF)));
  }

  // @Override
  public Memory setIntRE(long index, int value) {
    return setInt(index, intReverseBytes(value));
  }

  // @Override
  public Memory setLongRE(long index, long value) {
    return setLong(index, longReverseBytes(value));
  }

  // @Override
  public Memory setFloat(long index, float value) {
    return setInt(index, Float.floatToRawIntBits(value));
  }

  // @Override
  public Memory setFloatRE(long index, float value) {
    return setIntRE(index, Float.floatToRawIntBits(value));
  }

  // @Override
  public Memory setDouble(long index, double value) {
    return setLong(index, Double.doubleToRawLongBits(value));
  }

  // @Override
  public Memory setDoubleRE(long index, double value) {
    return setLongRE(index, Double.doubleToRawLongBits(value));
  }

  // @Override
  public Memory setBytes(long index, Memory src) {
    return setBytes(index, src, src.readableBytes());
  }

  // @Override
  public Memory setBytes(long index, Memory src, long length) {
    checkIndex(index, length);
    if (src == null) {
      throw new IllegalArgumentException("src must be not null.");
    }
    if (length > src.readableBytes()) {
      throw new IndexOutOfBoundsException(
          "length("
              + length
              + ") exceeds src.readableBytes("
              + src.readableBytes()
              + ") where src is: "
              + src);
    }

    setBytes(index, src, src.readerIndex(), length);
    src.readerIndex(src.readerIndex() + length);
    return this;
  }

  // @Override
  public Memory setBytes(long index, byte[] src) {
    return setBytes(index, src, 0, src.length);
  }

  // @Override
  public boolean readBoolean() {
    return readByte() != 0;
  }

  // @Override
  public byte readByte() {
    checkReadableBytes(1);
    long i = readerIndex;
    byte b = getByte(i);
    readerIndex = i + 1;
    return b;
  }

  // @Override
  public short readUnsignedByte() {
    return (short) (readByte() & 0xFF);
  }

  // @Override
  public short readShort() {
    checkReadableBytes(2);
    short v = getShort(readerIndex);
    readerIndex += 2;
    return v;
  }

  // @Override
  public short readShortRE() {
    checkReadableBytes(2);
    short v = getShortRE(readerIndex);
    readerIndex += 2;
    return v;
  }

  // @Override
  public int readUnsignedShort() {
    return readShort() & 0xFFFF;
  }

  // @Override
  public int readUnsignedShortRE() {
    return readShortRE() & 0xFFFF;
  }

  // @Override
  public int readInt() {
    checkReadableBytes(4);
    int v = getInt(readerIndex);
    readerIndex += 4;
    return v;
  }

  // @Override
  public int readIntRE() {
    checkReadableBytes(4);
    int v = getIntRE(readerIndex);
    readerIndex += 4;
    return v;
  }

  // @Override
  public long readUnsignedInt() {
    return readInt() & 0xFFFFFFFFL;
  }

  // @Override
  public long readUnsignedIntRE() {
    return readIntRE() & 0xFFFFFFFFL;
  }

  // @Override
  public float readFloat() {
    return Float.intBitsToFloat(readInt());
  }

  // @Override
  public float readFloatRE() {
    return Float.intBitsToFloat(readIntRE());
  }

  // @Override
  public double readDouble() {
    return Double.longBitsToDouble(readLong());
  }

  // @Override
  public double readDoubleRE() {
    return Double.longBitsToDouble(readLongRE());
  }

  // @Override
  public long readLong() {
    checkReadableBytes(8);
    long v = getLong(readerIndex);
    readerIndex += 8;
    return v;
  }

  // @Override
  public long readLongRE() {
    checkReadableBytes(8);
    long v = getLongRE(readerIndex);
    readerIndex += 8;
    return v;
  }

  // @Override
  public Memory readBytes(Memory dst) {
    return readBytes(dst, dst.writableBytes());
  }

  // @Override
  public Memory readBytes(Memory dst, long length) {
    return readBytes(dst, 0, length);
  }

  // @Override
  public Memory readBytes(Memory dst, long dstIndex, long length) {
    checkReadableBytes(length);
    getBytes(readerIndex, dst, dstIndex, length);
    readerIndex += length;
    return this;
  }

  // @Override
  public Memory readBytes(byte[] dst) {
    return readBytes(dst, 0, dst.length);
  }

  // @Override
  public Memory readBytes(byte[] dst, long dstIndex, long length) {
    checkReadableBytes(length);
    getBytes(readerIndex, dst, dstIndex, length);
    readerIndex += length;
    return this;
  }

  // @Override
  public Memory skipBytes(long length) {
    checkReadableBytes(length);
    readerIndex += length;
    return this;
  }

  // @Override
  public Memory writeBoolean(boolean value) {
    return writeByte(value ? 1 : 0);
  }

  // @Override
  public Memory writeByte(int value) {
    checkWritableBytes(1);
    return setByte(writerIndex++, value);
  }

  // @Override
  public Memory writeShort(int value) {
    checkWritableBytes(2);
    setShort(writerIndex, value);
    writerIndex += 2;
    return this;
  }

  // @Override
  public Memory writeShortRE(int value) {
    checkWritableBytes(2);
    setShortRE(writerIndex, value);
    writerIndex += 2;
    return this;
  }

  // @Override
  public Memory writeInt(int value) {
    checkWritableBytes(4);
    setInt(writerIndex, value);
    writerIndex += 4;
    return this;
  }

  // @Override
  public Memory writeIntRE(int value) {
    checkWritableBytes(4);
    setIntRE(writerIndex, value);
    writerIndex += 4;
    return this;
  }

  // @Override
  public Memory writeLong(long value) {
    checkWritableBytes(8);
    setLong(writerIndex, value);
    writerIndex += 8;
    return this;
  }

  // @Override
  public Memory writeLongRE(long value) {
    checkWritableBytes(8);
    setLongRE(writerIndex, value);
    writerIndex += 8;
    return this;
  }

  // @Override
  public Memory writeFloat(float value) {
    return writeInt(Float.floatToRawIntBits(value));
  }

  // @Override
  public Memory writeFloatRE(float value) {
    return writeIntRE(Float.floatToRawIntBits(value));
  }

  // @Override
  public Memory writeDoubleRE(double value) {
    return writeLongRE(Double.doubleToRawLongBits(value));
  }

  // @Override
  public Memory writeDouble(double value) {
    return writeLong(Double.doubleToRawLongBits(value));
  }

  // @Override
  public Memory writeBytes(Memory src) {
    return writeBytes(src, src.readableBytes());
  }

  // @Override
  public Memory writeBytes(Memory src, long length) {
    return writeBytes(src, 0, length);
  }

  // @Override
  public Memory writeBytes(Memory src, long srcIndex, long length) {
    ensureWritable(length);
    setBytes(writerIndex, src, srcIndex, length);
    writerIndex += length;
    return this;
  }

  // @Override
  public Memory writeBytes(byte[] src) {
    return writeBytes(src, 0, src.length);
  }

  // @Override
  public Memory writeBytes(byte[] src, long srcIndex, long length) {
    ensureWritable(length);
    setBytes(writerIndex, src, srcIndex, length);
    writerIndex += length;
    return this;
  }

  @Override
  public long memoryAddress() throws IllegalAccessException {
    if (MemoryAllocatorApi.RESTRICTED_LEVEL > 0) {
      if (MemoryAllocatorApi.RESTRICTED_LEVEL > 1) {
        System.err.println("Calling restricted method MemoryAllocator#of(..).");
      }
      if (address < 0L) {
        throw new IllegalStateException("Memory address must be positive value.");
      }
      if (address == 0L) {
        throw new IllegalStateException("Memory buffer already closed.");
      }
      return address;
    } else {
      System.err.println(MemoryAllocatorApi.RESTRICTED_MESSAGE);
      System.err.println(MemoryAllocatorApi.RESTRICTED_PROPERTY_VALUE);
      throw new IllegalAccessException(MemoryAllocatorApi.RESTRICTED_MESSAGE);
    }
  }

  @Override
  public Thread ownerThread() {
    return ownerThread;
  }

  // @Override
  public ByteOrder byteOrder() {
    return bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
  }

  // @Override
  public Memory copy() {
    return copy(0, capacity);
  }

  // @Override
  public Memory slice() {
    return slice(readerIndex, readableBytes());
  }

  private void checkWritableBytes(long minWritableBytes) {
    if (minWritableBytes > capacity - writerIndex) {
      throw new IndexOutOfBoundsException(
          "writerIndex("
              + writerIndex
              + ") + minWritableBytes("
              + minWritableBytes
              + ") exceeds capacity("
              + capacity
              + "): "
              + this);
    }
  }

  private void checkReadableBytes(long minimumReadableBytes) {
    if (minimumReadableBytes < 0) {
      throw new IllegalArgumentException(
          "minimumReadableBytes: " + minimumReadableBytes + " (expected: >= 0)");
    }
    if (readerIndex > writerIndex - minimumReadableBytes) {
      throw new IndexOutOfBoundsException(
          "readerIndex("
              + readerIndex
              + ") + length("
              + minimumReadableBytes
              + ") exceeds writerIndex("
              + writerIndex
              + "): "
              + this);
    }
  }

  void checkIndex(long index, long fieldLength) {
    if (isOutOfBounds(index, fieldLength, capacity)) {
      throw new IndexOutOfBoundsException(
          "index: "
              + index
              + ", length: "
              + fieldLength
              + " (expected: range(0, "
              + capacity
              + "))");
    }
    if (phantomCleaner.address == 0L) {
      throw new MemoryAccessException("Accessing closed memory.");
    }
  }

  boolean isOutOfBounds(long index, long length, long capacity) {
    return (index | length | (index + length) | (capacity - (index + length))) < 0;
  }

  // @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MemoryApi that = (MemoryApi) o;
    if (!isReadable() && !that.isReadable()) {
      return true;
    }
    if (readableBytes() != that.readableBytes()) {
      return false;
    }
    return 0
        == NativeMemoryAccess.nativeCompareMemory(
            address + readerIndex, that.address + that.readerIndex, readableBytes());
  }

  // @Override
  public int hashCode() {
    if (!isReadable()) {
      return 0;
    }
    long hcLength = readerIndex + readableBytes();
    int result = 1;
    for (long i = readerIndex; i < hcLength; i++) {
      result = 31 * result + getByte(i);
    }
    return result;
  }

  // @Override
  public String toString() {
    return "[DefaultMemory] => [address: "
        + address
        + ", capacity: "
        + capacity
        + ", readerIndex: "
        + readerIndex
        + ", writerIndex: "
        + writerIndex
        + ", markedReaderIndex: "
        + markedReaderIndex
        + ", markedWriterIndex: "
        + markedWriterIndex
        + "]";
  }

  /** Implementation */

  // @Override
  public Memory getBytes(long index, Memory dst, long dstIndex, long length) {
    // check buffer overflow
    checkIndex(index, length);
    if (isOutOfBounds(dstIndex, length, dst.capacity())) {
      throw new IndexOutOfBoundsException(
          "dstIdx: "
              + dstIndex
              + ", length: "
              + length
              + " (expected: dstIdx("
              + dstIndex
              + ") <= length("
              + length
              + ")))");
    }
    long dstAddr = ((MemoryApi) dst).address;
    NativeMemoryAccess.nativeCopyMemory(dstAddr + dstIndex, address + index, length);
    return this;
  }

  // @Override
  public Memory getBytes(long index, byte[] dst, long dstIndex, long length) {
    // check buffer overflow
    checkIndex(index, length);
    if (isOutOfBounds(dstIndex, length, dst.length)) {
      throw new IndexOutOfBoundsException(
          "dstIdx: "
              + dstIndex
              + ", length: "
              + length
              + " (expected: dstIdx("
              + dstIndex
              + ") <= length("
              + length
              + ")))");
    }
    NativeMemoryAccess.nativeGetByteArray(address + index, dst, dstIndex, length);
    return this;
  }

  // @Override
  public Memory setBytes(long index, Memory src, long srcIndex, long length) {
    // check buffer overflow
    checkIndex(index, length);
    if (isOutOfBounds(srcIndex, length, src.capacity())) {
      throw new IndexOutOfBoundsException(
          "srcIdx: "
              + srcIndex
              + ", length: "
              + length
              + " (expected: srcIdx("
              + srcIndex
              + ") <= length("
              + length
              + ")))");
    }
    long srcAddr = ((MemoryApi) src).address;
    NativeMemoryAccess.nativeCopyMemory(address + index, srcAddr + srcIndex, length);
    return this;
  }

  // @Override
  public Memory setBytes(long index, byte[] src, long srcIndex, long length) {
    // check buffer overflow
    checkIndex(index, length);
    if (isOutOfBounds(srcIndex, length, src.length)) {
      throw new IndexOutOfBoundsException(
          "srcIdx: "
              + srcIndex
              + ", length: "
              + length
              + " (expected: srcIdx("
              + srcIndex
              + ") <= length("
              + length
              + ")))");
    }
    NativeMemoryAccess.nativeSetByteArray(address + index, src, srcIndex, length);
    return this;
  }

  // @Override
  public byte getByte(long index) {
    // check buffer overflow
    checkIndex(index, BYTE_SIZE);
    return NativeMemoryAccess.nativeGetByte(address + index);
  }

  // @Override
  public short getShort(long index) {
    // check buffer overflow
    checkIndex(index, SHORT_SIZE);
    short value;
    long offset = address + index;
    if ((offset & 1) == 0) {
      value = NativeMemoryAccess.nativeGetShort(offset);
    } else {
      value =
          (short)
              (((NativeMemoryAccess.nativeGetByte(offset) & 0xFF) << pickPos(8, 0))
                  | ((NativeMemoryAccess.nativeGetByte(offset + 1) & 0xFF) << pickPos(8, 8)));
    }
    return bigEndian == BE ? value : shortReverseBytes(value);
  }

  // @Override
  public int getInt(long index) {
    // check buffer overflow
    checkIndex(index, INT_SIZE);
    int value;
    long offset = address + index;
    if ((offset & 3) == 0) {
      value = NativeMemoryAccess.nativeGetInt(offset);
    } else if ((offset & 1) == 0) {
      value =
          (NativeMemoryAccess.nativeGetShort(offset) & 0xFFFF) << pickPos(16, 0)
              | (NativeMemoryAccess.nativeGetShort(offset + 2) & 0xFFFF) << pickPos(16, 16);
    } else {
      value =
          ((NativeMemoryAccess.nativeGetByte(offset) & 0xFF) << pickPos(24, 0)
              | (NativeMemoryAccess.nativeGetByte(offset + 1) & 0xFF) << pickPos(24, 8)
              | (NativeMemoryAccess.nativeGetByte(offset + 2) & 0xFF) << pickPos(24, 16)
              | (NativeMemoryAccess.nativeGetByte(offset + 3) & 0xFF) << pickPos(24, 24));
    }
    return bigEndian == BE ? value : intReverseBytes(value);
  }

  // @Override
  public long getLong(long index) {
    // check buffer overflow
    checkIndex(index, LONG_SIZE);
    long value;
    long offset = address + index;
    if ((offset & 7) == 0) {
      value = NativeMemoryAccess.nativeGetLong(offset);
    } else if ((offset & 3) == 0) {
      value =
          ((NativeMemoryAccess.nativeGetInt(offset) & 0xFFFFFFFFL) << pickPos(32, 0))
              | ((NativeMemoryAccess.nativeGetInt(offset + 4) & 0xFFFFFFFFL) << pickPos(32, 32));
    } else if ((offset & 1) == 0) {
      value =
          (((NativeMemoryAccess.nativeGetShort(offset) & 0xFFFFL) << pickPos(48, 0))
              | ((NativeMemoryAccess.nativeGetShort(offset + 2) & 0xFFFFL) << pickPos(48, 16))
              | ((NativeMemoryAccess.nativeGetShort(offset + 4) & 0xFFFFL) << pickPos(48, 32))
              | ((NativeMemoryAccess.nativeGetShort(offset + 6) & 0xFFFFL) << pickPos(48, 48)));
    } else {
      value =
          (((NativeMemoryAccess.nativeGetByte(offset) & 0xFFL) << pickPos(56, 0))
              | ((NativeMemoryAccess.nativeGetByte(offset + 1) & 0xFFL) << pickPos(56, 8))
              | ((NativeMemoryAccess.nativeGetByte(offset + 2) & 0xFFL) << pickPos(56, 16))
              | ((NativeMemoryAccess.nativeGetByte(offset + 3) & 0xFFL) << pickPos(56, 24))
              | ((NativeMemoryAccess.nativeGetByte(offset + 4) & 0xFFL) << pickPos(56, 32))
              | ((NativeMemoryAccess.nativeGetByte(offset + 5) & 0xFFL) << pickPos(56, 40))
              | ((NativeMemoryAccess.nativeGetByte(offset + 6) & 0xFFL) << pickPos(56, 48))
              | ((NativeMemoryAccess.nativeGetByte(offset + 7) & 0xFFL) << pickPos(56, 56)));
    }
    return bigEndian == BE ? value : longReverseBytes(value);
  }

  // @Override
  public Memory setByte(long index, int value) {
    // check buffer overflow
    checkIndex(index, BYTE_SIZE);
    NativeMemoryAccess.nativeSetByte(address + index, (byte) value);
    return this;
  }

  // @Override
  public Memory setShort(long index, int value) {
    // check buffer overflow
    checkIndex(index, SHORT_SIZE);
    short x = bigEndian == BE ? (short) value : shortReverseBytes((short) value);
    long offset = address + index;
    if ((offset & 1) == 0) {
      NativeMemoryAccess.nativeSetShort(offset, x);
    } else {
      NativeMemoryAccess.nativeSetByte(offset, pick((byte) x, (byte) (x >>> 8)));
      NativeMemoryAccess.nativeSetByte(offset + 1, pick((byte) (x >>> 8), (byte) x));
    }
    return this;
  }

  // @Override
  public Memory setInt(long index, int value) {
    // check buffer overflow
    checkIndex(index, INT_SIZE);
    int x = bigEndian == BE ? value : intReverseBytes(value);
    long offset = address + index;
    if ((offset & 3) == 0) {
      NativeMemoryAccess.nativeSetInt(offset, x);
    } else if ((offset & 1) == 0) {
      NativeMemoryAccess.nativeSetShort(offset, pick((short) x, (short) (x >>> 16)));
      NativeMemoryAccess.nativeSetShort(offset + 2, pick((short) (x >>> 16), (short) x));
    } else {
      NativeMemoryAccess.nativeSetByte(offset, pick((byte) x, (byte) (x >>> 24)));
      NativeMemoryAccess.nativeSetByte(offset + 1, pick((byte) (x >>> 8), (byte) (x >>> 16)));
      NativeMemoryAccess.nativeSetByte(offset + 2, pick((byte) (x >>> 16), (byte) (x >>> 8)));
      NativeMemoryAccess.nativeSetByte(offset + 3, pick((byte) (x >>> 24), (byte) x));
    }
    return this;
  }

  // @Override
  public Memory setLong(long index, long value) {
    // check buffer overflow
    checkIndex(index, LONG_SIZE);
    long x = bigEndian == BE ? value : longReverseBytes(value);
    long offset = address + index;
    if ((offset & 7) == 0) {
      NativeMemoryAccess.nativeSetLong(offset, x);
    } else if ((offset & 3) == 0) {
      NativeMemoryAccess.nativeSetInt(offset, pick((int) x, (int) (x >>> 32)));
      NativeMemoryAccess.nativeSetInt(offset + 4, pick((int) (x >>> 32), (int) x));
    } else if ((offset & 1) == 0) {
      NativeMemoryAccess.nativeSetShort(offset, pick((short) x, (short) (x >>> 48)));
      NativeMemoryAccess.nativeSetShort(offset + 2, pick((short) (x >>> 16), (short) (x >>> 32)));
      NativeMemoryAccess.nativeSetShort(offset + 4, pick((short) (x >>> 32), (short) (x >>> 16)));
      NativeMemoryAccess.nativeSetShort(offset + 6, pick((short) (x >>> 48), (short) x));
    } else {
      NativeMemoryAccess.nativeSetByte(offset, pick((byte) x, (byte) (x >>> 56)));
      NativeMemoryAccess.nativeSetByte(offset + 1, pick((byte) (x >>> 8), (byte) (x >>> 48)));
      NativeMemoryAccess.nativeSetByte(offset + 2, pick((byte) (x >>> 16), (byte) (x >>> 40)));
      NativeMemoryAccess.nativeSetByte(offset + 3, pick((byte) (x >>> 24), (byte) (x >>> 32)));
      NativeMemoryAccess.nativeSetByte(offset + 4, pick((byte) (x >>> 32), (byte) (x >>> 24)));
      NativeMemoryAccess.nativeSetByte(offset + 5, pick((byte) (x >>> 40), (byte) (x >>> 16)));
      NativeMemoryAccess.nativeSetByte(offset + 6, pick((byte) (x >>> 48), (byte) (x >>> 8)));
      NativeMemoryAccess.nativeSetByte(offset + 7, pick((byte) (x >>> 56), (byte) x));
    }
    return this;
  }

  // @Override
  public Memory copy(long index, long length) {
    // check buffer overflow
    checkIndex(index, length);

    MemoryApi newBuf = (MemoryApi) allocator.allocate(length, byteOrder());
    NativeMemoryAccess.nativeCopyMemory(newBuf.address, address + index, length);
    return newBuf;
  }

  // @Override
  public Memory slice(long index, long length) {
    // check buffer overflow
    checkIndex(index, length);
    return new SlicedMemoryApi(this, index, length);
  }

  // @Override
  public Memory duplicate() {
    MemoryApi memory = new MemoryApi(buffer, ownerThread, address, capacity, byteOrder(), allocator);
    memory.readerIndex = readerIndex;
    memory.writerIndex = writerIndex;
    memory.markedReaderIndex = markedReaderIndex;
    memory.markedWriterIndex = markedWriterIndex;
    return memory;
  }

  // @Override
  public Memory byteOrder(ByteOrder byteOrder) {
    this.bigEndian = byteOrder == ByteOrder.BIG_ENDIAN;
    return this;
  }

  // @Override
  public boolean release() {
    if (phantomCleaner.address > 0) {
      MemoryAllocatorApi.NativeMemoryAllocator.nativeFree(address);
      address = 0L;
      phantomCleaner.address = 0L;
      return true;
    } else {
      return false;
    }
  }

  static final class NativeMemoryAccess {

    private NativeMemoryAccess() {}

    static native long nativeSetMemory(long addr, int value, long size);

    static native long nativeCopyMemory(long dstAddr, long srcAddr, long len);

    static native int nativeCompareMemory(long addr1, long addr2, long size);

    private static native byte nativeGetByte(long address);

    private static native void nativeSetByte(long address, byte value);

    private static native short nativeGetShort(long address);

    private static native void nativeSetShort(long address, short value);

    private static native int nativeGetInt(long address);

    private static native void nativeSetInt(long address, int value);

    private static native long nativeGetLong(long address);

    private static native void nativeSetLong(long address, long value);

    private static native void nativeGetByteArray(
        long address, byte[] dst, long dstIdx, long length);

    private static native void nativeSetByteArray(
        long address, byte[] src, long srcIdx, long length);

    private static native void nativeGetShortArray(
        long address, short[] dst, long dstIdx, long length);

    private static native void nativeSetShortArray(
        long address, short[] src, long srcIdx, long length);

    private static native void nativeGetIntArray(long address, int[] dst, long dstIdx, long length);

    private static native void nativeSetIntArray(long address, int[] src, long srcIdx, long length);

    private static native void nativeGetLongArray(
        long address, long[] dst, long dstIdx, long length);

    private static native void nativeSetLongArray(
        long address, long[] src, long srcIdx, long length);
  }

  static class SlicedMemoryApi extends MemoryApi implements Memory.Sliced {

    private final MemoryApi prev;

    SlicedMemoryApi(MemoryApi prev, long index, long length) {
      super(prev.buffer, prev.ownerThread, prev.address + index, length, prev.byteOrder(), prev.allocator);
      this.prev = prev;
      this.readerIndex = prev.readerIndex - index < 0 ? 0 : prev.readerIndex - index;
      this.writerIndex = prev.writerIndex - index < 0 ? 0 : prev.writerIndex - index;
    }

    // @Override
    public Memory unSlice() {
      return prev;
    }

    // @Override
    public boolean release() {
      return prev.release();
    }
  }

  static final class PhantomCleaner extends PhantomReference {

    long address;

    PhantomCleaner(long rawAddr, MemoryApi referent, ReferenceQueue q) {
      super(referent, q);
      this.address = rawAddr;
      referent.phantomCleaner = this;
    }

    public int hashCode() {
      return (int) (31 * address);
    }
  }
}
