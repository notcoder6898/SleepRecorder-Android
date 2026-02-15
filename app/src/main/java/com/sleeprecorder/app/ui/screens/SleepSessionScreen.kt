package com.sleeprecorder.app.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sleeprecorder.app.service.AudioRecordingService
import com.sleeprecorder.app.ui.viewmodel.SleepRecorderViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SleepSessionScreen(
    viewModel: SleepRecorderViewModel,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    var isServiceBound by remember { mutableStateOf(false) }
    var recordingService by remember { mutableStateOf<AudioRecordingService?>(null) }
    
    var currentTime by remember { mutableStateOf(Date()) }
    var sessionStartTime by remember { mutableStateOf(Date()) }
    var showStopDialog by remember { mutableStateOf(false) }
    
    // 定时更新时间
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000)
        }
    }
    
    // 绑定服务
    DisposableEffect(Unit) {
        val intent = Intent(context, AudioRecordingService::class.java).apply {
            putExtra("sensitivity", viewModel.settings.value?.sensitivity ?: 0.5f)
            putExtra("minDuration", viewModel.settings.value?.minRecordDuration ?: 3)
        }
        
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                recordingService = (service as AudioRecordingService.LocalBinder).getService()
                isServiceBound = true
            }
            
            override fun onServiceDisconnected(name: ComponentName?) {
                recordingService = null
                isServiceBound = false
            }
        }
        
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        context.startService(intent)
        
        onDispose {
            context.unbindService(connection)
        }
    }
    
    val recordingState by recordingService?.recordingState?.collectAsState() 
        ?: remember { mutableStateOf(AudioRecordingService.RecordingState()) }
    val currentDecibel by recordingService?.currentDecibel?.collectAsState() 
        ?: remember { mutableStateOf(0f) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部状态栏
            StatusBar()
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 大时间显示
            TimeDisplay(currentTime = currentTime)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 已睡眠时长
            ElapsedTimeDisplay(startTime = sessionStartTime, currentTime = currentTime)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // 录音状态
            RecordingStatus(
                isRecording = recordingState.isRecordingSegment,
                segmentCount = recordingState.segmentCount,
                decibel = currentDecibel
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 底部控制
            BottomControls(
                onStopClick = { showStopDialog = true }
            )
        }
    }
    
    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text("结束睡眠？") },
            text = { Text("结束后将生成睡眠报告") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val segments = recordingService?.stopMonitoring() ?: emptyList()
                        viewModel.endSleepSession(segments)
                        showStopDialog = false
                        onFinish()
                    }
                ) {
                    Text("结束", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun StatusBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = null,
                tint = Color.Green,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "建议保持充电",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = null,
                tint = Color(0xFFFFA500),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "07:00",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun TimeDisplay(currentTime: Date) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    Text(
        text = timeFormat.format(currentTime),
        fontSize = 80.sp,
        fontWeight = FontWeight.Thin,
        color = MaterialTheme.colorScheme.onSurface,
        letterSpacing = 2.sp
    )
}

@Composable
fun ElapsedTimeDisplay(startTime: Date, currentTime: Date) {
    val diff = currentTime.time - startTime.time
    val hours = (diff / 1000 / 3600).toInt()
    val minutes = ((diff / 1000 % 3600) / 60).toInt()
    
    Text(
        text = String.format("已睡眠 %02d:%02d", hours, minutes),
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )
}

@Composable
fun RecordingStatus(
    isRecording: Boolean,
    segmentCount: Int,
    decibel: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 录音指示器
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PulsingDot(isActive = isRecording)
            
            Text(
                text = if (isRecording) "正在录音..." else "监听中",
                fontSize = 16.sp,
                color = if (isRecording) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 状态卡片
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusPill(
                icon = "🎵",
                value = "$segmentCount",
                label = "片段"
            )
            StatusPill(
                icon = "📢",
                value = String.format("%.1f dB", decibel),
                label = "当前音量"
            )
        }
    }
}

@Composable
fun PulsingDot(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .size(12.dp)
            .scale(if (isActive) scale else 1f)
            .clip(CircleShape)
            .background(if (isActive) Color.Red else Color.Gray)
    )
}

@Composable
fun StatusPill(icon: String, value: String, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun BottomControls(onStopClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 设置闹钟按钮（占位）
        OutlinedButton(
            onClick = { },
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(Icons.Default.Alarm, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("闹钟已设置")
        }
        
        // 结束睡眠按钮
        Button(
            onClick = onStopClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(
                text = "结束睡眠",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.background
            )
        }
    }
}