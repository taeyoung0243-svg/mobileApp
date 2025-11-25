package com.example.w05

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.w05.ui.theme.MyApplicationTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // TODO: 여기에 카운터와 스톱워치 UI를 만들도록 안내
                val count = remember { mutableStateOf(0) }
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CounterApp(count)
                    Spacer(modifier = Modifier.height(32.dp))
                    StopWatchApp()
                }
            }
        }
    }
}



@Composable
fun CounterApp(count: MutableState<Int>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Count: ${count.value}",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        ) // TODO: 상태값 표시
        Row {
            Button(onClick = { count.value++ }) { Text("Increase") }
            Button(onClick = { count.value = 0 }) { Text("Reset") }
        }
    }
}


@Composable
fun StopWatchApp() {
    // 1. 시간(밀리초)과 타이머 실행 여부를 기억할 State 변수 추가
    var timeInMillis by remember { mutableStateOf(1234L) }
    var isRunning by remember { mutableStateOf(false) }

    // 1. isRunning 상태가 true일 때만 실행되는 LaunchedEffect 추가
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (true) {
                delay(10L) // 10밀리초마다
                timeInMillis += 10L // 시간을 10밀리초씩 증가
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = formatTime(timeInMillis), // 2. State 변수를 사용해 시간 표시
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )
        Row {
            // 3. 버튼 클릭 시 isRunning 상태를 변경
            Button(onClick = { isRunning = true }) { Text("Start") }
            Button(onClick = { isRunning = false }) { Text("Stop") }
            Button(onClick = {
                isRunning = false
                timeInMillis = 0L
            }) { Text("Reset") }
        }
    }
}


// 시간을 MM:SS:ss 형식으로 변환하는 헬퍼 함수
private fun formatTime(timeInMillis: Long): String {
    val minutes = (timeInMillis / 1000) / 60
    val seconds = (timeInMillis / 1000) % 60
    val millis = (timeInMillis % 1000) / 10
    return String.format("%02d:%02d:%02d", minutes, seconds, millis)
}
@Composable
fun ColorToggleButtonApp() {
    // 배경색 상태를 저장하는 변수. 초기값은 Color.Red.
    // 'by' 키워드를 사용하여 MutableState<Color>의 value 속성에 직접 접근하도록 함.
    var currentColor by remember { mutableStateOf(Color.Red) }

    // 원형 버튼을 화면 중앙에 배치하기 위한 외부 Box
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 클릭 가능한 원형 버튼 역할을 하는 내부 Box
        Box(
            modifier = Modifier
                .clip(CircleShape) // 모양을 원형으로 자름
                .background(currentColor) // 현재 색상으로 배경 설정
                .clickable { // 클릭 이벤트 처리
                    // 현재 색상이 빨간색이면 파란색으로, 아니면 빨간색으로 변경
                    currentColor = if (currentColor == Color.Red) Color.Blue else Color.Red
                }
                .padding(30.dp), // 원 안쪽에 여백을 줘서 텍스트와 경계 사이에 공간을 둠
            contentAlignment = Alignment.Center // Box 안의 내용을 중앙에 정렬
        ) {
            Text(
                text = "Click Me",
                color = Color.White, // 텍스트 색상은 흰색으로 고정
                fontSize = 30.sp
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 300, heightDp = 300)
@Composable
fun ColorToggleButtonAppPreview() {
    ColorToggleButtonApp()
}
@Preview(showBackground = true)
@Composable
fun CounterAppPreview() {
    val count = remember { mutableStateOf(0) }

    CounterApp(count)
}

@Preview(showBackground = true)
@Composable
fun StopWatchPreview() {
    StopWatchApp()
}
