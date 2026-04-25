package com.example.damaqi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DaMaQiScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaMaQiScreen() {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("查账参数", "配置/彩种参数", "后台密码")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("打码器 - 便携版") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = tabIndex == idx,
                        onClick = { tabIndex = idx },
                        text = { Text(title) }
                    )
                }
            }
            when (tabIndex) {
                0 -> AuditTab()
                1 -> ConfigTab()
                2 -> BackgroundTab()
            }
        }
    }
}

@Composable
fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Number
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    )
}

@Composable
fun ResultBox(value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (value.isEmpty()) "--" else value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (value.isEmpty()) Color.Gray else Color(0xFFD32F2F),
            textAlign = TextAlign.Center
        )
    }
}

private fun parseLong(s: String, allowNegative: Boolean = false): Long? {
    val t = s.trim()
    if (t.isEmpty()) return null
    val v = t.toLongOrNull() ?: return null
    if (!allowNegative && v < 0) return null
    return v
}

@Composable
fun AuditTab() {
    val ctx = LocalContext.current
    var line by remember { mutableStateOf("100") }
    var machine by remember { mutableStateOf("20130717") }
    var serial by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("0") }
    var curr by remember { mutableStateOf("0") }
    var allow by remember { mutableStateOf("0") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        LabeledField(label = "线号 (Line Num)", value = line, onValueChange = { line = it })
        LabeledField(label = "机台 (Machine ID)", value = machine, onValueChange = { machine = it })
        LabeledField(label = "序列号 (Serial)", value = serial, onValueChange = { serial = it })
        LabeledField(label = "前次赢分 (Last Money)", value = last, onValueChange = { last = it })
        LabeledField(label = "本次赢分 (Curr Money)", value = curr, onValueChange = { curr = it })
        LabeledField(label = "允许偏差 (0-99)", value = allow, onValueChange = { allow = it })

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                val l = parseLong(line) ?: return@Button toast(ctx, "线号无效")
                val m = parseLong(machine) ?: return@Button toast(ctx, "机台无效")
                val s = parseLong(serial) ?: return@Button toast(ctx, "序列号无效")
                val lm = parseLong(last, allowNegative = true) ?: return@Button toast(ctx, "前次赢分无效")
                val cm = parseLong(curr, allowNegative = true) ?: return@Button toast(ctx, "本次赢分无效")
                val a = parseLong(allow) ?: return@Button toast(ctx, "允许偏差无效")
                result = DaMaQi.generateAudit(AuditInput(l, m, s, lm, cm, a))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("生成查账参数")
        }

        ResultBox(result)

        Spacer(Modifier.height(16.dp))
        Text(
            "新石器：黄金时代(91)、宝石列车(92)、魔法世界(93)、魔龙帝国(99)",
            color = Color.Gray,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "四海魔龙：黄金时代(91)、宝石列车(92)、魔法世界(93)、森林冒险(94)、极地风暴(95)、魔龙帝国(99)",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ConfigTab() {
    val ctx = LocalContext.current
    var line by remember { mutableStateOf("100") }
    var machine by remember { mutableStateOf("20130717") }
    var serial by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "注意：配置和彩票参数的序列号相同",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LabeledField(label = "线号 (Line Num)", value = line, onValueChange = { line = it })
        LabeledField(label = "机台 (Machine ID)", value = machine, onValueChange = { machine = it })
        LabeledField(label = "序列号 (Serial)", value = serial, onValueChange = { serial = it })

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                val l = parseLong(line) ?: return@Button toast(ctx, "线号无效")
                val m = parseLong(machine) ?: return@Button toast(ctx, "机台无效")
                val s = parseLong(serial) ?: return@Button toast(ctx, "序列号无效")
                result = DaMaQi.generateConfig(l, m, s)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("生成配置参数")
        }

        ResultBox(result)
    }
}

@Composable
fun BackgroundTab() {
    val ctx = LocalContext.current
    var machine by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        LabeledField(label = "机台 (Machine ID)", value = machine, onValueChange = { machine = it })

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                val m = parseLong(machine) ?: return@Button toast(ctx, "机台无效")
                result = DaMaQi.generateBackground(m)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("生成后台密码")
        }

        ResultBox(result)
    }
}

private fun toast(ctx: android.content.Context, msg: String) {
    Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
}
