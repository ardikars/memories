/*
 * SPDX-FileCopyrightText: 2020-2021 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package memories.spi;

/**
 * Used to wrap low-level memory address.
 *
 * @since 1.0.0
 */
public interface Memory {

  /**
   * Get buffer capacity.
   *
   * @return returns the number of bytes (octets) this buffer can contain.
   * @since 1.0.0
   */
  long capacity();

  /**
   * Reallocate buffer.
   *
   * @param newCapacity new capacity.
   * @return returns new {@link Memory}.
   * @since 1.0.0
   */
  Memory capacity(long newCapacity);

  /**
   * Get reader buffer index.
   *
   * @return returns the {@code readerIndex} of this buffer.
   * @since 1.0.0
   */
  long readerIndex();

  /**
   * Sets the {@code readerIndex} of this buffer.
   *
   * @throws IndexOutOfBoundsException if the specified {@code readerIndex} is less than {@code 0}
   *     or greater than {@code this.writerIndex}
   * @param readerIndex reader index.
   * @return returns this {@link Memory}.
   * @since 1.0.0
   */
  Memory readerIndex(long readerIndex);

  /**
   * Get writer buffer index.
   *
   * @return returns the {@code writerIndex} of this buffer.
   * @since 1.0.0
   */
  long writerIndex();

  /**
   * Sets the {@code writerIndex} of this buffer.
   *
   * @throws IndexOutOfBoundsException if the specified {@code writerIndex} is less than {@code
   *     this.readerIndex} or greater than {@code this.capacity}
   * @param writerIndex writer index.
   * @return returns this {@link Memory}.
   * @since 1.0.0
   */
  Memory writerIndex(long writerIndex);

  /**
   * Combination of {@code writerIndex(...)} and {@code readerIndex(...)}.
   *
   * @param readerIndex reader index.
   * @param writerIndex writer index.
   * @return returns this {@link Memory}.
   * @since 1.0.0
   */
  Memory setIndex(long readerIndex, long writerIndex);

  /**
   * Get readable bytes from a buffer.
   *
   * @return returns the number of readable bytes which is equal to {@code (this.writerIndex -
   *     this.readerIndex)}.
   * @since 1.0.0
   */
  long readableBytes();

  /**
   * Get readable bytes from a buffer.
   *
   * @return returns the number of writable bytes which is equal to {@code (this.capacity -
   *     this.writerIndex)}.
   * @since 1.0.0
   */
  long writableBytes();

  /**
   * @return returns {@code true} if and only if {@code (this.writerIndex - this.readerIndex)} is
   *     greater than {@code 0}.
   * @since 1.0.0
   */
  boolean isReadable();

  /**
   * Checks if this {@link Memory} is readable for given {@code numBytes}.
   *
   * @param numBytes number of bytes.
   * @return returns {@code true} if and only if this buffer contains equal to or more than the
   *     specified number of elements.
   * @since 1.0.0
   */
  boolean isReadable(long numBytes);

  /**
   * @return returns {@code true} if and only if {@code (this.capacity - this.writerIndex)} is
   *     greater than {@code 0}.
   * @since 1.0.0
   */
  boolean isWritable();

  /**
   * Chaeks if this {@link Memory} is writable for given {@code numByes}.
   *
   * @param numBytes numBytes.
   * @return returns {@code true} if and only if this buffer has enough room to allow writing the
   *     specified number of elements.
   * @since 1.0.0
   */
  boolean isWritable(long numBytes);

  /**
   * Sets the {@code readerIndex} and {@code writerIndex} of this buffer to {@code 0}. This method
   * is identical to {@link #setIndex(long, long) setIndex(0, 0)}.
   *
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory clear();

  /**
   * Marks the current {@code readerIndex} in this buffer. You can reposition the current {@code
   * readerIndex} to the marked {@code readerIndex} by calling {@link #resetReaderIndex()}. The
   * initial value of the marked {@code readerIndex} is {@code 0}.
   *
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory markReaderIndex();

  /**
   * Repositions the current {@code readerIndex} to the marked {@code readerIndex} in this buffer.
   *
   * @throws IndexOutOfBoundsException if the current {@code writerIndex} is less than the marked
   *     {@code readerIndex}
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory resetReaderIndex();

  /**
   * Marks the current {@code writerIndex} in this buffer. You can reposition the current {@code
   * writerIndex} to the marked {@code writerIndex} by calling {@link #resetWriterIndex()}. The
   * initial value of the marked {@code writerIndex} is {@code 0}.
   *
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory markWriterIndex();

  /**
   * Repositions the current {@code writerIndex} to the marked {@code writerIndex} in this buffer.
   *
   * @throws IndexOutOfBoundsException if the current {@code readerIndex} is greater than the marked
   *     {@code writerIndex}
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory resetWriterIndex();

  /**
   * Makes sure the number of {@linkplain #writableBytes() the writable bytes} is equal to or
   * greater than the specified value. If there is enough writable bytes in this buffer, this method
   * returns with no side effect. Otherwise, it raises an {@link IllegalArgumentException}.
   *
   * @param minWritableBytes the expected minimum number of writable bytes
   * @throws IndexOutOfBoundsException if {@link #writerIndex()} + {@code minWritableBytes} &gt;
   *     {@link #capacity()}
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory ensureWritable(long minWritableBytes);

  /**
   * Gets a byte at the specified absolute {@code index} in this buffer. This method does not modify
   * {@code readerIndex} or {@code writerIndex} of this buffer.
   *
   * @param index index.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 1} is greater than {@code this.capacity}
   * @return byte value.
   * @since 1.0.0
   */
  byte getByte(long index);

