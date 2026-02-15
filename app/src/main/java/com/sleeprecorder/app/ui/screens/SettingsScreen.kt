package com.sleeprecorder.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
nimport androidx.compose.foundation.selection.selectableGroup
nimport androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeprecorder.app.ui.viewmodel.SleepRecorderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SleepRecorderViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    
    var sensitivity by remember { mutableFloatStateOf(0.5f) }
    var minDuration by remember { mutableIntStateOf(3) }
    var keepDays by remember { mutableIntStateOf(7) }
    var nasEnabled by remember { mutableStateOf(false) }
    
    LaunchedEffect(settings) {
        settings?.let {
            sensitivity = it.sensitivity
            minDuration = it.minRecordDuration
            keepDays = it.keepDays
            nasEnabled = it.nasEnabled
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = {
                        // 保存设置
                        viewModel.updateSettings(
                            sensitivity = sensitivity,
                            minDuration = minDuration,
                            keepDays = keepDays,
                            nasEnabled = nasEnabled
                        )
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 录音设置
            SettingsSection(title = "录音设置") {
                // 灵敏度
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("触发灵敏度")
                        Text(
                            text = when {
                                sensitivity < 0.3f -> "低"
                                sensitivity < 0.6f -> "中"
                                else -> "高"
                            },
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = sensitivity,
                        onValueChange = { sensitivity = it },
                        valueRange = 0.1f..0.9f,
                        steps = 7
                    )
                }
                
                // 最短录音时长
                var expanded by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("最短录音时长")
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        TextButton(onClick = { expanded = true }) {
                            Text("$minDuration 秒")
                        }
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf(1, 2, 3, 5, 10).forEach { seconds ->
                                DropdownMenuItem(
                                    text = { Text("$seconds 秒") },
                                    onClick = {
                                        minDuration = seconds
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // 存储设置
            SettingsSection(title = "存储设置") {
                // 保留天数
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("本地保留天数")
                    Text("$keepDays 天")
                }
                
                // NAS 开关
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("启用 NAS 备份")
                    Switch(
                        checked = nasEnabled,
                        onCheckedChange = { nasEnabled = it }
                    )
                }
            }
            
            // 省电说明
            SettingsSection(title = "省电说明") {
                InfoItem(icon = "⚡", text = "建议在睡眠期间保持设备充电")
                InfoItem(icon = "🌙", text = "屏幕会自动保持最低亮度")
                InfoItem(icon = "🎵", text = "采样率已优化为 16kHz 以节省电量")
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun InfoItem(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}