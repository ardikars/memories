package memories.api;

import memories.spi.MappedMemory;
import memories.spi.Memory;
import memories.spi.MemoryAllocator;

class MappedMemoryApi extends MemoryApi implements MappedMemory  {

  MappedMemoryApi(
      Object buffer,
      Thread ownerThread,
      long address,
      long capacity,
      ByteOrder byteOrder,
      MemoryAllocator allocator) {
    super(buffer, ownerThread, address, capacity, byteOrder, allocator);
  }

  @Override
  public Memory capacity(long newCapacity) {
    return this;
  }

  @Override
  void checkIndex(long index, long fieldLength) {
    if (isOutOfBounds(index, fieldLength, capacity)) {
      throw new IndexOutOfBoundsException(
          "index: "
              + index
              + ", length: "
              + fieldLength
              + " (expected: range(0, "
              + capacity()
              + "))");
    }
  }

  @Override
  public MappedMemory sync() {
    FileApi.NativeFile.nativeMemorySync(address, capacity(), 0);
    return null;
  }

  @Override
  public boolean release() {
    return FileApi.NativeFile.nativeMemoryUnMapping(address, capacity()) == 0;
  }
}
