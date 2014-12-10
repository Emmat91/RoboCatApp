LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := Detection
LOCAL_SRC_FILES := \
	C:\android\RoboApp_master\app\src\main\jni\Android.mk \
	C:\android\RoboApp_master\app\src\main\jni\Application.mk \
	C:\android\RoboApp_master\app\src\main\jni\DetectionBasedTracker_jni.cpp \

LOCAL_C_INCLUDES += C:\android\RoboApp_master\app\src\main\jni
LOCAL_C_INCLUDES += C:\android\RoboApp_master\app\src\release\jni

include $(BUILD_SHARED_LIBRARY)
