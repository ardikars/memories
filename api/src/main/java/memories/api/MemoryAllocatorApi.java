/*
 * SPDX-FileCopyrightText: 2020-2021 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package memories.api;

import memories.spi.Memory;
import memories.spi.MemoryAllocator;

import java.io.*;
import java.lang.ref.ReferenceQueue;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MemoryAllocatorApi implements MemoryAllocator {

  static final Set REFS = Collections.synchronizedSet(new HashSet());
  static final ReferenceQueue RQ = new ReferenceQueue();

  static final boolean HAS_BYTE_BUFFER;
  static final int JAVA_MAJOR_VERSION;

  static final Memory.ByteOrder NATIVE_BYTE_ORDER;

  static {
    final String osName = System.getProperty("os.name").toUpperCase().trim();
    final String osArch = System.getProperty("os.arch").toLowerCase().trim();
    String name = getName(osName);
    String arch = getArch(osArch);
    loadLibrary(getPath(name, arch));
    NATIVE_BYTE_ORDER = byteOrder(NativeMemoryAllocator.nativeByteOrderIsBE());
    boolean hasByteBuffer;
    try {
      Class.forName("java.nio.ByteBuffer");
      hasByteBuffer = true;
    } catch (ClassNotFoundException e) {
      hasByteBuffer = false;
    }
    HAS_BYTE_BUFFER = hasByteBuffer;
    int javaMajorVersion;
    final String[] components =
        System.getProperty("java.specification.version", "1.6").split("\\.");
    final int[] version = new int[components.length];
    for (int i = 0; i < components.length; i++) {
      version[i] = Integer.parseInt(components[i]);
    }
    if (version[0] == 1) {
      assert version[1] >= 6;
      javaMajorVersion = version[1];
    } else {
      javaMajorVersion = version[0];
    }
    JAVA_MAJOR_VERSION = javaMajorVersion;
  }

  static Memory.ByteOrder byteOrder(boolean isBE) {
    if (isBE) {
      return Memory.ByteOrder.BIG_ENDIAN;
    } else {
      return Memory.ByteOrder.LITTLE_ENDIAN;
    }
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
      return "x86_64";
    } else if ("aarch64".equals(osArch)) {
      return "aarch64";
    } else if ("armhf".equals(osArch)) {
      return "armhf";
    } else if ("armv7".equals(osArch)) {
      return "armv7";
    } else if (osArch.contains("ppc64")) {
      return "ppc64le";
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
        is = MemoryAllocatorApi.class.getResourceAsStream(path);
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

  public Memory allocate(long size, Memory.ByteOrder byteOrder, boolean clear) {
    long address = NativeMemoryAllocator.nativeMalloc(size);

    if (clear) {
      MemoryApi.NativeMemoryAccess.nativeSetMemory(address, 0, size);
    }

    Thread ownerThread = Thread.currentThread();
    MemoryApi buffer = new MemoryApi(null, ownerThread, address, size, byteOrder, this);
    MemoryApi.PhantomCleaner cleaner =
        new MemoryApi.PhantomCleaner(MemoryApi.PhantomCleaner.TYPE_JNI, address, buffer, RQ);
    REFS.add(cleaner);
    clean();
    return buffer;
  }

  @Override
  public Memory wrap(Object buffer) {
    return wrap(buffer, NATIVE_BYTE_ORDER);
  }

  @Override
  public Memory wrap(Object buffer, Memory.ByteOrder byteOrder) {
    if (buffer != null && buffer.getClass().getName().equals("java.nio.DirectByteBuffer")) {
      long memoryAddress = NativeMemoryAllocator.nativeGetDirectBufferAddress(buffer);
      long memoryCapacity = NativeMemoryAllocator.nativeGetDirectBufferCapacity(buffer);
      if (memoryAddress < 0L) {
        throw new IllegalStateException("Memory address must be positive value.");
      }
      if (memoryAddress == 0L) {
        throw new IllegalStateException("Memory buffer already closed.");
      }

      Thread ownerThread = Thread.currentThread();
      MemoryApi newBuffer =
          new MemoryApi(buffer, ownerThread, memoryAddress, memoryCapacity, byteOrder, this);
      MemoryApi.PhantomCleaner cleaner =
          new MemoryApi.PhantomCleaner(
              MemoryApi.PhantomCleaner.TYPE_NIO, memoryAddress, newBuffer, RQ);
      REFS.add(cleaner);
      clean();
      return newBuffer;
    }
    throw new IllegalArgumentException("Unsupported buffer type.");
  }

  void clean() {
    // cleanup native memory when garbage collected.
    MemoryApi.PhantomCleaner cleaned;
    while ((cleaned = (MemoryApi.PhantomCleaner) RQ.poll()) != null) {
      if (cleaned.address != 0L) {
        // force deallocate memory and set address to '0'.
        NativeMemoryAllocator.nativeFree(cleaned.address);
        REFS.remove(cleaned);
        cleaned.address = 0L;
      }
    }
  }

  static final class NativeMemoryAllocator {

    private NativeMemoryAllocator() {}

    static native boolean nativeByteOrderIsBE();

    static native long nativeMalloc(long size);

    static native void nativeFree(long address);

    static native long nativeRealloc(long address, long newSize);

    static native long nativeGetDirectBufferAddress(Object buffer);

    static native long nativeGetDirectBufferCapacity(Object buffer);

    static native void nativeCleanDirectByteBuffer(int jver, Object buffer);
  }
}
