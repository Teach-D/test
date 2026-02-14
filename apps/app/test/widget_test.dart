import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:point_roulette/screens/network_error_screen.dart';
import 'package:point_roulette/widgets/loading_overlay.dart';

void main() {
  group('NetworkErrorScreen', () {
    testWidgets('네트워크 오류 메시지와 아이콘이 표시된다', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: NetworkErrorScreen(onRetry: () {}),
        ),
      );

      expect(find.byIcon(Icons.wifi_off_rounded), findsOneWidget);
      expect(find.text('네트워크 연결을 확인해주세요'), findsOneWidget);
      expect(find.text('다시 시도'), findsOneWidget);
    });

    testWidgets('다시 시도 버튼 탭 시 onRetry 콜백이 호출된다',
        (WidgetTester tester) async {
      var retryCount = 0;

      await tester.pumpWidget(
        MaterialApp(
          home: NetworkErrorScreen(onRetry: () => retryCount++),
        ),
      );

      await tester.tap(find.text('다시 시도'));
      await tester.pump();

      expect(retryCount, 1);
    });
  });

  group('LoadingOverlay', () {
    testWidgets('isLoading이 true이면 로딩 인디케이터가 표시된다',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: LoadingOverlay(isLoading: true),
          ),
        ),
      );

      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });

    testWidgets('isLoading이 false이면 로딩 인디케이터가 표시되지 않는다',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: LoadingOverlay(isLoading: false),
          ),
        ),
      );

      expect(find.byType(CircularProgressIndicator), findsNothing);
    });
  });
}
