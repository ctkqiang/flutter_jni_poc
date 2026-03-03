#include <jni.h>
#include <android/log.h>
#include <cstdint>

#define LOG_TAG "JNIPOC"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

class AndroidShellHolder {
public:
    void DestroyShell() {
        LOGI("[漏洞触发] DestroyShell 被调用！当前对象指针: %p", this);
    }
};

extern "C" {

JNIEXPORT void JNICALL
Java_xin_ctkqiang_flutter_1jni_1poc_MainActivity_nativeDestroy__J(JNIEnv* env, jobject thiz, jlong shell_holder) {
    AndroidShellHolder* holder = reinterpret_cast<AndroidShellHolder*>(shell_holder);
    
    holder->DestroyShell();
    
    
}

} // extern "C"