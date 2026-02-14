import 'package:flutter/material.dart';
import 'package:point_roulette/config/app_config.dart';
import 'package:point_roulette/screens/webview_screen.dart';

/// 앱 루트 위젯
class PointRouletteApp extends StatelessWidget {
  const PointRouletteApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: AppConfig.appName,
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        // 웹 앱(indigo-600)과 동일한 색상 테마 적용
        colorSchemeSeed: const Color(0xFF4F46E5),
        useMaterial3: true,
      ),
      home: const WebViewScreen(),
    );
  }
}
