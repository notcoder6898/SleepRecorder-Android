package com.sleeprecorder.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.Date

@Entity(tableName = "sleep_records")
data class SleepRecord(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val date: Date = Date(),
    val startTime: Date,
    val endTime: Date,
    val duration: Long // 毫秒
)

@Entity(
    tableName = "audio_segments",
    foreignKeys = [
        ForeignKey(
            entity = SleepRecord::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recordId")]
)
data class AudioSegment(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val recordId: String,
    val startTime: Date,
    val duration: Long, // 毫秒
    val fileName: String,
    val filePath: String,
    val peakDecibel: Float,
    val averageDecibel: Float
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val sensitivity: Float = 0.5f,
    val minRecordDuration: Int = 3,
    val alarmSound: String = "gentle",
    val alarmDismissMethod: String = "slide",
    val keepDays: Int = 7,
    val nasEnabled: Boolean = false,
    val nasServer: String = "",
    val nasUsername: String = "",
    val nasPassword: String = "",
    val nasPath: String = "/SleepRecorder"
)