  /**
   * Gets an unsigned byte at the specified absolute {@code index} in this buffer. This method does
   * not modify {@code readerIndex} or {@code writerIndex} of this buffer.
   *
   * @param index index.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 1} is greater than {@code this.capacity}
   * @return unsigned byte value stored in {@code short}.
   * @since 1.0.0
   */
  short getUnsignedByte(long index);

  /**
   * Gets a 16-bit short integer at the specified absolute {@code index} in this buffer. This method
   * does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
   *
   * @param index index.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 2} is greater than {@code this.capacity}
   * @return short value.
   * @since 1.0.0
   */
  short getShort(long index);

  /**
   * Gets a 16-bit short integer at the specified absolute {@code index} in this buffer in Reserved
   * Native Endian Byte Order. This method does not modify {@code readerIndex} or {@code
   * writerIndex} of this buffer.
   *
   * @param index index.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 2} is greater than {@code this.capacity}
   * @return reserved native endian short value.
   * @since 1.0.0
   */
  short getShortRE(long index);

  /**
   * Gets an unsigned 16-bit short integer at the specified absolute {@code index} in this buffer.
   * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
   *
   * @param index index.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or *
   *     {@code index + 2} is greater than {@code this.capacity} * @since 1.0.0
   * @return unsigned short value stored in {@code integer}.
   */
  int getUnsignedShort(long index);

  /**
   * Gets an unsigned 16-bit short integer at the specified absolute {@code index} in this buffer in
   * Reserved Native Endian Byte Order. This method does not modify {@code readerIndex} or {@code
   * writerIndex} of this buffer.
   *
   * @param index index.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 2} is greater than {@code this.capacity}
   * @return unsigned reserved native endian short value stored in {@code integer}.
   * @since 1.0.0
   */
  int getUnsignedShortRE(long index);

  /**
   * Gets a 32-bit integer at the specified absolute {@code index} in this buffer. This method does
   * not modify {@code readerIndex} or {@code writerIndex} of this buffer.
   *
   * @param index index.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 4} is greater than {@code this.capacity}
   * @return integer value.
   * @since 1.0.0
   */
  int getInt(long index);

  /**
   * Gets a 32-bit integer at the specified absolute {@code index} in this buffer with Reserved
   * Native Endian Byte Order. This method does not modify {@code readerIndex} or {@code
   * writerIndex} of this buffer.
   *
   * @param index index.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 4} is greater than {@code this.capacity}
   * @return reserved native endian integer value.
   * @since 1.0.0
   */
  int getIntRE(long index);

  /**
   * Gets an unsigned 32-bit integer at the specified absolute {@code index} in this buffer. This
   * method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
   *
   * @param index index.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 4} is greater than {@code this.capacity}
   * @return unsigned integer value stored in {@code long}.
   * @since 1.0.0
   */
  long getUnsignedInt(long index);

  /**
   * Gets an unsigned 32-bit integer at the specified absolute {@code index} in this buffer in
   * Reserved Native Endian Byte Order. This method does not modify {@code readerIndex} or {@code
   * writerIndex} of this buffer.
   *
   * @param index index.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 4} is greater than {@code this.capacity}
   * @return unsigned reserved native endian integer value stored in {@code long}.
   * @since 1.0.0
   */
  long getUnsignedIntRE(long index);

  /**
   * Gets a 64-bit long integer at the specified absolute {@code index} in this buffer. This method
   * does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
   *
   * @param index index.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 8} is greater than {@code this.capacity}
   * @return long value.
   * @since 1.0.0
   */
  long getLong(long index);

  /**
   * Gets a 64-bit long integer at the specified absolute {@code index} in this buffer in Reserved
   * Native Endian Byte Order. This method does not modify {@code readerIndex} or {@code
   * writerIndex} of this buffer.
   *
   * @param index index.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 8} is greater than {@code this.capacity}
   * @return reserved native endian long value.
   * @since 1.0.0
   */
  long getLongRE(long index);

  /**
   * Gets a 32-bit floating point number at the specified absolute {@code index} in this buffer.
   * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
   *
   * @param index index.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 4} is greater than {@code this.capacity}
   * @return float value.
   * @since 1.0.0
   */
  float getFloat(long index);

