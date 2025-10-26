package com.example.app_10bubble_game

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_10bubble_game.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import kotlin.random.Random

data class Bubble(
    val id: Int,
    var position: Offset,
    val radius: Float,
    val color: Color,
    val creationTime: Long = System.currentTimeMillis(),
    val velocityX: Float = 0f,
    val velocityY: Float = 0f,
    val isPopping: Boolean = false
)

// 게임 상태 관리 클래스
class GameState(
    initialBubbles: List<Bubble> = emptyList()
) {
    var bubbles by mutableStateOf(initialBubbles)
    var score by mutableStateOf(0)
    var highScore by mutableStateOf(0)
    var isGameOver by mutableStateOf(false)
    var timeLeft by mutableStateOf(60)
    var combo by mutableStateOf(0)
    var lastClickTime by mutableStateOf(0L)
    var difficulty by mutableStateOf(1f)
}

fun makeNewBubble(maxWidth: Dp, maxHeight: Dp, difficulty: Float): Bubble {
    return Bubble(
        id = Random.nextInt(),
        position = Offset(
            x = Random.nextFloat() * maxWidth.value,
            y = Random.nextFloat() * maxHeight.value
        ),
        radius = Random.nextFloat() * 40 + 40, // 크기 약간 줄임
        velocityX = (Random.nextFloat() * 4 + 2) * difficulty,
        velocityY = (Random.nextFloat() * 4 + 2) * difficulty,
        color = Color(
            red = Random.nextInt(256),
            green = Random.nextInt(256),
            blue = Random.nextInt(256),
            alpha = 220
        )
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BubbleGameScreen()
                }
            }
        }
    }
}

// --- 게임 전체 화면 ---
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BubbleGameScreen() {
    val gameState = remember { GameState() }
    var showDialog by remember { mutableStateOf(false) }

    // 타이머 로직
    LaunchedEffect(gameState.isGameOver) {
        if (!gameState.isGameOver) {
            while (gameState.timeLeft > 0) {
                delay(1000L)
                gameState.timeLeft--

                // 난이도 증가 (10초마다)
                if (gameState.timeLeft % 10 == 0) {
                    gameState.difficulty += 0.2f
                }

                // 3초가 지난 버블 제거
                val currentTime = System.currentTimeMillis()
                gameState.bubbles = gameState.bubbles.filter {
                    currentTime - it.creationTime < 3000
                }

                // 게임 종료 처리
                if (gameState.timeLeft <= 0) {
                    gameState.isGameOver = true
                    if (gameState.score > gameState.highScore) {
                        gameState.highScore = gameState.score
                    }
                    showDialog = true
                    break
                }
            }
        }
    }

    // 콤보 초기화 (1초 동안 클릭 없으면)
    LaunchedEffect(gameState.lastClickTime) {
        if (gameState.lastClickTime > 0) {
            delay(1000L)
            val currentTime = System.currentTimeMillis()
            if (currentTime - gameState.lastClickTime >= 1000) {
                gameState.combo = 0
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 상단 상태 바
        GameStatusRow(
            score = gameState.score,
            timeLeft = gameState.timeLeft,
            combo = gameState.combo,
            highScore = gameState.highScore
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // 빈 공간 클릭 시 점수 감소 및 콤보 초기화
                    if (gameState.score > 0) {
                        gameState.score--
                    }
                    gameState.combo = 0
                }
        ) {
            val density = LocalDensity.current
            val canvasWidthPx = with(density) { maxWidth.toPx() }
            val canvasHeightPx = with(density) { maxHeight.toPx() }

            // 버블 물리 엔진
            LaunchedEffect(key1 = gameState.isGameOver) {
                if (!gameState.isGameOver) {
                    while (true) {
                        delay(16)

                        // 버블이 없으면 새로 3개 생성
                        if (gameState.bubbles.isEmpty()) {
                            val newBubbles = List(3) {
                                makeNewBubble(maxWidth, maxHeight, gameState.difficulty)
                            }
                            gameState.bubbles = newBubbles
                        }

                        // 새 버블 생성 (난이도에 따라 확률 증가)
                        val spawnChance = 0.03f + (gameState.difficulty * 0.01f)
                        if (Random.nextFloat() < spawnChance && gameState.bubbles.size < 20) {
                            val newBubble = makeNewBubble(maxWidth, maxHeight, gameState.difficulty)
                            gameState.bubbles = gameState.bubbles + newBubble
                        }

                        // 물리 엔진 로직
                        gameState.bubbles = updateBubblePositions(
                            gameState.bubbles,
                            canvasWidthPx,
                            canvasHeightPx,
                            density
                        )
                    }
                }
            }

            // 각 버블을 화면에 그림
            gameState.bubbles.forEach { bubble ->
                BubbleComposable(
                    bubble = bubble,
                    onClick = {
                        // 클릭 시 콤보 계산
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - gameState.lastClickTime < 1000) {
                            gameState.combo++
                        } else {
                            gameState.combo = 1
                        }
                        gameState.lastClickTime = currentTime

                        // 점수 계산 (콤보 보너스)
                        val points = 1 + (gameState.combo / 5)
                        gameState.score += points

                        // 버블 제거
                        gameState.bubbles = gameState.bubbles.filterNot { it.id == bubble.id }
                    }
                )
            }
        }
    }

    // 게임 종료 다이얼로그
    if (showDialog) {
        GameOverDialog(
            score = gameState.score,
            highScore = gameState.highScore,
            onRestart = {
                restartGame(gameState)
                showDialog = false
            },
            onExit = { showDialog = false }
        )
    }
}

