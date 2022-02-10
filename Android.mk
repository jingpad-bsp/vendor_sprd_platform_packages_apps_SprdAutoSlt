
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

#LOCAL_MODULE_TAGS := optional
LOCAL_PRIVATE_PLATFORM_APIS := true
#LOCAL_MODULE_TAGS := tests
LOCAL_JAVA_LIBRARIES := telephony-common
LOCAL_JAVA_LIBRARIES += radio_interactor_common
LOCAL_JAVA_LIBRARIES += com.broadcom.bt
LOCAL_SRC_FILES := $(call all-java-files-under, src)

#LOCAL_DEX_PREOPT := false

LOCAL_PACKAGE_NAME := SprdAutoSlt
LOCAL_MULTILIB := both
LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4
LOCAL_STATIC_JAVA_LIBRARIES += zixing_core
LOCAL_STATIC_JAVA_LIBRARIES += Fpextservicejar

#LOCAL_JNI_SHARED_LIBRARIES := libfmjni
LOCAL_JNI_SHARED_LIBRARIES += libjni_sprdautoslt
LOCAL_JNI_SHARED_LIBRARIES += libjni_sprdautofingerprint
#LOCAL_JNI_SHARED_LIBRARIES += libjni_sprdautodualcameraverify

LOCAL_STATIC_JAVA_LIBRARIES += vendor.sprd.hardware.fingerprintmmi-V1.0-java

LOCAL_SHARED_LIBRARIES := libc++ \
                          librilutils

LOCAL_STATIC_LIBRARIES := libatci
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_ENABLED := disabled

#LOCAL_REQUIRED_MODULES := libCameraVerfication libopencv_java3
#LOCAL_JNI_SHARED_LIBRARIES += libCameraVerfication
#LOCAL_JNI_SHARED_LIBRARIES += libopencv_java3



include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

#LOCAL_STATIC_JAVA_LIBRARIES += com.broadcom.bt
#LOCAL_PREBUILT_LIBS += libjni_at:libs/armeabi/libjni_at.so
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := zixing_core:libs/zixing_core.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += Fpextservicejar:libs/FpExtensionService.jar
LOCAL_MODULE_TAGS := optional

#ifeq ($(strip $(TARGET_ARCH)),arm64)
#LOCAL_MULTILIB := both
#LOCAL_PREBUILT_LIBS := libCameraVerfication:libs/arm64-v8a/libCameraVerfication.so
#LOCAL_PREBUILT_LIBS += libopencv_java3:libs/arm64-v8a/libopencv_java3.so
#else
#ifeq ($(strip $(TARGET_ARCH)),x86_64)
#$(warning "libjni_dualcameraverify x86_64")
#LOCAL_MULTILIB := 64
#LOCAL_PREBUILT_LIBS := libCameraVerfication:libs/x86_64/libCameraVerfication.so
#LOCAL_PREBUILT_LIBS += libopencv_java3:libs/x86_64/libopencv_java3.so
#else
#LOCAL_MULTILIB := 32
#LOCAL_PREBUILT_LIBS := libCameraVerfication:libs/armeabi-v7a/libCameraVerfication.so
#LOCAL_PREBUILT_LIBS += libopencv_java3:libs/armeabi-v7a/libopencv_java3.so
#endif
#endif

include $(BUILD_MULTI_PREBUILT) 
# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))

