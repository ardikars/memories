// SPDX-FileCopyrightText: 2020-2021 Memories Project
//
// SPDX-License-Identifier: Apache-2.0

package memories.jdk2;

import java.io.*;
import memories.spi.Memory;
import memories.spi.MemoryAllocator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class MemoryAllocatorTest {

  private MemoryAllocator allocator;

  @BeforeEach
  void setUp() {
    allocator = new JDK2MemoryAllocator();
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
            JDK2MemoryAllocator.checkNull(null, "Error");
          }
        });
    JDK2MemoryAllocator.checkNull("", "");
  }

  @Test
  void closeInputStream() {
    String path = "/native/memories/jni/linux-x86_64/memories.jnilib";
    InputStream is = JDK2MemoryAllocator.class.getResourceAsStream(path);
    try {
      JDK2MemoryAllocator.closeInputStream(null);
      JDK2MemoryAllocator.closeInputStream(is);
    } catch (IOException e) {
    }
  }

  @Test
  void closeOutputStream() {
    try {
      File temp = File.createTempFile("test-memories", ".jnilib");
      OutputStream os = new FileOutputStream(temp);
      JDK2MemoryAllocator.closeOutputStream(null);
      JDK2MemoryAllocator.closeOutputStream(os);
    } catch (IOException e) {
    }
  }

  @Test
  void getName() {
    Assertions.assertEquals("linux", JDK2MemoryAllocator.getName("LINUX".toUpperCase()));
    Assertions.assertEquals("darwin", JDK2MemoryAllocator.getName("MAC OS".toUpperCase()));
    Assertions.assertEquals("windows", JDK2MemoryAllocator.getName("WINDOWS".toUpperCase()));
    Assertions.assertEquals(null, JDK2MemoryAllocator.getName("Unknown".toUpperCase()));
  }

  @Test
  void getArch() {
    Assertions.assertEquals("x86", JDK2MemoryAllocator.getArch("i386".toLowerCase()));
    Assertions.assertEquals("x86", JDK2MemoryAllocator.getArch("i686".toLowerCase()));
    Assertions.assertEquals("x86", JDK2MemoryAllocator.getArch("i586".toLowerCase()));
    Assertions.assertEquals("x86_64", JDK2MemoryAllocator.getArch("x86_64".toLowerCase()));
    Assertions.assertEquals("x86_64", JDK2MemoryAllocator.getArch("amd64".toLowerCase()));
    Assertions.assertEquals("x86_64", JDK2MemoryAllocator.getArch("x64".toLowerCase()));
    Assertions.assertEquals(null, JDK2MemoryAllocator.getArch("Unknown".toLowerCase()));
  }

  @Test
  void getPath() {
    String name = JDK2MemoryAllocator.getName("LINUX".toUpperCase());
    String arch = JDK2MemoryAllocator.getArch("i386".toLowerCase());
    Assertions.assertEquals(
        "/native/memories/jni/linux-x86/memories.jnilib", JDK2MemoryAllocator.getPath(name, arch));
    Assertions.assertEquals(null, JDK2MemoryAllocator.getPath(null, arch));
    Assertions.assertEquals(null, JDK2MemoryAllocator.getPath(name, null));
  }

  @Test
  void loadLibrary() {
    JDK2MemoryAllocator.loadLibrary(null); // returns immediately
    JDK2MemoryAllocator.loadLibrary(""); // returns immediately
  }
}
