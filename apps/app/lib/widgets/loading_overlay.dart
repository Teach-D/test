import 'package:flutter/material.dart';

/// 페이지 로딩 중 표시되는 반투명 오버레이 위젯
class LoadingOverlay extends StatelessWidget {
  const LoadingOverlay({super.key, required this.isLoading});

  /// 로딩 표시 여부
  final bool isLoading;

  @override
  Widget build(BuildContext context) {
    if (!isLoading) return const SizedBox.shrink();

    return const ColoredBox(
      color: Color(0x80000000), // 50% 투명도 검정
      child: Center(
        child: CircularProgressIndicator(
          color: Colors.white,
        ),
      ),
    );
  }
}
