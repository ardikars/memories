/*
 * SPDX-FileCopyrightText: 2020-2021 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package memories.spi;

/**
 * Off-heap memory allocator.
 *
 * @since 1.0.0
 */
public interface MemoryAllocator {

  /**
   * Allocate new uninitialized block of memory with given size and native byte order.
   *
   * @param size size of memory block.
   * @return returns new {@link Memory} block.
   * @since 1.0.0
   */
  Memory allocate(long size);

  /**
   * Allocate new uninitialized block of memory with given size and specific byte order.
   *
   * @param size size of memory block.
   * @param byteOrder byte order.
   * @return returns new {@link Memory} block.
   * @since 1.0.0
   */
  Memory allocate(long size, Memory.ByteOrder byteOrder);

  /**
   * Allocate new block of memory with given size, byte order, and zeroing param ({@code true}
   * initialize buffer with zero value, {@code false} uninitialize buffer).
   *
   * @param size size of memory bbock.
   * @param byteOrder byte order.
   * @param clear zeroing the buffer.
   * @return returns new {@link Memory} block.
   * @since 1.0.0
   */
  Memory allocate(long size, Memory.ByteOrder byteOrder, boolean clear);

  /**
   * Wrap direct buffer into {@link Memory} without cleaner.
   *
   * @param buffer direct buffer.
   * @return returns wrapped memoryAddress.
   * @since 1.0.0
   */
  Memory of(Object buffer);

  /**
   * Wrap direct buffer into {@link Memory} without cleaner.
   *
   * @param buffer direct buffer.
   * @param byteOrder byte order.
   * @return returns wrapped memoryAddress.
   * @since 1.0.0
   */
  Memory of(Object buffer, Memory.ByteOrder byteOrder);
}
