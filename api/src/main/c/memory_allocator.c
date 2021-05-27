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

JNIEXPORT jlong nativeGetDirectBufferAddress(JNIEnv *env, jclass UNUSED(j_cls), jobject jbuf) {
  void *buf = (void *) (*env)->GetDirectBufferAddress(env, jbuf);
  return A2L(buf);
}

JNIEXPORT jlong nativeGetDirectBufferCapacity(JNIEnv *env, jclass UNUSED(j_cls), jobject jbuf) {
  return (jlong) (*env)->GetDirectBufferCapacity(env, jbuf);
}

JNIEXPORT void nativeCleanDirectByteBuffer(JNIEnv *env, jclass UNUSED(j_cls), jint j_ver, jobject jbuf) {
  jclass the_class;
  jfieldID the_fid;
  if ((the_class = (*env)->FindClass(env, "java/nio/DirectByteBuffer")) == NULL) {
    fprintf(stderr, "FATAL: Class java.nio.DirectByteBuffer not found");
    fflush(stderr);
    return;
  }
  if (j_ver > 8) {
    if ((the_fid = (*env)->GetFieldID(env, the_class, "cleaner", "Ljdk/internal/ref/Cleaner;")) == NULL) {
      fprintf(stderr, "FATAL: Field cleaner not found in java.nio.DirectByteBuffer");
      fflush(stderr);
      return;
    }
  } else {
    if ((the_fid = (*env)->GetFieldID(env, the_class, "cleaner", "Lsun/misc/Cleaner;")) == NULL) {
      fprintf(stderr, "FATAL: Field cleaner not found in java.nio.DirectByteBuffer");
      fflush(stderr);
      return;
    }
  }
  jobject the_cleaner = (*env)->GetObjectField(env, jbuf, the_fid);
  if (the_class == NULL) {
    fprintf(stderr, "FATAL: Field cleaner is NULL in java.nio.DirectByteBuffer");
    fflush(stderr);
    return;
  }
  jclass the_cleaner_cls = (*env)->GetObjectClass(env, the_cleaner);
  jmethodID the_clean_mid = (*env)->GetMethodID(env, the_cleaner_cls, "clean", "()V");
  (*env)->CallVoidMethod(env, the_cleaner, the_clean_mid);
}

int memory_allocator_register_native_methods(JNIEnv *env) {
  jclass cls;
  if ((cls = (*env)->FindClass(env, "memories/api/MemoryAllocatorApi$NativeMemoryAllocator")) == NULL) {
    fprintf(stderr, "FATAL: Class memories.api.MemoryAllocatorApi$NativeMemoryAllocator not found");
    fflush(stderr);
    return JNI_ERR;
  }
  const JNINativeMethod methods[] = {
    {"nativeByteOrderIsBE","()Z",(void *) nativeByteOrderIsBE},
    {"nativeMalloc","(J)J",(void *) nativeMalloc},
    {"nativeFree","(J)V",(void *) nativeFree},
    {"nativeRealloc","(JJ)J",(void *) nativeRealloc},
    {"nativeGetDirectBufferAddress","(Ljava/lang/Object;)J",(void *) nativeGetDirectBufferAddress},
    {"nativeGetDirectBufferCapacity","(Ljava/lang/Object;)J",(void *) nativeGetDirectBufferCapacity},
    {"nativeCleanDirectByteBuffer","(ILjava/lang/Object;)V",(void *) nativeCleanDirectByteBuffer}
  };
  return (*env)->RegisterNatives(env, cls, methods, sizeof(methods) / sizeof(methods[0]));
}