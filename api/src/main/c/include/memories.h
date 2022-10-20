/*
 * SPDX-FileCopyrightText: 2020-2021 Memories Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

/* ignore unused variable */
#if !defined(UNUSED)
 #if defined(__GNUC__)
  #define UNUSED(x) UNUSED_ ## x __attribute__((unused))
 #elif defined(__LCLINT__)
  #define UNUSED(x) /*@unused@*/ x
 #else
  #define UNUSED(x) x
 #endif
#endif /* !defined(UNUSED) */
#define UNUSED_ENV(X) UNUSED(X)


/* memory allocator */
#if defined(__sun__) || defined(_AIX) || defined(__linux__)
#  include <alloca.h>
#endif
#if defined(_WIN32)
  #if defined(_MSC_VER)
    #define alloca _alloca
    #pragma warning( disable : 4152 ) /* function/data conversion */
    #pragma warning( disable : 4054 ) /* cast function pointer to data pointer */
    #pragma warning( disable : 4055 ) /* cast data pointer to function pointer */
    #pragma warning( disable : 4204 ) /* structure initializer */
    #pragma warning( disable : 4710 ) /* swprintf not inlined */
    #pragma warning( disable : 4201 ) /* nameless struct/union (jni_md.h) */
  #else
#   include <malloc.h>
  #endif /* _MSC_VER */
#else
  #if !defined(_XOPEN_SOURCE) /* AIX power-aix 1 7 00F84C0C4C00 defins 700 */
    #define _XOPEN_SOURCE 600
  #endif
#endif /* _WIN32 */


/* pointer/address converter */
#if defined(SOLARIS2) || defined(__GNUC__)
  #if defined(_WIN64)
    #define L2A(X) ((void *)(long long)(X))
    #define A2L(X) ((jlong)(long long)(X))
  #else
    #define L2A(X) ((void *)(unsigned long)(X))
    #define A2L(X) ((jlong)(unsigned long)(X))
  #endif
#endif
#if defined(_MSC_VER)
  #include "snprintf.h"
  #define STRDUP _strdup
  #if defined(_WIN64)
    #define L2A(X) ((void *)(X))
    #define A2L(X) ((jlong)(X))
  #else
    #define L2A(X) ((void *)(unsigned long)(X))
    #define A2L(X) ((jlong)(unsigned long)(X))
  #endif
#else
  #include <stdio.h>
  #define STRDUP strdup
#endif

extern jint VERSION;
extern jclass CLEANER;
extern jfieldID CLEANER_FID;
extern jmethodID CLEAN_MID;

int memory_register_native_methods(JNIEnv *env);

int memory_allocator_register_native_methods(JNIEnv *env);

int memory_file_register_native_methods(JNIEnv *env);
