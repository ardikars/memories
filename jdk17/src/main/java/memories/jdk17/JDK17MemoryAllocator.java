package memories.jdk17;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import memories.spi.Memory;
import memories.spi.MemoryAllocator;

import java.lang.ref.ReferenceQueue;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class JDK17MemoryAllocator implements MemoryAllocator {

  static final Set REFS = Collections.synchronizedSet(new HashSet());
  static final ReferenceQueue RQ = new ReferenceQueue();

  @Override
  public Memory allocate(long size) {
    return null;
  }

  @Override
  public Memory allocate(long size, Memory.ByteOrder byteOrder) {
    return null;
  }

  @Override
  public Memory allocate(long size, Memory.ByteOrder byteOrder, boolean clear) {
    MemorySegment segment = MemorySegment.allocateNative(size, 1);
    MemoryAddress address = segment.address().address();
    return null;
  }
}
