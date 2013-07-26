LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := KatanaNative
LOCAL_SRC_FILES := split.c join.c
LOCAL_CFLAGS += -O2
LOCAL_LDLIBS += -lm

include $(BUILD_SHARED_LIBRARY)