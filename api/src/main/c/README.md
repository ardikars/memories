### Build native library with NDK

```bash
cd $MEMORIES_PROJECT/api/src/main/c
NDK_PROJECT_PATH=$MEMORIES_PROJECT/api/src/main/c NDK_LIBS_OUT=$MEMORIES_PROJECT/api/src/main/resources/libs ndk-build
```