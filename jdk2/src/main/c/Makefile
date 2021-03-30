# SPDX-FileCopyrightText: 2020-2021 Memories Project
#
# SPDX-License-Identifier: Apache-2.0

RM?=rm

LIBRARY_FLAGS = -D HAVE_MEMORIES_H -O -std=c89 -Wall

ifeq ($(OS),Windows_NT)
    LIBRARY_FLAGS += -shared -I "$(JAVA_HOME)/include" -I "$(JAVA_HOME)/include/win32" -Wl,--export-all-symbols -Wl,--add-stdcall-alias -Wno-ignored-attributes
    TARGET = ../resources/native/memories/jni/windows
    ifeq ($(PROCESSOR_ARCHITEW6432),AMD64)
        UNAME_M := 'x86_64'
    else
        ifeq ($(PROCESSOR_ARCHITECTURE),AMD64)
            UNAME_M := 'x86_64'
        endif
        ifeq ($(PROCESSOR_ARCHITECTURE),x86)
            UNAME_M := 'x86'
        endif
    endif
else
	LIBRARY_FLAGS += -Werror
    UNAME_S := $(shell uname -s)
    UNAME_M := $(shell uname -m)
    ifeq ($(UNAME_S),Linux)
        LIBRARY_FLAGS += -shared -fPIC -I "$(JAVA_HOME)/include" -I "$(JAVA_HOME)/include/linux"
        TARGET = "../resources/native/memories/jni/linux-$(UNAME_M)"
        $(shell mkdir -p "$(TARGET)")
        $(shell ls "$(TARGET)")
    endif
    ifeq ($(UNAME_S),Darwin)
        LIBRARY_FLAGS += -dynamiclib -I "$(JAVA_HOME)/include" -I "$(JAVA_HOME)/include/darwin"
        TARGET = "../resources/native/memories/jni/darwin-$(UNAME_M)"
        $(shell mkdir -p "$(TARGET)")
        $(shell ls "$(TARGET)")
    endif
endif

LIBRARY_FLAGS += -I "include"

all:
    $(CC) ${LIBRARY_FLAGS} $(CFLAGS) $(LDFLAGS) -o ${TARGET}/memories.jnilib memories.c memory.c memory_allocator.c