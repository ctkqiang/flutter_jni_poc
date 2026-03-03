package xin.ctkqiang.flutter_jni_poc

class AndroidShellHolder {
    fun getPointer(): Long {
        return System.identityHashCode(this).toLong()
    }
}