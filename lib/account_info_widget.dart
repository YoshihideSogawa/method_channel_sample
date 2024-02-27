import 'dart:developer' as developer;
import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// アカウント情報のWidgetです。
class AccountInfoWidget extends StatelessWidget {
  const AccountInfoWidget({super.key});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        ElevatedButton(
          onPressed: () {
            selectAccount(context);
          },
          child: const Icon(Icons.add_reaction_outlined),
        ),
        ElevatedButton(
          onPressed: () {
            fetchAccount(context);
          },
          child: const Icon(Icons.account_circle_rounded),
        ),
      ],
    );
  }

  /// アカウント情報を取得します。
  Future<void> fetchAccount(BuildContext context) async {
    String message;
    try {
      final result = await _platform.invokeMethod('getAccountInfo');

      message = 'Account Name > ${result["name"]}\n'
          'Token > ${result["token"]}';
      print(result);
    } on PlatformException catch (e, t) {
      message = 'Failed to get Account Name: ${e.message}.';
      developer.log('$e\n$t');
    } on MissingPluginException catch (e, t) {
      message = 'Failed to get Account Name: ${e.message}.';
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

  /// アカウントを選択します。
  Future<void> selectAccount(BuildContext context) async {
    String message = '';
    try {
      final result = await _platform.invokeMethod('selectAccount');
      message = result;
    } on PlatformException catch (e, t) {
      developer.log('$e\n$t');
    } on MissingPluginException catch (e, t) {
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

const _platform = MethodChannel('samples.flutter.dev/account');
