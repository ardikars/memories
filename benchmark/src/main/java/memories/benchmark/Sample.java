package memories.benchmark;

import java.nio.ByteBuffer;
import memories.api.MemoryAllocatorApi;
import memories.spi.Memory;
import memories.spi.MemoryAllocator;

public class Sample {

  public static void main(String[] args) {
    final MemoryAllocator allocator = MemoryAllocatorApi.getInstance();
    readWrite(allocator);
    wrapDirectByteBuffer(allocator);
    asBuffer(allocator);
  }

  private static void readWrite(MemoryAllocator allocator) {
    final Memory memory = allocator.allocate(4);
    assert 0 == memory.writerIndex();
    assert 0 == memory.readerIndex();
    memory.writeInt(10);
    assert 4 == memory.writerIndex();
    assert 0 == memory.readerIndex();
    assert 10 == memory.readInt();
    assert 4 == memory.writerIndex();
    assert 4 == memory.readerIndex();
    assert memory.release();
  }

  private static void wrapDirectByteBuffer(MemoryAllocator allocator) {
    final ByteBuffer buffer = ByteBuffer.allocateDirect(4);
    buffer.putInt(0, 10);
    final Memory memory = allocator.wrap(buffer);
    assert 10 == memory.getInt(0);
    assert memory
        .release(); // release buffer immediately without waiting both buffer and memory GC'ed
  }

  private static void asBuffer(MemoryAllocator allocator) {
    final Memory memory = allocator.allocate(4);
    memory.setInt(0, 10);
    ByteBuffer buffer = (ByteBuffer) memory.as(ByteBuffer.class);
    assert buffer.isDirect();
    assert !buffer.isReadOnly();
    assert 10 == buffer.getInt(0);
    assert memory.capacity() == buffer.capacity();
    assert memory.release();
  }
}
