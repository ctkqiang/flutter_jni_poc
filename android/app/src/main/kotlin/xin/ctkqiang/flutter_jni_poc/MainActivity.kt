package xin.ctkqiang.flutter_jni_poc

import io.flutter.embedding.android.FlutterActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    init {
        System.loadLibrary("native_lib")
    }

    external fun nativeDestroy(shellHolder: Long)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvStatus = findViewById<TextView>(R.id.tv_status)
        val btnNormal = findViewById<Button>(R.id.btn_normal)
        val btnEvil = findViewById<Button>(R.id.btn_evil)

        btnNormal.setOnClickListener {
            val holder = AndroidShellHolder()
            val validPtr = holder.getPointer()
            
            tvStatus.text = "正常调用：传入有效指针 $validPtr"
            nativeDestroy(validPtr)
        }

        btnEvil.setOnClickListener {
            val fakePtr = 0x12345678L
            tvStatus.text = "恶意调用：传入伪造指针 $fakePtr"
            
            nativeDestroy(fakePtr)
        }
    }
}