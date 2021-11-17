####
# SPDX-FileCopyrightText: 2020-2021 Memories Project
#
# SPDX-License-Identifier: Apache-2.0
####

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := memories

LOCAL_SRC_FILES := \
	../memories.c \
	../memory.c \
	../memory_allocator.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../include
include $(BUILD_SHARED_LIBRARY)
