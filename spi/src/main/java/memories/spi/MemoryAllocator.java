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
   * Allocate new block of memory with given size and native byte order.
   *
   * @param size size of memory block.
   * @return returns new {@link Memory} block.
   * @since 1.0.0
   */
  Memory allocate(long size);

  /**
   * Allocate new block of memory with given size and specific byte order.
   *
   * @param size size of memory block.
   * @param byteOrder byte order.
   * @return returns new {@link Memory} block.
   * @since 1.0.0
   */
  Memory allocate(long size, Memory.ByteOrder byteOrder);

  /**
   * Allocate new block of memory with given size, byte order, and zeroing param.
   *
   * @param size size of memory bbock.
   * @param byteOrder byte order.
   * @param clear zeroing the bugger.
   * @return returns new {@link Memory} block.
   * @since 1.0.0
   */
  Memory allocate(long size, Memory.ByteOrder byteOrder, boolean clear);
}
