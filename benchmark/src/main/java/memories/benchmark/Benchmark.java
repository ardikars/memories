/*
 * SPDX-FileCopyrightText: 2020-2021 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package memories.benchmark;

import java.nio.ByteBuffer;
import memories.api.MemoryAllocatorApi;
import memories.spi.Memory;
import memories.spi.MemoryAllocator;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class Benchmark {

  private static final int SIZE = 16;
  private static final int ITERATION = 10;
  private static final int ITERATION_WARMUP = 2;

  private static final MemoryAllocator ALLOCATOR = MemoryAllocatorApi.getInstance();
  private static final Memory jniMemory = ALLOCATOR.allocate(SIZE);
  private static final ByteBuffer nioMemory = ByteBuffer.allocateDirect(SIZE);
  // private static final MemorySegment panamaMemory = MemorySegment.allocateNative(SIZE);

  private static final byte BYTE_VALUE = 8;
  private static final short SHORT_VALUE = 8;
  private static final int INT_VALUE = 8;
  private static final long LONG_VALUE = 8L;
  private static final float FLOAT_VALUE = 8.5F;
  private static final double DOUBLE_VALUE = 8.5D;

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(Benchmark.class.getSimpleName()).forks(1).build();
    new Runner(opt).run();
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniMallocAndFree() {
    ALLOCATOR.allocate(SIZE).release();
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniReleaseDirectByteBuffer() {
    ByteBuffer buf = ByteBuffer.allocateDirect(SIZE);
    ALLOCATOR.wrap(buf).release();
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetByte() {
    jniMemory.getByte(0);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetByte() {
    nioMemory.get(0);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetByte() {
    jniMemory.setByte(0, BYTE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetByte() {
    nioMemory.put(0, BYTE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetShort1() {
    jniMemory.getShort(0);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetShort1() {
    nioMemory.getShort(0);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetShort2() {
    jniMemory.getShort(1);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetShort2() {
    nioMemory.getShort(1);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetShort1() {
    jniMemory.setShort(0, SHORT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetShort1() {
    nioMemory.putShort(0, SHORT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetShort2() {
    jniMemory.setShort(1, SHORT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetShort2() {
    nioMemory.putShort(1, SHORT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetInt1() {
    jniMemory.getInt(0);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetInt1() {
    nioMemory.getInt(0);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetInt2() {
    jniMemory.getInt(1);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetInt2() {
    nioMemory.getInt(1);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetInt3() {
    jniMemory.getInt(2);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetInt3() {
    nioMemory.getInt(2);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetInt4() {
    jniMemory.getInt(3);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetInt4() {
    nioMemory.getInt(3);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetInt1() {
    jniMemory.setInt(0, INT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetInt1() {
    nioMemory.putInt(0, INT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetInt2() {
    jniMemory.setInt(1, INT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetInt2() {
    nioMemory.putInt(1, INT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetInt3() {
    jniMemory.setInt(2, INT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetInt3() {
    nioMemory.putInt(2, INT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetInt4() {
    jniMemory.setInt(3, INT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetInt4() {
    nioMemory.putInt(3, INT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetLong1() {
    jniMemory.getLong(0);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetLong1() {
    nioMemory.getLong(0);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetLong2() {
    jniMemory.getLong(1);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetLong2() {
    nioMemory.getLong(1);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetLong3() {
    jniMemory.getLong(2);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetLong3() {
    nioMemory.getLong(2);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetLong4() {
    jniMemory.getLong(3);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetLong4() {
    nioMemory.getLong(3);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetLong5() {
    jniMemory.getLong(4);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetLong5() {
    nioMemory.getLong(4);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetLong6() {
    jniMemory.getLong(5);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetLong6() {
    nioMemory.getLong(5);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetLong7() {
    jniMemory.getLong(6);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetLong7() {
    nioMemory.getLong(6);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetLong8() {
    jniMemory.getLong(7);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetLong8() {
    nioMemory.getLong(7);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetLong1() {
    jniMemory.setLong(0, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetLong1() {
    nioMemory.putLong(0, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetLong2() {
    jniMemory.setLong(1, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetLong2() {
    nioMemory.putLong(1, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetLong3() {
    jniMemory.setLong(2, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetLong3() {
    nioMemory.putLong(2, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetLong4() {
    jniMemory.setLong(3, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetLong4() {
    nioMemory.putLong(3, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetLong5() {
    jniMemory.setLong(4, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetLong5() {
    nioMemory.putLong(4, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetLong6() {
    jniMemory.setLong(5, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetLong6() {
    nioMemory.putLong(5, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetLong7() {
    jniMemory.setLong(6, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetLong7() {
    nioMemory.putLong(6, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetLong8() {
    jniMemory.setLong(7, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetLong8() {
    nioMemory.putLong(7, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetFloat1() {
    jniMemory.getFloat(0);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetFloat1() {
    nioMemory.getFloat(0);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetFloat2() {
    jniMemory.getFloat(1);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetFloat2() {
    nioMemory.getFloat(1);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetFloat3() {
    jniMemory.getFloat(2);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetFloat3() {
    nioMemory.getFloat(2);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetFloat4() {
    jniMemory.getFloat(3);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetFloat4() {
    nioMemory.getFloat(3);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetFloat1() {
    jniMemory.setFloat(0, FLOAT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetFloat1() {
    nioMemory.putFloat(0, FLOAT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetFloat2() {
    jniMemory.setFloat(1, FLOAT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetFloat2() {
    nioMemory.putFloat(1, FLOAT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetFloat3() {
    jniMemory.setFloat(2, FLOAT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetFloat3() {
    nioMemory.putFloat(2, FLOAT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetFloat4() {
    jniMemory.setFloat(3, FLOAT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetFloat4() {
    nioMemory.putFloat(3, FLOAT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetDouble1() {
    jniMemory.getDouble(0);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetDouble1() {
    nioMemory.getDouble(0);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetDouble2() {
    jniMemory.getDouble(1);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetDouble2() {
    nioMemory.getDouble(1);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetDouble3() {
    jniMemory.getDouble(2);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetDouble3() {
    nioMemory.getDouble(2);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetDouble4() {
    jniMemory.getDouble(3);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetDouble4() {
    nioMemory.getDouble(3);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetDouble5() {
    jniMemory.getDouble(4);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetDouble5() {
    nioMemory.getDouble(4);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetDouble6() {
    jniMemory.getDouble(5);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetDouble6() {
    nioMemory.getDouble(5);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetDouble7() {
    jniMemory.getDouble(6);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetDouble7() {
    nioMemory.getDouble(6);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetDouble8() {
    jniMemory.getDouble(7);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioGetDouble8() {
    nioMemory.getDouble(7);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetDouble1() {
    jniMemory.setDouble(0, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetDouble1() {
    nioMemory.putDouble(0, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetDouble2() {
    jniMemory.setDouble(1, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetDouble2() {
    nioMemory.putDouble(1, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetDouble3() {
    jniMemory.setDouble(2, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetDouble3() {
    nioMemory.putDouble(2, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetDouble4() {
    jniMemory.setDouble(3, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetDouble4() {
    nioMemory.putDouble(3, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetDouble5() {
    jniMemory.setDouble(4, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetDouble5() {
    nioMemory.putDouble(4, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetDouble6() {
    jniMemory.setDouble(5, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetDouble6() {
    nioMemory.putDouble(5, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetDouble7() {
    jniMemory.setDouble(6, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetDouble7() {
    nioMemory.putDouble(6, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetDouble8() {
    jniMemory.setDouble(7, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void nioSetDouble8() {
    nioMemory.putDouble(7, DOUBLE_VALUE);
  }

  /* PANAMA  */
  /*
  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaMallocAndFree() {
    MemorySegment memorySegment = MemorySegment.allocateNative(SIZE);
    memorySegment.close();
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaReleaseDirectByteBuffer() {
    ByteBuffer buf = ByteBuffer.allocateDirect(SIZE);
    MemorySegment segment = MemorySegment.ofByteBuffer(buf);
    segment.close();
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetByte() {
    MemoryAccess.getByteAtOffset(panamaMemory, 0);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetByte() {
    MemoryAccess.setByteAtOffset(panamaMemory, 0, BYTE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetShort1() {
    MemoryAccess.getShortAtOffset(panamaMemory, 0);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetShort2() {
    MemoryAccess.getShortAtOffset(panamaMemory, 1);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetShort1() {
    MemoryAccess.setShortAtOffset(panamaMemory, 0, SHORT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetShort2() {
    MemoryAccess.setShortAtOffset(panamaMemory, 1, SHORT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetInt1() {
    MemoryAccess.getShortAtOffset(panamaMemory, 0);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetInt2() {
    MemoryAccess.getShortAtOffset(panamaMemory, 1);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetInt3() {
    MemoryAccess.getShortAtOffset(panamaMemory, 2);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetInt4() {
    MemoryAccess.getShortAtOffset(panamaMemory, 3);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetInt1() {
    MemoryAccess.setIntAtOffset(panamaMemory, 0, INT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetInt2() {
    MemoryAccess.setIntAtOffset(panamaMemory, 1, INT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetInt3() {
    MemoryAccess.setIntAtOffset(panamaMemory, 2, INT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetInt4() {
    MemoryAccess.setIntAtOffset(panamaMemory, 3, INT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetLong1() {
    MemoryAccess.getLongAtOffset(panamaMemory, 1);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetLong2() {
    MemoryAccess.getLongAtOffset(panamaMemory, 2);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetLong3() {
    MemoryAccess.getLongAtOffset(panamaMemory, 3);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetLong4() {
    MemoryAccess.getLongAtOffset(panamaMemory, 4);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetLong5() {
    MemoryAccess.getLongAtOffset(panamaMemory, 4);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetLong6() {
    MemoryAccess.getLongAtOffset(panamaMemory, 5);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetLong7() {
    MemoryAccess.getLongAtOffset(panamaMemory, 6);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetLong8() {
    MemoryAccess.getLongAtOffset(panamaMemory, 7);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetLong1() {
    MemoryAccess.setLongAtOffset(panamaMemory, 0, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetLong2() {
    MemoryAccess.setLongAtOffset(panamaMemory, 1, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetLong3() {
    MemoryAccess.setLongAtOffset(panamaMemory, 2, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetLong4() {
    MemoryAccess.setLongAtOffset(panamaMemory, 3, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetLong5() {
    MemoryAccess.setLongAtOffset(panamaMemory, 4, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetLong6() {
    MemoryAccess.setLongAtOffset(panamaMemory, 5, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetLong7() {
    MemoryAccess.setLongAtOffset(panamaMemory, 6, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetLong8() {
    MemoryAccess.setLongAtOffset(panamaMemory, 7, LONG_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetFloat1() {
    MemoryAccess.getFloatAtOffset(panamaMemory, 0);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetFloat2() {
    MemoryAccess.getFloatAtOffset(panamaMemory, 1);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetFloat3() {
    MemoryAccess.getFloatAtOffset(panamaMemory, 2);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetFloat4() {
    MemoryAccess.getFloatAtOffset(panamaMemory, 3);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetFloat1() {
    MemoryAccess.setFloatAtOffset(panamaMemory, 0, FLOAT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetFloat2() {
    MemoryAccess.setFloatAtOffset(panamaMemory, 1, FLOAT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetFloat3() {
    MemoryAccess.setFloatAtOffset(panamaMemory, 2, FLOAT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetFloat4() {
    MemoryAccess.setFloatAtOffset(panamaMemory, 3, FLOAT_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetDouble1() {
    MemoryAccess.getDoubleAtOffset(panamaMemory, 0);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetDouble2() {
    MemoryAccess.getDoubleAtOffset(panamaMemory, 1);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetDouble3() {
    MemoryAccess.getDoubleAtOffset(panamaMemory, 2);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetDouble4() {
    MemoryAccess.getDoubleAtOffset(panamaMemory, 3);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetDouble5() {
    MemoryAccess.getDoubleAtOffset(panamaMemory, 4);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetDouble6() {
    MemoryAccess.getDoubleAtOffset(panamaMemory, 5);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetDouble7() {
    MemoryAccess.getDoubleAtOffset(panamaMemory, 6);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaGetDouble8() {
    MemoryAccess.getDoubleAtOffset(panamaMemory, 7);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetDouble1() {
    MemoryAccess.setDoubleAtOffset(panamaMemory, 0, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetDouble2() {
    MemoryAccess.setDoubleAtOffset(panamaMemory, 1, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetDouble3() {
    MemoryAccess.setDoubleAtOffset(panamaMemory, 2, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetDouble4() {
    MemoryAccess.setDoubleAtOffset(panamaMemory, 3, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetDouble5() {
    MemoryAccess.setDoubleAtOffset(panamaMemory, 4, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetDouble6() {
    MemoryAccess.setDoubleAtOffset(panamaMemory, 5, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetDouble7() {
    MemoryAccess.setDoubleAtOffset(panamaMemory, 6, DOUBLE_VALUE);
  }

  @Warmup(iterations = ITERATION_WARMUP)
  @Measurement(iterations = ITERATION)
  @org.openjdk.jmh.annotations.Benchmark
  public void panamaSetDouble8() {
    MemoryAccess.setDoubleAtOffset(panamaMemory, 7, DOUBLE_VALUE);
  }
  */
}
