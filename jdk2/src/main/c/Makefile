# SPDX-FileCopyrightText: 2020-2021 Memories Project
#
# SPDX-License-Identifier: Apache-2.0

RM?=rm

LIBRARY_FLAGS = -D HAVE_MEMORIES_H -O -std=c89 -Wall

ifeq ($(OS),Windows_NT)
	LIBRARY_FLAGS += -shared -I "$(JAVA_HOME)/include" -I "$(JAVA_HOME)/include/win32" -Wl,--export-all-symbols -Wl,--add-stdcall-alias
	TARGET = ../resources/native/memories/jni/windows
	LIBRARY_FLAGS += -Wno-ignored-attributes
else
	LIBRARY_FLAGS += -Werror
    UNAME_S := $(shell uname -s)
    ifeq ($(UNAME_S),Linux)
        LIBRARY_FLAGS += -shared -fPIC -I "$(JAVA_HOME)/include" -I "$(JAVA_HOME)/include/linux"
		TARGET = ../resources/native/memories/jni/linux
    endif
    ifeq ($(UNAME_S),Darwin)
        LIBRARY_FLAGS += -dynamiclib -I "$(JAVA_HOME)/include" -I "$(JAVA_HOME)/include/darwin"
		TARGET = ../resources/native/memories/jni/darwin
    endif
endif

LIBRARY_FLAGS += -I "include"

all: x86_64 x86

x86_64:
	$(CC) ${LIBRARY_FLAGS} -m64 $(CFLAGS) $(LDFLAGS) -o ${TARGET}-x86-64/memories.jnilib memories.c memory.c memory_allocator.c

x86:
	$(CC) ${LIBRARY_FLAGS} -m32 $(CFLAGS) $(LDFLAGS) -o ${TARGET}-x86/memories.jnilib memories.c memory.c memory_allocator.c
