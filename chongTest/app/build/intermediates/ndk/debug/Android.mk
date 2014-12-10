LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := app
LOCAL_SRC_FILES := \
	C:\Android\RoboApp_master\app\src\main\jni\Android.mk \
	C:\Android\RoboApp_master\app\src\main\jni\Application.mk \
	C:\Android\RoboApp_master\app\src\main\jni\DetectionBasedTracker_jni.cpp \

LOCAL_C_INCLUDES += C:\Android\RoboApp_master\app\src\main\jni
LOCAL_C_INCLUDES += C:\Android\RoboApp_master\app\src\debug\jni

include $(BUILD_SHARED_LIBRARY)
