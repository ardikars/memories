/*
 * SPDX-FileCopyrightText: 2020-2021 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package memories.benchmark;

import memories.api.MemoryAllocatorApi;
import memories.spi.Memory;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class SetAndGet {

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(SetAndGet.class.getSimpleName()).forks(1).build();
    new Runner(opt).run();
  }

  @Warmup(iterations = 2) // Warmup Iteration = 3
  @Measurement(iterations = 3)
  @Benchmark
  public void jniGetByte(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.getByte(0);
    }
    plan.jniMemory.release();
  }

  @Warmup(iterations = 2) // Warmup Iteration = 3
  @Measurement(iterations = 3)
  @Benchmark
  public void jniSetByte(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.setByte(0, 1);
    }
    plan.jniMemory.release();
  }

  @Warmup(iterations = 2) // Warmup Iteration = 3
  @Measurement(iterations = 3)
  @Benchmark
  public void jniGetShort(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.getShort(0);
    }
    plan.jniMemory.release();
  }

  @Warmup(iterations = 2) // Warmup Iteration = 3
  @Measurement(iterations = 3)
  @Benchmark
  public void jniSetShort(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.setShort(0, 1);
    }
    plan.jniMemory.release();
  }

  @Warmup(iterations = 2) // Warmup Iteration = 3
  @Measurement(iterations = 3)
  @Benchmark
  public void jniGetInt(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.getInt(0);
    }
    plan.jniMemory.release();
  }

  @Warmup(iterations = 2) // Warmup Iteration = 3
  @Measurement(iterations = 3)
  @Benchmark
  public void jniSetInt(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.setInt(0, 1);
    }
    plan.jniMemory.release();
  }

  @Warmup(iterations = 2) // Warmup Iteration = 3
  @Measurement(iterations = 3)
  @Benchmark
  public void jniGetLong(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.getLong(0);
    }
    plan.jniMemory.release();
  }

  @Warmup(iterations = 2) // Warmup Iteration = 3
  @Measurement(iterations = 3)
  @Benchmark
  public void jniSetLong(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.setLong(0, 1L);
    }
    plan.jniMemory.release();
  }

  @Warmup(iterations = 2) // Warmup Iteration = 3
  @Measurement(iterations = 3)
  @Benchmark
  public void jniGetFloat(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.getFloat(0);
    }
    plan.jniMemory.release();
  }

  @Warmup(iterations = 2) // Warmup Iteration = 3
  @Measurement(iterations = 3)
  @Benchmark
  public void jniSetFloat(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.setFloat(0, 1.5F);
    }
    plan.jniMemory.release();
  }

  @Warmup(iterations = 2) // Warmup Iteration = 3
  @Measurement(iterations = 3)
  @Benchmark
  public void jniGetDouble(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.getDouble(0);
    }
    plan.jniMemory.release();
  }

  @Warmup(iterations = 2) // Warmup Iteration = 3
  @Measurement(iterations = 3)
  @Benchmark
  public void jniSetDouble(ExecutionPlan plan) {
    for (int i = 0; i < plan.iterations; i++) {
      plan.jniMemory.setDouble(0, 1.5D);
    }
    plan.jniMemory.release();
  }

  @State(Scope.Benchmark)
  public static class ExecutionPlan {

    @Param({"100", "200"})
    public int iterations;

    public Memory jniMemory;

    @Setup(Level.Invocation)
    public void setUp() {
      jniMemory = new MemoryAllocatorApi().allocate(8);
    }
  }
}