  /**
   * Gets a 32-bit floating point number at the specified absolute {@code index} in this buffer in
   * Reserved Native Endian Byte Order. This method does not modify {@code readerIndex} or {@code
   * writerIndex} of this buffer.
   *
   * @param index index.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 4} is greater than {@code this.capacity}
   * @return reserved native endian float value.
   * @since 1.0.0
   */
  float getFloatRE(long index);

  /**
   * Gets a 64-bit floating point number at the specified absolute {@code index} in this buffer.
   * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
   *
   * @param index index.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 8} is greater than {@code this.capacity}
   * @return double value.
   * @since 1.0.0
   */
  double getDouble(long index);

  /**
   * Gets a 64-bit floating point number at the specified absolute {@code index} in this buffer in
   * Reserved Native Endian Byte Order. This method does not modify {@code readerIndex} or {@code
   * writerIndex} of this buffer.
   *
   * @param index index.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 8} is greater than {@code this.capacity}
   * @return reserved native endian double value.
   * @since 1.0.0
   */
  double getDoubleRE(long index);

  /**
   * Transfers this buffer's data to the specified destination starting at the specified absolute
   * {@code index} until the destination becomes non-writable. This method is basically same with
   * {@link #getBytes(long, Memory, long, long)}, except that this method increases the {@code
   * writerIndex} of the destination by the number of the transferred bytes while {@link
   * #getBytes(long, Memory, long, long)} does not. This method does not modify {@code readerIndex}
   * or {@code writerIndex} of the source buffer (i.e. {@code this}).
   *
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or if
   *     {@code index + dst.writableBytes} is greater than {@code this.capacity}
   * @param index index.
   * @param dst destination.
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory getBytes(long index, Memory dst);

  /**
   * Transfers this buffer's data to the specified destination starting at the specified absolute
   * {@code index}. This method is basically same with {@link #getBytes(long, Memory, long, long)},
   * except that this method increases the {@code writerIndex} of the destination by the number of
   * the transferred bytes while {@link #getBytes(long, Memory, long, long)} does not. This method
   * does not modify {@code readerIndex} or {@code writerIndex} of the source buffer (i.e. {@code
   * this}).
   *
   * @param index index.
   * @param dst destination.
   * @param length the number of bytes to transfer
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0}, if
   *     {@code index + length} is greater than {@code this.capacity}, or if {@code length} is
   *     greater than {@code dst.writableBytes}
   * @since 1.0.0
   */
  Memory getBytes(long index, Memory dst, long length);

  /**
   * Transfers this buffer's data to the specified destination starting at the specified absolute
   * {@code index}. This method does not modify {@code readerIndex} or {@code writerIndex} of both
   * the source (i.e. {@code this}) and the destination.
   *
   * @param index index.
   * @param dst destination.
   * @param dstIndex the first index of the destination
   * @param length the number of bytes to transfer
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0}, if the
   *     specified {@code dstIndex} is less than {@code 0}, if {@code index + length} is greater
   *     than {@code this.capacity}, or if {@code dstIndex + length} is greater than {@code
   *     dst.capacity}
   * @since 1.0.0
   */
  Memory getBytes(long index, Memory dst, long dstIndex, long length);

  /**
   * Transfers this buffer's data to the specified destination starting at the specified absolute
   * {@code index}. This method does not modify {@code readerIndex} or {@code writerIndex} of this
   * buffer
   *
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or if
   *     {@code index + dst.length} is greater than {@code this.capacity}
   * @param index index.
   * @param dst destination.
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory getBytes(long index, byte[] dst);

  /**
   * Transfers this buffer's data to the specified destination starting at the specified absolute
   * {@code index}. This method does not modify {@code readerIndex} or {@code writerIndex} of this
   * buffer.
   *
   * @param dst dst.
   * @param index index.
   * @param dstIndex the first index of the destination
   * @param length the number of bytes to transfer
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0}, if the
   *     specified {@code dstIndex} is less than {@code 0}, if {@code index + length} is greater
   *     than {@code this.capacity}, or if {@code dstIndex + length} is greater than {@code
   *     dst.length}
   * @since 1.0.0
   */
  Memory getBytes(long index, byte[] dst, long dstIndex, long length);

