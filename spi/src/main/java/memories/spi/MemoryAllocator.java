/*
 * SPDX-FileCopyrightText: 2020 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package memories.spi;

import memories.spi.exception.MemoryLeakException;

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
   * @throws MemoryLeakException if malloc returns NULL.
   * @since 1.0.0
   */
  Memory allocate(long size);

  /**
   * Allocate new block of memory with given size and specific byte order.
   *
   * @param size size of memory block.
   * @param byteOrder byte order.
   * @return returns new {@link Memory} block.
   * @throws MemoryLeakException if malloc returns NULL.
   * @since 1.0.0
   */
  Memory allocate(long size, Memory.ByteOrder byteOrder);
}
