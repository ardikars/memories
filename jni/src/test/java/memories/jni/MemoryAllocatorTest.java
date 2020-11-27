// SPDX-FileCopyrightText: 2020 Memories Project
//
// SPDX-License-Identifier: Apache-2.0

package memories.jni;

import memories.spi.Memory;
import memories.spi.MemoryAllocator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.io.*;

@RunWith(JUnitPlatform.class)
public class MemoryAllocatorTest {

  private MemoryAllocator allocator;

  @BeforeEach
  void setUp() {
    allocator = new JNIMemoryAllocator();
  }

  @Test
  void allocateThenDeallocate() {
    Memory memory = allocator.allocate(8);
    memory.release();
  }

  @Test
  void allocateThenDeallocateWithGivenByteOrder() {
    Memory beMemory = allocator.allocate(8, Memory.ByteOrder.BIG_ENDIAN);
    beMemory.release();

    Memory leMemory = allocator.allocate(8, Memory.ByteOrder.LITTLE_ENDIAN);
    leMemory.release();
  }

  @Test
  void checkNull() throws IOException {
    Assertions.assertThrows(
        IOException.class,
        new Executable() {
          @Override
          public void execute() throws Throwable {
            JNIMemoryAllocator.checkNull(null, "Error");
          }
        });
    JNIMemoryAllocator.checkNull("", "");
  }

  @Test
  void closeInputStream() {
    String path = "/native/memories/jni/linux-x86-64/memories.jnilib";
    InputStream is = JNIMemoryAllocator.class.getResourceAsStream(path);
    try {
      JNIMemoryAllocator.closeInputStream(null);
      JNIMemoryAllocator.closeInputStream(is);
    } catch (IOException e) {
    }
  }

  @Test
  void closeOutputStream() {
    try {
      File temp = File.createTempFile("test-memories", ".jnilib");
      OutputStream os = new FileOutputStream(temp);
      JNIMemoryAllocator.closeOutputStream(null);
      JNIMemoryAllocator.closeOutputStream(os);
    } catch (IOException e) {
    }
  }
}
