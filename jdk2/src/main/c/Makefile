# SPDX-FileCopyrightText: 2020-2021 Memories Project
#
# SPDX-License-Identifier: Apache-2.0


LIBRARY_FLAGS = -D HAVE_MEMORIES_H -O -std=c89 -Wall

ifeq ($(OS),Windows_NT)
    LIBRARY_FLAGS += -shared -I "$(JAVA_HOME)/include" -I "$(JAVA_HOME)/include/win32" -Wl,--export-all-symbols -Wl,--add-stdcall-alias -Wno-ignored-attributes
    TARGET = ../resources/native/memories/jni/windows
else
    LIBRARY_FLAGS += -Werror
    UNAME_S := $(shell uname -s)
    UNAME_M := ${ARCH}
    ifeq ($(UNAME_S),Linux)
        LIBRARY_FLAGS += -shared -fPIC -I "$(JAVA_HOME)/include" -I "$(JAVA_HOME)/include/linux"
        TARGET = ../resources/native/memories/jni/linux-$(UNAME_M)
    endif
    ifeq ($(UNAME_S),Darwin)
        LIBRARY_FLAGS += -dynamiclib -I "$(JAVA_HOME)/include" -I "$(JAVA_HOME)/include/darwin"
        TARGET = ../resources/native/memories/jni/darwin-$(UNAME_M)
    endif
    $(shell mkdir -p ${TARGET})
endif

LIBRARY_FLAGS += -I "include"

all: build

build:
	$(CC) ${LIBRARY_FLAGS} $(CFLAGS) $(LDFLAGS) -o ${TARGET}/memories.jnilib memories.c memory.c memory_allocator.c
