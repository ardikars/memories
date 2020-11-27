/*
 * SPDX-FileCopyrightText: 2020 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <jni.h>
#include <memories.h>

/** Throw an exception by name */
void throwByName(JNIEnv *env, const char *name, const char *msg) {
  jclass cls;
  (*env)->ExceptionClear(env);
  cls = (*env)->FindClass(env, name);
  if (cls != NULL) { /* Otherwise an exception has already been thrown */
    (*env)->ThrowNew(env, cls, msg);
    /* It's a good practice to clean up the local references. */
    (*env)->DeleteLocalRef(env, cls);
  }
}