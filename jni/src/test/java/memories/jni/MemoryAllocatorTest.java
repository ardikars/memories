// SPDX-FileCopyrightText: 2020 Memories Project
//
// SPDX-License-Identifier: Apache-2.0

package memories.jni;

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

  @Test
  void getName() {
    Assertions.assertEquals("linux", JNIMemoryAllocator.getName("LINUX".toUpperCase()));
    Assertions.assertEquals("darwin", JNIMemoryAllocator.getName("MAC OS".toUpperCase()));
    Assertions.assertEquals("windows", JNIMemoryAllocator.getName("WINDOWS".toUpperCase()));
    Assertions.assertEquals(null, JNIMemoryAllocator.getName("Unknown".toUpperCase()));
  }

  @Test
  void getArch() {
    Assertions.assertEquals("x86", JNIMemoryAllocator.getArch("i386".toLowerCase()));
    Assertions.assertEquals("x86", JNIMemoryAllocator.getArch("i686".toLowerCase()));
    Assertions.assertEquals("x86", JNIMemoryAllocator.getArch("i586".toLowerCase()));
    Assertions.assertEquals("x86-64", JNIMemoryAllocator.getArch("x86_64".toLowerCase()));
    Assertions.assertEquals("x86-64", JNIMemoryAllocator.getArch("amd64".toLowerCase()));
    Assertions.assertEquals("x86-64", JNIMemoryAllocator.getArch("x64".toLowerCase()));
    Assertions.assertEquals(null, JNIMemoryAllocator.getArch("Unknown".toLowerCase()));
  }

  @Test
  void getPath() {
    String name = JNIMemoryAllocator.getName("LINUX".toUpperCase());
    String arch = JNIMemoryAllocator.getArch("i386".toLowerCase());
    Assertions.assertEquals(
        "/native/memories/jni/linux-x86/memories.jnilib", JNIMemoryAllocator.getPath(name, arch));
    Assertions.assertEquals(null, JNIMemoryAllocator.getPath(null, arch));
    Assertions.assertEquals(null, JNIMemoryAllocator.getPath(name, null));
  }

  @Test
  void loadLibrary() {
    JNIMemoryAllocator.loadLibrary(null); // returns immediately
    JNIMemoryAllocator.loadLibrary(""); // returns immediately
  }
}
