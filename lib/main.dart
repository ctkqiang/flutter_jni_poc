import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const JNIVulnerabilityTestApp());
}

class JNIVulnerabilityTestApp extends StatelessWidget {
  const JNIVulnerabilityTestApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'JNI 漏洞测试',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
        useMaterial3: true,
        brightness: Brightness.light,
        colorSchemeSeed: Colors.blue,
      ),
      home: const TestPage(),
    );
  }
}

class TestPage extends StatefulWidget {
  const TestPage({super.key});

  @override
  State<TestPage> createState() => _TestPageState();
}

class _TestPageState extends State<TestPage> {
  String _statusInfo = '待机';
  Color _statusIndicator = Colors.grey;
  bool _busy = false;
  String _lastOperation = '';
  int _operationCount = 0;
  int _successCount = 0;
  int _failureCount = 0;
  String _lastError = '';

  static const platform = MethodChannel('flutter_jni_poc');

  void _executeNormalCall() async {
    setState(() {
      _busy = true;
      _statusInfo = '执行 JNI nativeDestroy 有效指针';
      _statusIndicator = Colors.blue;
      _lastOperation = '正常';
    });

    try {
      // 调用原生方法，传入有效指针 (1 表示有效指针)
      await platform.invokeMethod('nativeDestroy', {'pointer': 1});
      setState(() {
        _busy = false;
        _statusInfo = '操作完成：有效指针已处理';
        _statusIndicator = Colors.green;
        _operationCount++;
        _successCount++;
        _lastError = '';
      });
    } catch (e) {
      setState(() {
        _busy = false;
        _statusInfo = '操作失败';
        _statusIndicator = Colors.red;
        _operationCount++;
        _failureCount++;
        _lastError = e.toString();
      });
    }
  }

  void _executeMaliciousCall() async {
    setState(() {
      _busy = true;
      _statusInfo = '执行 JNI nativeDestroy 伪造指针';
      _statusIndicator = Colors.orange;
      _lastOperation = '恶意';
    });

    try {
      // 调用原生方法，传入伪造指针 (0 表示伪造指针)
      await platform.invokeMethod('nativeDestroy', {'pointer': 0});
      setState(() {
        _busy = false;
        _statusInfo = '操作完成：触发崩溃';
        _statusIndicator = Colors.red;
        _operationCount++;
        _failureCount++;
        _lastError = '空指针解引用崩溃';
      });
    } catch (e) {
      setState(() {
        _busy = false;
        _statusInfo = '操作失败';
        _statusIndicator = Colors.red;
        _operationCount++;
        _failureCount++;
        _lastError = e.toString();
      });
    }
  }

