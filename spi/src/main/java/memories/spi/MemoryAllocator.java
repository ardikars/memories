/*
 * SPDX-FileCopyrightText: 2020-2021 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package memories.spi;

import memories.spi.annotation.Restricted;

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
   * Wrap raw memory address without cleaner.
   *
   * @param memoryAddress memory address.
   * @param memoryCapacity block size.
   * @return returns wrapped memoryAddress.
   * @throws IllegalAccessException restricted method call.
   * @since 1.0.0
   */
  @Restricted
  Memory of(long memoryAddress, long memoryCapacity) throws IllegalAccessException;

  /**
   * Wrap raw memory address without cleaner.
   *
   * @param memoryAddress memory address.
   * @param memoryCapacity block size.
   * @param byteOrder byte order.
   * @return returns wrapped memoryAddress.
   * @throws IllegalAccessException restricted method call.
   * @since 1.0.0
   */
  @Restricted
  Memory of(long memoryAddress, long memoryCapacity, Memory.ByteOrder byteOrder)
      throws IllegalAccessException;

  /**
   * Wrap raw memory address.
   *
   * @param memoryAddress memory address.
   * @param memoryCapacity block size.
   * @param byteOrder byte order.
   * @param autoClean {@code true} for auto release the buffer if this object is no longer in use,
   *     {@code false} otherwise (without cleaner).
   * @throws IllegalAccessException restricted method call.
   * @return returns wrapped memoryAddress.
   * @since 1.0.0
   */
  @Restricted
  Memory of(long memoryAddress, long memoryCapacity, Memory.ByteOrder byteOrder, boolean autoClean)
      throws IllegalAccessException;

  /**
   * Wrap direct buffer into {@link Memory} without cleaner.
   *
   * @param buffer direct buffer.
   * @throws IllegalAccessException restricted method call.
   * @return returns wrapped memoryAddress.
   * @since 1.0.0
   */
  @Restricted
  Memory of(Object buffer) throws IllegalAccessException;

  /**
   * Wrap direct buffer into {@link Memory} without cleaner.
   *
   * @param buffer direct buffer.
   * @param byteOrder byte order.
   * @throws IllegalAccessException restricted method call.
   * @return returns wrapped memoryAddress.
   * @since 1.0.0
   */
  @Restricted
  Memory of(Object buffer, Memory.ByteOrder byteOrder) throws IllegalAccessException;

  /**
   * Wrap direct buffer into {@link Memory}.
   *
   * @param buffer direct buffer.
   * @param byteOrder byte order.
   * @param autoClean {@code true} for auto release the buffer if this object is no longer in use,
   *     {@code false} otherwise (without cleaner).
   * @throws IllegalAccessException restricted method call.
   * @return returns wrapped memoryAddress.
   * @since 1.0.0
   */
  @Restricted
  Memory of(Object buffer, Memory.ByteOrder byteOrder, boolean autoClean)
      throws IllegalAccessException;
}
