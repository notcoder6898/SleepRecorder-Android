package com.sleeprecorder.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.sleeprecorder.app.MainActivity
import com.sleeprecorder.app.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log10
import kotlin.math.sqrt

class AudioRecordingService : Service() {
    
    private val binder = LocalBinder()
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitoringJob: Job? = null
    
    private var wakeLock: PowerManager.WakeLock? = null
    
    // 录音参数
    private var decibelThreshold = -40f
    private var minRecordDurationMs = 3000L
    private var silenceTimeoutMs = 1500L
    
    // 当前片段记录
    private var currentSegmentFile: File? = null
    private var currentSegmentStartTime: Long = 0
    private var currentSegmentPeakDecibel = 0f
    private var recordedSegments = mutableListOf<SegmentInfo>()
    
    // 状态流
    private val _recordingState = MutableStateFlow(RecordingState())
    val recordingState: StateFlow<RecordingState> = _recordingState
    
    private val _currentDecibel = MutableStateFlow(0f)
    val currentDecibel: StateFlow<Float> = _currentDecibel
    
    data class RecordingState(
        val isActive: Boolean = false,
        val isRecordingSegment: Boolean = false,
        val segmentCount: Int = 0,
        val startTime: Long = 0
    )
    
    data class SegmentInfo(
        val file: File,
        val startTime: Date,
        val duration: Long,
        val peakDecibel: Float
    )
    
    inner class LocalBinder : Binder() {
        fun getService(): AudioRecordingService = this@AudioRecordingService
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }
    
    override fun onBind(intent: Intent): IBinder = binder
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sensitivity = intent?.getFloatExtra("sensitivity", 0.5f) ?: 0.5f
        val minDuration = intent?.getIntExtra("minDuration", 3) ?: 3
        
        // 根据灵敏度计算阈值
        decibelThreshold = -50f + (1f - sensitivity) * 30f
        minRecordDurationMs = minDuration * 1000L
        
        startForeground(NOTIFICATION_ID, createNotification())
        startMonitoring()
        
        return START_STICKY
    }
    
    private fun startMonitoring() {
        _recordingState.value = RecordingState(
            isActive = true,
            startTime = System.currentTimeMillis()
        )
        
        monitoringJob = serviceScope.launch {
            while (isActive) {
                val decibel = measureDecibel()
                _currentDecibel.value = decibel
                
                val isSoundDetected = decibel > decibelThreshold
                val currentState = _recordingState.value
                
                when {
                    isSoundDetected && !currentState.isRecordingSegment -> {
                        startSegmentRecording()
                    }
                    !isSoundDetected && currentState.isRecordingSegment -> {
                        delay(silenceTimeoutMs)
                        if (_currentDecibel.value <= decibelThreshold) {
                            stopSegmentRecording()
                        }
                    }
                    isSoundDetected && currentState.isRecordingSegment -> {
                        currentSegmentPeakDecibel = maxOf(currentSegmentPeakDecibel, decibel)
                    }
                }
                
                delay(100) // 100ms 检测间隔
            }
        }
    }
    
    private fun measureDecibel(): Float {
        // 简化的分贝测量
        // 实际项目中可以使用 AudioRecord 获取原始音频数据计算
        return if (isRecording) {
            // 模拟分贝值，实际应从 MediaRecorder 获取
            (Math.random() * 30 - 50).toFloat()
        } else {
            -100f
        }
    }
    
    private fun startSegmentRecording() {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(getExternalFilesDir(null), "recording_$timestamp.m4a")
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16000)
                setAudioChannels(1)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            
            isRecording = true
            currentSegmentFile = file
            currentSegmentStartTime = System.currentTimeMillis()
            currentSegmentPeakDecibel = _currentDecibel.value
            
            _recordingState.value = _recordingState.value.copy(isRecordingSegment = true)
            
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    
    private fun stopSegmentRecording() {
        val duration = System.currentTimeMillis() - currentSegmentStartTime
        
        if (duration >= minRecordDurationMs) {
            try {
                mediaRecorder?.apply {
                    stop()
                    reset()
                    release()
                }
                
                currentSegmentFile?.let { file ->
                    recordedSegments.add(SegmentInfo(
                        file = file,
                        startTime = Date(currentSegmentStartTime),
                        duration = duration,
                        peakDecibel = currentSegmentPeakDecibel
                    ))
                }
                
                _recordingState.value = _recordingState.value.copy(
                    isRecordingSegment = false,
                    segmentCount = recordedSegments.size
                )
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // 时长太短，删除文件
            currentSegmentFile?.delete()
            mediaRecorder?.release()
        }
        
        mediaRecorder = null
        isRecording = false
        currentSegmentFile = null
    }
    
    fun stopMonitoring(): List<SegmentInfo> {
        monitoringJob?.cancel()
        
        if (_recordingState.value.isRecordingSegment) {
            stopSegmentRecording()
        }
        
        _recordingState.value = RecordingState()
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        
        return recordedSegments.toList()
    }
    
    fun getRecordedSegments(): List<SegmentInfo> = recordedSegments.toList()
    
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SleepRecorder::RecordingWakeLock"
        ).apply {
            acquire(10 * 60 * 60 * 1000L) // 10小时
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "睡眠监测录音",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "后台录音服务"
                setSound(null, null)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("睡眠监测中")
            .setContentText("正在监听夜间声音...")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        mediaRecorder?.release()
        wakeLock?.release()
    }
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "sleep_recorder_channel"
    }
}