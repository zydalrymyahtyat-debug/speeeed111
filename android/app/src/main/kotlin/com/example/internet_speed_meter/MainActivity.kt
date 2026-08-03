package com.example.internet_speed_meter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.Manifest

class MainActivity : FlutterActivity() {
    private val channelName = "internet_speed_meter/service"
    private var methodChannel: MethodChannel? = null
    private val PERMISSION_REQUEST_CODE = 100

    private val speedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val down = intent.getDoubleExtra("download", 0.0)
            val up = intent.getDoubleExtra("upload", 0.0)
            methodChannel?.invokeMethod("speedUpdate", mapOf("download" to down, "upload" to up))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), PERMISSION_REQUEST_CODE)
            }
        }

        val filter = IntentFilter("speedUpdate")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(speedReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(speedReceiver, filter)
        }
    }

    override fun onDestroy() {
        unregisterReceiver(speedReceiver)
        super.onDestroy()
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName)
        methodChannel?.setMethodCallHandler { call, result ->
            when (call.method) {
                "startService" -> {
                    val intent = Intent(this, SpeedMeterService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
                    else startService(intent)
                    result.success(true)
                }
                "stopService" -> {
                    stopService(Intent(this, SpeedMeterService::class.java))
                    result.success(true)
                }
                else -> result.notImplemented()
            }
        }
    }
}
