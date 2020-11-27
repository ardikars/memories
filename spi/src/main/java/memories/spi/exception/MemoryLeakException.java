/*
 * SPDX-FileCopyrightText: 2020 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package memories.spi.exception;

public final class MemoryLeakException extends RuntimeException {
  public MemoryLeakException(String message) {
    super(message);
  }
}
