/*
 * SPDX-FileCopyrightText: 2020 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package memories.jni;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.atomic.AtomicLong;
import memories.spi.Memory;
import memories.spi.MemoryAllocator;
import memories.spi.exception.MemoryAccessException;

class JNIMemory implements Memory {

  static final ByteOrder NATIVE;

  private static final int BYTE_SIZE = 1;
  private static final int SHORT_SIZE = 2;
  private static final int INT_SIZE = 4;
  private static final int LONG_SIZE = 8;

  static {
    NATIVE = nativeByteOrder(Unsafe.nativeByteOrderIsBE());
  }

  private final MemoryAllocator allocator;
  protected long address;
  protected long capacity;
  protected boolean reverse;
  protected long readerIndex;
  protected long writerIndex;
  protected long markedReaderIndex;
  protected long markedWriterIndex;
  private Reference reference;

  JNIMemory(long address, long capacity, ByteOrder byteOrder, MemoryAllocator allocator) {
    this.address = address;
    this.capacity = capacity;
    this.reverse = byteOrder != NATIVE;
    this.allocator = allocator;
  }

  static ByteOrder nativeByteOrder(int val) {
    return val == 1 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
  }

  static ByteOrder byteOrder(boolean isReversed, ByteOrder nativeOrder) {
    if (isReversed) {
      if (ByteOrder.LITTLE_ENDIAN == nativeOrder) {
        return ByteOrder.BIG_ENDIAN;
      } else {
        return ByteOrder.LITTLE_ENDIAN;
      }
    }
    return nativeOrder;
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

  //
  // These methods construct integers from bytes.  The byte ordering
  // is the native endianness of this platform.
  private int pickPos(int top, int pos) {
    if (reverse) {
      return top - pos;
    } else {
      return pos;
    }
  }

  private byte pick(byte le, byte be) {
    return reverse ? be : le;
  }

  private short pick(short le, short be) {
    return reverse ? be : le;
  }

  private int pick(int le, int be) {
    return reverse ? be : le;
  }

  public long capacity() {
    return capacity;
  }

  public Memory capacity(long newCapacity) {
    if (newCapacity <= 0) {
      throw new IllegalArgumentException(
          "newCapacity: " + newCapacity + " (expected: newCapacity(" + newCapacity + ") > 0)");
    }
    if (newCapacity <= capacity) {
      this.capacity = newCapacity;
      this.readerIndex = readerIndex > newCapacity ? newCapacity : readerIndex;
      this.writerIndex = writerIndex > newCapacity ? newCapacity : writerIndex;
      this.markedReaderIndex = 0L;
      this.markedWriterIndex = 0L;
      return this;
    } else {
      synchronized (this) {
        ensureAccessible();
        long newAddress = JNIMemoryAllocator.Unsafe.nativeMalloc(newCapacity);
        Unsafe.nativeMemcpy(newAddress, address, capacity);
        Unsafe.nativeFree(address);
        this.address = newAddress;
        this.reference.address.set(newAddress);
        this.capacity = newCapacity;
        return this;
      }
    }
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

  // @Override
  public ByteOrder byteOrder() {
    return byteOrder(reverse, NATIVE);
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
    ensureAccessible();
  }

  void ensureAccessible() {
    if (reference.address.get() == 0L) {
      throw new MemoryAccessException("Accessing closed buffer.");
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
    JNIMemory that = (JNIMemory) o;
    if (!isReadable() && !that.isReadable()) {
      return true;
    }
    if (readableBytes() != that.readableBytes()) {
      return false;
    }
    return 0
        == Unsafe.nativeMemcmp(
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
    long dstAddr = ((JNIMemory) dst).address;
    Unsafe.nativeMemcpy(dstAddr + dstIndex, address + index, length);
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
    Unsafe.nativeGetBytes(address + index, dst, dstIndex, length);
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
    long srcAddr = ((JNIMemory) src).address;
    Unsafe.nativeMemcpy(address + index, srcAddr + srcIndex, length);
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
    Unsafe.nativeSetBytes(address + index, src, srcIndex, length);
    return this;
  }

  // @Override
  public byte getByte(long index) {
    // check buffer overflow
    checkIndex(index, BYTE_SIZE);
    return Unsafe.nativeGetByte(address + index);
  }

  // @Override
  public short getShort(long index) {
    // check buffer overflow
    checkIndex(index, SHORT_SIZE);
    long offset = address + index;
    if ((offset & 1) == 0) {
      if (reverse) {
        return shortReverseBytes(Unsafe.nativeGetShort(offset));
      } else {
        return Unsafe.nativeGetShort(offset);
      }
    } else {
      return (short)
          (((Unsafe.nativeGetByte(offset) & 0xFF) << pickPos(8, 0))
              | ((Unsafe.nativeGetByte(offset + 1) & 0xFF) << pickPos(8, 8)));
    }
  }

  // @Override
  public int getInt(long index) {
    // check buffer overflow
    checkIndex(index, INT_SIZE);

    long offset = address + index;
    if ((offset & 3) == 0) {
      if (reverse) {
        return intReverseBytes(Unsafe.nativeGetInt(offset));
      } else {
        return Unsafe.nativeGetInt(offset);
      }
    } else if ((offset & 1) == 0) {
      return (Unsafe.nativeGetShort(offset) & 0xFFFF) << pickPos(16, 0)
          | (Unsafe.nativeGetShort(offset + 2) & 0xFFFF) << pickPos(16, 16);
    } else {
      return ((Unsafe.nativeGetByte(offset) & 0xFF) << pickPos(24, 0)
          | (Unsafe.nativeGetByte(offset + 1) & 0xFF) << pickPos(24, 8)
          | (Unsafe.nativeGetByte(offset + 2) & 0xFF) << pickPos(24, 16)
          | (Unsafe.nativeGetByte(offset + 3) & 0xFF) << pickPos(24, 24));
    }
  }

  // @Override
  public long getLong(long index) {
    // check buffer overflow
    checkIndex(index, LONG_SIZE);

    long offset = address + index;
    if ((offset & 7) == 0) {
      if (reverse) {
        return longReverseBytes(Unsafe.nativeGetLong(offset));
      } else {
        return Unsafe.nativeGetLong(offset);
      }
    } else if ((offset & 3) == 0) {
      return ((Unsafe.nativeGetInt(offset) & 0xFFFFFFFFL) << pickPos(32, 0))
          | ((Unsafe.nativeGetInt(offset + 4) & 0xFFFFFFFFL) << pickPos(32, 32));
    } else if ((offset & 1) == 0) {
      return (((Unsafe.nativeGetShort(offset) & 0xFFFFL) << pickPos(48, 0))
          | ((Unsafe.nativeGetShort(offset + 2) & 0xFFFFL) << pickPos(48, 16))
          | ((Unsafe.nativeGetShort(offset + 4) & 0xFFFFL) << pickPos(48, 32))
          | ((Unsafe.nativeGetShort(offset + 6) & 0xFFFFL) << pickPos(48, 48)));
    } else {
      return (((Unsafe.nativeGetByte(offset) & 0xFFL) << pickPos(56, 0))
          | ((Unsafe.nativeGetByte(offset + 1) & 0xFFL) << pickPos(56, 8))
          | ((Unsafe.nativeGetByte(offset + 2) & 0xFFL) << pickPos(56, 16))
          | ((Unsafe.nativeGetByte(offset + 3) & 0xFFL) << pickPos(56, 24))
          | ((Unsafe.nativeGetByte(offset + 4) & 0xFFL) << pickPos(56, 32))
          | ((Unsafe.nativeGetByte(offset + 5) & 0xFFL) << pickPos(56, 40))
          | ((Unsafe.nativeGetByte(offset + 6) & 0xFFL) << pickPos(56, 48))
          | ((Unsafe.nativeGetByte(offset + 7) & 0xFFL) << pickPos(56, 56)));
    }
  }

  // @Override
  public Memory setByte(long index, int value) {
    // check buffer overflow
    checkIndex(index, BYTE_SIZE);
    Unsafe.nativeSetByte(address + index, (byte) value);
    return this;
  }

  // @Override
  public Memory setShort(long index, int value) {
    // check buffer overflow
    checkIndex(index, SHORT_SIZE);
    long offset = address + index;
    if ((offset & 1) == 0) {
      if (reverse) {
        Unsafe.nativeSetShort(offset, shortReverseBytes((short) value));
      } else {
        Unsafe.nativeSetShort(offset, (short) value);
      }
    } else {
      Unsafe.nativeSetByte(offset, pick((byte) (value >>> 0), (byte) (value >>> 8)));
      Unsafe.nativeSetByte(offset + 1, pick((byte) (value >>> 8), (byte) (value >>> 0)));
    }
    return this;
  }

  // @Override
  public Memory setInt(long index, int value) {
    // check buffer overflow
    checkIndex(index, INT_SIZE);
    long offset = address + index;
    if ((offset & 3) == 0) {
      if (reverse) {
        Unsafe.nativeSetInt(offset, intReverseBytes(value));
      } else {
        Unsafe.nativeSetInt(offset, value);
      }
    } else if ((offset & 1) == 0) {
      Unsafe.nativeSetShort(offset, pick((short) value, (short) (value >>> 16)));
      Unsafe.nativeSetShort(offset + 2, pick((short) (value >>> 16), (short) value));
    } else {
      Unsafe.nativeSetByte(offset, pick((byte) value, (byte) (value >>> 24)));
      Unsafe.nativeSetByte(offset + 1, pick((byte) (value >>> 8), (byte) (value >>> 16)));
      Unsafe.nativeSetByte(offset + 2, pick((byte) (value >>> 16), (byte) (value >>> 8)));
      Unsafe.nativeSetByte(offset + 3, pick((byte) (value >>> 24), (byte) value));
    }
    return this;
  }

  // @Override
  public Memory setLong(long index, long value) {
    // check buffer overflow
    checkIndex(index, LONG_SIZE);
    long offset = address + index;
    if ((offset & 7) == 0) {
      if (reverse) {
        Unsafe.nativeSetLong(offset, longReverseBytes(value));
      } else {
        Unsafe.nativeSetLong(offset, value);
      }
    } else if ((offset & 3) == 0) {
      Unsafe.nativeSetInt(offset, pick((int) (value), (int) (value >>> 32)));
      Unsafe.nativeSetInt(offset + 4, pick((int) (value >>> 32), (int) (value)));
    } else if ((offset & 1) == 0) {
      Unsafe.nativeSetShort(offset, pick((short) (value), (short) (value >>> 48)));
      Unsafe.nativeSetShort(offset + 2, pick((short) (value >>> 16), (short) (value >>> 32)));
      Unsafe.nativeSetShort(offset + 4, pick((short) (value >>> 32), (short) (value >>> 16)));
      Unsafe.nativeSetShort(offset + 6, pick((short) (value >>> 48), (short) (value)));
    } else {
      Unsafe.nativeSetByte(offset, pick((byte) value, (byte) (value >>> 56)));
      Unsafe.nativeSetByte(offset + 1, pick((byte) (value >>> 8), (byte) (value >>> 48)));
      Unsafe.nativeSetByte(offset + 2, pick((byte) (value >>> 16), (byte) (value >>> 40)));
      Unsafe.nativeSetByte(offset + 3, pick((byte) (value >>> 24), (byte) (value >>> 32)));
      Unsafe.nativeSetByte(offset + 4, pick((byte) (value >>> 32), (byte) (value >>> 24)));
      Unsafe.nativeSetByte(offset + 5, pick((byte) (value >>> 40), (byte) (value >>> 16)));
      Unsafe.nativeSetByte(offset + 6, pick((byte) (value >>> 48), (byte) (value >>> 8)));
      Unsafe.nativeSetByte(offset + 7, pick((byte) (value >>> 56), (byte) value));
    }
    return this;
  }

  // @Override
  public Memory copy(long index, long length) {
    // check buffer overflow
    checkIndex(index, length);
    JNIMemory newBuf = (JNIMemory) allocator.allocate(length, byteOrder());
    Unsafe.nativeMemcpy(newBuf.address, address + index, length);
    return newBuf;
  }

  // @Override
  public Memory slice(long index, long length) {
    // check buffer overflow
    checkIndex(index, length);
    return new SlicedJniMemory(this, index, length);
  }

  // @Override
  public Memory duplicate() {
    JNIMemory memory = new JNIMemory(address, capacity, byteOrder(), allocator);
    memory.readerIndex = readerIndex;
    memory.writerIndex = writerIndex;
    memory.markedReaderIndex = markedReaderIndex;
    memory.markedWriterIndex = markedWriterIndex;
    return memory;
  }

  // @Override
  public Memory byteOrder(ByteOrder byteOrder) {
    this.reverse = byteOrder != NATIVE;
    return this;
  }

  // @Override
  public boolean release() {
    synchronized (this) {
      if (reference.address.get() > 0) {
        Unsafe.nativeFree(reference.address.getAndSet(0));
        return true;
      } else {
        return false;
      }
    }
  }

  static final class Unsafe {

    private Unsafe() {}

    static native void nativeFree(long address);

    private static native int nativeByteOrderIsBE();

    private static native long nativeMemcmp(long addr1, long addr2, long size);

    private static native long nativeMemcpy(long dstAddr, long srcAddr, long len);

    private static native long nativeRealloc(long address, long newSize);

    private static native byte nativeGetByte(long address);

    private static native void nativeSetByte(long address, byte value);

    private static native short nativeGetShort(long address);

    private static native void nativeSetShort(long address, short value);

    private static native int nativeGetInt(long address);

    private static native void nativeSetInt(long address, int value);

    private static native long nativeGetLong(long address);

    private static native void nativeSetLong(long address, long value);

    private static native void nativeGetBytes(long address, byte[] dst, long dstIdx, long length);

    private static native void nativeSetBytes(long address, byte[] src, long srcIdx, long length);
  }

  static class SlicedJniMemory extends JNIMemory implements Memory.Sliced {

    private final JNIMemory prev;

    SlicedJniMemory(JNIMemory prev, long index, long length) {
      super(prev.address + index, length, prev.byteOrder(), prev.allocator);
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

  static final class Reference extends PhantomReference {

    final AtomicLong address;
    StackTraceElement[] stackTraceElements;

    Reference(long rawAddr, JNIMemory referent, ReferenceQueue q) {
      super(referent, q);
      this.address = new AtomicLong(rawAddr);
      referent.reference = this;
      fillStackTrace(JNIMemoryAllocator.LEAK_DETECTION);
    }

    void fillStackTrace(boolean enabled) {
      if (enabled) {
        stackTraceElements = Thread.currentThread().getStackTrace();
      }
    }
  }
}
