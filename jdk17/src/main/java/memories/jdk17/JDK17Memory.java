package memories.jdk17;

import jdk.incubator.foreign.*;
import memories.spi.Memory;
import memories.spi.MemoryAllocator;
import memories.spi.exception.MemoryAccessException;

import java.lang.invoke.VarHandle;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.nio.ByteOrder;

class JDK17Memory implements Memory {

  private static final VarHandle byte_handle =
      MemoryHandles.varHandle(byte.class, java.nio.ByteOrder.nativeOrder());
  private static final VarHandle char_LE_handle =
      unalignedHandle(MemoryLayouts.BITS_16_LE, char.class);
  private static final VarHandle short_LE_handle =
      unalignedHandle(MemoryLayouts.BITS_16_LE, short.class);
  private static final VarHandle int_LE_handle =
      unalignedHandle(MemoryLayouts.BITS_32_LE, int.class);
  private static final VarHandle float_LE_handle =
      unalignedHandle(MemoryLayouts.BITS_32_LE, float.class);
  private static final VarHandle long_LE_handle =
      unalignedHandle(MemoryLayouts.BITS_64_LE, long.class);
  private static final VarHandle double_LE_handle =
      unalignedHandle(MemoryLayouts.BITS_64_LE, double.class);
  private static final VarHandle char_BE_handle =
      unalignedHandle(MemoryLayouts.BITS_16_BE, char.class);
  private static final VarHandle short_BE_handle =
      unalignedHandle(MemoryLayouts.BITS_16_BE, short.class);
  private static final VarHandle int_BE_handle =
      unalignedHandle(MemoryLayouts.BITS_32_BE, int.class);
  private static final VarHandle float_BE_handle =
      unalignedHandle(MemoryLayouts.BITS_32_BE, float.class);
  private static final VarHandle long_BE_handle =
      unalignedHandle(MemoryLayouts.BITS_64_BE, long.class);
  private static final VarHandle double_BE_handle =
      unalignedHandle(MemoryLayouts.BITS_64_BE, double.class);

  private final MemoryAllocator allocator;
  private final java.nio.ByteOrder byteOrder;
  private final java.nio.ByteOrder reversedByteOrder;
  protected MemorySegment segment;
  protected long capacity;
  protected long readerIndex;
  protected long writerIndex;
  protected long markedReaderIndex;
  protected long markedWriterIndex;
  private Reference reference;

  JDK17Memory(
      MemorySegment segment, long capacity, ByteOrder byteOrder, MemoryAllocator allocator) {
    this.allocator = allocator;
    this.segment = segment;
    this.capacity = capacity;
    this.byteOrder =
        byteOrder == ByteOrder.BIG_ENDIAN
            ? java.nio.ByteOrder.BIG_ENDIAN
            : java.nio.ByteOrder.LITTLE_ENDIAN;
    this.reversedByteOrder =
        byteOrder == ByteOrder.BIG_ENDIAN
            ? java.nio.ByteOrder.LITTLE_ENDIAN
            : java.nio.ByteOrder.BIG_ENDIAN;
  }

  private static VarHandle unalignedHandle(ValueLayout elementLayout, Class<?> carrier) {
    return MemoryHandles.varHandle(carrier, 1, elementLayout.order());
  }

  @Override
  public long capacity() {
    return capacity;
  }

  @Override
  public Memory capacity(long newCapacity) {
    if (newCapacity <= 0) {
      throw new IllegalArgumentException(
          "newCapacity: " + newCapacity + " (expected: newCapacity(" + newCapacity + ") > 0)");
    }
    if (newCapacity > capacity) {
      MemorySegment newSegment = MemorySegment.allocateNative(newCapacity, 1);
      newSegment.copyFrom(segment);
      this.segment.close();
      this.segment = newSegment;
    } else {
      this.capacity = newCapacity;
      if (readerIndex > capacity) {
        readerIndex = capacity;
      }
      if (writerIndex > capacity) {
        writerIndex = capacity;
      }
      if (markedReaderIndex > capacity) {
        markedReaderIndex = capacity;
      }
      if (markedWriterIndex > capacity) {
        markedWriterIndex = capacity;
      }
    }
    return this;
  }

