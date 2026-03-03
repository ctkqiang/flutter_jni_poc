import 'package:flutter/material.dart';

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
      ),
      home: const HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  String _statusMessage = '等待操作...';
  Color _statusColor = Colors.grey;
  bool _isLoading = false;

  void _simulateNormalCall() {
    setState(() {
      _isLoading = true;
      _statusMessage = '正在执行正常 JNI 调用...';
      _statusColor = Colors.blue;
    });

    Future.delayed(const Duration(seconds: 1), () {
      setState(() {
        _isLoading = false;
        _statusMessage = '正常调用完成：有效指针已处理';
        _statusColor = Colors.green;
      });
    });
  }

  void _simulateEvilCall() {
    setState(() {
      _isLoading = true;
      _statusMessage = '正在执行恶意 JNI 调用...';
      _statusColor = Colors.orange;
    });

    Future.delayed(const Duration(seconds: 1), () {
      setState(() {
        _isLoading = false;
        _statusMessage = '恶意调用：伪造指针触发崩溃';
        _statusColor = Colors.red;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('JNI 漏洞测试工具'),
        centerTitle: true,
        elevation: 4,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Card(
              elevation: 4,
              child: Padding(
                padding: EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'JNI 漏洞测试说明',
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    SizedBox(height: 12),
                    Text(
                      '本应用演示通过 JNI 调用原生代码时的指针验证漏洞。'
                      '正常调用传入有效的对象指针，恶意调用传入伪造的指针以触发崩溃。',
                      style: TextStyle(fontSize: 16),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),
            Card(
              elevation: 4,
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      '测试状态',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 16),
                    Container(
                      width: double.infinity,
                      padding: const EdgeInsets.all(16),
                      decoration: BoxDecoration(
                        color: _statusColor.withValues(alpha: .1),
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(color: _statusColor, width: 1),
                      ),
                      child: Column(
                        children: [
                          if (_isLoading)
                            const CircularProgressIndicator()
                          else
                            Icon(
                              _statusColor == Colors.green
                                  ? Icons.check_circle
                                  : _statusColor == Colors.red
                                  ? Icons.error
                                  : _statusColor == Colors.orange
                                  ? Icons.warning
                                  : Icons.info,
                              color: _statusColor,
                              size: 36,
                            ),
                          const SizedBox(height: 12),
                          Text(
                            _statusMessage,
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.w500,
                              color: _statusColor,
                            ),
                            textAlign: TextAlign.center,
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),
            Card(
              elevation: 4,
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      '测试操作',
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
                            onPressed: _isLoading ? null : _simulateNormalCall,
                            icon: const Icon(Icons.safety_check),
                            label: const Text('正常调用'),
                            style: ElevatedButton.styleFrom(
                              padding: const EdgeInsets.symmetric(vertical: 16),
                              backgroundColor: Colors.blue,
                              foregroundColor: Colors.white,
                            ),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: ElevatedButton.icon(
                            onPressed: _isLoading ? null : _simulateEvilCall,
                            icon: const Icon(Icons.warning),
                            label: const Text('恶意调用'),
                            style: ElevatedButton.styleFrom(
                              padding: const EdgeInsets.symmetric(vertical: 16),
                              backgroundColor: Colors.red,
                              foregroundColor: Colors.white,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    if (_isLoading)
                      const LinearProgressIndicator()
                    else
                      const SizedBox(),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),
            const Card(
              elevation: 4,
              child: Padding(
                padding: EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      '技术细节',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    SizedBox(height: 12),
                    ListTile(
                      leading: Icon(Icons.code),
                      title: Text('JNI 函数：nativeDestroy'),
                      subtitle: Text('接收指针参数并尝试调用对象方法'),
                    ),
                    ListTile(
                      leading: Icon(Icons.memory),
                      title: Text('漏洞类型：空指针解引用'),
                      subtitle: Text('未验证传入指针的有效性'),
                    ),
                    ListTile(
                      leading: Icon(Icons.security),
                      title: Text('安全建议'),
                      subtitle: Text('始终验证 JNI 参数，检查指针非空'),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