  /**
   * Sets the specified byte at the specified absolute {@code index} in this buffer. The 24
   * high-order bits of the specified value are ignored. This method does not modify {@code
   * readerIndex} or {@code writerIndex} of this buffer.
   *
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 1} is greater than {@code this.capacity}
   * @param index index.
   * @param value value.
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory setByte(long index, int value);

  /**
   * Sets the specified 16-bit short integer at the specified absolute {@code index} in this buffer.
   * The 16 high-order bits of the specified value are ignored. This method does not modify {@code
   * readerIndex} or {@code writerIndex} of this buffer.
   *
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 2} is greater than {@code this.capacity}
   * @param index index.
   * @param value value.
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory setShort(long index, int value);

  /**
   * Sets the specified 16-bit short integer at the specified absolute {@code index} in this buffer
   * with the Reserved Native Endian Byte Order. The 16 high-order bits of the specified value are
   * ignored. This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
   *
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 2} is greater than {@code this.capacity}
   * @param index index.
   * @param value value.
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory setShortRE(long index, int value);

  /**
   * Sets the specified 32-bit integer at the specified absolute {@code index} in this buffer. This
   * method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
   *
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 4} is greater than {@code this.capacity}
   * @param index index.
   * @param value value.
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory setInt(long index, int value);

  /**
   * Sets the specified 32-bit integer at the specified absolute {@code index} in this buffer with
   * Reserved Native Endian byte order . This method does not modify {@code readerIndex} or {@code
   * writerIndex} of this buffer.
   *
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 4} is greater than {@code this.capacity}
   * @param index index.
   * @param value value.
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory setIntRE(long index, int value);

  /**
   * Sets the specified 64-bit long integer at the specified absolute {@code index} in this buffer.
   * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
   *
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 8} is greater than {@code this.capacity}
   * @param index index.
   * @param value value.
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory setLong(long index, long value);

  /**
   * Sets the specified 64-bit long integer at the specified absolute {@code index} in this buffer
   * in Reserved Native Endian Byte Order. This method does not modify {@code readerIndex} or {@code
   * writerIndex} of this buffer.
   *
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 8} is greater than {@code this.capacity}
   * @param index index.
   * @param value value.
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory setLongRE(long index, long value);

  /**
   * Sets the specified 32-bit floating-point number at the specified absolute {@code index} in this
   * buffer. This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
   *
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 4} is greater than {@code this.capacity}
   * @param index index.
   * @param value value.
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory setFloat(long index, float value);

  /**
   * Sets the specified 32-bit floating-point number at the specified absolute {@code index} in this
   * buffer in Reserved Native Endian Byte Order. This method does not modify {@code readerIndex} or
   * {@code writerIndex} of this buffer.
   *
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 4} is greater than {@code this.capacity}
   * @param index index.
   * @param value value.
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory setFloatRE(long index, float value);

  /**
   * Sets the specified 64-bit floating-point number at the specified absolute {@code index} in this
   * buffer. This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
   *
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 8} is greater than {@code this.capacity}
   * @param index index.
   * @param value value.
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory setDouble(long index, double value);

  /**
   * Sets the specified 64-bit floating-point number at the specified absolute {@code index} in this
   * buffer in Reserved Native Endian Byte Order. This method does not modify {@code readerIndex} or
   * {@code writerIndex} of this buffer.
   *
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
   *     {@code index + 8} is greater than {@code this.capacity}
   * @param index index.
   * @param value value.
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory setDoubleRE(long index, double value);

  /**
   * Transfers the specified source buffer's data to this buffer starting at the specified absolute
   * {@code index} until the source buffer becomes unreadable. This method is basically same with
   * {@link #setBytes(long, Memory, long, long)}, except that this method increases the {@code
   * readerIndex} of the source buffer by the number of the transferred bytes while {@link
   * #setBytes(long, Memory, long, long)} does not. This method does not modify {@code readerIndex}
   * or {@code writerIndex} of the source buffer (i.e. {@code this}).
   *
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or if
   *     {@code index + src.readableBytes} is greater than {@code this.capacity}
   * @param index index.
   * @param src source.
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory setBytes(long index, Memory src);

  /**
   * Transfers the specified source buffer's data to this buffer starting at the specified absolute
   * {@code index}. This method is basically same with {@link #setBytes(long, Memory, long, long)},
   * except that this method increases the {@code readerIndex} of the source buffer by the number of
   * the transferred bytes while {@link #setBytes(long, Memory, long, long)} does not. This method
   * does not modify {@code readerIndex} or {@code writerIndex} of the source buffer (i.e. {@code
   * this}).
   *
   * @param index index.
   * @param src source.
   * @param length the number of bytes to transfer
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0}, if
   *     {@code index + length} is greater than {@code this.capacity}, or if {@code length} is
   *     greater than {@code src.readableBytes}
   * @since 1.0.0
   */
  Memory setBytes(long index, Memory src, long length);

  /**
   * Transfers the specified source buffer's data to this buffer starting at the specified absolute
   * {@code index}. This method does not modify {@code readerIndex} or {@code writerIndex} of both
   * the source (i.e. {@code this}) and the destination.
   *
   * @param index index.
   * @param src source.
   * @param srcIndex the first index of the source
   * @param length the number of bytes to transfer
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0}, if the
   *     specified {@code srcIndex} is less than {@code 0}, if {@code index + length} is greater
   *     than {@code this.capacity}, or if {@code srcIndex + length} is greater than {@code
   *     src.capacity}
   * @since 1.0.0
   */
  Memory setBytes(long index, Memory src, long srcIndex, long length);

