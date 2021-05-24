/*
 * SPDX-FileCopyrightText: 2020-2021 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <jni.h>
#include <memories.h>

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  JNIEnv* env = NULL;
  if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_2) != JNI_OK) {
    fprintf(stderr, "FATAL: JNI version missmatch");
    fflush(stderr);
    return JNI_ERR;
  }
  if (memory_register_native_methods(env) != JNI_OK) {
    return JNI_ERR;
  }
  if (memory_allocator_register_native_methods(env) != JNI_OK) {
    return JNI_ERR;
  }
  return JNI_VERSION_1_2;
}