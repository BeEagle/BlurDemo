LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_LDLIBS := -llog
LOCAL_LDFLAGS += -ljnigraphics
LOCAL_MODULE    := NativeFastBlur
LOCAL_SRC_FILES := blur.c

include $(BUILD_SHARED_LIBRARY)