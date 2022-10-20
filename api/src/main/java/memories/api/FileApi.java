package memories.api;

import memories.spi.File;
import memories.spi.MappedMemory;
import memories.spi.Memory;

import java.io.FileNotFoundException;

public class FileApi implements File {

  public static final int PROT_READ = 0x1;
  public static final int PROT_WRITE = 0x2;
  public static final int PROT_EXEC = 0x04;
  public static final int PROT_NONE = 0x00;
  public static final int PROT_GROWSDOWN = 0x01000000;
  public static final int PROT_GROWSUP = 0x02000000;

  public static final int MAP_SHARED = 0x01;
  public static final int MAP_PRIVATE = 0x02;
  public static final int MAP_FIXED = 0x10;
  private final int fd;

  public FileApi(String absolutePath) throws FileNotFoundException {
    this.fd = NativeFile.nativeOpen(absolutePath);
    if (this.fd <= 0) {
      // #define	EACCES 13	/* Permission denied */
      throw new FileNotFoundException("File not found: " + absolutePath + ".");
    }
  }

  @Override
  public MappedMemory map(long offset, long length) {
    final long size = status().size();
    if (offset < 0 || offset >= size) {
      throw new IllegalArgumentException(
          "offset: "
              + offset
              + " (expected: offset("
              + offset
              + ") > 0 and offset("
              + offset
              + ") < size("
              + size
              + ").");
    }
    if (offset + length > size) {
      throw new IllegalArgumentException(
          "length: "
              + length
              + " (expected: length("
              + length
              + ") < remindedSize("
              + (size - offset)
              + ")");
    }
    final long pageSize = NativeFile.nativePageSize();
    final long pageOffset = offset / pageSize;
    final long reminder = offset % pageSize;
    final long ptr =
        NativeFile.nativeMemoryMapping(
            0, length, PROT_READ | PROT_WRITE, MAP_SHARED, fd, pageOffset);
    final MappedMemoryApi memory =
        new MappedMemoryApi(
            null,
            Thread.currentThread(),
            ptr + reminder,
            length,
            Memory.ByteOrder.BIG_ENDIAN,
            null);
    return memory;
  }

  @Override
  public File.Status status() {
    return NativeFile.nativeStatus(fd);
  }

  @Override
  public void close() {
    NativeFile.nativeClose(fd);
  }

  private static final class Status implements File.Status {

    private final long size;

    private Status(long size) {
      this.size = size;
    }

    @Override
    public long size() {
      return size;
    }
  }

  static final class NativeFile {

    static native long nativePageSize();

    // returns fd
    static native int nativeOpen(String absolutePath);

    // returns fstat
    static native Status nativeStatus(int fd);

    // returns ptr to mmap
    static native long nativeMemoryMapping(
        long address, long length, int protectedMode, int flags, int fd, long offset);

    // returns rc
    static native int nativeMemoryUnMapping(long address, long length);

    static native int nativeMemorySync(long address, long length, int flags);

    static native void nativeClose(int fd);
  }
}