  /**
   * Transfers the specified source array's data to this buffer starting at the specified absolute
   * {@code index}. This method does not modify {@code readerIndex} or {@code writerIndex} of this
   * buffer.
   *
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or if
   *     {@code index + src.length} is greater than {@code this.capacity}
   * @param index index.
   * @param src source.
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory setBytes(long index, byte[] src);

  /**
   * Transfers the specified source array's data to this buffer starting at the specified absolute
   * {@code index}. This method does not modify {@code readerIndex} or {@code writerIndex} of this
   * buffer.
   *
   * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0}, if the
   *     specified {@code srcIndex} is less than {@code 0}, if {@code index + length} is greater
   *     than {@code this.capacity}, or if {@code srcIndex + length} is greater than {@code
   *     src.length}
   * @param index index.
   * @param src source.
   * @param srcIndex source index.
   * @param length length.
   * @return this {@link Memory}.
   * @since 1.0.0
   */
  Memory setBytes(long index, byte[] src, long srcIndex, long length);

  /**
   * Gets a byte at the current {@code readerIndex} and increases the {@code readerIndex} by {@code
   * 1} in this buffer.
   *
   * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 1}
   * @return byte value.
   * @since 1.0.0
   */
  byte readByte();

  /**
   * Gets an unsigned byte at the current {@code readerIndex} and increases the {@code readerIndex}
   * by {@code 1} in this buffer.
   *
   * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 1}
   * @return unsigned byte stored in {@code short}.
   * @since 1.0.0
   */
  short readUnsignedByte();

  /**
   * Gets a 16-bit short integer at the current {@code readerIndex} and increases the {@code
   * readerIndex} by {@code 2} in this buffer.
   *
   * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 2}
   * @return short value.
   * @since 1.0.0
   */
  short readShort();

  /**
   * Gets a 16-bit short integer at the current {@code readerIndex} in the Reserved Native Endian
   * Byte Order and increases the {@code readerIndex} by {@code 2} in this buffer.
   *
   * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 2}
   * @return reserved native endian short value.
   * @since 1.0.0
   */
  short readShortRE();

  /**
   * Gets an unsigned 16-bit short integer at the current {@code readerIndex} and increases the
   * {@code readerIndex} by {@code 2} in this buffer.
   *
   * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 2}
   * @return unsigned short value stored in {@code integer}.
   * @since 1.0.0
   */
  int readUnsignedShort();

  /**
   * Gets an unsigned 16-bit short integer at the current {@code readerIndex} in the Reserved Native
   * Endian Byte Order and increases the {@code readerIndex} by {@code 2} in this buffer.
   *
   * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 2}
   * @return unsigned reserved native endian short value stored in {@code integer}.
   * @since 1.0.0
   */
  int readUnsignedShortRE();

  /**
   * Gets a 32-bit integer at the current {@code readerIndex} and increases the {@code readerIndex}
   * by {@code 4} in this buffer.
   *
   * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 4}
   * @return integer value.
   * @since 1.0.0
   */
  int readInt();

  /**
   * Gets a 32-bit integer at the current {@code readerIndex} in the Reserved Native Endian Byte
   * Order and increases the {@code readerIndex} by {@code 4} in this buffer.
   *
   * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 4}
   * @return reserved native endian integer value.
   * @since 1.0.0
   */
  int readIntRE();

  /**
   * Gets an unsigned 32-bit integer at the current {@code readerIndex} and increases the {@code
   * readerIndex} by {@code 4} in this buffer.
   *
   * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 4}
   * @return unsigned integer value.
   * @since 1.0.0
   */
  long readUnsignedInt();

  /**
   * Gets an unsigned 32-bit integer at the current {@code readerIndex} in the Reserved Native
   * Endian Byte Order and increases the {@code readerIndex} by {@code 4} in this buffer.
   *
   * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 4}
   * @return unsigned reserved native endian integer value.
   * @since 1.0.0
   */
  long readUnsignedIntRE();

  /**
   * Gets a 64-bit integer at the current {@code readerIndex} and increases the {@code readerIndex}
   * by {@code 8} in this buffer.
   *
   * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 8}
   * @return long value.
   * @since 1.0.0
   */
  long readLong();

  /**
   * Gets a 64-bit integer at the current {@code readerIndex} in the Reserved Native Endian Byte
   * Order and increases the {@code readerIndex} by {@code 8} in this buffer.
   *
   * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 8}
   * @return reserved native endian long value.
   * @since 1.0.0
   */
  long readLongRE();

  /**
   * Gets a 32-bit floating point number at the current {@code readerIndex} and increases the {@code
   * readerIndex} by {@code 4} in this buffer.
   *
   * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 4}
   * @return float value.
   * @since 1.0.0
   */
  float readFloat();