  @Override
  public long readerIndex() {
    return readerIndex;
  }

  @Override
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

  @Override
  public long writerIndex() {
    return writerIndex;
  }

  @Override
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

  @Override
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

  @Override
  public long readableBytes() {
    return writerIndex - readerIndex;
  }

  @Override
  public long writableBytes() {
    return capacity - writerIndex;
  }

  @Override
  public boolean isReadable() {
    return writerIndex > readerIndex;
  }

  @Override
  public boolean isReadable(long numBytes) {
    return numBytes > 0 && writerIndex - readerIndex >= numBytes;
  }

  @Override
  public boolean isWritable() {
    return capacity > writerIndex;
  }

  @Override
  public boolean isWritable(long numBytes) {
    return numBytes > 0 && capacity - writerIndex >= numBytes;
  }

  @Override
  public Memory clear() {
    writerIndex = readerIndex = 0;
    return this;
  }

  @Override
  public Memory markReaderIndex() {
    markedReaderIndex = readerIndex;
    return this;
  }

  @Override
  public Memory resetReaderIndex() {
    readerIndex = markedReaderIndex;
    return this;
  }

  @Override
  public Memory markWriterIndex() {
    markedWriterIndex = writerIndex;
    return this;
  }

  @Override
  public Memory resetWriterIndex() {
    writerIndex = markedWriterIndex;
    return this;
  }

  @Override
  public Memory ensureWritable(long minWritableBytes) {
    if (minWritableBytes < 0) {
      throw new IllegalArgumentException(
          "minWritableBytes: " + minWritableBytes + " (expected: >= 0)");
    }
    checkWritableBytes(minWritableBytes);
    return this;
  }

  @Override
  public boolean getBoolean(long index) {
    return (byte) byte_handle.get(segment, index) > 0;
  }

  @Override
  public byte getByte(long index) {
    return (byte) byte_handle.get(segment, index);
  }

  @Override
  public short getUnsignedByte(long index) {
    return (short) (((byte) byte_handle.get(segment, index)) & 0xFF);
  }

  @Override
  public short getShort(long index) {
    return MemoryAccess.getShortAtOffset(segment, index, byteOrder);
  }

  @Override
  public short getShortRE(long index) {
    return MemoryAccess.getShortAtOffset(segment, index, reversedByteOrder);
  }

  @Override
  public int getUnsignedShort(long index) {
    return MemoryAccess.getShortAtOffset(segment, index, byteOrder) & 0xFFFF;
  }

  @Override
  public int getUnsignedShortRE(long index) {
    return MemoryAccess.getShortAtOffset(segment, index, reversedByteOrder) & 0xFFFF;
  }

  @Override
  public int getInt(long index) {
    return MemoryAccess.getIntAtOffset(segment, index, byteOrder);
  }

  @Override
  public int getIntRE(long index) {
    return MemoryAccess.getIntAtOffset(segment, index, reversedByteOrder);
  }

  @Override
  public long getUnsignedInt(long index) {
    return (long) MemoryAccess.getIntAtOffset(segment, index, byteOrder) & 0xFFFFFFFFFFFFFFFFL;
  }

  @Override
  public long getUnsignedIntRE(long index) {
    return (long) MemoryAccess.getIntAtOffset(segment, index, reversedByteOrder)
        & 0xFFFFFFFFFFFFFFFFL;
  }

  @Override
  public long getLong(long index) {
    return MemoryAccess.getLongAtOffset(segment, index, byteOrder);
  }

  @Override
  public long getLongRE(long index) {
    return MemoryAccess.getLongAtOffset(segment, index, reversedByteOrder);
  }

  @Override
  public float getFloat(long index) {
    return MemoryAccess.getFloatAtOffset(segment, index, byteOrder);
  }

  @Override
  public float getFloatRE(long index) {
    return MemoryAccess.getFloatAtOffset(segment, index, reversedByteOrder);
  }

  @Override
  public double getDouble(long index) {
    return MemoryAccess.getDoubleAtOffset(segment, index, byteOrder);
  }

