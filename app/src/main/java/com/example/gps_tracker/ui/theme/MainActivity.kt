package com.example.gps_tracker // 본인의 패키지명으로 확인하세요

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 앱이 켜질 때 위치 권한 요청
        checkAndRequestPermissions()

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "GPS Tracker",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        // 트래킹 시작 버튼
                        Button(
                            onClick = {
                                if (hasLocationPermissions()) {
                                    val intent = Intent(this@MainActivity, LocationTrackingService::class.java).apply {
                                        action = "ACTION_START_TRACKING"
                                    }
                                    startForegroundService(intent)
                                    Toast.makeText(this@MainActivity, "GPS 트래킹을 시작합니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@MainActivity, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                                    checkAndRequestPermissions()
                                }
                            },
                            modifier = Modifier.width(200.dp)
                        ) {
                            Text("트래킹 시작")
                        }

                        // 트래킹 종료 버튼
                        Button(
                            onClick = {
                                val intent = Intent(this@MainActivity, LocationTrackingService::class.java).apply {
                                    action = "ACTION_STOP_TRACKING"
                                }
                                startService(intent)
                                Toast.makeText(this@MainActivity, "트래킹 종료 및 GPX 저장 완료", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.width(200.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("트래킹 종료 & 저장")
                        }
                    }
                }
            }
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkAndRequestPermissions() {
        if (!hasLocationPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
}