  /**
   * Gets a 32-bit floating point number at the current {@code readerIndex} in Reserved Native
   * Endian Byte Order and increases the {@code readerIndex} by {@code 4} in this buffer.
   *
   * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 4}
   * @return reserved native endian float value.
   * @since 1.0.0
   */
  float readFloatRE();

  /**
   * Gets a 64-bit floating point number at the current {@code readerIndex} and increases the {@code
   * readerIndex} by {@code 8} in this buffer.
   *
   * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 8}
   * @return double value.
   * @since 1.0.0
   */
  double readDouble();

  /**
   * Gets a 64-bit floating point number at the current {@code readerIndex} in Reserved Native
   * Endian Byte Order and increases the {@code readerIndex} by {@code 8} in this buffer.
   *
   * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 8}
   * @return reserved native endian double value.
   * @since 1.0.0
   */
  double readDoubleRE();

  /**
   * Transfers this buffer's data to the specified destination starting at the current {@code
   * readerIndex} until the destination becomes non-writable, and increases the {@code readerIndex}
   * by the number of the transferred bytes. This method is basically same with {@link
   * #readBytes(Memory, long, long)}, except that this method increases the {@code writerIndex} of
   * the destination by the number of the transferred bytes while {@link #readBytes(Memory, long,
   * long)} does not.
   *
   * @param dst dst.
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if {@code dst.writableBytes} is greater than {@code
   *     this.readableBytes}
   * @since 1.0.0
   */
  Memory readBytes(Memory dst);

  /**
   * Transfers this buffer's data to the specified destination starting at the current {@code
   * readerIndex} and increases the {@code readerIndex} by the number of the transferred bytes (=
   * {@code length}). This method is basically same with {@link #readBytes(Memory, long, long)},
   * except that this method increases the {@code writerIndex} of the destination by the number of
   * the transferred bytes (= {@code length}) while {@link #readBytes(Memory, long, long)} does not.
   *
   * @param dst dst.
   * @param length length.
   * @throws IndexOutOfBoundsException if {@code length} is greater than {@code this.readableBytes}
   *     or if {@code length} is greater than {@code dst.writableBytes}
   * @return returns this instance.
   * @since 1.0.0
   */
  Memory readBytes(Memory dst, long length);

  /**
   * Transfers this buffer's data to the specified destination starting at the current {@code
   * readerIndex} and increases the {@code readerIndex} by the number of the transferred bytes (=
   * {@code length}).
   *
   * @param dst destination.
   * @param dstIndex the first index of the destination
   * @param length the number of bytes to transfer
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if the specified {@code dstIndex} is less than {@code 0}, if
   *     {@code length} is greater than {@code this.readableBytes}, or if {@code dstIndex + length}
   *     is greater than {@code dst.capacity}
   * @since 1.0.0
   */
  Memory readBytes(Memory dst, long dstIndex, long length);

  /**
   * Transfers this buffer's data to the specified destination starting at the current {@code
   * readerIndex} and increases the {@code readerIndex} by the number of the transferred bytes (=
   * {@code dst.length}).
   *
   * @param dst destination.
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if {@code dst.length} is greater than {@code
   *     this.readableBytes}
   * @since 1.0.0
   */
  Memory readBytes(byte[] dst);

  /**
   * Transfers this buffer's data to the specified destination starting at the current {@code
   * readerIndex} and increases the {@code readerIndex} by the number of the transferred bytes (=
   * {@code length}).
   *
   * @param dst destination.
   * @param dstIndex the first index of the destination
   * @param length the number of bytes to transfer
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if the specified {@code dstIndex} is less than {@code 0}, if
   *     {@code length} is greater than {@code this.readableBytes}, or if {@code dstIndex + length}
   *     is greater than {@code dst.length}
   * @since 1.0.0
   */
  Memory readBytes(byte[] dst, long dstIndex, long length);

  /**
   * Increases the current {@code readerIndex} by the specified {@code length} in this buffer.
   *
   * @param length length.
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if {@code length} is greater than {@code this.readableBytes}
   * @since 1.0.0
   */
  Memory skipBytes(long length);

  /**
   * Sets the specified byte at the current {@code writerIndex} and increases the {@code
   * writerIndex} by {@code 1} in this buffer. The 24 high-order bits of the specified value are
   * ignored.
   *
   * @param value value.
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 1}
   * @since 1.0.0
   */
  Memory writeByte(int value);

  /**
   * Sets the specified 16-bit short integer at the current {@code writerIndex} and increases the
   * {@code writerIndex} by {@code 2} in this buffer. The 16 high-order bits of the specified value
   * are ignored.
   *
   * @param value value.
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 2}
   * @since 1.0.0
   */
  Memory writeShort(int value);

