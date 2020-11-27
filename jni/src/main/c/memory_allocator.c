/*
 * SPDX-FileCopyrightText: 2020 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <jni.h>
#include <stdlib.h>
#include <memories.h>
#include <memories_jni_JNIMemoryAllocator_Unsafe.h>

JNIEXPORT jlong JNICALL Java_memories_jni_JNIMemoryAllocator_00024Unsafe_nativeMalloc(JNIEnv *env, jclass UNUSED(j_cls), jlong j_size) {
  void *buf = malloc((size_t) j_size);
  if (buf == NULL) {
    throwByName(env, EMemoryLeakExcetion, "malloc returns NULL.");
    return 0;
  } else {
    return A2L(buf);
  }
}