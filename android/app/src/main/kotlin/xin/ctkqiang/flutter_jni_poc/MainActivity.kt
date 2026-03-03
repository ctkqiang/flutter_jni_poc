package xin.ctkqiang.flutter_jni_poc

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Random

class MainActivity : AppCompatActivity() {

    init {
        System.loadLibrary("native_lib")
    }

    external fun nativeDestroy(shellHolder: Long)

    private lateinit var tvStatus: TextView
    private lateinit var tvStats: TextView
    private lateinit var btnNormal: Button
    private lateinit var btnEvil: Button
    private lateinit var btnRandom: Button
    private lateinit var btnSequence: Button
    private var operationCount = 0
    private var successCount = 0
    private var failureCount = 0
    private val random = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 创建原生 UI 布局
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)
        
        // 状态显示
        tvStatus = TextView(this)
        tvStatus.text = "JNI 漏洞测试工具\n待机状态"
        tvStatus.textSize = 18f
        tvStatus.setPadding(0, 0, 0, 24)
        layout.addView(tvStatus)
        
        // 操作统计
        tvStats = TextView(this)
        tvStats.text = "操作数: 0 | 成功: 0 | 失败: 0"
        tvStats.textSize = 14f
        tvStats.setTextColor(resources.getColor(android.R.color.darker_gray))
        tvStats.setPadding(0, 0, 0, 24)
        layout.addView(tvStats)
        
        // 按钮布局
        val buttonLayout = LinearLayout(this)
        buttonLayout.orientation = LinearLayout.VERTICAL
        buttonLayout.setPadding(0, 0, 0, 16)
        
        // 有效指针按钮
        btnNormal = Button(this)
        btnNormal.text = "有效指针"
        btnNormal.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
        btnNormal.setTextColor(resources.getColor(android.R.color.white))
        btnNormal.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnNormal.setPadding(0, 16, 0, 16)
        btnNormal.setMargin(0, 0, 0, 12)
        buttonLayout.addView(btnNormal)
        
        // 伪造指针按钮
        btnEvil = Button(this)
        btnEvil.text = "伪造指针"
        btnEvil.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
        btnEvil.setTextColor(resources.getColor(android.R.color.white))
        btnEvil.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnEvil.setPadding(0, 16, 0, 16)
        btnEvil.setMargin(0, 0, 0, 12)
        buttonLayout.addView(btnEvil)
        
        // 随机指针按钮
        btnRandom = Button(this)
        btnRandom.text = "随机指针"
        btnRandom.setBackgroundColor(resources.getColor(android.R.color.holo_orange_light))
        btnRandom.setTextColor(resources.getColor(android.R.color.white))
        btnRandom.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnRandom.setPadding(0, 16, 0, 16)
        btnRandom.setMargin(0, 0, 0, 12)
        buttonLayout.addView(btnRandom)
        
        // 序列测试按钮
        btnSequence = Button(this)
        btnSequence.text = "序列测试"
        btnSequence.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
        btnSequence.setTextColor(resources.getColor(android.R.color.white))
        btnSequence.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnSequence.setPadding(0, 16, 0, 16)
        buttonLayout.addView(btnSequence)
        
        layout.addView(buttonLayout)
        
        // 设置布局
        setContentView(layout)
        
        // 设置按钮点击事件
        btnNormal.setOnClickListener {
            executeNormalCall()
            updateStats(tvStats)
        }
        
        btnEvil.setOnClickListener {
            executeEvilCall()
            updateStats(tvStats)
        }
        
        btnRandom.setOnClickListener {
            executeRandomCall()
            updateStats(tvStats)
        }
        
        btnSequence.setOnClickListener {
            executeSequenceCall()
            updateStats(tvStats)
        }
    }
    
    private fun executeNormalCall() {
        val holder = AndroidShellHolder()
        val validPtr = holder.getPointer()
        
        tvStatus.text = "执行中：有效指针 $validPtr"
        
        try {
            nativeDestroy(validPtr)
            tvStatus.text = "成功：有效指针已处理"
            operationCount++
            successCount++
            Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            tvStatus.text = "失败：${e.message}"
            operationCount++
            failureCount++
            Toast.makeText(this, "操作失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun executeEvilCall() {
        val fakePtr = 0x12345678L
        
        tvStatus.text = "执行中：伪造指针 $fakePtr"
        
        try {
            nativeDestroy(fakePtr)
            tvStatus.text = "成功：伪造指针已处理"
            operationCount++
            successCount++
            Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            tvStatus.text = "失败：${e.message}"
            operationCount++
            failureCount++
            Toast.makeText(this, "操作失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun executeRandomCall() {
        val randomPtr = random.nextLong()
        
        tvStatus.text = "执行中：随机指针 $randomPtr"
        
        try {
            nativeDestroy(randomPtr)
            tvStatus.text = "成功：随机指针已处理"
            operationCount++
            successCount++
            Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            tvStatus.text = "失败：${e.message}"
            operationCount++
            failureCount++
            Toast.makeText(this, "操作失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun executeSequenceCall() {
        tvStatus.text = "执行中：序列测试开始"
        
        Thread {
            val testValues = listOf(
                0L, // 空指针
                1L, // 最小指针
                0xFFFFFFFFL, // 最大32位指针
                0x7FFFFFFFFFFFFFFFL, // 最大64位指针
                random.nextLong(), // 随机指针
                random.nextLong(), // 另一个随机指针
                random.nextLong() // 第三个随机指针
            )
            
            for (ptr in testValues) {
                runOnUiThread {
                    tvStatus.text = "执行中：测试指针 $ptr"
                }
                
                try {
                    nativeDestroy(ptr)
                    Thread.sleep(500)
                } catch (e: Exception) {
                    Thread.sleep(500)
                }
            }
            
            runOnUiThread {
                tvStatus.text = "序列测试完成"
                operationCount += testValues.size
                Toast.makeText(this, "序列测试完成", Toast.LENGTH_SHORT).show()
                updateStats(tvStats)
            }
        }.start()
    }
    
    private fun updateStats(tvStats: TextView) {
        tvStats.text = "操作数: $operationCount | 成功: $successCount | 失败: $failureCount"
    }
    
    private fun View.setMargin(left: Int, top: Int, right: Int, bottom: Int) {
        val params = layoutParams as LinearLayout.LayoutParams
        params.setMargins(left, top, right, bottom)
        layoutParams = params
    }
}