  /**
   * Sets the specified 16-bit short integer in the Reserved Native Endian Byte Order at the current
   * {@code writerIndex} and increases the {@code writerIndex} by {@code 2} in this buffer. The 16
   * high-order bits of the specified value are ignored.
   *
   * @param value value.
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 2}
   * @since 1.0.0
   */
  Memory writeShortRE(int value);

  /**
   * Sets the specified 32-bit integer at the current {@code writerIndex} and increases the {@code
   * writerIndex} by {@code 4} in this buffer.
   *
   * @param value value.
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 4}
   * @since 1.0.0
   */
  Memory writeInt(int value);

  /**
   * Sets the specified 32-bit integer at the current {@code writerIndex} in the Reserved Native
   * Endian Byte Order and increases the {@code writerIndex} by {@code 4} in this buffer.
   *
   * @param value value.
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 4}
   * @since 1.0.0
   */
  Memory writeIntRE(int value);

  /**
   * Sets the specified 64-bit long integer at the current {@code writerIndex} and increases the
   * {@code writerIndex} by {@code 8} in this buffer.
   *
   * @param value value.
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 8}
   * @since 1.0.0
   */
  Memory writeLong(long value);

  /**
   * Sets the specified 64-bit long integer at the current {@code writerIndex} in the Reserved
   * Native Endian Byte Order and increases the {@code writerIndex} by {@code 8} in this buffer.
   *
   * @param value value.
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 8}
   * @since 1.0.0
   */
  Memory writeLongRE(long value);

  /**
   * Sets the specified 32-bit floating point number at the current {@code writerIndex} and
   * increases the {@code writerIndex} by {@code 4} in this buffer.
   *
   * @param value value.
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 4}
   * @since 1.0.0
   */
  Memory writeFloat(float value);

  /**
   * Sets the specified 32-bit floating point number at the current {@code writerIndex} in Reserved
   * Native Endian Byte Order and increases the {@code writerIndex} by {@code 4} in this buffer.
   *
   * @param value value.
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 4}
   * @since 1.0.0
   */
  Memory writeFloatRE(float value);

  /**
   * Sets the specified 64-bit floating point number at the current {@code writerIndex} and
   * increases the {@code writerIndex} by {@code 8} in this buffer.
   *
   * @param value value.
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 8}
   * @since 1.0.0
   */
  Memory writeDouble(double value);

  /**
   * Sets the specified 64-bit floating point number at the current {@code writerIndex} in Reserved
   * Native Endian Byte Order and increases the {@code writerIndex} by {@code 8} in this buffer.
   *
   * @param value value.
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 8}
   * @since 1.0.0
   */
  Memory writeDoubleRE(double value);

  /**
   * Transfers the specified source buffer's data to this buffer starting at the current {@code
   * writerIndex} until the source buffer becomes unreadable, and increases the {@code writerIndex}
   * by the number of the transferred bytes. This method is basically same with {@link
   * #writeBytes(Memory, long, long)}, except that this method increases the {@code readerIndex} of
   * the source buffer by the number of the transferred bytes while {@link #writeBytes(Memory, long,
   * long)} does not.
   *
   * @param src source.
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if {@code src.readableBytes} is greater than {@code
   *     this.writableBytes}
   * @since 1.0.0
   */
  Memory writeBytes(Memory src);

  /**
   * Transfers the specified source buffer's data to this buffer starting at the current {@code
   * writerIndex} and increases the {@code writerIndex} by the number of the transferred bytes (=
   * {@code length}). This method is basically same with {@link #writeBytes(Memory, long, long)},
   * except that this method increases the {@code readerIndex} of the source buffer by the number of
   * the transferred bytes (= {@code length}) while {@link #writeBytes(Memory, long, long)} does
   * not.
   *
   * @param src source.
   * @param length the number of bytes to transfer.
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if {@code length} is greater than {@code this.writableBytes}
   *     or if {@code length} is greater then {@code src.readableBytes}
   * @since 1.0.0
   */
  Memory writeBytes(Memory src, long length);

  /**
   * Transfers the specified source buffer's data to this buffer starting at the current {@code
   * writerIndex} and increases the {@code writerIndex} by the number of the transferred bytes (=
   * {@code length}).
   *
   * @param src source.
   * @param srcIndex the first index of the source
   * @param length the number of bytes to transfer
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if the specified {@code srcIndex} is less than {@code 0}, if
   *     {@code srcIndex + length} is greater than {@code src.capacity}, or if {@code length} is
   *     greater than {@code this.writableBytes}
   * @since 1.0.0
   */
  Memory writeBytes(Memory src, long srcIndex, long length);

  /**
   * Transfers the specified source array's data to this buffer starting at the current {@code
   * writerIndex} and increases the {@code writerIndex} by the number of the transferred bytes (=
   * {@code src.length}).
   *
   * @param src source.
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if {@code src.length} is greater than {@code
   *     this.writableBytes}
   * @since 1.0.0
   */
  Memory writeBytes(byte[] src);

