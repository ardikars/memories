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

  static final Memory.ByteOrder NATIVE_BYTE_ORDER;

  static final int RESTRICTED_LEVEL;
  static final String RESTRICTED_MESSAGE =
      "Access to restricted method is disabled by default; to enabled access to restricted method, the Memories property 'memories.restricted' must be set to a value other then deny. The possible values for this property are:";
  static final String RESTRICTED_PROPERTY_VALUE =
      "0) deny: issues a runtime exception on each restricted call. This is the default value;\n"
          + "1) permit: allows restricted calls;\n"
          + "2) warn: like permit, but also prints a one-line warning on each restricted call.\n";

  static {
    final String osName = System.getProperty("os.name").toUpperCase().trim();
    final String osArch = System.getProperty("os.arch").toLowerCase().trim();
    String name = getName(osName);
    String arch = getArch(osArch);
    loadLibrary(getPath(name, arch));
    NATIVE_BYTE_ORDER = byteOrder(NativeMemoryAllocator.nativeByteOrderIsBE());
    String restrictedAccess = System.getProperty("memories.restricted", "deny");
    if (restrictedAccess.equals("deny")) {
      RESTRICTED_LEVEL = 0;
    } else if (restrictedAccess.equals("permit")) {
      RESTRICTED_LEVEL = 1;
    } else if (restrictedAccess.equals("warn")) {
      RESTRICTED_LEVEL = 2;
    } else {
      RESTRICTED_LEVEL = 0;
    }
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
    MemoryApi buffer = new MemoryApi(ownerThread, address, size, byteOrder, this);
    MemoryApi.PhantomCleaner cleaner = new MemoryApi.PhantomCleaner(address, buffer, RQ);
    REFS.add(cleaner);

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
    return buffer;
  }

  @Override
  public Memory of(long memoryAddress, long size) throws IllegalAccessException {
    return of(memoryAddress, size, NATIVE_BYTE_ORDER, false);
  }

  @Override
  public Memory of(long memoryAddress, long size, Memory.ByteOrder byteOrder)
      throws IllegalAccessException {
    return of(memoryAddress, size, byteOrder, false);
  }

  @Override
  public Memory of(long memoryAddress, long size, Memory.ByteOrder byteOrder, boolean autoClean)
      throws IllegalAccessException {
    if (RESTRICTED_LEVEL > 0) {
      if (RESTRICTED_LEVEL > 1) {
        System.err.println("Calling restricted method MemoryAllocator#of(..).");
      }
      if (memoryAddress < 0L) {
        throw new IllegalStateException("Memory address must be positive value.");
      }
      if (memoryAddress == 0L) {
        throw new IllegalStateException("Memory buffer already closed.");
      }

      Thread ownerThread = Thread.currentThread();

      MemoryApi buffer = new MemoryApi(ownerThread, memoryAddress, size, byteOrder, this);

      if (autoClean) {
        MemoryApi.PhantomCleaner cleaner = new MemoryApi.PhantomCleaner(memoryAddress, buffer, RQ);
        REFS.add(cleaner);
      }

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
      return buffer;
    } else {
      throw new IllegalAccessException(RESTRICTED_MESSAGE);
    }
  }

  static final class NativeMemoryAllocator {

    private NativeMemoryAllocator() {}

    static native boolean nativeByteOrderIsBE();

    static native long nativeMalloc(long size);

    static native void nativeFree(long address);

    static native long nativeRealloc(long address, long newSize);
  }
}