  void _resetStats() {
    setState(() {
      _operationCount = 0;
      _successCount = 0;
      _failureCount = 0;
      _lastOperation = '';
      _lastError = '';
      _statusInfo = '待机';
      _statusIndicator = Colors.grey;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('JNI 漏洞测试工具'),
        centerTitle: true,
        elevation: 4,
        actions: [
          IconButton(
            onPressed: _resetStats,
            icon: const Icon(Icons.refresh),
            tooltip: '重置统计',
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // 测试配置卡片
            Card(
              elevation: 4,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text(
                          '测试配置',
                          style: TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 12,
                            vertical: 4,
                          ),
                          decoration: BoxDecoration(
                            color: Colors.blue.withValues(alpha: 0.1),
                            borderRadius: BorderRadius.circular(16),
                            border: Border.all(color: Colors.blue, width: 1),
                          ),
                          child: const Text(
                            'v1.0.0',
                            style: TextStyle(
                              fontSize: 12,
                              color: Colors.blue,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    GridView(
                      // GridView 用于创建网格布局
                      shrinkWrap: true, // 使 GridView 适应内容高度
                      physics:
                          const NeverScrollableScrollPhysics(), // 禁用 GridView 滚动
                      gridDelegate:
                          const SliverGridDelegateWithFixedCrossAxisCount(
                            crossAxisCount: 2, // 2 列
                            crossAxisSpacing: 16, // 列间距
                            mainAxisSpacing: 12, // 行间距
                            childAspectRatio: 2.5, // 子项宽高比
                          ),
                      children: [
                        _buildInfoItem(
                          '目标函数',
                          'nativeDestroy(JNIEnv*, jobject, jlong)',
                        ),
                        _buildInfoItem('操作数', '$_operationCount'),
                        _buildInfoItem('ABI', 'arm64-v8a'),
                        _buildInfoItem(
                          '上次操作',
                          _lastOperation.isEmpty ? '无' : _lastOperation,
                        ),
                        _buildInfoItem('成功数', '$_successCount'),
                        _buildInfoItem('失败数', '$_failureCount'),
                      ],
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 20),

            // 系统状态卡片
            Card(
              elevation: 4,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      '系统状态',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 16),
                    Container(
                      width: double.infinity,
                      padding: const EdgeInsets.all(20),
                      decoration: BoxDecoration(
                        color: _statusIndicator.withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(color: _statusIndicator, width: 1),
                      ),
                      child: Column(
                        children: [
                          if (_busy)
                            const CircularProgressIndicator(strokeWidth: 3)
                          else
                            Icon(
                              _statusIndicator == Colors.green
                                  ? Icons.check_circle
                                  : _statusIndicator == Colors.red
                                  ? Icons.error
                                  : _statusIndicator == Colors.orange
                                  ? Icons.warning
                                  : Icons.info,
                              color: _statusIndicator,
                              size: 48,
                            ),
                          const SizedBox(height: 16),
                          Text(
                            _statusInfo,
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.w500,
                              color: _statusIndicator,
                            ),
                            textAlign: TextAlign.center,
                          ),
                          if (_lastError.isNotEmpty)
                            Padding(
                              padding: const EdgeInsets.only(top: 8),
                              child: Text(
                                '错误: $_lastError',
                                style: const TextStyle(
                                  fontSize: 14,
                                  color: Colors.red,
                                  fontWeight: FontWeight.w400,
                                ),
                                textAlign: TextAlign.center,
                              ),
                            ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 20),

            // 测试控制卡片
            Card(
              elevation: 4,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      '测试控制',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 16),
                    Row(
                      children: [
                        Expanded(
                          child: ElevatedButton.icon(
                            onPressed: _busy ? null : _executeNormalCall,
                            icon: const Icon(Icons.check_circle),
                            label: const Text('有效指针'),
                            style: ElevatedButton.styleFrom(
                              padding: const EdgeInsets.symmetric(vertical: 16),
                              backgroundColor: Colors.blue,
                              foregroundColor: Colors.white,
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(8),
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: ElevatedButton.icon(
                            onPressed: _busy ? null : _executeMaliciousCall,
                            icon: const Icon(Icons.error_outline),
                            label: const Text('伪造指针'),
                            style: ElevatedButton.styleFrom(
                              padding: const EdgeInsets.symmetric(vertical: 16),
                              backgroundColor: Colors.red,
                              foregroundColor: Colors.white,
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(8),
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    if (_busy)
                      LinearProgressIndicator(
                        backgroundColor: Colors.grey[200],
                        valueColor: const AlwaysStoppedAnimation<Color>(
                          Colors.blue,
                        ),
                        minHeight: 6,
                      )
                    else
                      const SizedBox(),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 20),

            // 技术细节卡片
            Card(
              elevation: 4,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      '技术细节',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 12),
                    const Divider(),
                    const SizedBox(height: 16),
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                '函数签名',
                                style: TextStyle(
                                  fontSize: 14,
                                  fontWeight: FontWeight.w500,
                                  color: Colors.grey[600],
                                ),
                              ),
                              SizedBox(height: 4),
                              Text(
                                'Java_xin_ctkqiang_flutter_1jni_1poc_MainActivity_nativeDestroy__J',
                                style: TextStyle(
                                  fontSize: 12,
                                  fontFamily: 'Monospace',
                                  color: Colors.black87,
                                ),
                                softWrap: true,
                              ),
                            ],
                          ),
                        ),
                        const SizedBox(width: 20),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                '漏洞类型',
                                style: TextStyle(
                                  fontSize: 14,
                                  fontWeight: FontWeight.w500,
                                  color: Colors.grey[600],
                                ),
                              ),
                              const SizedBox(height: 4),
                              Text(
                                '空指针解引用',
                                style: TextStyle(
                                  fontSize: 14,
                                  color: Colors.black87,
                                ),
                              ),
                              const SizedBox(height: 8),
                              Text(
                                '风险等级',
                                style: TextStyle(
                                  fontSize: 14,
                                  fontWeight: FontWeight.w500,
                                  color: Colors.grey[600],
                                ),
                              ),
                              const SizedBox(height: 4),
                              Text(
                                '高',
                                style: TextStyle(
                                  fontSize: 14,
                                  color: Colors.red,
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    const Divider(),
                    const SizedBox(height: 16),
                    Text(
                      '缓解策略:',
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w500,
                        color: Colors.grey[600],
                      ),
                    ),
                    const SizedBox(height: 8),
                    const Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('1. 解引用前验证指针非空'),
                        Text('2. 实现内存访问边界检查'),
                        Text('3. 使用 JNIEnv 辅助函数进行安全操作'),
                        Text('4. 实现异常处理机制'),
                      ],
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 30),
          ],
        ),
      ),
    );
  }

  Widget _buildInfoItem(String label, String value) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: TextStyle(
            fontSize: 12,
            color: Colors.grey[600],
            fontWeight: FontWeight.w500,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          value,
          style: const TextStyle(
            fontSize: 14,
            color: Colors.black87,
            fontWeight: FontWeight.w400,
          ),
        ),
      ],
    );
  }
}
