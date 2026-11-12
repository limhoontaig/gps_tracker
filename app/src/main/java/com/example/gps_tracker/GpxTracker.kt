package com.example.gps_tracker

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class GpxTracker(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationCallback: LocationCallback
    private val locationRequest: LocationRequest

    // 수집된 위치 데이터를 임시로 담을 리스트
    private val locationList = mutableListOf<Location>()
    private var isTracking = false

    init {
        // 위치 요청 설정 (5초마다, 혹은 5미터 이동 시 갱신)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).apply {
            setMinUpdateDistanceMeters(5f)
            setWaitForAccurateLocation(false)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    locationList.add(location)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (isTracking) return
        locationList.clear()
        isTracking = true
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopTracking(): File? {
        if (!isTracking) return null
        isTracking = false
        fusedLocationClient.removeLocationUpdates(locationCallback)

        // 트래킹 종료 시 GPX 파일로 저장
        return saveGpxFile()
    }

    private fun saveGpxFile(): File? {
        if (locationList.isEmpty()) return null

        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            val fileName = "track_${System.currentTimeMillis()}.gpx"
            val file = File(context.filesDir, fileName)

            file.bufferedWriter().use { writer ->
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                writer.write("<gpx version=\"1.1\" creator=\"AndroidGPXTracker\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n")
                writer.write("  <trk>\n")
                writer.write("    <name>Recorded Track ${Date()}</name>\n")
                writer.write("    <trkseg>\n")

                for (loc in locationList) {
                    val timeStr = dateFormat.format(Date(loc.time))
                    writer.write("      <trkpt lat=\"${loc.latitude}\" lon=\"${loc.longitude}\">\n")
                    if (loc.hasAltitude()) {
                        writer.write("        <ele>${loc.altitude}</ele>\n")
                    }
                    writer.write("        <time>$timeStr</time>\n")
                    writer.write("      </trkpt>\n")
                }

                writer.write("    </trkseg>\n")
                writer.write("  </trk>\n")
                writer.write("</gpx>\n")
            }
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}