// 버블 이동 계산 함수
fun updateBubblePositions(
    bubbles: List<Bubble>,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    density: Density
): List<Bubble> {
    return bubbles.map { bubble ->
        with(density) {
            val radiusPx = bubble.radius.dp.toPx()
            var xPx = bubble.position.x.dp.toPx()
            var yPx = bubble.position.y.dp.toPx()
            val vxPx = bubble.velocityX.dp.toPx()
            val vyPx = bubble.velocityY.dp.toPx()

            xPx += vxPx
            yPx += vyPx

            var newVx = bubble.velocityX
            var newVy = bubble.velocityY

            if (xPx < radiusPx || xPx > canvasWidthPx - radiusPx) newVx *= -1
            if (yPx < radiusPx || yPx > canvasHeightPx - radiusPx) newVy *= -1

            xPx = xPx.coerceIn(radiusPx, canvasWidthPx - radiusPx)
            yPx = yPx.coerceIn(radiusPx, canvasHeightPx - radiusPx)

            bubble.copy(
                position = Offset(
                    x = xPx.toDp().value,
                    y = yPx.toDp().value
                ),
                velocityX = newVx,
                velocityY = newVy
            )
        }
    }
}

@Composable
fun GameStatusRow(score: Int, timeLeft: Int, combo: Int, highScore: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Score: $score", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = "Time: ${timeLeft}s", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (combo > 1) {
                Text(
                    text = "COMBO x$combo 🔥",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B35)
                )
            }
            Text(
                text = "Best: $highScore",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

// 버블 UI (애니메이션 효과 추가)
@Composable
fun BubbleComposable(bubble: Bubble, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Canvas(
        modifier = Modifier
            .size((bubble.radius * 2).dp)
            .offset(x = bubble.position.x.dp, y = bubble.position.y.dp)
            .scale(0.95f + (scale * 0.05f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() }
                )
            }
    ) {
        // 외곽선
        drawCircle(
            color = bubble.color.copy(alpha = 0.5f),
            radius = size.width / 2 + 4f,
            center = center
        )
        // 메인 버블
        drawCircle(
            color = bubble.color,
            radius = size.width / 2,
            center = center
        )
        // 하이라이트 효과
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = size.width / 4,
            center = Offset(center.x - size.width / 6, center.y - size.width / 6)
        )
    }
}

@Composable
fun GameOverDialog(score: Int, highScore: Int, onRestart: () -> Unit, onExit: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("게임 오버!", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("당신의 점수: $score 점")
                if (score == highScore && score > 0) {
                    Text(
                        "🎉 최고 기록 달성! 🎉",
                        color = Color(0xFFFF6B35),
                        fontWeight = FontWeight.Bold
                    )
                } else if (highScore > 0) {
                    Text("최고 기록: $highScore 점")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onRestart) {
                Text("다시 시작")
            }
        },
        dismissButton = {
            TextButton(onClick = onExit) {
                Text("종료")
            }
        }
    )
}

// 게임 재시작 함수
fun restartGame(gameState: GameState) {
    gameState.score = 0
    gameState.timeLeft = 60
    gameState.isGameOver = false
    gameState.bubbles = emptyList()
    gameState.combo = 0
    gameState.difficulty = 1f
    gameState.lastClickTime = 0L
}

@Preview(showBackground = true)
@Composable
fun BubbleGamePreview() {
    MyApplicationTheme {
        BubbleGameScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun GameStatusPreview() {
    MyApplicationTheme {
        GameStatusRow(score = 150, timeLeft = 45, combo = 8, highScore = 200)
    }
}

@Preview(showBackground = true)
@Composable
fun GameOverDialogPreview() {
    MyApplicationTheme {
        GameOverDialog(
            score = 180,
            highScore = 200,
            onRestart = {},
            onExit = {}
        )
    }
}