  @Override
  public double getDoubleRE(long index) {
    return MemoryAccess.getDoubleAtOffset(segment, index, reversedByteOrder);
  }

  @Override
  public Memory getBytes(long index, Memory dst) {
    return getBytes(index, dst, dst.writableBytes());
  }

  @Override
  public Memory getBytes(long index, Memory dst, long length) {
    getBytes(index, dst, dst.writerIndex(), length);
    dst.writerIndex(dst.writerIndex() + length);
    return this;
  }

  @Override
  public Memory getBytes(long index, Memory dst, long dstIndex, long length) {
    JDK17Memory memory = (JDK17Memory) dst;
    memory.segment.asSlice(dstIndex, length).copyFrom(segment.asSlice(index, length));
    return this;
  }

  @Override
  public Memory getBytes(long index, byte[] dst) {
    getBytes(index, dst, 0, dst.length);
    return this;
  }

  @Override
  public Memory getBytes(long index, byte[] dst, long dstIndex, long length) {
    MemorySegment.ofArray(dst).asSlice(dstIndex, length).copyFrom(segment.asSlice(index, length));
    return this;
  }

  @Override
  public Memory setBoolean(long index, boolean value) {
    MemoryAccess.setByteAtOffset(segment, index, (byte) (value ? 1 : 0));
    return this;
  }

  @Override
  public Memory setByte(long index, int value) {
    MemoryAccess.setByteAtOffset(segment, index, (byte) value);
    return this;
  }

  @Override
  public Memory setShort(long index, int value) {
    MemoryAccess.setShortAtOffset(segment, index, byteOrder, (short) value);
    return this;
  }

  @Override
  public Memory setShortRE(long index, int value) {
    MemoryAccess.setShortAtOffset(segment, index, reversedByteOrder, (short) value);
    return this;
  }

  @Override
  public Memory setInt(long index, int value) {
    MemoryAccess.setIntAtOffset(segment, index, byteOrder, value);
    return this;
  }

  @Override
  public Memory setIntRE(long index, int value) {
    MemoryAccess.setIntAtOffset(segment, index, reversedByteOrder, value);
    return this;
  }

  @Override
  public Memory setLong(long index, long value) {
    MemoryAccess.setLongAtOffset(segment, index, byteOrder, value);
    return this;
  }

  @Override
  public Memory setLongRE(long index, long value) {
    MemoryAccess.setLongAtOffset(segment, index, reversedByteOrder, value);
    return this;
  }

  @Override
  public Memory setFloat(long index, float value) {
    MemoryAccess.setFloatAtOffset(segment, index, byteOrder, value);
    return this;
  }

  @Override
  public Memory setFloatRE(long index, float value) {
    MemoryAccess.setFloatAtOffset(segment, index, reversedByteOrder, value);
    return this;
  }

  @Override
  public Memory setDouble(long index, double value) {
    MemoryAccess.setDoubleAtOffset(segment, index, byteOrder, value);
    return this;
  }

  @Override
  public Memory setDoubleRE(long index, double value) {
    MemoryAccess.setDoubleAtOffset(segment, index, reversedByteOrder, value);
    return this;
  }

  @Override
  public Memory setBytes(long index, Memory src) {
    return setBytes(index, src, src.readableBytes());
  }

  @Override
  public Memory setBytes(long index, Memory src, long length) {
    setBytes(index, src, src.readerIndex(), length);
    src.readerIndex(src.readerIndex() + length);
    return this;
  }

  @Override
  public Memory setBytes(long index, Memory src, long srcIndex, long length) {
    JDK17Memory memory = (JDK17Memory) src;
    segment.asSlice(index, length).copyFrom(memory.segment.asSlice(srcIndex, length));
    return this;
  }

  @Override
  public Memory setBytes(long index, byte[] src) {
    MemoryAddress.ofLong(243).asSegmentRestricted(4, new Runnable() {
      @Override
      public void run() {

      }
    }, null);
    return null;
  }

  @Override
  public Memory setBytes(long index, byte[] src, long srcIndex, long length) {
    return null;
  }

