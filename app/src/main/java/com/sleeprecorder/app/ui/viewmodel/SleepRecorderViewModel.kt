package com.sleeprecorder.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sleeprecorder.app.data.*
import com.sleeprecorder.app.service.AudioRecordingService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class SleepRecorderViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val sleepRecordDao = database.sleepRecordDao()
    private val audioSegmentDao = database.audioSegmentDao()
    private val settingsDao = database.settingsDao()
    
    // 所有睡眠记录
    val allRecords: StateFlow<List<SleepRecord>> = sleepRecordDao.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // 设置
    val settings: StateFlow<AppSettings?> = settingsDao.getSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    
    // 统计数据
    val sleepStats: StateFlow<SleepStats> = allRecords.map { records ->
        SleepStats(
            totalRecords = records.size,
            averageDuration = if (records.isNotEmpty()) {
                val avgMs = records.sumOf { it.duration } / records.size
                val hours = (avgMs / 1000 / 3600).toInt()
                val minutes = ((avgMs / 1000 % 3600) / 60).toInt()
                "${hours}h${minutes}m"
            } else "0h0m",
            totalSegments = records.size // 简化计算
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, SleepStats())
    
    init {
        viewModelScope.launch {
            // 初始化默认设置
            if (settingsDao.getSettings() == null) {
                settingsDao.insert(AppSettings())
            }
        }
    }
    
    // 结束睡眠会话，保存记录
    fun endSleepSession(segments: List<AudioRecordingService.SegmentInfo>) {
        viewModelScope.launch {
            val endTime = Date()
            val startTime = segments.firstOrNull()?.startTime ?: endTime
            val duration = endTime.time - startTime.time
            
            val record = SleepRecord(
                startTime = startTime,
                endTime = endTime,
                duration = duration
            )
            
            sleepRecordDao.insert(record)
            
            // 保存音频片段
            val audioSegments = segments.map { info ->
                AudioSegment(
                    recordId = record.id,
                    startTime = info.startTime,
                    duration = info.duration,
                    fileName = info.file.name,
                    filePath = info.file.absolutePath,
                    peakDecibel = info.peakDecibel,
                    averageDecibel = info.peakDecibel // 简化
                )
            }
            
            audioSegments.forEach { audioSegmentDao.insert(it) }
        }
    }
    
    suspend fun getRecordById(id: String): SleepRecord? {
        return allRecords.value.find { it.id == id }
    }
    
    suspend fun getSegmentsForRecord(recordId: String): List<AudioSegment> {
        return audioSegmentDao.getSegmentsForRecord(recordId)
    }
    
    fun deleteRecord(recordId: String) {
        viewModelScope.launch {
            allRecords.value.find { it.id == recordId }?.let {
                sleepRecordDao.delete(it)
            }
        }
    }
    
    fun updateSettings(
        sensitivity: Float,
        minDuration: Int,
        keepDays: Int,
        nasEnabled: Boolean
    ) {
        viewModelScope.launch {
            val current = settingsDao.getSettings() ?: AppSettings()
            settingsDao.update(
                current.copy(
                    sensitivity = sensitivity,
                    minRecordDuration = minDuration,
                    keepDays = keepDays,
                    nasEnabled = nasEnabled
                )
            )
        }
    }
    
    data class SleepStats(
        val totalRecords: Int = 0,
        val averageDuration: String = "0h0m",
        val totalSegments: Int = 0
    )
}