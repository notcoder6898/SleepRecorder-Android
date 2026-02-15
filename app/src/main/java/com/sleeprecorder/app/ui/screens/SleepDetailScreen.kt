package com.sleeprecorder.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeprecorder.app.data.AudioSegment
import com.sleeprecorder.app.data.SleepRecord
import com.sleeprecorder.app.ui.viewmodel.SleepRecorderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepDetailScreen(
    recordId: String,
    viewModel: SleepRecorderViewModel,
    onBack: () -> Unit
) {
    var record by remember { mutableStateOf<SleepRecord?>(null) }
    var segments by remember { mutableStateOf<List<AudioSegment>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(recordId) {
        record = viewModel.getRecordById(recordId)
        segments = viewModel.getSegmentsForRecord(recordId)
    }
    
    val dateFormat = SimpleDateFormat("MM月dd日", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("睡眠详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        record?.let { r ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 概览卡片
                OverviewCard(record = r)
                
                // 声音片段列表
                SegmentsCard(segments = segments)
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除记录？") },
            text = { Text("此操作不可恢复") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecord(recordId)
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun OverviewCard(record: SleepRecord) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val durationHours = (record.duration / 1000 / 3600).toInt()
    val durationMinutes = ((record.duration / 1000 % 3600) / 60).toInt()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = SimpleDateFormat("MM月dd日", Locale.getDefault()).format(record.date),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${timeFormat.format(record.startTime)} - ${timeFormat.format(record.endTime)}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Text(
                    text = "${durationHours}h${durationMinutes}m",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SegmentsCard(segments: List<AudioSegment>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "声音片段 (${segments.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (segments.isEmpty()) {
                Text(
                    text = "本晚未检测到明显声音",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                segments.forEach { segment ->
                    SegmentItem(segment = segment)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SegmentItem(segment: AudioSegment) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val seconds = (segment.duration / 1000).toInt()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = timeFormat.format(segment.startTime),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${seconds}秒 · 峰值 ${String.format("%.1f", segment.peakDecibel)} dB",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            // 播放按钮（占位）
            IconButton(onClick = { }) {
                Text("▶️")
            }
        }
    }
}