  @Override
  public boolean readBoolean() {
    return false;
  }

  @Override
  public byte readByte() {
    return 0;
  }

  @Override
  public short readUnsignedByte() {
    return 0;
  }

  @Override
  public short readShort() {
    return 0;
  }

  @Override
  public short readShortRE() {
    return 0;
  }

  @Override
  public int readUnsignedShort() {
    return 0;
  }

  @Override
  public int readUnsignedShortRE() {
    return 0;
  }

  @Override
  public int readInt() {
    return 0;
  }

  @Override
  public int readIntRE() {
    return 0;
  }

  @Override
  public long readUnsignedInt() {
    return 0;
  }

  @Override
  public long readUnsignedIntRE() {
    return 0;
  }

  @Override
  public long readLong() {
    return 0;
  }

  @Override
  public long readLongRE() {
    return 0;
  }

  @Override
  public float readFloat() {
    return 0;
  }

  @Override
  public float readFloatRE() {
    return 0;
  }

  @Override
  public double readDouble() {
    return 0;
  }

  @Override
  public double readDoubleRE() {
    return 0;
  }

  @Override
  public Memory readBytes(Memory dst) {
    return null;
  }

  @Override
  public Memory readBytes(Memory dst, long length) {
    return null;
  }

  @Override
  public Memory readBytes(Memory dst, long dstIndex, long length) {
    return null;
  }

  @Override
  public Memory readBytes(byte[] dst) {
    return null;
  }

  @Override
  public Memory readBytes(byte[] dst, long dstIndex, long length) {
    return null;
  }

  @Override
  public Memory skipBytes(long length) {
    return null;
  }

  @Override
  public Memory writeBoolean(boolean value) {
    return null;
  }

  @Override
  public Memory writeByte(int value) {
    return null;
  }

  @Override
  public Memory writeShort(int value) {
    return null;
  }

  @Override
  public Memory writeShortRE(int value) {
    return null;
  }

  @Override
  public Memory writeInt(int value) {
    return null;
  }

  @Override
  public Memory writeIntRE(int value) {
    return null;
  }

  @Override
  public Memory writeLong(long value) {
    return null;
  }

  @Override
  public Memory writeLongRE(long value) {
    return null;
  }

  @Override
  public Memory writeFloat(float value) {
    return null;
  }

  @Override
  public Memory writeFloatRE(float value) {
    return null;
  }

  @Override
  public Memory writeDouble(double value) {
    return null;
  }

  @Override
  public Memory writeDoubleRE(double value) {
    return null;
  }

  @Override
  public Memory writeBytes(Memory src) {
    return null;
  }

  @Override
  public Memory writeBytes(Memory src, long length) {
    return null;
  }

  @Override
  public Memory writeBytes(Memory src, long srcIndex, long length) {
    return null;
  }

  @Override
  public Memory writeBytes(byte[] src) {
    return null;
  }

  @Override
  public Memory writeBytes(byte[] src, long srcIndex, long length) {
    return null;
  }

  @Override
  public Memory copy() {
    return null;
  }

  @Override
  public Memory copy(long index, long length) {
    return null;
  }

  @Override
  public Memory slice() {
    return null;
  }

  @Override
  public Memory slice(long index, long length) {
    return null;
  }

  @Override
  public Memory duplicate() {
    return null;
  }

  @Override
  public ByteOrder byteOrder() {
    return null;
  }

  @Override
  public Memory byteOrder(ByteOrder byteOrder) {
    return null;
  }

  @Override
  public boolean release() {
    return false;
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
    if (reference.address == 0L) {
      throw new MemoryAccessException("Accessing closed memory.");
    }
  }

  boolean isOutOfBounds(long index, long length, long capacity) {
    return (index | length | (index + length) | (capacity - (index + length))) < 0;
  }

  static final class Reference extends PhantomReference {

    long address;

    Reference(long rawAddr, JDK17Memory referent, ReferenceQueue q) {
      super(referent, q);
      this.address = rawAddr;
      referent.reference = this;
    }
  }
}
