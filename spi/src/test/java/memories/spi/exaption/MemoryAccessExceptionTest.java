/*
 * SPDX-FileCopyrightText: 2020-2021 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package memories.spi.exaption;

import memories.spi.exception.MemoryAccessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/** */
@RunWith(JUnitPlatform.class)
public class MemoryAccessExceptionTest {

  @Test
  void throwExceptionTest() {
    Assertions.assertThrows(
        MemoryAccessException.class,
        new Executable() {
          @Override
          public void execute() throws Throwable {
            throw new MemoryAccessException("throwing exception.");
          }
        });
  }
}
