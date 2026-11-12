package com.example.gps_tracker

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File

class LocationTrackingService : Service() {

    private lateinit var gpxTracker: GpxTracker
    private val CHANNEL_ID = "LocationTrackingChannel"

    override fun onCreate() {
        super.onCreate()
        gpxTracker = GpxTracker(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ACTION_START_TRACKING" -> startTrackingService()
            "ACTION_STOP_TRACKING" -> stopTrackingService()
        }
        // 시스템에 의해 서비스가 강제 종료되었을 때 자동 재시작 방지 또는 유지 설정
        return START_STICKY
    }

    private fun startTrackingService() {
        createNotificationChannel()

        // 상태바에 고정될 알림 생성 (포그라운드 서비스 필수 조건)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GPS 트래커 실행 중")
            .setContentText("위치 정보를 안전하게 기록하고 있습니다...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

        // 포그라운드 서비스 시작 선언 (알림 ID와 Notification 객체 전달)
        startForeground(1, notification)

        // 앞서 구현한 GpxTracker 시작
        gpxTracker.startTracking()
    }

    private fun stopTrackingService() {
        val savedFile: File? = gpxTracker.stopTracking()
        // 필요시 저장된 파일 경로 처리 로직 추가

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "GPS Tracking Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }
}