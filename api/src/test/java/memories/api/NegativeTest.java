package memories.api;

import java.util.concurrent.atomic.AtomicInteger;
import memories.spi.Memory;
import memories.spi.MemoryAllocator;
import memories.spi.exception.MemoryAccessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
class NegativeTest {

  private static final int INTEGER_BYTES = 4;

  private static final MemoryAllocator allocator = new MemoryAllocatorApi();

  static void methodRef1(Memory memory) {
    //
    memory = null;
  }

  static void methodRef2(Memory memory) {
    //
    memory = null;
  }

  static void freeOnMethodRef(Memory memory) {
    Assertions.assertTrue(memory.release());
  }

  @Test
  void refTest() {
    Memory memory = allocator.allocate(INTEGER_BYTES);
    methodRef1(memory);
    System.gc();
    methodRef2(memory);
    System.gc();
    memory.release();
  }

  @Test
  void doubleFreeOnRefTest() {
    final Memory memory = allocator.allocate(INTEGER_BYTES);
    freeOnMethodRef(memory);
    Assertions.assertThrows(
        MemoryAccessException.class,
        new Executable() {
          @Override
          public void execute() throws Throwable {
            memory.getInt(0);
          }
        });
    Assertions.assertFalse(memory.release());
  }

  // @Test
  void multiThreadGuardTest() {
    final AtomicInteger counter = new AtomicInteger();
    final int maxThread = Runtime.getRuntime().availableProcessors() * 20;
    final Memory[] memories = new Memory[maxThread];
    for (int i = 0; i < memories.length; i++) {
      memories[i] = allocator.allocate(INTEGER_BYTES);
      memories[i].setInt(0, Integer.MAX_VALUE);
      Assertions.assertEquals(Integer.MAX_VALUE, memories[i].getInt(0));
    }
    final Thread[] getterThreads = new Thread[maxThread];
    for (int i = 0; i < getterThreads.length; i++) {
      final int finalI = i;
      getterThreads[i] =
          new Thread(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    if (Integer.MAX_VALUE != memories[finalI].getInt(0)) {
                      throw new AssertionError("Invalid value.");
                    }
                  } catch (Throwable e) {
                    if (!(e instanceof MemoryAccessException)) {
                      throw new AssertionError(e.getMessage());
                    }
                  }
                }
              });
    }
    final Thread[] releaseThreads = new Thread[maxThread];
    for (int i = 0; i < releaseThreads.length; i++) {
      final int finalI = i;
      releaseThreads[i] =
          new Thread(
              new Runnable() {
                @Override
                public void run() {
                  memories[finalI].release();
                  counter.incrementAndGet();
                }
              });
    }
    for (int i = 0; i < maxThread; i++) {
      getterThreads[i].start();
      releaseThreads[i].start();
    }
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      //
    }
    try {
      for (int i = 0; i < maxThread; i++) {
        getterThreads[i].join();
        releaseThreads[i].join();
      }
      Assertions.assertEquals(maxThread, counter.get());
    } catch (InterruptedException e) {
      assert false;
    }
  }

  // @Test
  void increaseCapacityMultiThreadGuardTest() {
    final AtomicInteger counter = new AtomicInteger();
    final int maxThread = Runtime.getRuntime().availableProcessors() * 20;
    final Memory memory = allocator.allocate(INTEGER_BYTES);
    final Thread[] threads = new Thread[maxThread];
    for (int i = 0; i < threads.length; i++) {
      threads[i] =
          new Thread(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    memory.capacity(memory.capacity() + 1);
                    counter.incrementAndGet();
                  } catch (Throwable e) {
                    Assertions.assertTrue(e instanceof MemoryAccessException);
                  }
                }
              });
    }
    for (int i = 0; i < maxThread; i++) {
      threads[i].start();
    }
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      //
    }
    try {
      for (int i = 0; i < maxThread; i++) {
        threads[i].join();
      }
      memory.release();
      Assertions.assertEquals(maxThread, counter.get());
    } catch (InterruptedException e) {
      assert false;
    }
  }
}
