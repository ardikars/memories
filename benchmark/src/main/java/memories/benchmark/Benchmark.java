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
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class Benchmark {

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(Benchmark.class.getSimpleName()).forks(1).build();
    new Runner(opt).run();
  }

  @Warmup() // Warmup Iteration = 3
  @Measurement()
  @org.openjdk.jmh.annotations.Benchmark
  public void jniMallocAndFree(ExecutionPlan plan) {
    MemoryAllocator allocator = MemoryAllocatorApi.getInstance();
    for (int i = 1; i <= plan.iterations; i++) {
      allocator.allocate(i).release();
    }
    for (int i = plan.iterations; i >= 1; i--) {
      allocator.allocate(i).release();
    }
  }

  @Warmup() // Warmup Iteration = 3
  @Measurement()
  @org.openjdk.jmh.annotations.Benchmark
  public void jniReleaseDirectByteBuffer(ExecutionPlan plan) {
    MemoryAllocator allocator = MemoryAllocatorApi.getInstance();
    for (int i = 1; i <= plan.iterations; i++) {
      ByteBuffer buf = ByteBuffer.allocateDirect(i);
      allocator.wrap(buf).release();
    }
    for (int i = plan.iterations; i >= 1; i--) {
      ByteBuffer buf = ByteBuffer.allocateDirect(i);
      allocator.wrap(buf).release();
    }
  }

  @Warmup() // Warmup Iteration = 3
  @Measurement()
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetByte(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.getByte(0);
    }
    plan.jniMemory.release();
  }

  @Warmup() // Warmup Iteration = 3
  @Measurement()
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetByte(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.setByte(0, 1);
    }
    plan.jniMemory.release();
  }

  @Warmup() // Warmup Iteration = 3
  @Measurement()
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetShort(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.getShort(0);
    }
    plan.jniMemory.release();
  }

  @Warmup() // Warmup Iteration = 3
  @Measurement()
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetShort(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.setShort(0, 1);
    }
    plan.jniMemory.release();
  }

  @Warmup() // Warmup Iteration = 3
  @Measurement()
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetInt(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.getInt(0);
    }
    plan.jniMemory.release();
  }

  @Warmup() // Warmup Iteration = 3
  @Measurement()
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetInt(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.setInt(0, 1);
    }
    plan.jniMemory.release();
  }

  @Warmup() // Warmup Iteration = 3
  @Measurement()
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetLong(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.getLong(0);
    }
    plan.jniMemory.release();
  }

  @Warmup() // Warmup Iteration = 3
  @Measurement()
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetLong(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.setLong(0, 1L);
    }
    plan.jniMemory.release();
  }

  @Warmup() // Warmup Iteration = 3
  @Measurement()
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetFloat(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.getFloat(0);
    }
    plan.jniMemory.release();
  }

  @Warmup() // Warmup Iteration = 3
  @Measurement()
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetFloat(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.setFloat(0, 1.5F);
    }
    plan.jniMemory.release();
  }

  @Warmup() // Warmup Iteration = 3
  @Measurement()
  @org.openjdk.jmh.annotations.Benchmark
  public void jniGetDouble(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.getDouble(0);
    }
    plan.jniMemory.release();
  }

  @Warmup() // Warmup Iteration = 3
  @Measurement()
  @org.openjdk.jmh.annotations.Benchmark
  public void jniSetDouble(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.setDouble(0, 1.5D);
    }
    plan.jniMemory.release();
  }

  @State(Scope.Benchmark)
  public static class ExecutionPlan {

    @Param({"1"})
    public int iterations;

    public Memory jniMemory;

    @Setup(Level.Invocation)
    public void setUp() {
      jniMemory = MemoryAllocatorApi.getInstance().allocate(8);
    }
  }
}