  /**
   * Transfers the specified source array's data to this buffer starting at the current {@code
   * writerIndex} and increases the {@code writerIndex} by the number of the transferred bytes (=
   * {@code length}).
   *
   * @param src source.
   * @param srcIndex the first index of the source
   * @param length the number of bytes to transfer
   * @return this {@link Memory}.
   * @throws IndexOutOfBoundsException if the specified {@code srcIndex} is less than {@code 0}, if
   *     {@code srcIndex + length} is greater than {@code src.length}, or if {@code length} is
   *     greater than {@code this.writableBytes}
   * @since 1.0.0
   */
  Memory writeBytes(byte[] src, long srcIndex, long length);

  /**
   * Get the Thread owning this {@link Memory}.
   *
   * @return returns the Thread owning this {@link Memory}.
   * @since 1.0.0
   */
  Thread ownerThread();

  /**
   * Transfer {@link Memory} thread ownership.
   *
   * @param thread th new owner thread.
   * @return returns this {@link Memory}.
   * @since 1.0.0
   */
  Memory ownerThread(Thread thread);

  /**
   * Convert this {@link Memory} to specific buffer type.
   *
   * @param type buffer type.
   * @return returns buffer.
   * @throws IllegalArgumentException unsupported buffer type.
   * @since 1.0.0
   */
  Object as(Class type);

  /**
   * Returns a copy of this buffer's readable bytes. Modifying the content of the returned buffer or
   * this buffer does not affect each other at all. This method is identical to {@code
   * copy(readerIndex(), readableBytes())}. This method does not modify {@code readerIndex} or
   * {@code writerIndex} of this buffer.
   *
   * @return copied {@link Memory} buffer's.
   * @since 1.0.0
   */
  Memory copy();

  /**
   * Returns a copy of this buffer's sub-region. Modifying the content of the returned buffer or
   * this buffer does not affect each other at all. This method does not modify {@code readerIndex}
   * or {@code writerIndex} of this buffer.
   *
   * @param index index.
   * @param length length.
   * @return copied {@link Memory} buffer's.
   * @since 1.0.0
   */
  Memory copy(long index, long length);

  /**
   * Returns a slice of this buffer's readable bytes. Modifying the content of the returned buffer
   * or this buffer affects each other's content while they maintain separate indexes and marks.
   * This method is identical to {@code slice(readerIndex(), readableBytes())}. This method does not
   * modify {@code readerIndex} or {@code writerIndex} of this buffer.
   *
   * @return returns sliced {@link Memory} buffer's.
   * @since 1.0.0
   */
  Memory slice();

  /**
   * Returns a slice of this buffer's sub-region. Modifying the content of the returned buffer or
   * this buffer affects each other's content while they maintain separate indexes and marks. This
   * method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
   *
   * @param index index.
   * @param length length.
   * @return returns sliced {@link Memory} buffer's.
   * @since 1.0.0
   */
  Memory slice(long index, long length);

  /**
   * Duplicate the this {@link Memory} buffer. Modifying the content of the returned buffer or this
   * buffer affects each other's content while they maintain separate indexes and marks
   *
   * @return returns duplicated {@link Memory}.
   * @since 1.0.0
   */
  Memory duplicate();

  /**
   * Retrieves this buffer's native byte order.
   *
   * @return returns {@link ByteOrder#BIG_ENDIAN} or {@link ByteOrder#LITTLE_ENDIAN}.
   * @since 1.0.0
   */
  ByteOrder byteOrder();

  /**
   * Change this buffer's byte order.
   *
   * @param byteOrder byte order.
   * @return returns this buffer's with new byte order.
   * @since 1.0.0
   */
  Memory byteOrder(ByteOrder byteOrder);

  /**
   * Release this {@link Memory} buffer
   *
   * @return returns {@code true} if success, {@code false} otherwise.
   * @since 1.0.0
   */
  boolean release();

  /**
   * Indicate the buffer is sliced.
   *
   * @since 1.0.0
   */
  interface Sliced {

    /**
     * Unslice buffer.
     *
     * @return returns unsliced {@link Memory} buffer.
     * @since 1.0.0
     */
    Memory unSlice();
  }

  /**
   * Byte order.
   *
   * <p>There is no enum yet on java 1.2
   *
   * @since 1.0.0
   */
  interface ByteOrder {

    /**
     * Big endian.
     *
     * @since 1.0.0
     */
    ByteOrder BIG_ENDIAN = new BigEndian();

    /**
     * Little endian.
     *
     * @since 1.0.0
     */
    ByteOrder LITTLE_ENDIAN = new LittleEndian();

    /**
     * Big Endian byte order.
     *
     * @since 1.0.0
     */
    final class BigEndian implements ByteOrder {
      private BigEndian() {}
    }

    /**
     * Little Endian byte order.
     *
     * @since 1.0.0
     */
    final class LittleEndian implements ByteOrder {
      private LittleEndian() {}
    }
  }
}
