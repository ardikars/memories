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

#define GET(JTYPE, NTYPE)  \
JNIEXPORT NTYPE JNICALL \
Java_memories_jni_JNIMemory_00024Unsafe_nativeGet##JTYPE(JNIEnv* env, jobject self, jlong address) \
{ NTYPE tmp; MEMCPY(env, &tmp, L2A(address), sizeof(tmp)); return tmp; }

#define SET(JTYPE, NTYPE) \
JNIEXPORT void JNICALL \
Java_memories_jni_JNIMemory_00024Unsafe_nativeSet##JTYPE(JNIEnv *env, jobject self, jlong address, NTYPE value) \
{ MEMCPY(env, L2A(address), &value, sizeof(value)); }


#define UNSAFE(J, N) GET(J, N) SET(J, N)

UNSAFE(Byte, jbyte);
UNSAFE(Char, jchar);
UNSAFE(Boolean, jboolean);
UNSAFE(Short, jshort);
UNSAFE(Int, jint);
UNSAFE(Long, jlong);
UNSAFE(Float, jfloat);
UNSAFE(Double, jdouble);

JNIEXPORT void JNICALL Java_memories_jni_JNIMemory_00024Unsafe_nativeGetBytes(JNIEnv *env, jclass UNUSED(j_cls), jlong j_address, jbyteArray j_dst, jlong j_dst_idx, jlong j_dst_len) {
  (*env)->SetByteArrayRegion(env, j_dst, j_dst_idx, j_dst_len, L2A(j_address));
}

JNIEXPORT void JNICALL Java_memories_jni_JNIMemory_00024Unsafe_nativeSetBytes(JNIEnv *env, jclass UNUSED(j_cls), jlong j_address, jbyteArray j_src, jlong j_src_idx, jlong j_src_len) {
  (*env)->GetByteArrayRegion(env, j_src, j_src_idx, j_src_len, L2A(j_address));
}
