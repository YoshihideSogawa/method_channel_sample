package com.example.method_channel_sample

import android.accounts.AccountManager
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Build.VERSION
import androidx.annotation.RequiresApi
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    // バッテリーのチャンネル
    private val channelBattery = "samples.flutter.dev/battery"

    /// アカウントのチャンネル
    private val channelAccount = "samples.flutter.dev/account"

    private var result: MethodChannel.Result? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            channelBattery
        ).setMethodCallHandler { call, result ->
            if (call.method == "getBatteryLevel") {
                val batteryLevel = getBatteryLevel()
                if (batteryLevel != -1) {
                    result.success(batteryLevel)
                } else {
                    result.error("UNAVAILABLE", "Battery level not available.", null)
                }
            } else {
                result.notImplemented()
            }
        }

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            channelAccount
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "getAccountInfo" -> {
                    getAccountInfo(result)
                }

                "selectAccount" -> {
                    this.result = result
                    selectAccount()
                }

                else -> {
                    result.notImplemented()
                }
            }

        }
    }

    private fun getBatteryLevel(): Int {
        val batteryLevel: Int =
            // LOLLIPOP以下（10年前のOS…）
            if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
                batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            } else {
                val intent = ContextWrapper(applicationContext).registerReceiver(
                    null,
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                )
                intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 / intent.getIntExtra(
                    BatteryManager.EXTRA_SCALE,
                    -1
                )
            }

        return batteryLevel
    }

    private fun getAccountInfo(result: MethodChannel.Result) {
        val accountManager = AccountManager.get(this)
        val accounts = accountManager.accounts
        // アカウントが存在する場合
        if (accounts.isNotEmpty()) {
            // tokenを取得
            accountManager.getAuthToken(
                accounts[0],
                "mail",
                null,
                this,
                { future ->
                    val authToken = future.result.getString(AccountManager.KEY_AUTHTOKEN)
                    result.success(
                        mutableMapOf(
                            "name" to accounts[0].name,
                            "token" to authToken
                        )
                    )
                },
                null
            )
        } else {
            result.success(mutableMapOf("name" to "No Accounts"))
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun selectAccount() {
        val intent = AccountManager.newChooseAccountIntent(
            null, null, arrayOf("com.google"), null, null, null, null
        )
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                val accountName = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                result?.success(accountName)
            } else if (resultCode == RESULT_CANCELED) {
                result?.success("CANCELLED")
            }
        }
    }

}
