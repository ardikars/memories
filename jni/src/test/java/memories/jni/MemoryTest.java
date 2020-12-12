// SPDX-FileCopyrightText: 2020 Memories Project
//
// SPDX-License-Identifier: Apache-2.0

package memories.jni;

import java.lang.ref.ReferenceQueue;
import memories.spi.Memory;
import memories.spi.MemoryAllocator;
import memories.spi.exception.MemoryAccessException;
import memories.spi.exception.MemoryLeakException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class MemoryTest {

  private static final int BYTE_BYTES = 1;
  private static final int SHORT_BYTES = 2;
  private static final int INTEGER_BYTES = 4;
  private static final int LONG_BYTES = 8;

  private static final MemoryAllocator allocator = new JNIMemoryAllocator();

  // @Test
  void capacity() {
    Memory largeBuffer = allocator.allocate(LONG_BYTES);

    Assertions.assertEquals(LONG_BYTES, largeBuffer.capacity());

    largeBuffer
        .setIndex(largeBuffer.capacity(), largeBuffer.capacity())
        .markReaderIndex()
        .markWriterIndex()
        .capacity(largeBuffer.capacity() * SHORT_BYTES);
    Assertions.assertEquals(LONG_BYTES * SHORT_BYTES, largeBuffer.capacity());
    Assertions.assertEquals(largeBuffer.readerIndex(), largeBuffer.readerIndex());
    Assertions.assertEquals(largeBuffer.writerIndex(), largeBuffer.writerIndex());

    largeBuffer
        .setIndex(largeBuffer.capacity(), largeBuffer.capacity())
        .markReaderIndex()
        .markWriterIndex()
        .capacity(LONG_BYTES);
    Assertions.assertEquals(LONG_BYTES, largeBuffer.capacity());
    Assertions.assertEquals(LONG_BYTES, largeBuffer.readerIndex());
    Assertions.assertEquals(LONG_BYTES, largeBuffer.writerIndex());

    largeBuffer
        .capacity(largeBuffer.capacity() * SHORT_BYTES)
        .setIndex(BYTE_BYTES, SHORT_BYTES)
        .capacity(LONG_BYTES);
    Assertions.assertEquals(LONG_BYTES, largeBuffer.capacity());
    Assertions.assertEquals(BYTE_BYTES, largeBuffer.readerIndex());
    Assertions.assertEquals(SHORT_BYTES, largeBuffer.writerIndex());

    Assertions.assertThrows(IllegalArgumentException.class, () -> largeBuffer.capacity(-1));

    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readerIndex() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertEquals(0, largeBuffer.readerIndex());
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.readerIndex(LONG_BYTES));
    largeBuffer.writerIndex(LONG_BYTES);
    Assertions.assertEquals(LONG_BYTES, largeBuffer.readerIndex(LONG_BYTES).readerIndex());
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.readerIndex(-BYTE_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void writerIndex() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertEquals(0, largeBuffer.writerIndex());
    Assertions.assertEquals(LONG_BYTES, largeBuffer.writerIndex(LONG_BYTES).writerIndex());
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.writerIndex(-BYTE_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.writerIndex(largeBuffer.capacity() + BYTE_BYTES));
    largeBuffer.readerIndex(LONG_BYTES);
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void setIndex() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    largeBuffer.setIndex(LONG_BYTES, LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.setIndex(LONG_BYTES, LONG_BYTES - BYTE_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.setIndex(LONG_BYTES, LONG_BYTES + BYTE_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.setIndex(LONG_BYTES, -BYTE_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.setIndex(LONG_BYTES, BYTE_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.setIndex(-BYTE_BYTES, LONG_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readableBytes() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertEquals(0, largeBuffer.readableBytes());
    Assertions.assertEquals(LONG_BYTES, largeBuffer.writerIndex(LONG_BYTES).readableBytes());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void writableBytes() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertEquals(LONG_BYTES, largeBuffer.writableBytes());
    Assertions.assertEquals(0, largeBuffer.writerIndex(LONG_BYTES).writableBytes());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void isReadable() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertFalse(largeBuffer.isReadable());
    Assertions.assertTrue(largeBuffer.writerIndex(LONG_BYTES).isReadable());
    Assertions.assertFalse(largeBuffer.isReadable(LONG_BYTES + BYTE_BYTES));
    Assertions.assertTrue(largeBuffer.setByte(BYTE_BYTES, LONG_BYTES).isReadable(BYTE_BYTES));
    Assertions.assertFalse(largeBuffer.isReadable(-BYTE_BYTES));
    Assertions.assertFalse(largeBuffer.writerIndex(LONG_BYTES).isReadable(-BYTE_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void isWritable() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertTrue(largeBuffer.isWritable());
    Assertions.assertFalse(largeBuffer.setIndex(LONG_BYTES, LONG_BYTES).isWritable());
    Assertions.assertFalse(largeBuffer.setIndex(0, 0).isWritable(LONG_BYTES + BYTE_BYTES));
    Assertions.assertTrue(largeBuffer.setIndex(BYTE_BYTES, BYTE_BYTES).isWritable(BYTE_BYTES));
    Assertions.assertFalse(largeBuffer.setIndex(0, 0).isWritable(-BYTE_BYTES));
    Assertions.assertFalse(largeBuffer.setIndex(0, LONG_BYTES).isWritable(-BYTE_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void markReader() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertEquals(0, largeBuffer.markReaderIndex().resetReaderIndex().readerIndex());
    Assertions.assertEquals(
        BYTE_BYTES,
        largeBuffer
            .setIndex(BYTE_BYTES, LONG_BYTES)
            .markReaderIndex()
            .readerIndex(LONG_BYTES)
            .resetReaderIndex()
            .readerIndex());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void markWriter() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertEquals(0, largeBuffer.markReaderIndex().writerIndex());
    Assertions.assertEquals(
        BYTE_BYTES,
        largeBuffer
            .setIndex(BYTE_BYTES, BYTE_BYTES)
            .markWriterIndex()
            .writerIndex(SHORT_BYTES)
            .resetWriterIndex()
            .writerIndex());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void ensureWritable() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    largeBuffer.ensureWritable(BYTE_BYTES);
    Assertions.assertThrows(
        IllegalArgumentException.class,
        new Executable() {
          @Override
          public void execute() throws Throwable {
            largeBuffer.ensureWritable(-BYTE_BYTES);
          }
        });
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        new Executable() {
          @Override
          public void execute() throws Throwable {
            largeBuffer.setIndex(LONG_BYTES, LONG_BYTES).ensureWritable(BYTE_BYTES);
          }
        });
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        new Executable() {
          @Override
          public void execute() throws Throwable {
            largeBuffer.setIndex(0, 0).ensureWritable(LONG_BYTES + BYTE_BYTES);
          }
        });
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void getBoolean() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES; i++) {
      largeBuffer.setByte(i, i);
      if (i < 1) {
        Assertions.assertFalse(largeBuffer.getBoolean(i));
      } else {
        Assertions.assertTrue(largeBuffer.getBoolean(i));
      }
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void getUnsignedByte() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES; i++) {
      largeBuffer.setByte(i, 0xFF);
      Assertions.assertEquals(0xFF, largeBuffer.getUnsignedByte(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void getShortRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / SHORT_BYTES; i++) {
      largeBuffer.setShort(i, i);
      Assertions.assertEquals(Short.reverseBytes((short) i), largeBuffer.getShortRE(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void getUnsignedShort() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / SHORT_BYTES; i++) {
      largeBuffer.setShort(i, 0xFFFF);
      Assertions.assertEquals(0xFFFF, largeBuffer.getUnsignedShort(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void getUnsignedShortRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / SHORT_BYTES; i++) {
      largeBuffer.setShort(i, 0xFFFF);
      Assertions.assertEquals(
          Short.reverseBytes((short) 0xFFFF) & 0xFFFF, largeBuffer.getUnsignedShortRE(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void getIntRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / INTEGER_BYTES; i++) {
      largeBuffer.setInt(i, i);
      Assertions.assertEquals(Integer.reverseBytes(i), largeBuffer.getIntRE(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void getUnsignedInt() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / INTEGER_BYTES; i++) {
      largeBuffer.setInt(i, 0xFFFFFFFF);
      Assertions.assertEquals(0xFFFFFFFFL, largeBuffer.getUnsignedInt(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void getUnsignedIntRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / INTEGER_BYTES; i++) {
      largeBuffer.setInt(i, 0xFFFFFFFF);
      Assertions.assertEquals(
          Integer.reverseBytes(0xFFFFFFFF) & 0xFFFFFFFFL, largeBuffer.getUnsignedIntRE(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void getLongRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / LONG_BYTES; i++) {
      largeBuffer.setLong(i, 0xFFFFFFFFFFFFFFFFL);
      Assertions.assertEquals(Long.reverseBytes(0xFFFFFFFFFFFFFFFFL), largeBuffer.getLongRE(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void getFloat() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / INTEGER_BYTES; i += 4) {
      largeBuffer.setFloat(i, i + 0.5F);
      Assertions.assertEquals(i + 0.5F, largeBuffer.getFloat(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void getFloatRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / INTEGER_BYTES; i += 4) {
      largeBuffer.setFloat(i, i + 0.5F);
      Assertions.assertEquals(
          Float.intBitsToFloat(largeBuffer.getIntRE(i)), largeBuffer.getFloatRE(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void getDouble() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / LONG_BYTES; i++) {
      largeBuffer.setDouble(i, i + 0.5D);
      Assertions.assertEquals(i + 0.5D, largeBuffer.getDouble(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void getDoubleRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / LONG_BYTES; i++) {
      largeBuffer.setDouble(i, i + 0.5D);
      Assertions.assertEquals(
          Double.longBitsToDouble(largeBuffer.getLongRE(i)), largeBuffer.getDoubleRE(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void getBytes() {
    final Memory smallBuffer = allocator.allocate(SHORT_BYTES);
    final Memory mediumBuffer = allocator.allocate(INTEGER_BYTES);
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);

    for (int i = 0; i < SHORT_BYTES; i++) {
      smallBuffer.setByte(i, i);
      Assertions.assertEquals(i, smallBuffer.getByte(i));
    }
    for (int i = 0; i < INTEGER_BYTES; i++) {
      mediumBuffer.setByte(i, i);
      Assertions.assertEquals(i, mediumBuffer.getByte(i));
    }

    smallBuffer.getBytes(0, largeBuffer, SHORT_BYTES);
    mediumBuffer.getBytes(0, largeBuffer, INTEGER_BYTES);
    smallBuffer.getBytes(0, largeBuffer, SHORT_BYTES);
    Assertions.assertEquals(0, largeBuffer.getByte(0));
    Assertions.assertEquals(1, largeBuffer.getByte(1));
    Assertions.assertEquals(0, largeBuffer.getByte(2));
    Assertions.assertEquals(1, largeBuffer.getByte(3));
    Assertions.assertEquals(2, largeBuffer.getByte(4));
    Assertions.assertEquals(3, largeBuffer.getByte(5));
    Assertions.assertEquals(0, largeBuffer.getByte(6));
    Assertions.assertEquals(1, largeBuffer.getByte(7));

    byte[] largeBytes = new byte[LONG_BYTES];
    largeBuffer.resetWriterIndex();
    smallBuffer.getBytes(0, largeBytes, 0, SHORT_BYTES);
    mediumBuffer.getBytes(0, largeBytes, SHORT_BYTES, INTEGER_BYTES);
    smallBuffer.getBytes(0, largeBytes, INTEGER_BYTES + SHORT_BYTES, SHORT_BYTES);
    Assertions.assertEquals(largeBytes[0], largeBuffer.getByte(0));
    Assertions.assertEquals(largeBytes[1], largeBuffer.getByte(1));
    Assertions.assertEquals(largeBytes[2], largeBuffer.getByte(2));
    Assertions.assertEquals(largeBytes[3], largeBuffer.getByte(3));
    Assertions.assertEquals(largeBytes[4], largeBuffer.getByte(4));
    Assertions.assertEquals(largeBytes[5], largeBuffer.getByte(5));
    Assertions.assertEquals(largeBytes[6], largeBuffer.getByte(6));
    Assertions.assertEquals(largeBytes[7], largeBuffer.getByte(7));

    byte[] bufBytes = new byte[LONG_BYTES];
    largeBuffer.getBytes(0, bufBytes);
    Assertions.assertArrayEquals(largeBytes, bufBytes);

    Memory newBuf = allocator.allocate(largeBuffer.capacity());

    largeBuffer.getBytes(0, newBuf);
    for (int i = 0; i < newBuf.capacity(); i++) {
      Assertions.assertEquals(newBuf.getByte(i), largeBuffer.getByte(i));
    }
    Assertions.assertTrue(newBuf.release());

    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.getBytes(0, smallBuffer.writerIndex(SHORT_BYTES), 0, LONG_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.getBytes(0, new byte[] {0, 0}, 0, LONG_BYTES));

    Assertions.assertTrue(smallBuffer.release());
    Assertions.assertTrue(mediumBuffer.release());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void setBoolean() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES; i++) {
      largeBuffer.setBoolean(i, i % 2 == 0);
      if (i % 2 == 0) {
        Assertions.assertTrue(largeBuffer.getByte(i) == 1);
      } else {
        Assertions.assertTrue(largeBuffer.getByte(i) == 0);
      }
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void setShortRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / SHORT_BYTES; i++) {
      largeBuffer.setShortRE(i, i);
      Assertions.assertEquals(Short.reverseBytes((short) i), largeBuffer.getShort(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void setIntRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / INTEGER_BYTES; i++) {
      largeBuffer.setIntRE(i, i);
      Assertions.assertEquals(Integer.reverseBytes(i), largeBuffer.getInt(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void setLongRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / LONG_BYTES; i++) {
      largeBuffer.setLongRE(i, 0xFFFFFFFFFFFFFFFFL);
      Assertions.assertEquals(Long.reverseBytes(0xFFFFFFFFFFFFFFFFL), largeBuffer.getLong(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void setFloat() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / INTEGER_BYTES; i += 4) {
      largeBuffer.setFloat(i, i + 0.5F);
      Assertions.assertEquals(i + 0.5F, largeBuffer.getFloat(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void setFloatRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / INTEGER_BYTES; i += 4) {
      largeBuffer.setFloatRE(i, i + 0.5F);
      Assertions.assertEquals(
          Float.intBitsToFloat(largeBuffer.getIntRE(i)), largeBuffer.getFloatRE(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void setDouble() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / LONG_BYTES; i++) {
      largeBuffer.setDouble(i, i + 0.5D);
      Assertions.assertEquals(i + 0.5D, largeBuffer.getDouble(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void setDoubleRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / LONG_BYTES; i++) {
      largeBuffer.setDoubleRE(i, i + 0.5D);
      Assertions.assertEquals(
          Double.longBitsToDouble(largeBuffer.getLongRE(i)), largeBuffer.getDoubleRE(i));
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void setBytes() {
    final Memory smallBuffer = allocator.allocate(SHORT_BYTES);
    final Memory mediumBuffer = allocator.allocate(INTEGER_BYTES);
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);

    for (int i = 0; i < SHORT_BYTES; i++) {
      smallBuffer.setByte(i, i);
      Assertions.assertEquals(i, smallBuffer.getByte(i));
    }
    for (int i = 0; i < INTEGER_BYTES; i++) {
      mediumBuffer.setByte(i, i);
      Assertions.assertEquals(i, mediumBuffer.getByte(i));
    }
    smallBuffer.setIndex(0, SHORT_BYTES);
    mediumBuffer.setIndex(0, INTEGER_BYTES);

    largeBuffer.setBytes(0, smallBuffer);
    largeBuffer.setBytes(SHORT_BYTES, mediumBuffer);
    smallBuffer.setIndex(0, SHORT_BYTES);
    largeBuffer.setBytes(INTEGER_BYTES + SHORT_BYTES, smallBuffer);

    Assertions.assertEquals(0, largeBuffer.getByte(0));
    Assertions.assertEquals(1, largeBuffer.getByte(1));
    Assertions.assertEquals(0, largeBuffer.getByte(2));
    Assertions.assertEquals(1, largeBuffer.getByte(3));
    Assertions.assertEquals(2, largeBuffer.getByte(4));
    Assertions.assertEquals(3, largeBuffer.getByte(5));
    Assertions.assertEquals(0, largeBuffer.getByte(6));
    Assertions.assertEquals(1, largeBuffer.getByte(7));

    final byte[] bytes = new byte[LONG_BYTES];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = (byte) i;
    }
    largeBuffer.setIndex(0, 0);
    largeBuffer.setBytes(0, bytes, 0, bytes.length);
    for (int i = 0; i < BYTE_BYTES; i++) {
      Assertions.assertEquals(i, largeBuffer.getByte(0));
    }
    largeBuffer.setIndex(0, 0);
    largeBuffer.setBytes(0, bytes);
    for (int i = 0; i < BYTE_BYTES; i++) {
      Assertions.assertEquals(i, largeBuffer.getByte(0));
    }
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> largeBuffer.setBytes(0, null, SHORT_BYTES));
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> largeBuffer.setBytes(0, null, INTEGER_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> {
          smallBuffer.setIndex(BYTE_BYTES, SHORT_BYTES);
          largeBuffer.setBytes(0, smallBuffer, SHORT_BYTES);
        });
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> {
          smallBuffer.setIndex(SHORT_BYTES, INTEGER_BYTES);
          largeBuffer.setBytes(0, smallBuffer, INTEGER_BYTES);
        });

    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.setBytes(0, smallBuffer, 1, SHORT_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.setBytes(0, mediumBuffer, 1, INTEGER_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.setBytes(0, bytes, 1, LONG_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.setBytes(0, bytes, 1, LONG_BYTES));

    Assertions.assertTrue(smallBuffer.release());
    Assertions.assertTrue(mediumBuffer.release());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readBoolean() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    largeBuffer.writeBoolean(true);
    largeBuffer.writeBoolean(true);
    largeBuffer.writeBoolean(true);
    largeBuffer.writeBoolean(true);
    largeBuffer.writeBoolean(false);
    largeBuffer.writeBoolean(false);
    largeBuffer.writeBoolean(false);
    largeBuffer.writeBoolean(false);
    Assertions.assertTrue(largeBuffer.readBoolean());
    Assertions.assertTrue(largeBuffer.readBoolean());
    Assertions.assertTrue(largeBuffer.readBoolean());
    Assertions.assertTrue(largeBuffer.readBoolean());
    Assertions.assertFalse(largeBuffer.readBoolean());
    Assertions.assertFalse(largeBuffer.readBoolean());
    Assertions.assertFalse(largeBuffer.readBoolean());
    Assertions.assertFalse(largeBuffer.readBoolean());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readByte() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> largeBuffer.readByte());
    for (int i = 0; i < LONG_BYTES; i++) {
      largeBuffer.writeByte(i);
      Assertions.assertEquals(i, largeBuffer.readByte());
    }
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> largeBuffer.readByte());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readUnsignedByte() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES; i++) {
      largeBuffer.writeByte(i);
      Assertions.assertEquals(i, largeBuffer.readUnsignedByte());
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readShort() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> largeBuffer.readShort());
    for (int i = 0; i < LONG_BYTES / SHORT_BYTES; i++) {
      largeBuffer.writeShort(i);
      Assertions.assertEquals(i, largeBuffer.readShort());
    }
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> largeBuffer.readShort());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readShortRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> largeBuffer.readShortRE());
    for (int i = 0; i < LONG_BYTES / SHORT_BYTES; i++) {
      largeBuffer.writeShortRE(i);
      Assertions.assertEquals(i, largeBuffer.readShortRE());
    }
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> largeBuffer.readShortRE());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readUnsignedShort() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / SHORT_BYTES; i++) {
      largeBuffer.writeShort(0xFFFF);
      Assertions.assertEquals(0xFFFF, largeBuffer.readUnsignedShort());
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readUnsignedShortRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / SHORT_BYTES; i++) {
      largeBuffer.writeShortRE(0xFFFF);
      Assertions.assertEquals(0xFFFF, largeBuffer.readUnsignedShortRE());
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readInt() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> largeBuffer.readInt());
    for (int i = 0; i < LONG_BYTES / INTEGER_BYTES; i++) {
      largeBuffer.writeInt(i);
      Assertions.assertEquals(i, largeBuffer.readInt());
    }
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> largeBuffer.readInt());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readIntRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> largeBuffer.readIntRE());
    for (int i = 0; i < LONG_BYTES / INTEGER_BYTES; i++) {
      largeBuffer.writeIntRE(i);
      Assertions.assertEquals(i, largeBuffer.readIntRE());
    }
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> largeBuffer.readIntRE());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readUnsignedInt() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / INTEGER_BYTES; i++) {
      largeBuffer.writeInt(0xFFFFFFFF);
      Assertions.assertEquals(0xFFFFFFFFL, largeBuffer.readUnsignedInt());
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readUnsignedIntRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / INTEGER_BYTES; i++) {
      largeBuffer.writeIntRE(0xFFFFFFFF);
      Assertions.assertEquals(0xFFFFFFFFL, largeBuffer.readUnsignedIntRE());
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readFloat() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / INTEGER_BYTES; i++) {
      largeBuffer.writeFloat(i + 0.5F);
      Assertions.assertEquals(i + 0.5F, largeBuffer.readFloat());
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readFloatRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / INTEGER_BYTES; i++) {
      largeBuffer.writeFloatRE(i + 0.5F);
      Assertions.assertEquals(i + 0.5F, largeBuffer.readFloatRE());
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readDouble() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / LONG_BYTES; i++) {
      largeBuffer.writeDouble(i + 0.5D);
      Assertions.assertEquals(i + 0.5D, largeBuffer.readDouble());
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readDoubleRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    for (int i = 0; i < LONG_BYTES / LONG_BYTES; i++) {
      largeBuffer.writeDoubleRE(i + 0.5D);
      Assertions.assertEquals(i + 0.5D, largeBuffer.readDoubleRE());
    }
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readLong() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> largeBuffer.readLong());
    largeBuffer.writeLong(Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, largeBuffer.readLong());
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> largeBuffer.readLong());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readLongRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> largeBuffer.readLongRE());
    largeBuffer.writeLongRE(Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, largeBuffer.readLongRE());
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> largeBuffer.readLongRE());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void readBytes() {
    final Memory smallBuffer = allocator.allocate(SHORT_BYTES);
    final Memory mediumBuffer = allocator.allocate(INTEGER_BYTES);
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);

    final byte[] smallBytesDst = new byte[SHORT_BYTES];
    final Memory smallBufDst = allocator.allocate(SHORT_BYTES);
    final byte[] mediumBytesDst = new byte[INTEGER_BYTES];
    final Memory mediumBufDst = allocator.allocate(INTEGER_BYTES);
    final byte[] largeBytesDst = new byte[LONG_BYTES];
    final Memory largeBufDst = allocator.allocate(LONG_BYTES);

    for (int i = 0; i < SHORT_BYTES; i++) {
      smallBuffer.writeByte(i);
    }
    for (int i = 0; i < INTEGER_BYTES; i++) {
      mediumBuffer.writeByte(i);
    }
    for (int i = 0; i < LONG_BYTES; i++) {
      largeBuffer.writeByte(i);
    }
    // Memory readBytes(Memory dst)
    Assertions.assertEquals(smallBuffer.writerIndex(), SHORT_BYTES);
    smallBuffer.readBytes(smallBufDst);
    smallBuffer.setIndex(0, SHORT_BYTES);
    smallBufDst.setIndex(0, SHORT_BYTES);
    for (int i = 0; i < SHORT_BYTES; i++) {
      Assertions.assertEquals(smallBuffer.readByte(), smallBufDst.readByte());
    }
    Assertions.assertEquals(mediumBuffer.writerIndex(), INTEGER_BYTES);
    mediumBuffer.readBytes(mediumBufDst);
    mediumBuffer.setIndex(0, INTEGER_BYTES);
    mediumBufDst.setIndex(0, INTEGER_BYTES);
    for (int i = 0; i < INTEGER_BYTES; i++) {
      Assertions.assertEquals(mediumBuffer.readByte(), mediumBufDst.readByte());
    }
    Assertions.assertEquals(largeBuffer.writerIndex(), LONG_BYTES);
    largeBuffer.readBytes(largeBufDst);
    largeBuffer.setIndex(0, LONG_BYTES);
    largeBufDst.setIndex(0, LONG_BYTES);
    for (int i = 0; i < LONG_BYTES; i++) {
      Assertions.assertEquals(largeBuffer.readByte(), largeBufDst.readByte());
    }

    smallBuffer.setIndex(0, SHORT_BYTES);
    smallBufDst.setIndex(0, 0);
    mediumBuffer.setIndex(0, INTEGER_BYTES);
    mediumBufDst.setIndex(0, 0);
    largeBuffer.setIndex(0, LONG_BYTES);
    largeBufDst.setIndex(0, 0);

    // Memory readBytes(Memory dst, long length)
    Assertions.assertEquals(smallBuffer.writerIndex(), SHORT_BYTES);
    smallBuffer.readBytes(smallBufDst, SHORT_BYTES);
    smallBuffer.setIndex(0, SHORT_BYTES);
    smallBufDst.setIndex(0, SHORT_BYTES);
    for (int i = 0; i < SHORT_BYTES; i++) {
      Assertions.assertEquals(smallBuffer.readByte(), smallBufDst.readByte());
    }
    Assertions.assertEquals(mediumBuffer.writerIndex(), INTEGER_BYTES);
    mediumBuffer.readBytes(mediumBufDst, INTEGER_BYTES);
    mediumBuffer.setIndex(0, INTEGER_BYTES);
    mediumBufDst.setIndex(0, INTEGER_BYTES);
    for (int i = 0; i < INTEGER_BYTES; i++) {
      Assertions.assertEquals(mediumBuffer.readByte(), mediumBufDst.readByte());
    }
    Assertions.assertEquals(largeBuffer.writerIndex(), LONG_BYTES);
    largeBuffer.readBytes(largeBufDst, LONG_BYTES);
    largeBuffer.setIndex(0, LONG_BYTES);
    largeBufDst.setIndex(0, LONG_BYTES);
    for (int i = 0; i < LONG_BYTES; i++) {
      Assertions.assertEquals(largeBuffer.readByte(), largeBufDst.readByte());
    }

    smallBuffer.setIndex(BYTE_BYTES, SHORT_BYTES);
    smallBufDst.setIndex(BYTE_BYTES, SHORT_BYTES);
    mediumBuffer.setIndex(BYTE_BYTES, INTEGER_BYTES);
    mediumBufDst.setIndex(BYTE_BYTES, INTEGER_BYTES);
    largeBuffer.setIndex(BYTE_BYTES, LONG_BYTES);
    largeBufDst.setIndex(BYTE_BYTES, LONG_BYTES);

    // Memory readBytes(Memory dst, long dstIndex, long length)
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> smallBuffer.readBytes(smallBufDst, -1));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> smallBuffer.readBytes(smallBufDst, SHORT_BYTES));
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> mediumBuffer.readBytes(mediumBufDst, -1));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> mediumBuffer.readBytes(mediumBufDst, INTEGER_BYTES));
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> largeBuffer.readBytes(largeBufDst, -1));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.readBytes(largeBufDst, LONG_BYTES));

    // Memory readBytes(byte[] dst)
    smallBuffer.setIndex(0, SHORT_BYTES);
    smallBufDst.setIndex(0, 0);
    mediumBuffer.setIndex(0, INTEGER_BYTES);
    mediumBufDst.setIndex(0, 0);
    largeBuffer.setIndex(0, LONG_BYTES);
    largeBufDst.setIndex(0, 0);

    smallBuffer.readBytes(smallBytesDst);
    for (int i = 0; i < SHORT_BYTES; i++) {
      Assertions.assertEquals(smallBuffer.getByte(i), smallBytesDst[i]);
    }
    mediumBuffer.readBytes(mediumBytesDst);
    for (int i = 0; i < INTEGER_BYTES; i++) {
      Assertions.assertEquals(mediumBuffer.getByte(i), mediumBytesDst[i]);
    }
    largeBuffer.readBytes(largeBytesDst);
    for (int i = 0; i < LONG_BYTES; i++) {
      Assertions.assertEquals(largeBuffer.getByte(i), largeBytesDst[i]);
    }

    // Memory readBytes(byte[] dst, long dstIndex, long length)
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> smallBuffer.readBytes(smallBytesDst, 0, -1));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> smallBuffer.readBytes(smallBytesDst, BYTE_BYTES, SHORT_BYTES));
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> mediumBuffer.readBytes(mediumBytesDst, 0, -1));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> mediumBuffer.readBytes(mediumBytesDst, BYTE_BYTES, INTEGER_BYTES));
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> largeBuffer.readBytes(largeBytesDst, 0, -1));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.readBytes(largeBytesDst, BYTE_BYTES, LONG_BYTES));
    Assertions.assertTrue(smallBufDst.release());
    Assertions.assertTrue(mediumBufDst.release());
    Assertions.assertTrue(largeBufDst.release());

    Assertions.assertTrue(smallBuffer.release());
    Assertions.assertTrue(mediumBuffer.release());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void skipBytes() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);

    for (int i = 0; i < LONG_BYTES; i++) {
      largeBuffer.writeByte(i);
    }
    largeBuffer.skipBytes(1);
    Assertions.assertEquals(1, largeBuffer.readByte());
    largeBuffer.skipBytes(2);
    Assertions.assertEquals(4, largeBuffer.readByte());
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        new Executable() {
          @Override
          public void execute() throws Throwable {
            largeBuffer.skipBytes(4);
          }
        });
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void writeBoolean() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    largeBuffer.writeBoolean(true);
    largeBuffer.writeBoolean(true);
    largeBuffer.writeBoolean(true);
    largeBuffer.writeBoolean(true);
    largeBuffer.writeBoolean(false);
    largeBuffer.writeBoolean(false);
    largeBuffer.writeBoolean(false);
    largeBuffer.writeBoolean(false);
    Assertions.assertTrue(largeBuffer.readBoolean());
    Assertions.assertTrue(largeBuffer.readBoolean());
    Assertions.assertTrue(largeBuffer.readBoolean());
    Assertions.assertTrue(largeBuffer.readBoolean());
    Assertions.assertFalse(largeBuffer.readBoolean());
    Assertions.assertFalse(largeBuffer.readBoolean());
    Assertions.assertFalse(largeBuffer.readBoolean());
    Assertions.assertFalse(largeBuffer.readBoolean());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void writeByte() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.setIndex(0, LONG_BYTES).writeByte(BYTE_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void writeShort() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.setIndex(0, LONG_BYTES).writeShort(BYTE_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void writeShortRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.setIndex(0, LONG_BYTES).writeShortRE(BYTE_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void writeInt() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.setIndex(0, LONG_BYTES).writeInt(BYTE_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void writeIntRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.setIndex(0, LONG_BYTES).writeIntRE(BYTE_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void writeLong() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.setIndex(0, LONG_BYTES).writeLong(BYTE_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void writeLongRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.setIndex(0, LONG_BYTES).writeLongRE(BYTE_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void writeFloat() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.setIndex(0, LONG_BYTES).writeFloat(BYTE_BYTES + 0.5F));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void writeFloatRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.setIndex(0, LONG_BYTES).writeFloatRE(BYTE_BYTES + 0.5F));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void writeDouble() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        new Executable() {
          @Override
          public void execute() throws Throwable {
            largeBuffer.setIndex(0, LONG_BYTES).writeDouble(BYTE_BYTES + 0.5D);
          }
        });
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void writeDoubleRE() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.setIndex(0, LONG_BYTES).writeDoubleRE(BYTE_BYTES + 0.5D));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void writeBytes() {
    final Memory smallBuffer = allocator.allocate(SHORT_BYTES);
    final Memory mediumBuffer = allocator.allocate(INTEGER_BYTES);
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);

    final byte[] smallBytesDst = new byte[SHORT_BYTES];
    final Memory smallBufSrc = allocator.allocate(SHORT_BYTES);
    final byte[] mediumBytesDst = new byte[INTEGER_BYTES];
    final Memory mediumBufSrc = allocator.allocate(INTEGER_BYTES);
    final byte[] largeBytesDst = new byte[LONG_BYTES];
    final Memory largeBufSrc = allocator.allocate(LONG_BYTES);

    for (int i = 0; i < SHORT_BYTES; i++) {
      smallBufSrc.writeByte(i);
    }
    for (int i = 0; i < INTEGER_BYTES; i++) {
      mediumBufSrc.writeByte(i);
    }
    for (int i = 0; i < LONG_BYTES; i++) {
      largeBufSrc.writeByte(i);
    }
    // Memory readBytes(Memory dst)
    Assertions.assertEquals(smallBufSrc.writerIndex(), SHORT_BYTES);
    smallBuffer.writeBytes(smallBufSrc);
    smallBuffer.setIndex(0, SHORT_BYTES);
    smallBufSrc.setIndex(0, SHORT_BYTES);
    for (int i = 0; i < SHORT_BYTES; i++) {
      Assertions.assertEquals(smallBuffer.readByte(), smallBufSrc.readByte());
    }
    Assertions.assertEquals(mediumBufSrc.writerIndex(), INTEGER_BYTES);
    mediumBuffer.writeBytes(mediumBufSrc);
    mediumBuffer.setIndex(0, INTEGER_BYTES);
    mediumBufSrc.setIndex(0, INTEGER_BYTES);
    for (int i = 0; i < INTEGER_BYTES; i++) {
      Assertions.assertEquals(mediumBuffer.readByte(), mediumBufSrc.readByte());
    }
    Assertions.assertEquals(LONG_BYTES, largeBufSrc.writerIndex());
    largeBuffer.writeBytes(largeBufSrc);
    largeBuffer.setIndex(0, LONG_BYTES);
    largeBufSrc.setIndex(0, LONG_BYTES);
    for (int i = 0; i < LONG_BYTES; i++) {
      Assertions.assertEquals(largeBuffer.readByte(), largeBufSrc.readByte());
    }

    smallBufSrc.setIndex(0, SHORT_BYTES);
    smallBuffer.setIndex(0, 0);
    mediumBufSrc.setIndex(0, INTEGER_BYTES);
    mediumBuffer.setIndex(0, 0);
    largeBufSrc.setIndex(0, LONG_BYTES);
    largeBuffer.setIndex(0, 0);

    // Memory readBytes(Memory dst, long length)
    Assertions.assertEquals(smallBufSrc.writerIndex(), SHORT_BYTES);
    smallBuffer.writeBytes(smallBufSrc, SHORT_BYTES);
    smallBuffer.setIndex(0, SHORT_BYTES);
    smallBufSrc.setIndex(0, SHORT_BYTES);
    for (int i = 0; i < SHORT_BYTES; i++) {
      Assertions.assertEquals(smallBuffer.readByte(), smallBufSrc.readByte());
    }
    Assertions.assertEquals(mediumBufSrc.writerIndex(), INTEGER_BYTES);
    mediumBuffer.writeBytes(mediumBufSrc, INTEGER_BYTES);
    mediumBuffer.setIndex(0, INTEGER_BYTES);
    mediumBufSrc.setIndex(0, INTEGER_BYTES);
    for (int i = 0; i < INTEGER_BYTES; i++) {
      Assertions.assertEquals(mediumBuffer.readByte(), mediumBufSrc.readByte());
    }
    Assertions.assertEquals(LONG_BYTES, largeBufSrc.writerIndex());
    largeBuffer.writeBytes(largeBufSrc, LONG_BYTES);
    largeBuffer.setIndex(0, LONG_BYTES);
    largeBufSrc.setIndex(0, LONG_BYTES);
    for (int i = 0; i < LONG_BYTES; i++) {
      Assertions.assertEquals(largeBuffer.readByte(), largeBufSrc.readByte());
    }

    smallBuffer.setIndex(BYTE_BYTES, SHORT_BYTES);
    smallBufSrc.setIndex(BYTE_BYTES, SHORT_BYTES);
    mediumBuffer.setIndex(BYTE_BYTES, INTEGER_BYTES);
    mediumBufSrc.setIndex(BYTE_BYTES, INTEGER_BYTES);
    largeBuffer.setIndex(BYTE_BYTES, LONG_BYTES);
    largeBufSrc.setIndex(BYTE_BYTES, LONG_BYTES);

    //     Memory writeBytes(Memory dst, long dstIndex, long length)
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> smallBuffer.writeBytes(smallBufSrc, -1));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> smallBuffer.writeBytes(smallBufSrc, SHORT_BYTES));
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> mediumBuffer.readBytes(mediumBufSrc, -1));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> mediumBuffer.writeBytes(mediumBufSrc, INTEGER_BYTES));
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> largeBuffer.writeBytes(largeBufSrc, -1));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.writeBytes(largeBufSrc, LONG_BYTES));

    // Memory writeBytes(byte[] dst)
    smallBuffer.setIndex(0, SHORT_BYTES);
    smallBufSrc.setIndex(0, 0);
    mediumBuffer.setIndex(0, INTEGER_BYTES);
    mediumBufSrc.setIndex(0, 0);
    largeBuffer.setIndex(0, LONG_BYTES);
    largeBufSrc.setIndex(0, 0);

    smallBuffer.readBytes(smallBytesDst);
    for (int i = 0; i < SHORT_BYTES; i++) {
      Assertions.assertEquals(smallBuffer.getByte(i), smallBytesDst[i]);
    }
    mediumBuffer.readBytes(mediumBytesDst);
    for (int i = 0; i < INTEGER_BYTES; i++) {
      Assertions.assertEquals(mediumBuffer.getByte(i), mediumBytesDst[i]);
    }
    largeBuffer.readBytes(largeBytesDst);
    for (int i = 0; i < LONG_BYTES; i++) {
      Assertions.assertEquals(largeBuffer.getByte(i), largeBytesDst[i]);
    }

    // Memory writeBytes(byte[] dst, long dstIndex, long length)
    byte[] srcBytes = new byte[] {127, 0, 0, 1};
    mediumBuffer.setIndex(0, 0);
    mediumBuffer.writeBytes(srcBytes);
    byte[] dstBytes = new byte[srcBytes.length];
    mediumBuffer.readBytes(dstBytes);
    Assertions.assertArrayEquals(srcBytes, dstBytes);

    Assertions.assertThrows(
        IllegalArgumentException.class, () -> smallBuffer.readBytes(smallBytesDst, 0, -1));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> smallBuffer.readBytes(smallBytesDst, BYTE_BYTES, SHORT_BYTES));
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> mediumBuffer.readBytes(mediumBytesDst, 0, -1));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> mediumBuffer.readBytes(mediumBytesDst, BYTE_BYTES, INTEGER_BYTES));
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> largeBuffer.readBytes(largeBytesDst, 0, -1));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.readBytes(largeBytesDst, BYTE_BYTES, LONG_BYTES));

    Assertions.assertTrue(smallBufSrc.release());
    Assertions.assertTrue(mediumBufSrc.release());
    Assertions.assertTrue(largeBufSrc.release());

    Assertions.assertTrue(smallBuffer.release());
    Assertions.assertTrue(mediumBuffer.release());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void byteOrder() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertEquals(Memory.ByteOrder.BIG_ENDIAN, largeBuffer.byteOrder());
    Assertions.assertEquals(
        Memory.ByteOrder.LITTLE_ENDIAN,
        largeBuffer.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN).byteOrder());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void copy() {
    final Memory smallBuffer = allocator.allocate(SHORT_BYTES);
    final Memory mediumBuffer = allocator.allocate(INTEGER_BYTES);
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);

    for (int i = 0; i < SHORT_BYTES; i++) {
      smallBuffer.writeByte(i);
    }
    for (int i = 0; i < INTEGER_BYTES; i++) {
      mediumBuffer.writeByte(i);
    }
    for (int i = 0; i < LONG_BYTES; i++) {
      largeBuffer.writeByte(i);
    }
    Memory smallCopy = smallBuffer.copy();
    Memory smallCopySliced = smallBuffer.copy(BYTE_BYTES, BYTE_BYTES);
    Memory mediumCopy = mediumBuffer.copy();
    Memory mediumCopySliced = mediumBuffer.copy(SHORT_BYTES, SHORT_BYTES);
    Memory largeCopy = largeBuffer.copy();
    Memory largeCopySliced = largeBuffer.copy(INTEGER_BYTES, INTEGER_BYTES);

    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> smallBuffer.copy(-BYTE_BYTES, BYTE_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> smallBuffer.copy(BYTE_BYTES, LONG_BYTES + BYTE_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> mediumBuffer.copy(-BYTE_BYTES, BYTE_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> mediumBuffer.copy(BYTE_BYTES, LONG_BYTES + BYTE_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.copy(-BYTE_BYTES, BYTE_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.copy(BYTE_BYTES, LONG_BYTES + BYTE_BYTES));

    for (int i = 0; i < SHORT_BYTES; i++) {
      Assertions.assertEquals(smallCopy.getByte(i), smallBuffer.getByte(i));
    }
    for (int i = BYTE_BYTES; i < BYTE_BYTES + BYTE_BYTES; i++) {
      Assertions.assertEquals(smallCopySliced.getByte(i - BYTE_BYTES), smallBuffer.getByte(i));
    }
    for (int i = 0; i < INTEGER_BYTES; i++) {
      Assertions.assertEquals(mediumCopy.getByte(i), mediumBuffer.getByte(i));
    }
    for (int i = SHORT_BYTES; i < SHORT_BYTES + SHORT_BYTES; i++) {
      Assertions.assertEquals(mediumCopySliced.getByte(i - SHORT_BYTES), mediumBuffer.getByte(i));
    }
    for (int i = 0; i < LONG_BYTES; i++) {
      Assertions.assertEquals(largeCopy.getByte(i), largeBuffer.getByte(i));
    }
    for (int i = INTEGER_BYTES; i < INTEGER_BYTES + INTEGER_BYTES; i++) {
      Assertions.assertEquals(largeCopySliced.getByte(i - INTEGER_BYTES), largeBuffer.getByte(i));
    }

    Assertions.assertTrue(smallCopy.release());
    Assertions.assertTrue(smallCopySliced.release());
    Assertions.assertTrue(mediumCopy.release());
    Assertions.assertTrue(mediumCopySliced.release());
    Assertions.assertTrue(largeCopy.release());
    Assertions.assertTrue(largeCopySliced.release());

    Assertions.assertTrue(smallBuffer.release());
    Assertions.assertTrue(mediumBuffer.release());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void slice() {
    final Memory smallBuffer = allocator.allocate(SHORT_BYTES);
    final Memory mediumBuffer = allocator.allocate(INTEGER_BYTES);
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);

    Assertions.assertEquals(0, smallBuffer.slice().capacity());
    Assertions.assertEquals(1, smallBuffer.writerIndex(BYTE_BYTES).slice().capacity());
    Assertions.assertEquals(0, mediumBuffer.slice().capacity());
    Assertions.assertEquals(1, mediumBuffer.writerIndex(BYTE_BYTES).slice().capacity());
    Assertions.assertEquals(0, largeBuffer.slice().capacity());
    Assertions.assertEquals(1, largeBuffer.writerIndex(BYTE_BYTES).slice().capacity());

    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> smallBuffer.writerIndex(SHORT_BYTES).slice(-BYTE_BYTES, SHORT_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> smallBuffer.writerIndex(SHORT_BYTES).slice(0, SHORT_BYTES + BYTE_BYTES));

    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> mediumBuffer.writerIndex(INTEGER_BYTES).slice(-BYTE_BYTES, INTEGER_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> mediumBuffer.writerIndex(INTEGER_BYTES).slice(0, INTEGER_BYTES + BYTE_BYTES));

    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.writerIndex(LONG_BYTES).slice(-BYTE_BYTES, LONG_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.writerIndex(LONG_BYTES).slice(0, LONG_BYTES + BYTE_BYTES));

    Assertions.assertTrue(smallBuffer.release());
    Assertions.assertTrue(mediumBuffer.release());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void getByte() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.getByte(-BYTE_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.getByte(LONG_BYTES + BYTE_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void getShort() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.getShort(-BYTE_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.getShort(LONG_BYTES + BYTE_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void getInt() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> largeBuffer.getInt(-BYTE_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.getInt(LONG_BYTES + BYTE_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void getLong() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.getLong(-BYTE_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.getLong(LONG_BYTES + BYTE_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void setByte() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.setByte(-BYTE_BYTES, LONG_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.setByte(LONG_BYTES + BYTE_BYTES, LONG_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void setShort() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.setShort(-BYTE_BYTES, LONG_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.setShort(LONG_BYTES + BYTE_BYTES, LONG_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void setInt() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.setInt(-BYTE_BYTES, LONG_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.setInt(LONG_BYTES + BYTE_BYTES, LONG_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void setLong() {
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);
    Assertions.assertThrows(
        IndexOutOfBoundsException.class, () -> largeBuffer.setLong(-BYTE_BYTES, LONG_BYTES));
    Assertions.assertThrows(
        IndexOutOfBoundsException.class,
        () -> largeBuffer.setLong(LONG_BYTES + BYTE_BYTES, LONG_BYTES));
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void duplicate() {
    final Memory smallBuffer = allocator.allocate(SHORT_BYTES);
    final Memory mediumBuffer = allocator.allocate(INTEGER_BYTES);
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);

    Assertions.assertEquals(smallBuffer.capacity(), smallBuffer.duplicate().capacity());
    Assertions.assertEquals(mediumBuffer.capacity(), mediumBuffer.duplicate().capacity());
    Assertions.assertEquals(largeBuffer.capacity(), largeBuffer.duplicate().capacity());
    Assertions.assertEquals(smallBuffer.writerIndex(), smallBuffer.duplicate().writerIndex());
    Assertions.assertEquals(mediumBuffer.writerIndex(), mediumBuffer.duplicate().writerIndex());
    Assertions.assertEquals(largeBuffer.writerIndex(), largeBuffer.duplicate().writerIndex());
    Assertions.assertEquals(smallBuffer.readerIndex(), smallBuffer.duplicate().readerIndex());
    Assertions.assertEquals(mediumBuffer.readerIndex(), mediumBuffer.duplicate().readerIndex());
    Assertions.assertEquals(largeBuffer.readerIndex(), largeBuffer.duplicate().readerIndex());

    Assertions.assertTrue(smallBuffer.release());
    Assertions.assertTrue(mediumBuffer.release());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void unSlice() {
    final Memory smallBuffer = allocator.allocate(SHORT_BYTES);
    final Memory mediumBuffer = allocator.allocate(INTEGER_BYTES);
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);

    Memory smallSlice = smallBuffer.slice(BYTE_BYTES, BYTE_BYTES);
    Memory mediumSlice = mediumBuffer.slice(BYTE_BYTES, SHORT_BYTES);
    Memory largeSlice = largeBuffer.slice(BYTE_BYTES, INTEGER_BYTES);
    Assertions.assertEquals(smallBuffer, ((Memory.Sliced) smallSlice).unSlice());
    Assertions.assertEquals(mediumBuffer, ((Memory.Sliced) mediumSlice).unSlice());
    Assertions.assertEquals(largeBuffer, ((Memory.Sliced) largeSlice).unSlice());

    Assertions.assertTrue(smallBuffer.release());
    Assertions.assertTrue(mediumBuffer.release());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  public void slicedRelease() {
    final Memory smallBuffer = allocator.allocate(SHORT_BYTES);
    final Memory slicedSmallBuffer = smallBuffer.slice();
    Assertions.assertTrue(slicedSmallBuffer.release());
    Assertions.assertFalse(smallBuffer.release());

    final Memory mediumBuffer = allocator.allocate(INTEGER_BYTES);
    final Memory slicedMediumBuffer = mediumBuffer.slice();
    Assertions.assertTrue(mediumBuffer.release());
    Assertions.assertFalse(slicedMediumBuffer.release());
  }

  @Test
  void clear() {
    final Memory smallBuffer = allocator.allocate(SHORT_BYTES);
    final Memory mediumBuffer = allocator.allocate(INTEGER_BYTES);
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);

    smallBuffer.setIndex(BYTE_BYTES, BYTE_BYTES);
    mediumBuffer.setIndex(BYTE_BYTES, BYTE_BYTES);
    largeBuffer.setIndex(BYTE_BYTES, BYTE_BYTES);
    smallBuffer.clear();
    mediumBuffer.clear();
    largeBuffer.clear();
    Assertions.assertEquals(0, smallBuffer.readerIndex());
    Assertions.assertEquals(0, smallBuffer.writerIndex());
    Assertions.assertEquals(0, mediumBuffer.readerIndex());
    Assertions.assertEquals(0, mediumBuffer.writerIndex());
    Assertions.assertEquals(0, largeBuffer.readerIndex());
    Assertions.assertEquals(0, largeBuffer.writerIndex());

    Assertions.assertTrue(smallBuffer.release());
    Assertions.assertTrue(mediumBuffer.release());
    Assertions.assertTrue(largeBuffer.release());
  }

  @Test
  void release() {
    final Memory smallBuffer = allocator.allocate(SHORT_BYTES);
    final Memory mediumBuffer = allocator.allocate(INTEGER_BYTES);
    final Memory largeBuffer = allocator.allocate(LONG_BYTES);

    Assertions.assertTrue(smallBuffer.slice().release());
    Assertions.assertTrue(mediumBuffer.slice().release());
    Assertions.assertTrue(largeBuffer.slice().release());

    Assertions.assertFalse(smallBuffer.release());
    Assertions.assertFalse(mediumBuffer.release());
    Assertions.assertFalse(largeBuffer.release());
  }

  @Test
  public void noLeak() {
    assert true;
    for (int i = 0; i < 100; i++) {
      System.gc();
      if (i % 2 == 0) {
        Memory allocate = allocator.allocate(4);
        assert allocate.release();
      } else {
        Memory allocate = allocator.allocate(8);
        assert allocate.release();
      }
    }
  }

  @Test
  public void checkLeakEnabled() {
    ReferenceQueue RQ = new ReferenceQueue();
    int capacity = 4;
    long address = ((JNIMemory) allocator.allocate(capacity)).address;
    JNIMemory buffer = new JNIMemory(address, capacity, Memory.ByteOrder.BIG_ENDIAN, allocator);
    final JNIMemory.Reference bufRef = new JNIMemory.Reference(address, buffer, RQ);
    bufRef.fillStackTrace(true);
    Assertions.assertThrows(
        MemoryLeakException.class,
        new Executable() {
          @Override
          public void execute() throws Throwable {
            JNIMemoryAllocator.checkLeak(bufRef, true);
          }
        });
  }

  @Test
  public void doubleFree() {
    final Memory buf = allocator.allocate(4);
    Assertions.assertTrue(buf.release());
    Assertions.assertFalse(buf.release());
  }

  @Test
  public void illegalAccess() {
    final Memory buf = allocator.allocate(4);
    buf.setInt(0, 10);
    Assertions.assertEquals(10, buf.getInt(0));
    Assertions.assertTrue(buf.release());
    Assertions.assertThrows(
        MemoryAccessException.class,
        new Executable() {
          @Override
          public void execute() throws Throwable {
            Assertions.assertEquals(10, buf.getInt(0));
          }
        });
  }

  @Test
  public void checkLeakDisabled() {
    ReferenceQueue RQ = new ReferenceQueue();
    int capacity = 4;
    long address = ((JNIMemory) allocator.allocate(capacity)).address;
    JNIMemory buffer = new JNIMemory(address, capacity, Memory.ByteOrder.BIG_ENDIAN, allocator);
    final JNIMemory.Reference bufRef = new JNIMemory.Reference(address, buffer, RQ);
    bufRef.fillStackTrace(false);
    JNIMemoryAllocator.checkLeak(bufRef, false);
  }

  @Test
  public void setAndGetLongByteOrder() {
    Memory longBE = allocator.allocate(8, Memory.ByteOrder.BIG_ENDIAN);
    longBE.setLong(0, 20);
    Assertions.assertEquals(20, longBE.getLong(0));
    longBE.release();

    Memory longLE = allocator.allocate(8, Memory.ByteOrder.LITTLE_ENDIAN);
    longLE.setLong(0, 20);
    Assertions.assertEquals(20, longLE.getLong(0));
    longLE.release();
  }

  @Test
  public void setAndGetIntByteOrder() {
    Memory intBE = allocator.allocate(8, Memory.ByteOrder.BIG_ENDIAN);
    intBE.setInt(0, 20);
    Assertions.assertEquals(20, intBE.getInt(0));
    intBE.release();

    Memory intLE = allocator.allocate(8, Memory.ByteOrder.LITTLE_ENDIAN);
    intLE.setInt(0, 20);
    Assertions.assertEquals(20, intLE.getInt(0));
    intLE.release();
  }

  @Test
  public void setAndGetShortByteOrder() {
    Memory shortBE = allocator.allocate(8, Memory.ByteOrder.BIG_ENDIAN);
    shortBE.setShort(0, 20);
    Assertions.assertEquals(20, shortBE.getShort(0));
    shortBE.release();

    Memory shortLE = allocator.allocate(8, Memory.ByteOrder.LITTLE_ENDIAN);
    shortLE.setShort(0, 20);
    Assertions.assertEquals(20, shortLE.getShort(0));
    shortLE.release();
  }

  @Test
  public void equalsAndHasCode() {
    Memory memory = allocator.allocate(8);
    Memory copied = memory.writerIndex(memory.capacity()).copy().writerIndex(memory.capacity());
    Assertions.assertTrue(memory.equals(copied));
    Assertions.assertTrue(memory.hashCode() == copied.hashCode());

    memory.setInt(0, memory.getInt(0) + 1);
    Assertions.assertFalse(memory.equals(""));
    Assertions.assertFalse(memory.equals(null));
    Assertions.assertFalse(memory.equals(copied));
    Assertions.assertFalse(memory.writerIndex(0).equals(copied));
    Assertions.assertTrue(memory.writerIndex(0).equals(copied.writerIndex(0)));

    Assertions.assertTrue(memory.writerIndex(0).hashCode() == copied.writerIndex(0).hashCode());

    memory.release();
    copied.release();
  }

  @Test
  public void getByteOrder() {
    Memory BE = allocator.allocate(1, Memory.ByteOrder.BIG_ENDIAN);
    Assertions.assertTrue(BE.byteOrder() instanceof Memory.ByteOrder.BigEndian);
    Assertions.assertTrue(
        BE.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN).byteOrder()
            instanceof Memory.ByteOrder.LittleEndian);
    BE.release();
    Memory LE = allocator.allocate(1, Memory.ByteOrder.LITTLE_ENDIAN);
    Assertions.assertTrue(LE.byteOrder() instanceof Memory.ByteOrder.LittleEndian);
    Assertions.assertTrue(
        LE.byteOrder(Memory.ByteOrder.BIG_ENDIAN).byteOrder()
            instanceof Memory.ByteOrder.BigEndian);
    LE.release();
  }

  @Test
  public void nativeByteOrder() {
    Assertions.assertTrue(JNIMemory.nativeByteOrderIsBE(1) == true);
    Assertions.assertTrue(JNIMemory.nativeByteOrderIsBE(0) == false);
  }

  // alignment test
  @Test
  public void forceUnalignForShort() {
    Memory memory = allocator.allocate(LONG_BYTES);
    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setShort(0, 10);
    Assertions.assertEquals(10, memory.getShort(0));
    memory.setShort(0, 0xFFF0);
    Assertions.assertEquals(0xFFF0, memory.getUnsignedShort(0));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setShort(0, 10);
    Assertions.assertEquals(10, memory.getShort(0));
    memory.setShort(0, 0xFFF0);
    Assertions.assertEquals(0xFFF0, memory.getUnsignedShort(0));

    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setShort(1, 11);
    Assertions.assertEquals(11, memory.getShort(1));
    memory.setShort(1, 0xFFF1);
    Assertions.assertEquals(0xFFF1, memory.getUnsignedShort(1));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setShort(1, 11);
    Assertions.assertEquals(11, memory.getShort(1));
    memory.setShort(1, 0xFFF1);
    Assertions.assertEquals(0xFFF1, memory.getUnsignedShort(1));
    memory.release();
  }

  @Test
  public void forceUnalignForShortRE() {
    Memory memory = allocator.allocate(LONG_BYTES);
    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setShortRE(0, 10);
    Assertions.assertEquals(10, memory.getShortRE(0));
    memory.setShortRE(0, 0xFFF0);
    Assertions.assertEquals(0xFFF0, memory.getUnsignedShortRE(0));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setShortRE(0, 10);
    Assertions.assertEquals(10, memory.getShortRE(0));
    memory.setShortRE(0, 0xFFF0);
    Assertions.assertEquals(0xFFF0, memory.getUnsignedShortRE(0));

    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setShortRE(1, 11);
    Assertions.assertEquals(11, memory.getShortRE(1));
    memory.setShortRE(1, 0xFFF1);
    Assertions.assertEquals(0xFFF1, memory.getUnsignedShortRE(1));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setShortRE(1, 11);
    Assertions.assertEquals(11, memory.getShortRE(1));
    memory.setShortRE(1, 0xFFF1);
    Assertions.assertEquals(0xFFF1, memory.getUnsignedShortRE(1));
    memory.release();
  }

  @Test
  public void forceUnalignForInt() {
    Memory memory = allocator.allocate(LONG_BYTES);

    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setInt(0, 10);
    Assertions.assertEquals(10, memory.getInt(0));
    memory.setInt(0, 0xFFFFFFF0);
    Assertions.assertEquals(0xFFFFFFF0L, memory.getUnsignedInt(0));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setInt(0, 10);
    Assertions.assertEquals(10, memory.getInt(0));
    memory.setInt(0, 0xFFFFFFF0);
    Assertions.assertEquals(0xFFFFFFF0L, memory.getUnsignedInt(0));

    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setInt(1, 11);
    Assertions.assertEquals(11, memory.getInt(1));
    memory.setInt(1, 0xFFFFFFF1);
    Assertions.assertEquals(0xFFFFFFF1L, memory.getUnsignedInt(1));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setInt(1, 11);
    Assertions.assertEquals(11, memory.getInt(1));
    memory.setInt(1, 0xFFFFFFF1);
    Assertions.assertEquals(0xFFFFFFF1L, memory.getUnsignedInt(1));

    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setInt(2, 12);
    Assertions.assertEquals(12, memory.getInt(2));
    memory.setInt(2, 0xFFFFFFF2);
    Assertions.assertEquals(0xFFFFFFF2L, memory.getUnsignedInt(2));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setInt(2, 12);
    Assertions.assertEquals(12, memory.getInt(2));
    memory.setInt(2, 0xFFFFFFF2);
    Assertions.assertEquals(0xFFFFFFF2L, memory.getUnsignedInt(2));

    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setInt(3, 13);
    Assertions.assertEquals(13, memory.getInt(3));
    memory.setInt(3, 0xFFFFFFF3);
    Assertions.assertEquals(0xFFFFFFF3L, memory.getUnsignedInt(3));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setInt(3, 13);
    Assertions.assertEquals(13, memory.getInt(3));
    memory.setInt(3, 0xFFFFFFF3);
    Assertions.assertEquals(0xFFFFFFF3L, memory.getUnsignedInt(3));
    memory.release();
  }

  @Test
  public void forceUnalignForIntRE() {
    Memory memory = allocator.allocate(LONG_BYTES);
    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setIntRE(0, 10);
    Assertions.assertEquals(10, memory.getIntRE(0));
    memory.setIntRE(0, 0xFFFFFFF0);
    Assertions.assertEquals(0xFFFFFFF0L, memory.getUnsignedIntRE(0));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setIntRE(0, 10);
    Assertions.assertEquals(10, memory.getIntRE(0));
    memory.setIntRE(0, 0xFFFFFFF0);
    Assertions.assertEquals(0xFFFFFFF0L, memory.getUnsignedIntRE(0));

    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setIntRE(1, 11);
    Assertions.assertEquals(11, memory.getIntRE(1));
    memory.setIntRE(1, 0xFFFFFFF1);
    Assertions.assertEquals(0xFFFFFFF1L, memory.getUnsignedIntRE(1));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setIntRE(1, 11);
    Assertions.assertEquals(11, memory.getIntRE(1));
    memory.setIntRE(1, 0xFFFFFFF1);
    Assertions.assertEquals(0xFFFFFFF1L, memory.getUnsignedIntRE(1));

    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setIntRE(2, 12);
    Assertions.assertEquals(12, memory.getIntRE(2));
    memory.setIntRE(2, 0xFFFFFFF2);
    Assertions.assertEquals(0xFFFFFFF2L, memory.getUnsignedIntRE(2));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setIntRE(2, 12);
    Assertions.assertEquals(12, memory.getIntRE(2));
    memory.setIntRE(2, 0xFFFFFFF2);
    Assertions.assertEquals(0xFFFFFFF2L, memory.getUnsignedIntRE(2));

    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setIntRE(3, 13);
    Assertions.assertEquals(13, memory.getIntRE(3));
    memory.setIntRE(3, 0xFFFFFFF3);
    Assertions.assertEquals(0xFFFFFFF3L, memory.getUnsignedIntRE(3));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setIntRE(3, 13);
    Assertions.assertEquals(13, memory.getIntRE(3));
    memory.setIntRE(3, 0xFFFFFFF3);
    Assertions.assertEquals(0xFFFFFFF3L, memory.getUnsignedIntRE(3));
    memory.release();
  }

  @Test
  public void forceUnalignForLong() {
    Memory memory = allocator.allocate(LONG_BYTES * 2);
    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setLong(0, 10);
    Assertions.assertEquals(10, memory.getLong(0));
    memory.setLong(0, Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, memory.getLong(0));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setLong(0, 10);
    Assertions.assertEquals(10, memory.getLong(0));
    memory.setLong(0, Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, memory.getLong(0));

    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setLong(1, 11);
    Assertions.assertEquals(11, memory.getLong(1));
    memory.setLong(1, Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, memory.getLong(1));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setLong(1, 11);
    Assertions.assertEquals(11, memory.getLong(1));
    memory.setLong(1, Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, memory.getLong(1));

    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setLong(2, 12);
    Assertions.assertEquals(12, memory.getLong(2));
    memory.setLong(2, Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, memory.getLong(2));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setLong(2, 12);
    Assertions.assertEquals(12, memory.getLong(2));
    memory.setLong(2, Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, memory.getLong(2));

    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setLong(3, 13);
    Assertions.assertEquals(13, memory.getLong(3));
    memory.setLong(3, Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, memory.getLong(3));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setLong(3, 13);
    Assertions.assertEquals(13, memory.getLong(3));
    memory.setLong(3, Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, memory.getLong(3));

    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setLong(4, 14);
    Assertions.assertEquals(14, memory.getLong(4));
    memory.setLong(4, Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, memory.getLong(4));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setLong(4, 14);
    Assertions.assertEquals(14, memory.getLong(4));
    memory.setLong(4, Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, memory.getLong(4));

    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setLong(5, 15);
    Assertions.assertEquals(15, memory.getLong(5));
    memory.setLong(5, Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, memory.getLong(5));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setLong(5, 15);
    Assertions.assertEquals(15, memory.getLong(5));
    memory.setLong(5, Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, memory.getLong(5));

    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setLong(6, 16);
    Assertions.assertEquals(16, memory.getLong(6));
    memory.setLong(6, Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, memory.getLong(6));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setLong(6, 16);
    Assertions.assertEquals(16, memory.getLong(6));
    memory.setLong(6, Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, memory.getLong(6));

    memory.byteOrder(Memory.ByteOrder.BIG_ENDIAN);
    memory.setLong(7, 17);
    Assertions.assertEquals(17, memory.getLong(7));
    memory.setLong(7, Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, memory.getLong(7));

    memory.byteOrder(Memory.ByteOrder.LITTLE_ENDIAN);
    memory.setLong(7, 17);
    Assertions.assertEquals(17, memory.getLong(7));
    memory.setLong(7, Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, memory.getLong(7));

    memory.release();
  }
}
