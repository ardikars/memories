/*
 * SPDX-FileCopyrightText: 2020 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package memories.jni;

import java.io.*;
import java.lang.ref.ReferenceQueue;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import memories.spi.Memory;
import memories.spi.MemoryAllocator;
import memories.spi.exception.MemoryLeakException;

public class JNIMemoryAllocator implements MemoryAllocator {

  static final boolean LEAK_DETECTION =
      System.getProperty("memories.leakDetection", "false").equals("true");

  static final Set REFS = Collections.synchronizedSet(new HashSet());
  static final ReferenceQueue RQ = new ReferenceQueue();

  static {
    final String osName = System.getProperty("os.name").toUpperCase().trim();
    final String osArch = System.getProperty("os.arch").toLowerCase().trim();
    String name = getName(osName);
    String arch = getArch(osArch);
    loadLibrary(getPath(name, arch));
  }

  static String getName(String osName) {
    if (osName.startsWith("LINUX")) {
      return "linux";
    } else if (osName.startsWith("MAC OS")) {
      return "darwin";
    } else if (osName.startsWith("WINDOWS")) {
      return "windows";
    }
    return null;
  }

  static String getArch(String osArch) {
    if ("i386".equals(osArch) || "i686".equals(osArch) || "i586".equals(osArch)) {
      return "x86";
    } else if ("x86_64".equals(osArch) || "amd64".equals(osArch) || "x64".equals(osArch)) {
      return "x86-64";
    }
    return null;
  }

  static String getPath(String name, String arch) {
    if (name != null && arch != null) {
      StringBuffer buffer = new StringBuffer();
      buffer.append("/native/memories/jni/");
      buffer.append(name);
      buffer.append('-');
      buffer.append(arch);
      buffer.append("/memories.jnilib");
      return buffer.toString();
    }
    return null;
  }

  static void loadLibrary(String path) {
    if (path == null || path.length() == 0) {
      return;
    }
    try {
      File temp = File.createTempFile("memories", ".jnilib");
      temp.deleteOnExit();
      final byte[] buffer = new byte[1024];
      int readBytes;
      InputStream is = null;
      try {
        is = JNIMemoryAllocator.class.getResourceAsStream(path);
        checkNull(is, "Error while opening " + path + ".");
        OutputStream os = null;
        try {
          os = new FileOutputStream(temp);
          checkNull(os, "Error while opening " + temp + ".");
          while ((readBytes = is.read(buffer)) != -1) {
            os.write(buffer, 0, readBytes);
          }
        } finally {
          closeOutputStream(os);
        }
      } finally {
        closeInputStream(is);
      }
      System.load(temp.getAbsolutePath());
    } catch (IOException e) {
      //
    }
  }

  static void closeInputStream(InputStream is) throws IOException {
    if (is != null) {
      is.close();
    }
  }

  static void closeOutputStream(OutputStream os) throws IOException {
    if (os != null) {
      os.close();
    }
  }

  static void checkNull(Object object, String message) throws IOException {
    if (object == null) {
      throw new IOException(message);
    }
  }

  static void checkLeak(JNIMemory.Reference ref, boolean enabled) {
    if (ref.address.get() != 0L) {
      JNIMemory.Unsafe.nativeFree(
          ref.address.getAndSet(0L)); // force deallocate memory and set address to '0'.
      if (enabled) {
        StringBuffer stackTraceBuffer = new StringBuffer();
        for (int i = ref.stackTraceElements.length - 1; i >= 0; i--) {
          stackTraceBuffer.append("\t[" + ref.stackTraceElements[i].toString() + "]\n");
        }
        throw new MemoryLeakException(
            "Memory.release() was not called before it's garbage collected.\n\tCreated at:\n "
                + stackTraceBuffer.toString());
      }
    }
  }

  public Memory allocate(long size) {
    return allocate(size, JNIMemory.NATIVE);
  }

  public Memory allocate(long size, Memory.ByteOrder byteOrder) {
    long address = Unsafe.nativeMalloc(size);
    JNIMemory buffer = new JNIMemory(address, size, byteOrder, this);
    JNIMemory.Reference bufRef = new JNIMemory.Reference(address, buffer, RQ);
    REFS.add(bufRef);

    // cleanup native memory wrapped in garbage collected object.
    JNIMemory.Reference ref;
    while ((ref = (JNIMemory.Reference) RQ.poll()) != null) {
      checkLeak(ref, LEAK_DETECTION);
    }
    return buffer;
  }

  static final class Unsafe {
    private Unsafe() {}

    static native long nativeMalloc(long size);
  }
}
