/*
 * SPDX-FileCopyrightText: 2020-2021 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package memories.jdk2;

import memories.spi.Memory;
import memories.spi.MemoryAllocator;

import java.io.*;
import java.lang.ref.ReferenceQueue;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class JDK2MemoryAllocator implements MemoryAllocator {

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
        is = JDK2MemoryAllocator.class.getResourceAsStream(path);
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

  public Memory allocate(long size) {
    return allocate(size, Memory.ByteOrder.BIG_ENDIAN);
  }

  public Memory allocate(long size, Memory.ByteOrder byteOrder) {
    return allocate(size, byteOrder, false);
  }

  @Override
  public Memory allocate(long size, Memory.ByteOrder byteOrder, boolean clear) {
    long address = NativeMemoryAllocator.nativeMalloc(size);

    if (clear) {
      JDK2Memory.NativeMemoryAccess.nativeSetMemory(address, 0, size);
    }

    JDK2Memory buffer = new JDK2Memory(address, size, byteOrder, this);
    JDK2Memory.Reference bufRef = new JDK2Memory.Reference(address, buffer, RQ);
    REFS.add(bufRef);

    // cleanup native memory wrapped in garbage collected object.
    JDK2Memory.Reference ref;
    while ((ref = (JDK2Memory.Reference) RQ.poll()) != null) {
      if (ref.address != 0L) {
        // force deallocate memory and set address to '0'.
        NativeMemoryAllocator.nativeFree(ref.address);
        ref.address = 0L;
      }
    }
    return buffer;
  }

  static final class NativeMemoryAllocator {

    private NativeMemoryAllocator() {}

    static native boolean nativeByteOrderIsBE();

    static native long nativeMalloc(long size);

    static native void nativeFree(long address);

    static native long nativeRealloc(long address, long newSize);
  }
}
