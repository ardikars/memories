/*
 * SPDX-FileCopyrightText: 2020-2021 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <jni.h>
#include <stdlib.h>
#include <memories.h>

#include <string.h>
#define MEMCPY(ENV,D,S,L) do { \
  memcpy(D,S,L); \
} while(0)
#define MEMSET(ENV,D,C,L) do { \
  memset(D,C,L); \
} while(0)

JNIEXPORT jlong JNICALL nativeSetMemory(JNIEnv * UNUSED_ENV(env), jclass UNUSED(j_cls), jlong j_addr, jint j_val, jlong j_size) {
  MEMSET(env, L2A(j_addr), j_val, j_size);
  return j_addr;
}

JNIEXPORT jlong JNICALL nativeCopyMemory(JNIEnv * UNUSED_ENV(env), jclass UNUSED(j_cls), jlong j_dst, jlong j_src, jlong j_len) {
  MEMCPY(env, L2A(j_dst), L2A(j_src), j_len);
  return j_dst;
}

JNIEXPORT jint JNICALL nativeCompareMemory(JNIEnv * UNUSED_ENV(eng), jclass UNUSED(j_cls), jlong j_addr1, jlong j_addr2, jlong j_size) {
  jint ret = memcmp(L2A(j_addr1), L2A(j_addr2), j_size);
  return ret;
}

#define GET(JTYPE, NTYPE)  \
JNIEXPORT NTYPE JNICALL \
nativeGet##JTYPE(JNIEnv* env, jobject self, jlong address) \
{ NTYPE tmp; MEMCPY(env, &tmp, L2A(address), sizeof(tmp)); return tmp; }

#define SET(JTYPE, NTYPE) \
JNIEXPORT void JNICALL \
nativeSet##JTYPE(JNIEnv *env, jobject self, jlong address, NTYPE value) \
{ MEMCPY(env, L2A(address), &value, sizeof(value)); }

#define GET_ARRAY(JTYPE, NTYPE)  \
JNIEXPORT void JNICALL \
nativeGet##JTYPE##Array(JNIEnv *env, jclass UNUSED(j_cls), jlong j_address, NTYPE##Array j_dst, jlong j_dst_idx, jlong j_dst_len) \
{ (*env)->Set##JTYPE##ArrayRegion(env, j_dst, j_dst_idx, j_dst_len, L2A(j_address)); }

#define SET_ARRAY(JTYPE, NTYPE) \
JNIEXPORT void JNICALL \
nativeSet##JTYPE##Array(JNIEnv *env, jclass UNUSED(j_cls), jlong j_address, NTYPE##Array j_src, jlong j_src_idx, jlong j_src_len) \
{ (*env)->Get##JTYPE##ArrayRegion(env, j_src, j_src_idx, j_src_len, L2A(j_address)); }

#define UNSAFE(J, N) GET(J, N) SET(J, N) GET_ARRAY(J, N) SET_ARRAY(J, N)

UNSAFE(Byte, jbyte);
UNSAFE(Char, jchar);
UNSAFE(Boolean, jboolean);
UNSAFE(Short, jshort);
UNSAFE(Int, jint);
UNSAFE(Long, jlong);
UNSAFE(Float, jfloat);
UNSAFE(Double, jdouble);

int memory_register_native_methods(JNIEnv *env) {
  jclass cls;
  if ((cls = (*env)->FindClass(env, "memories/api/MemoryApi$NativeMemoryAccess")) == NULL) {
    fprintf(stderr, "FATAL: Class memories.api.MemoryApi$NativeMemoryAccess not found");
    fflush(stderr);
    return JNI_ERR;
  }
  const JNINativeMethod methods[] = {
    {"nativeSetMemory","(JIJ)J",(void *) nativeSetMemory},
    {"nativeCopyMemory","(JJJ)J",(void *) nativeCopyMemory},
    {"nativeCompareMemory","(JJJ)I",(void *) nativeCompareMemory},
    {"nativeGetByte","(J)B",(void *) nativeGetByte},
    {"nativeSetByte","(JB)V",(void *) nativeSetByte},
    {"nativeGetShort","(J)S",(void *) nativeGetShort},
    {"nativeSetShort","(JS)V",(void *) nativeSetShort},
    {"nativeGetInt","(J)I",(void *) nativeGetInt},
    {"nativeSetInt","(JI)V",(void *) nativeSetInt},
    {"nativeGetLong","(J)J",(void *) nativeGetLong},
    {"nativeSetLong","(JJ)V",(void *) nativeSetLong},
    {"nativeGetByteArray","(J[BJJ)V",(void *) nativeGetByteArray},
    {"nativeSetByteArray","(J[BJJ)V",(void *) nativeSetByteArray},
    {"nativeGetShortArray","(J[SJJ)V",(void *) nativeGetShortArray},
    {"nativeSetShortArray","(J[SJJ)V",(void *) nativeSetShortArray},
    {"nativeGetIntArray","(J[IJJ)V",(void *) nativeGetIntArray},
    {"nativeSetIntArray","(J[IJJ)V",(void *) nativeSetIntArray},
    {"nativeGetLongArray","(J[JJJ)V",(void *) nativeGetLongArray},
    {"nativeSetLongArray","(J[JJJ)V",(void *) nativeSetLongArray}
  };
  return (*env)->RegisterNatives(env, cls, methods, sizeof(methods) / sizeof(methods[0]));
}
