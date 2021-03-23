/*
 * SPDX-FileCopyrightText: 2020-2021 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package memories.spi.exception;

public final class MemoryAccessException extends RuntimeException {
  public MemoryAccessException(String message) {
    super(message);
  }
}
