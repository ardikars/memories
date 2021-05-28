/*
 * SPDX-FileCopyrightText: 2020-2021 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <jni.h>
#include <memories.h>

jint VERSION;
jclass CLEANER;
jfieldID CLEANER_FID;
jmethodID CLEAN_MID;

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  JNIEnv* env = NULL;
  if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_2) != JNI_OK) {
    fprintf(stderr, "FATAL: JNI version missmatch.");
    fflush(stderr);
    return JNI_ERR;
  }
  if (memory_register_native_methods(env) != JNI_OK) {
    return JNI_ERR;
  }
  if (memory_allocator_register_native_methods(env) != JNI_OK) {
    return JNI_ERR;
  }
  VERSION = (*env)->GetVersion(env);
  if (VERSION < JNI_VERSION_1_4) {
    return VERSION;
  } else {
    jclass the_class;
    if ((the_class = (*env)->FindClass(env, "java/nio/DirectByteBuffer")) == NULL) {
      fprintf(stderr, "FATAL: Class java.nio.DirectByteBuffer not found.");
      fflush(stderr);
      return JNI_ERR;
    }
    if (VERSION <= 0x00010008) {
      /* sun.misc */
      if ((CLEANER = (*env)->FindClass(env, "sun/misc/Cleaner")) == NULL) {
        fprintf(stderr, "FATAL: Class sun.misc.Cleaner not found.");
        fflush(stderr);
        return JNI_ERR;
      }
      if ((CLEANER_FID = (*env)->GetFieldID(env, the_class, "cleaner", "Lsun/misc/Cleaner;")) == NULL) {
        fprintf(stderr, "FATAL: Field cleaner in java.nio.DirectByteBuffer not found.");
        fflush(stderr);
        return JNI_ERR;
      }
    } else {
      /* jdk.internal */
      if ((CLEANER = (*env)->FindClass(env, "jdk/internal/ref/Cleaner")) == NULL) {
        fprintf(stderr, "FATAL: Class jdk.internal.ref.Cleaner not found.");
        fflush(stderr);
        return JNI_ERR;
      }
      if ((CLEANER_FID = (*env)->GetFieldID(env, the_class, "cleaner", "Ljdk/internal/ref/Cleaner;")) == NULL) {
        fprintf(stderr, "FATAL: Field cleaner in java.nio.DirectByteBuffer not found.");
        fflush(stderr);
        return JNI_ERR;
      }
    }
    if ((CLEAN_MID = (*env)->GetMethodID(env, CLEANER, "clean", "()V")) == NULL) {
      fprintf(stderr, "FATAL: Field cleaner not found in java.nio.DirectByteBuffer.");
      fflush(stderr);
      return JNI_ERR;
    }
  }
  return VERSION;
}