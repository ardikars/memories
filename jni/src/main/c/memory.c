/*
 * SPDX-FileCopyrightText: 2020 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <jni.h>
#include <stdlib.h>
#include <memories.h>
#include <memories_jni_JNIMemory_Unsafe.h>

#include <string.h>
#define MEMCPY(ENV,D,S,L) do { \
  memcpy(D,S,L); \
} while(0)
#define MEMSET(ENV,D,C,L) do { \
  memset(D,C,L); \
} while(0)

JNIEXPORT void JNICALL Java_memories_jni_JNIMemory_00024Unsafe_nativeFree(JNIEnv * UNUSED_ENV(env), jclass UNUSED(j_cls), jlong j_address) {
  free(L2A(j_address));
}

JNIEXPORT jint JNICALL Java_memories_jni_JNIMemory_00024Unsafe_nativeByteOrderIsBE(JNIEnv * UNUSED_ENV(env), jclass UNUSED(j_cls)) {
  union {
    unsigned int i;
    char c[4];
  } bint = {0x01020304};
  return bint.c[0];
}

JNIEXPORT jlong JNICALL Java_memories_jni_JNIMemory_00024Unsafe_nativeMemcmp(JNIEnv * UNUSED_ENV(eng), jclass UNUSED(j_cls), jlong j_addr1, jlong j_addr2, jlong j_size) {
  jint ret = memcmp(L2A(j_addr1), L2A(j_addr2), j_size);
  return ret;
}

JNIEXPORT jlong JNICALL Java_memories_jni_JNIMemory_00024Unsafe_nativeMemcpy(JNIEnv * UNUSED_ENV(env), jclass UNUSED(j_cls), jlong j_dst, jlong j_src, jlong j_len) {
  MEMCPY(env, L2A(j_dst), L2A(j_src), j_len);
  return j_dst;
}

JNIEXPORT jlong JNICALL Java_memories_jni_JNIMemory_00024Unsafe_nativeRealloc(JNIEnv *env, jclass UNUSED(j_cls), jlong j_address, jlong j_new_size) {
  void *buf = realloc(L2A(j_address), (size_t) j_new_size);
  if (buf == NULL) {
    throwByName(env, EMemoryLeakExcetion, "realloc returns NULL.");
    return 0;
  } else {
    return A2L(buf);
  }
}

JNIEXPORT jbyte JNICALL Java_memories_jni_JNIMemory_00024Unsafe_nativeGetByte(JNIEnv * UNUSED_ENV(env), jclass UNUSED(j_cls), jlong j_address) {
  jbyte res = 0;
  MEMCPY(env, &res, L2A(j_address), sizeof(res));
  return res;
}

JNIEXPORT void JNICALL Java_memories_jni_JNIMemory_00024Unsafe_nativeSetByte(JNIEnv * UNUSED_ENV(env), jclass UNUSED(j_cls), jlong j_address, jbyte j_value) {
  MEMCPY(env, L2A(j_address), &j_value, sizeof(j_value));
}

JNIEXPORT jshort JNICALL Java_memories_jni_JNIMemory_00024Unsafe_nativeGetShort(JNIEnv * UNUSED_ENV(env), jclass UNUSED(j_cls), jlong j_address) {
  jshort res = 0;
  MEMCPY(env, &res, L2A(j_address), sizeof(res));
  return res;
}

JNIEXPORT void JNICALL Java_memories_jni_JNIMemory_00024Unsafe_nativeSetShort(JNIEnv * UNUSED_ENV(env), jclass UNUSED(j_cls), jlong j_address, jshort j_value) {
    MEMCPY(env, L2A(j_address), &j_value, sizeof(j_value));
}

JNIEXPORT jint JNICALL Java_memories_jni_JNIMemory_00024Unsafe_nativeGetInt(JNIEnv * UNUSED_ENV(env), jclass UNUSED(j_cls), jlong j_address) {
  jint res = 0;
  MEMCPY(env, &res, L2A(j_address), sizeof(res));
  return res;
}

JNIEXPORT void JNICALL Java_memories_jni_JNIMemory_00024Unsafe_nativeSetInt(JNIEnv * UNUSED_ENV(env), jclass UNUSED(j_cls), jlong j_address, jint j_value) {
  MEMCPY(env, L2A(j_address), &j_value, sizeof(j_value));
}

JNIEXPORT jlong JNICALL Java_memories_jni_JNIMemory_00024Unsafe_nativeGetLong(JNIEnv * UNUSED_ENV(env), jclass UNUSED(j_cls), jlong j_address) {
  jlong res = 0;
  MEMCPY(env, &res, L2A(j_address), sizeof(res));
  return res;
}

JNIEXPORT void JNICALL Java_memories_jni_JNIMemory_00024Unsafe_nativeSetLong(JNIEnv * UNUSED_ENV(env), jclass UNUSED(j_cls), jlong j_address, jlong j_value) {
  MEMCPY(env, L2A(j_address), &j_value, sizeof(j_value));
}

JNIEXPORT jbyteArray JNICALL Java_memories_jni_JNIMemory_00024Unsafe_nativeGetBytes(JNIEnv *env, jclass UNUSED(j_cls), jlong j_address, jbyteArray j_dst, jlong j_dst_idx, jlong j_dst_len) {
  (*env)->SetByteArrayRegion(env, j_dst, j_dst_idx, j_dst_len, L2A(j_address));
  return j_dst;
}

JNIEXPORT void JNICALL Java_memories_jni_JNIMemory_00024Unsafe_nativeSetBytes(JNIEnv *env, jclass UNUSED(j_cls), jlong j_address, jbyteArray j_src, jlong j_src_idx, jlong j_src_len) {
  (*env)->GetByteArrayRegion(env, j_src, j_src_idx, j_src_len, L2A(j_address));
}
