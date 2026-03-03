package xin.ctkqiang.flutter_jni_poc

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.util.Random

class MainActivity : AppCompatActivity() {

    init {
        System.loadLibrary("native_lib")
    }

    external fun nativeDestroy(shellHolder: Long)

    private lateinit var tvStatus: TextView
    private lateinit var tvStats: TextView
    private lateinit var tvLog: TextView
    private lateinit var btnNormal: Button
    private lateinit var btnEvil: Button
    private lateinit var btnRandom: Button
    private lateinit var btnSequence: Button
    private lateinit var btnMemoryTest: Button
    private var operationCount = 0
    private var successCount = 0
    private var failureCount = 0
    private val random = Random()
    private var memoryTestRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置标题
        title = "JNI 漏洞测试工具"
        
        // 创建原生 UI 布局
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)
        
        // 处理窗口 insets，确保内容不会被系统 UI 遮挡
        ViewCompat.setOnApplyWindowInsetsListener(layout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft + systemBars.left,
                v.paddingTop + systemBars.top,
                v.paddingRight + systemBars.right,
                v.paddingBottom + systemBars.bottom
            )
            insets
        }
        
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
        
        // 日志显示
        tvLog = TextView(this)
        tvLog.text = "日志: 待机状态"
        tvLog.textSize = 12f
        tvLog.setTextColor(resources.getColor(android.R.color.darker_gray))
        tvLog.setPadding(0, 0, 0, 24)
        tvLog.setSingleLine(false)
        tvLog.maxLines = 5
        layout.addView(tvLog)
        
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
        btnSequence.setMargin(0, 0, 0, 12)
        buttonLayout.addView(btnSequence)
        
        // 内存测试按钮
        btnMemoryTest = Button(this)
        btnMemoryTest.text = "内存测试"
        btnMemoryTest.setBackgroundColor(resources.getColor(android.R.color.holo_purple))
        btnMemoryTest.setTextColor(resources.getColor(android.R.color.white))
        btnMemoryTest.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnMemoryTest.setPadding(0, 16, 0, 16)
        buttonLayout.addView(btnMemoryTest)
        
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
        
        // 为伪造按钮添加长按自动执行功能（永不自动停止）
        var longPressHandler: android.os.Handler? = null
        val longPressRunnable = object : Runnable {
            override fun run() {
                executeEvilCall()
                updateStats(tvStats)
                longPressHandler?.postDelayed(this, 200) // 每200毫秒执行一次
            }
        }
        
        btnEvil.setOnLongClickListener {
            longPressHandler = android.os.Handler()
            longPressHandler?.post(longPressRunnable)
            true
        }
        
        // 监听触摸事件，当手指抬起时停止自动执行
        btnEvil.setOnTouchListener {
            v, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP || event.action == android.view.MotionEvent.ACTION_CANCEL) {
                longPressHandler?.removeCallbacks(longPressRunnable)
            }
            false
        }
        
        btnRandom.setOnClickListener {
            executeRandomCall()
            updateStats(tvStats)
        }
        
        btnSequence.setOnClickListener {
            executeSequenceCall()
            updateStats(tvStats)
        }
        
        btnMemoryTest.setOnClickListener {
            if (!memoryTestRunning) {
                executeMemoryTest()
            } else {
                stopMemoryTest()
            }
        }
    }
    
    private fun executeNormalCall() {
        val holder = AndroidShellHolder()
        val validPtr = holder.getPointer()
        
        tvStatus.text = "执行中：有效指针 $validPtr"
        updateLog("测试有效指针: $validPtr")
        
        try {
            nativeDestroy(validPtr)
            tvStatus.text = "成功：有效指针已处理"
            operationCount++
            successCount++
            updateLog("有效指针测试成功: $validPtr")
            Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            tvStatus.text = "失败：${e.message}"
            operationCount++
            failureCount++
            updateLog("有效指针测试失败: $validPtr, 错误: ${e.message}")
            Toast.makeText(this, "操作失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun executeEvilCall() {
        val fakePtr = 0x12345678L
        
        tvStatus.text = "执行中：伪造指针 $fakePtr"
        updateLog("测试伪造指针: $fakePtr")
        
        try {
            nativeDestroy(fakePtr)
            tvStatus.text = "成功：伪造指针已处理"
            operationCount++
            successCount++
            updateLog("伪造指针测试成功: $fakePtr")
            Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            tvStatus.text = "失败：${e.message}"
            operationCount++
            failureCount++
            updateLog("伪造指针测试失败: $fakePtr, 错误: ${e.message}")
            Toast.makeText(this, "操作失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun executeRandomCall() {
        val randomPtr = random.nextLong()
        
        tvStatus.text = "执行中：随机指针 $randomPtr"
        updateLog("测试随机指针: $randomPtr")
        
        try {
            nativeDestroy(randomPtr)
            tvStatus.text = "成功：随机指针已处理"
            operationCount++
            successCount++
            updateLog("随机指针测试成功: $randomPtr")
            Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            tvStatus.text = "失败：${e.message}"
            operationCount++
            failureCount++
            updateLog("随机指针测试失败: $randomPtr, 错误: ${e.message}")
            Toast.makeText(this, "操作失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun executeSequenceCall() {
        tvStatus.text = "执行中：序列测试开始"
        updateLog("序列测试开始，共测试 ${7}个指针")
        
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
                    updateLog("测试指针: $ptr")
                }
                
                try {
                    nativeDestroy(ptr)
                    runOnUiThread {
                        updateLog("指针测试成功: $ptr")
                        successCount++
                    }
                    Thread.sleep(500)
                } catch (e: Exception) {
                    runOnUiThread {
                        updateLog("指针测试失败: $ptr, 错误: ${e.message}")
                        failureCount++
                    }
                    Thread.sleep(500)
                }
            }
            
            runOnUiThread {
                tvStatus.text = "序列测试完成"
                operationCount += testValues.size
                updateStats(tvStats)
                updateLog("序列测试完成，共测试 ${testValues.size}个指针")
                Toast.makeText(this, "序列测试完成", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }
    
    private fun executeMemoryTest() {
        memoryTestRunning = true
        tvStatus.text = "执行中：内存测试开始"
        updateLog("内存测试开始，持续执行直到达到最大内存")
        
        Thread {
            while (memoryTestRunning) {
                val fakePtr = random.nextLong()
                
                runOnUiThread {
                    tvStatus.text = "执行中：内存测试 $fakePtr"
                    updateLog("内存测试指针: $fakePtr")
                }
                
                try {
                    nativeDestroy(fakePtr)
                    runOnUiThread {
                        operationCount++
                        successCount++
                        updateStats(tvStats)
                        updateLog("内存测试成功: $fakePtr")
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        operationCount++
                        failureCount++
                        updateStats(tvStats)
                        updateLog("内存测试失败: $fakePtr, 错误: ${e.message}")
                    }
                }
                
                // 短暂休眠，避免过于频繁的操作
                Thread.sleep(50)
            }
            
            runOnUiThread {
                tvStatus.text = "内存测试已停止"
                updateLog("内存测试已停止")
                Toast.makeText(this, "内存测试已停止", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }
    
    private fun stopMemoryTest() {
        memoryTestRunning = false
        tvStatus.text = "停止中：内存测试"
        updateLog("正在停止内存测试...")
    }
    
    private fun updateStats(tvStats: TextView) {
        tvStats.text = "操作数: $operationCount | 成功: $successCount | 失败: $failureCount"
    }
    
    private fun updateLog(message: String) {
        val memoryInfo = getMemoryInfo()
        tvLog.text = "日志: $message\n内存: $memoryInfo"
    }
    
    private fun getMemoryInfo(): String {
        val runtime = Runtime.getRuntime()
        val usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
        val maxMemInMB = runtime.maxMemory() / 1048576L
        return "使用: ${usedMemInMB}MB / 最大: ${maxMemInMB}MB"
    }
    
    private fun resetStats() {
        operationCount = 0
        successCount = 0
        failureCount = 0
        tvStatus.text = "JNI 漏洞测试工具\n待机状态"
        updateStats(tvStats)
        updateLog("统计已重置")
        Toast.makeText(this, "统计已重置", Toast.LENGTH_SHORT).show()
    }
    
    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_reset -> {
                resetStats()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun View.setMargin(left: Int, top: Int, right: Int, bottom: Int) {
        if (layoutParams is LinearLayout.LayoutParams) {
            val params = layoutParams as LinearLayout.LayoutParams
            params.setMargins(left, top, right, bottom)
            this.layoutParams = params
        }
    }
}
