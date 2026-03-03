import 'package:flutter/material.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'JNI 漏洞测试',
      theme: ThemeData(primarySwatch: Colors.blue),
      home: const MyHomePage(title: 'JNI 漏洞测试'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text('JNI 漏洞测试应用', style: TextStyle(fontSize: 24)),
            const SizedBox(height: 20),
            const Text('使用原生 Android UI 进行测试', style: TextStyle(fontSize: 18)),
          ],
        ),
      ),
    );
  }
}
