// SPDX-FileCopyrightText: 2020-2021 Memories Project
//
// SPDX-License-Identifier: Apache-2.0

package memories.api;

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
    allocator = new MemoryAllocatorApi();
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
            MemoryAllocatorApi.checkNull(null, "Error");
          }
        });
    MemoryAllocatorApi.checkNull("", "");
  }

  @Test
  void closeInputStream() {
    String path = "/native/memories/jni/linux-x86_64/memories.jnilib";
    InputStream is = MemoryAllocatorApi.class.getResourceAsStream(path);
    try {
      MemoryAllocatorApi.closeInputStream(null);
      MemoryAllocatorApi.closeInputStream(is);
    } catch (IOException e) {
    }
  }

  @Test
  void closeOutputStream() {
    try {
      File temp = File.createTempFile("test-memories", ".jnilib");
      OutputStream os = new FileOutputStream(temp);
      MemoryAllocatorApi.closeOutputStream(null);
      MemoryAllocatorApi.closeOutputStream(os);
    } catch (IOException e) {
    }
  }

  @Test
  void getName() {
    Assertions.assertEquals("linux", MemoryAllocatorApi.getName("LINUX".toUpperCase()));
    Assertions.assertEquals("darwin", MemoryAllocatorApi.getName("MAC OS".toUpperCase()));
    Assertions.assertEquals("windows", MemoryAllocatorApi.getName("WINDOWS".toUpperCase()));
    Assertions.assertEquals(null, MemoryAllocatorApi.getName("Unknown".toUpperCase()));
  }

  @Test
  void getArch() {
    Assertions.assertEquals("x86", MemoryAllocatorApi.getArch("i386".toLowerCase()));
    Assertions.assertEquals("x86", MemoryAllocatorApi.getArch("i686".toLowerCase()));
    Assertions.assertEquals("x86", MemoryAllocatorApi.getArch("i586".toLowerCase()));
    Assertions.assertEquals("x86_64", MemoryAllocatorApi.getArch("x86_64".toLowerCase()));
    Assertions.assertEquals("x86_64", MemoryAllocatorApi.getArch("amd64".toLowerCase()));
    Assertions.assertEquals("x86_64", MemoryAllocatorApi.getArch("x64".toLowerCase()));
    Assertions.assertEquals(null, MemoryAllocatorApi.getArch("Unknown".toLowerCase()));
  }

  @Test
  void getPath() {
    String name = MemoryAllocatorApi.getName("LINUX".toUpperCase());
    String arch = MemoryAllocatorApi.getArch("i386".toLowerCase());
    Assertions.assertEquals(
        "/native/memories/jni/linux-x86/memories.jnilib", MemoryAllocatorApi.getPath(name, arch));
    Assertions.assertEquals(null, MemoryAllocatorApi.getPath(null, arch));
    Assertions.assertEquals(null, MemoryAllocatorApi.getPath(name, null));
  }

  @Test
  void loadLibrary() {
    MemoryAllocatorApi.loadLibrary(null); // returns immediately
    MemoryAllocatorApi.loadLibrary(""); // returns immediately
  }
}
