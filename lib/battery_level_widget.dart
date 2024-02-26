import 'dart:developer' as developer;
import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// バッテリーレベルのWidgetです。
class BatteryLevelWidget extends StatelessWidget {
  const BatteryLevelWidget({super.key});

  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
      onPressed: () {
        fetchBatteryLevel(context);
      },
      child: const Icon(Icons.battery_unknown_sharp),
    );
  }

  /// バッテリーレベルのを取得します。
  Future<void> fetchBatteryLevel(BuildContext context) async {
    String message;
    try {
      final result = await platform.invokeMethod('getBatteryLevel');

      message = 'Battery level at $result % .';
    } on PlatformException catch (e, t) {
      message = 'Failed to get battery level: ${e.message}.';
      developer.log('$e\n$t');
    } on MissingPluginException catch (e, t) {
      message = 'Failed to get battery level: ${e.message}.';
      developer.log('$e\n$t');
    }

    if (!context.mounted) {
      return;
    }

    ScaffoldMessenger.of(context)
      ..clearSnackBars()
      ..showSnackBar(
        SnackBar(
          content: Text(message),
        ),
      );
  }
}

const platform = MethodChannel('samples.flutter.dev/battery');
