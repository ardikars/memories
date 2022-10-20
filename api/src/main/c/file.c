#include <stdio.h>
#include <sys/mman.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <jni.h>
#include <memories.h>

JNIEXPORT jint nativeOpen(JNIEnv *env, jclass UNUSED(j_cls), jstring jpathname) {
  const char* pathname = (*env)->GetStringUTFChars(env, jpathname, 0);
  int fd = open(pathname, O_RDWR);
  (*env)->ReleaseStringUTFChars(env, jpathname, pathname);
  return (jint) fd;
}

JNIEXPORT void nativeClose(JNIEnv *env, jclass UNUSED(j_cls), jint jfd) {
    close(jfd);
}

JNIEXPORT long nativeMemoryMapping(JNIEnv *env, jclass UNUSED(j_cls), jlong jaddress, jlong jlength, jint jprot, jint jflags, jint jfd, jlong joffset) {
  void *address;
  if (jaddress == 0) {
    address = NULL;
  } else {
    address = L2A(jaddress);
  }
  void *ptr = mmap(address, jlength, jprot, jflags, jfd, joffset);
  return A2L(ptr);
}

JNIEXPORT jint nativeMemoryUnMapping(JNIEnv *env, jclass UNUSED(j_cls), jlong jaddress, jlong jlength) {
  void *address = L2A(jaddress);
  return munmap(address, jlength);
}

JNIEXPORT jobject nativeStatus(JNIEnv *env, jclass UNUSED(j_cls), jint jfd) {
  struct stat statbuf;
  int err = fstat(jfd, &statbuf);
  if(err < 0){
    return NULL;
  }
  jclass jcls = (*env)->FindClass(env, "memories/api/FileApi$Status");
  jmethodID jmid = (*env)->GetMethodID(env, jcls, "<init>", "(J)V");
  return (*env)->NewObject(env, jcls, jmid, (jlong) statbuf.st_size);
}

JNIEXPORT jlong nativePageSize(JNIEnv *env, jclass UNUSED(j_cls)) {
  return sysconf(_SC_PAGESIZE);
}

JNIEXPORT jint nativeMemorySync(JNIEnv *env, jclass UNUSED(j_cls), jlong j_addr, jlong j_length, jint j_flags) {
  return msync(L2A(j_addr), j_length, j_flags);
}

int memory_file_register_native_methods(JNIEnv *env) {
  jclass cls;
  if ((cls = (*env)->FindClass(env, "memories/api/FileApi$NativeFile")) == NULL) {
    fprintf(stderr, "FATAL: Class memories.api.FileApi$NativeFile not found.");
    fflush(stderr);
    return JNI_ERR;
  }
  const JNINativeMethod methods[] = {
    {"nativeOpen","(Ljava/lang/String;)I",(void *) nativeOpen},
    {"nativeClose","(I)V",(void *) nativeClose},
    {"nativePageSize","()J",(void *) nativePageSize},
    {"nativeMemoryMapping","(JJIIIJ)J",(void *) nativeMemoryMapping},
    {"nativeMemoryUnMapping","(JJ)I",(void *) nativeMemoryUnMapping},
    {"nativeMemorySync","(JJI)I",(void *) nativeMemorySync},
    {"nativeStatus","(I)Lmemories/api/FileApi$Status;",(void *) nativeStatus}
  };
  return (*env)->RegisterNatives(env, cls, methods, sizeof(methods) / sizeof(methods[0]));
}
