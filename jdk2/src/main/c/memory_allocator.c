/*
 * SPDX-FileCopyrightText: 2020-2021 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <jni.h>
#include <stdlib.h>
#include <memories.h>

JNIEXPORT jboolean nativeByteOrderIsBE(JNIEnv * UNUSED_ENV(env), jclass UNUSED(j_cls)) {
  union {
    unsigned int i;
    char c[4];
  } bint = {0x01020304};
  jboolean ret = bint.c[0];
  return ret;
}

JNIEXPORT jlong nativeMalloc(JNIEnv *env, jclass UNUSED(j_cls), jlong j_size) {
  void *buf = malloc((size_t) j_size);
  if (buf == NULL) {
    return 0;
  } else {
    return A2L(buf);
  }
}

JNIEXPORT void nativeFree(JNIEnv * UNUSED_ENV(env), jclass UNUSED(j_cls), jlong j_address) {
  free(L2A(j_address));
}

JNIEXPORT jlong nativeRealloc(JNIEnv *env, jclass UNUSED(j_cls), jlong j_address, jlong j_new_size) {
  void *buf = realloc(L2A(j_address), (size_t) j_new_size);
  if (buf == NULL) {
    return 0;
  } else {
    return A2L(buf);
  }
}

int memory_allocator_register_native_methods(JNIEnv *env) {
  jclass cls;
  if ((cls = (*env)->FindClass(env, "memories/jdk2/JDK2MemoryAllocator$NativeMemoryAllocator")) == NULL) {
    fprintf(stderr, "FATAL: Class memories.jdk2.JDK2MemoryAllocator$NativeMemoryAllocator not found");
    fflush(stderr);
  return JNI_ERR;
  }
  const JNINativeMethod methods[] = {
    {"nativeByteOrderIsBE","()Z",(void *) nativeByteOrderIsBE},
    {"nativeMalloc","(J)J",(void *) nativeMalloc},
    {"nativeFree","(J)V",(void *) nativeFree},
    {"nativeRealloc","(JJ)J",(void *) nativeRealloc},
  };
  return (*env)->RegisterNatives(env, cls, methods, sizeof(methods) / sizeof(methods[0]));
}