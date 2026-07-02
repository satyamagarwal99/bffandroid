package com.gobff.getfriends.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gobff.getfriends.R
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.FreedokaFontFamily
import com.gobff.getfriends.ui.theme.GaretFontFamily
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val TruthDareOrange = Color(0xFFFD8461)
private val TruthDarePurple = Color(0xFF8A44EC)
private val TruthDareYellow = Color(0xFFF5B120)
private const val WheelSegmentStartOffset = 18f

private data class TruthDarePlayer(
    val name: String,
    val avatarRes: Int,
    val rating: String,
    val languages: List<String>
)

private enum class TruthDarePhase {
    Lobby,
    Loading,
    Game
}

private enum class TruthDareGameStage {
    Spin,
    Result,
    Choice,
    Question,
    AskOwn,
    Accepted,
    Rejected
}

private enum class TruthDareKind(val label: String) {
    Truth("TRUTH"),
    Dare("DARE")
}

private data class TruthDareWheelSegment(
    val label: String,
    val color: Color
)

@Composable
fun TruthDareScreen(
    modifier: Modifier = Modifier,
    walletHearts: Int = 0,
    onBack: () -> Unit = {},
    onRechargeRequested: () -> Unit = {}
) {
    var phase by remember { mutableStateOf(TruthDarePhase.Lobby) }

    BackHandler {
        when (phase) {
            TruthDarePhase.Lobby -> onBack()
            TruthDarePhase.Loading -> phase = TruthDarePhase.Lobby
            TruthDarePhase.Game -> onBack()
        }
    }

    when (phase) {
        TruthDarePhase.Lobby -> TruthDareLobbyScreen(
            walletHearts = walletHearts,
            onBack = onBack,
            onRechargeRequested = onRechargeRequested,
            onPlay = { phase = TruthDarePhase.Loading },
            modifier = modifier
        )

        TruthDarePhase.Loading -> TruthDareLoadingScreen(
            onFinished = { phase = TruthDarePhase.Game },
            modifier = modifier
        )

        TruthDarePhase.Game -> TruthDareGameScreen(
            onBack = onBack,
            modifier = modifier
        )
    }
}

@Composable
private fun TruthDareLobbyScreen(
    walletHearts: Int,
    onBack: () -> Unit,
    onRechargeRequested: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val players = remember {
        listOf(
            TruthDarePlayer("Anshu", R.drawable.women_avatar3, "4.6", listOf("தமிழ்", "हिंदी")),
            TruthDarePlayer("Meera", R.drawable.women_avatar5, "4.9", listOf("English", "বাংলা")),
            TruthDarePlayer("Rose", R.drawable.women_avatar8, "4.4", listOf("తెలుగు", "मराठी")),
            TruthDarePlayer("Nisha", R.drawable.women_avatar7, "4.8", listOf("മലയാളം", "ગુજરાતી")),
            TruthDarePlayer("Raj", R.drawable.women_avatar6, "4.4", listOf("తెలుగు", "मराठी"))
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            TruthDareLobbyHeader(
                walletHearts = walletHearts,
                onBack = onBack,
                onRechargeRequested = onRechargeRequested
            )

            Spacer(modifier = Modifier.height(40.dp))
            TruthDareSearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(48.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(28.dp),
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                players.forEach { player ->
                    TruthDarePlayerCard(
                        player = player,
                        onPlay = onPlay
                    )
                }
            }
            Spacer(modifier = Modifier.height(34.dp))
        }
    }
}

@Composable
private fun TruthDareLobbyHeader(
    walletHearts: Int,
    onBack: () -> Unit,
    onRechargeRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFFC9071), TruthDareOrange)
                )
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.truth_dare_bg_object),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 48.dp)
                .size(28.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack
                )
        )
        TruthDareHeartChip(
            hearts = String.format("%,d", walletHearts),
            onClick = onRechargeRequested,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 20.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.truth_dare_heading),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 112.dp)
                .size(width = 423.dp, height = 76.dp),
            contentScale = ContentScale.Fit
        )
        TruthDarePricePill(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 196.dp)
        )
    }
}

@Composable
private fun TruthDarePricePill(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Color(0xFFFFD861))
            .padding(horizontal = 14.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.single_heart),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "35 / Game",
            color = Color.Black,
            fontSize = 13.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TruthDareSearchBar(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(80.dp)
    Box(modifier = modifier.height(51.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 3.5.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .border(1.4.dp, Color.Black, shape)
                .padding(horizontal = 20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF777777),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Search by name or language...",
                color = Color(0xFFAAAAAA),
                fontSize = 15.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
        QuestionSparkle(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 42.dp, y = (-12).dp)
        )
    }
}

@Composable
private fun TruthDarePlayerCard(
    player: TruthDarePlayer,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Box(modifier = modifier.fillMaxWidth().height(78.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 3.5.dp)
                .clip(shape)
                .background(Color(0xFF3B2200))
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE3E3E3), shape)
                .padding(horizontal = 16.dp)
        ) {
            Box {
                Image(
                    painter = painterResource(id = player.avatarRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF27C756))
                        .border(1.5.dp, Color.White, CircleShape)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = player.name,
                        color = Color.Black,
                        fontSize = 17.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    RatingChip(player.rating)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    player.languages.forEachIndexed { index, language ->
                        LanguageTag(
                            text = language,
                            color = if (index == 0) Color(0xFF28B7A9) else Color(0xFFB93A98)
                        )
                    }
                }
            }
            ShadowPlayButton(onClick = onPlay)
        }
    }
}

@Composable
private fun RatingChip(rating: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(18.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFF7DE))
            .border(0.8.dp, Color(0xFFF5B120), RoundedCornerShape(20.dp))
            .padding(horizontal = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "★",
                color = Color(0xFFF5B120),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 10.sp
            )

            Spacer(modifier = Modifier.width(2.dp))

            Text(
                text = rating,
                color = Color(0xFFB77700),
                fontSize = 9.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                lineHeight = 9.sp
            )
        }
    }
}

@Composable
private fun LanguageTag(text: String, color: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, color, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 10.sp
        )
    }
}

@Composable
private fun ShadowPlayButton(onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = 92.dp, height = 38.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFC86CFF), Color(0xFFA842EF))
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Text(
            text = "Play",
            color = Color.White,
            fontSize = 18.sp,
            fontFamily = FreedokaFontFamily,
            fontWeight = FontWeight.Bold,
            style = TextStyle(
                shadow = Shadow(Color(0xFF6A259B), offset = Offset(1f, 2f), blurRadius = 0f)
            )
        )
    }
}

@Composable
private fun TruthDareLoadingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var count by remember { mutableIntStateOf(3) }

    LaunchedEffect(Unit) {
        while (count > 1) {
            delay(1_000)
            count -= 1
        }
        delay(1_000)
        onFinished()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF9656F0), TruthDarePurple)
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(TruthDareYellow)
            )
        }
        TruthDareLoadingWave(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 2.dp)
                .fillMaxWidth()
                .height(106.dp)
        )
        LoadingProfile(
            name = "Anshu",
            avatarRes = R.drawable.women_avatar8,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 132.dp)
        )
        CountdownBadge(
            count = count,
            modifier = Modifier.align(Alignment.Center)
        )
        LoadingProfile(
            name = "Akash",
            avatarRes = R.drawable.man_avatar1,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 130.dp)
        )
    }
}

@Composable
private fun LoadingProfile(
    name: String,
    avatarRes: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = avatarRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(132.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(4.dp, Color.White, RoundedCornerShape(18.dp))
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = name,
            color = Color.White,
            fontSize = 34.sp,
            fontFamily = FreedokaFontFamily,
            fontWeight = FontWeight.Bold,
            style = TextStyle(
                shadow = Shadow(Color.Black, offset = Offset(2f, 3f), blurRadius = 0f)
            )
        )
    }
}

@Composable
private fun CountdownBadge(count: Int, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(Color.White)
    ) {
        Text(
            text = count.toString(),
            color = Color(0xFFF64E3F),
            fontSize = 50.sp,
            fontFamily = FreedokaFontFamily,
            fontWeight = FontWeight.Bold,
            style = TextStyle(
                shadow = Shadow(Color.Black, offset = Offset(2f, 3f), blurRadius = 0f)
            )
        )
    }
}

@Composable
private fun TruthDareLoadingWave(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val wave = Path().apply {
            moveTo(0f, size.height * 0.42f)
            cubicTo(
                size.width * 0.25f,
                size.height * 0.42f,
                size.width * 0.30f,
                size.height * 0.10f,
                size.width * 0.50f,
                size.height * 0.22f
            )
            cubicTo(
                size.width * 0.68f,
                size.height * 0.33f,
                size.width * 0.72f,
                size.height * 0.54f,
                size.width,
                size.height * 0.32f
            )
            lineTo(size.width, 0f)
            lineTo(0f, 0f)
            close()
        }
        drawPath(wave, Color.White)
    }
}

@Composable
private fun TruthDareGameScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var secondsRemaining by remember { mutableIntStateOf(299) }
    var selectedResult by remember { mutableStateOf<String?>(null) }
    var previousSpinResult by remember { mutableStateOf<String?>(null) }
    var sameResultStreak by remember { mutableIntStateOf(0) }
    var gameStage by remember { mutableStateOf(TruthDareGameStage.Spin) }
    var activeKind by remember { mutableStateOf(TruthDareKind.Truth) }
    var activePrompt by remember { mutableStateOf("What's the most embarrassing thing you've done in public?") }
    val wheelRotation = remember { Animatable(0f) }
    val segments = remember {
        listOf(
            TruthDareWheelSegment("DARE", Color(0xFFFF526B)),
            TruthDareWheelSegment("TRUTH", Color(0xFF8C42EC)),
            TruthDareWheelSegment("DARE", Color(0xFFFF526B)),
            TruthDareWheelSegment("TRUTH", Color(0xFF8C42EC)),
            TruthDareWheelSegment("DARE", Color(0xFFFF526B)),
            TruthDareWheelSegment("TRUTH", Color(0xFF8C42EC)),
            TruthDareWheelSegment("DARE", Color(0xFFFF526B)),
            TruthDareWheelSegment("TRUTH", Color(0xFF8C42EC)),
            TruthDareWheelSegment("DARE", Color(0xFFFF526B)),
            TruthDareWheelSegment("TRUTH", Color(0xFF8C42EC)),
            TruthDareWheelSegment("DARE", Color(0xFFFF526B)),
            TruthDareWheelSegment("TRUTH", Color(0xFF8C42EC)),
            TruthDareWheelSegment("DARE", Color(0xFFFF526B)),
            TruthDareWheelSegment("TRUTH", Color(0xFF8C42EC)),
            TruthDareWheelSegment("DARE", Color(0xFFFF526B)),
            TruthDareWheelSegment("TRUTH", Color(0xFF8C42EC))
        )
    }
    var isSpinning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1_000)
            secondsRemaining -= 1
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TruthDareYellow)
    ) {
        Image(
            painter = painterResource(id = R.drawable.call_screen_bg_object),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        if (gameStage == TruthDareGameStage.Spin ||
            gameStage == TruthDareGameStage.Result ||
            gameStage == TruthDareGameStage.Choice
        ) {
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 158.dp)
                    .fillMaxWidth()
                    .height(330.dp)
            ) {
                TruthDareWheel(
                    segments = segments,
                    rotation = wheelRotation.value,
                    modifier = Modifier
                        .size(width = 820.dp, height = 330.dp)
                )
            }
        }


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 58.dp)
        ) {
            GameTimerChip(secondsRemaining = secondsRemaining)
            Spacer(modifier = Modifier.weight(1f))
            GameScoreBoard()
            Spacer(modifier = Modifier.weight(1f))
            QuitButton(onClick = onBack)
        }

        if (gameStage == TruthDareGameStage.Spin ||
            gameStage == TruthDareGameStage.Result ||
            gameStage == TruthDareGameStage.Choice
        ) {
            FixedWheelPointer(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 374.dp)
            )
        }

        when (gameStage) {
            TruthDareGameStage.Spin -> {
                Text(
                    text = if (isSpinning) "The wheel is deciding Akash's fate" else "It's your turn",
                    color = Color.White,
                    fontSize = if (isSpinning) 22.sp else 30.sp,
                    fontFamily = FreedokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        shadow = Shadow(Color.Black, offset = Offset(2f, 3f), blurRadius = 0f)
                    ),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 114.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                )

                SpinButton(
                    isSpinning = isSpinning,
                    onClick = {
                        if (!isSpinning) {
                            selectedResult = null
                            gameStage = TruthDareGameStage.Spin
                            isSpinning = true
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 230.dp)
                )
            }

            TruthDareGameStage.Result -> {
                TruthDareResultCard(
                    kind = activeKind,
                    chooserName = "Alia",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 120.dp)
                )
            }

            TruthDareGameStage.Choice -> {
                TruthDareChoicePanel(
                    kind = activeKind,
                    onAskOwn = {
                        gameStage = TruthDareGameStage.AskOwn
                    },
                    onPickForMe = {
                        activePrompt = randomTruthDarePrompt(activeKind)
                        gameStage = TruthDareGameStage.Question
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 190.dp)
                )
            }

            TruthDareGameStage.Question -> {
                TruthDareQuestionState(
                    kind = activeKind,
                    prompt = activePrompt,
                    isFriendAsking = false,
                    onAccepted = { gameStage = TruthDareGameStage.Accepted },
                    onRejected = { gameStage = TruthDareGameStage.Rejected },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            TruthDareGameStage.AskOwn -> {
                TruthDareQuestionState(
                    kind = activeKind,
                    prompt = if (activeKind == TruthDareKind.Truth) {
                        "What do you want to know?"
                    } else {
                        "What challenge do you have in mind?"
                    },
                    isFriendAsking = true,
                    onAccepted = { gameStage = TruthDareGameStage.Accepted },
                    onRejected = { gameStage = TruthDareGameStage.Rejected },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            TruthDareGameStage.Accepted -> {
                TruthDareEndState(
                    accepted = true,
                    kind = activeKind,
                    playerName = "Akash",
                    onNextRound = {
                        selectedResult = null
                        gameStage = TruthDareGameStage.Spin
                    },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            TruthDareGameStage.Rejected -> {
                TruthDareEndState(
                    accepted = false,
                    kind = activeKind,
                    playerName = "Akash",
                    onNextRound = {
                        selectedResult = null
                        gameStage = TruthDareGameStage.Spin
                    },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        LaunchedEffect(isSpinning) {
            if (!isSpinning) return@LaunchedEffect
            val targetLabel = when {
                sameResultStreak >= 2 && previousSpinResult == "DARE" -> "TRUTH"
                sameResultStreak >= 2 && previousSpinResult == "TRUTH" -> "DARE"
                else -> if (Random.nextBoolean()) "DARE" else "TRUTH"
            }
            val targetIndices = segments
                .mapIndexedNotNull { index, segment -> index.takeIf { segment.label == targetLabel } }
            val targetIndex = targetIndices.random()
            val sweep = 360f / segments.size
            val targetCenter = targetIndex * sweep + sweep / 2f
            val current = wheelRotation.value
            val normalizedCurrent = positiveModulo(current, 360f)
            val desiredModulo = positiveModulo(90f - WheelSegmentStartOffset - targetCenter, 360f)
            val extra = positiveModulo(desiredModulo - normalizedCurrent, 360f)
            val targetRotation = current + 360f * Random.nextInt(5, 7) + extra

            wheelRotation.animateTo(
                targetValue = targetRotation,
                animationSpec = tween(
                    durationMillis = 3_200,
                    easing = FastOutSlowInEasing
                )
            )
            val landedIndex = detectWheelSegmentIndex(
                rotation = wheelRotation.value,
                segmentCount = segments.size
            )
            selectedResult = segments[landedIndex].label
            activeKind = if (selectedResult == "DARE") TruthDareKind.Dare else TruthDareKind.Truth
            activePrompt = randomTruthDarePrompt(activeKind)
            sameResultStreak = if (selectedResult == previousSpinResult) {
                sameResultStreak + 1
            } else {
                1
            }
            previousSpinResult = selectedResult
            gameStage = TruthDareGameStage.Result
            isSpinning = false
        }

        LaunchedEffect(gameStage, activeKind) {
            if (gameStage == TruthDareGameStage.Result) {
                delay(900)
                gameStage = TruthDareGameStage.Choice
            }
        }

        TruthDareCallControls(
            onEnd = onBack,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        )
    }
}

@Composable
private fun TruthDareResultCard(
    kind: TruthDareKind,
    chooserName: String,
    modifier: Modifier = Modifier
) {
    val isTruth = kind == TruthDareKind.Truth
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(width = 210.dp, height = 132.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.26f))
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(16.dp))
                .background(if (isTruth) Color(0xFF8C42EC) else Color(0xFFFF6478))
                .border(4.dp, Color.White, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.td_question_bg),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = if (isTruth) R.drawable.td_truth else R.drawable.td_dare),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = "${kind.label}!",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontFamily = FreedokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        shadow = Shadow(Color.Black, offset = Offset(2f, 3f), blurRadius = 0f)
                    )
                )
                Text(
                    text = "$chooserName is choosing ${if (isTruth) "a question" else "a dare"} for you...",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun TruthDareChoicePanel(
    kind: TruthDareKind,
    onAskOwn: () -> Unit,
    onPickForMe: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp)
    ) {
        Text(
            text = if (kind == TruthDareKind.Truth) {
                "Ask your own question or let us pick one for you"
            } else {
                "Ask it yourself or let us pick one"
            },
            color = Color(0xFF5B4218),
            fontSize = 11.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        TruthDareActionButton(
            text = if (kind == TruthDareKind.Truth) "I'll Ask One" else "I'll Give One",
            color = Color(0xFF8C42EC),
            onClick = onAskOwn
        )
        Spacer(modifier = Modifier.height(10.dp))
        TruthDareActionButton(
            text = if (kind == TruthDareKind.Truth) "Pick for Me" else "Suggest Dares",
            color = Color(0xFFFF5F94),
            onClick = onPickForMe
        )
    }
}

@Composable
private fun TruthDareActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.22f))
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(10.dp))
                .background(color)
                .border(2.dp, Color.White, RoundedCornerShape(10.dp))
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 17.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TruthDareQuestionState(
    kind: TruthDareKind,
    prompt: String,
    isFriendAsking: Boolean,
    onAccepted: () -> Unit,
    onRejected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .offset(y = (-10).dp)
    ) {
        TruthDareQuestionChip(kind = kind)
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.size(width = 236.dp, height = 244.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.td_question_bg),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Image(
                painter = painterResource(id = R.drawable.td_eyes),
                contentDescription = null,
                modifier = Modifier
                    .offset(y = (-28).dp)
                    .size(width = 58.dp, height = 38.dp),
                contentScale = ContentScale.Fit
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 26.dp)
            ) {
                if (isFriendAsking) {
                    Image(
                        painter = painterResource(id = if (kind == TruthDareKind.Truth) R.drawable.td_mic else R.drawable.td_dare),
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Text(
                    text = if (isFriendAsking) {
                        if (kind == TruthDareKind.Truth) "Your friend is asking a question..." else "Your friend is giving you a dare..."
                    } else {
                        prompt
                    },
                    color = Color.White,
                    fontSize = if (isFriendAsking) 17.sp else 23.sp,
                    fontFamily = FreedokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isFriendAsking) 20.sp else 27.sp
                )
                if (isFriendAsking) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (kind == TruthDareKind.Truth) {
                            "Listen carefully and answer honestly."
                        } else {
                            "Listen carefully and get ready."
                        },
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(22.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TruthDareDecisionButton(
                text = if (kind == TruthDareKind.Truth) "Answered" else "Completed",
                color = Color(0xFF28BF4B),
                onClick = onAccepted,
                modifier = Modifier.weight(1f)
            )
            TruthDareDecisionButton(
                text = "Refused",
                color = Color(0xFFFF5252),
                onClick = onRejected,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TruthDareQuestionChip(kind: TruthDareKind, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF8C42EC))
            .border(2.dp, Color.White, RoundedCornerShape(22.dp))
            .padding(horizontal = 22.dp)
    ) {
        Image(
            painter = painterResource(id = if (kind == TruthDareKind.Truth) R.drawable.td_truth else R.drawable.td_dare),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (kind == TruthDareKind.Truth) "Truth Question" else "Dare Challenge",
            color = Color.White,
            fontSize = 14.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TruthDareDecisionButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(42.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 3.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.20f))
        )
        Text(
            text = "✓ $text",
            color = Color.White,
            fontSize = 13.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                .padding(top = 11.dp)
        )
    }
}

@Composable
private fun TruthDareEndState(
    accepted: Boolean,
    kind: TruthDareKind,
    playerName: String,
    onNextRound: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .offset(y = (-16).dp)
    ) {
        Image(
            painter = painterResource(id = if (accepted) R.drawable.td_accepted else R.drawable.td_rejected),
            contentDescription = null,
            modifier = Modifier.size(156.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = if (accepted) {
                if (kind == TruthDareKind.Truth) "Nice!" else "Challenge Complete!"
            } else {
                if (kind == TruthDareKind.Truth) "We'll never know..." else "Challenge Skipped"
            },
            color = if (accepted) Color(0xFF8C42EC) else Color(0xFFFF3F91),
            fontSize = 30.sp,
            fontFamily = FreedokaFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp,
            style = TextStyle(
                shadow = Shadow(Color.White, offset = Offset(1f, 2f), blurRadius = 0f)
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (accepted) {
                "$playerName earned 1 point"
            } else {
                if (kind == TruthDareKind.Truth) "No point this round" else "$playerName skipped the dare."
            },
            color = Color.White,
            fontSize = 15.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            style = TextStyle(
                shadow = Shadow(Color.Black, offset = Offset(1f, 2f), blurRadius = 0f)
            )
        )
        Spacer(modifier = Modifier.height(22.dp))
        TruthDareActionButton(
            text = "Next round",
            color = Color(0xFF8C42EC),
            onClick = onNextRound,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 44.dp)
        )
    }
}

@Composable
private fun GameTimerChip(secondsRemaining: Int, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Box(modifier = modifier.size(width = 88.dp, height = 38.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 3.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .border(1.5.dp, Color.Black, shape)
        ) {
            Text(
                text = formatGameTime(secondsRemaining),
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color(0xFFFF5A5A),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun GameScoreBoard(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        ScoreAvatar(R.drawable.women_avatar11)
        Text(
            text = "0",
            color = Color.Black,
            fontSize = 22.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        ScoreAvatar(R.drawable.man_avatar1)
        Text(
            text = "0",
            color = Color.Black,
            fontSize = 22.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun ScoreAvatar(avatarRes: Int) {
    Image(
        painter = painterResource(id = avatarRes),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(4.dp))
    )
}

@Composable
private fun QuitButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(width = 86.dp, height = 38.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 3.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .border(1.5.dp, Color.Black, shape)
        ) {
            Text(
                text = "Quit",
                color = Color.Black,
                fontSize = 17.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color(0xFFFF5A5A),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TruthDareWheel(
    segments: List<TruthDareWheelSegment>,
    rotation: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, -590f)
        val radius = size.width * 1.15f
        val topLeft = Offset(center.x - radius, center.y - radius)
        val wheelSize = Size(radius * 2f, radius * 2f)
        val sweep = 360f / segments.size
        val separatorColor = Color(0xFFFFF2D2)
        val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 22.sp.toPx()
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            )
            setShadowLayer(0f, 2f, 3f, android.graphics.Color.BLACK)
        }

        drawCircle(
            color = Color.Black.copy(alpha = 0.18f),
            radius = radius * 1.055f,
            center = center.copy(y = center.y + 8.dp.toPx())
        )

        rotate(rotation, pivot = center) {
            drawCircle(
                color = separatorColor,
                radius = radius * 1.045f,
                center = center
            )

            segments.forEachIndexed { index, segment ->
                val startAngle = index * sweep + WheelSegmentStartOffset
                drawArc(
                    color = segment.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = topLeft,
                    size = wheelSize
                )

                val boundaryAngle = Math.toRadians((startAngle + sweep).toDouble())
                val lineStart = Offset(
                    x = center.x + cos(boundaryAngle).toFloat() * radius * 0.12f,
                    y = center.y + sin(boundaryAngle).toFloat() * radius * 0.12f
                )
                val lineEnd = Offset(
                    x = center.x + cos(boundaryAngle).toFloat() * radius,
                    y = center.y + sin(boundaryAngle).toFloat() * radius
                )
                drawLine(
                    color = separatorColor,
                    start = lineStart,
                    end = lineEnd,
                    strokeWidth = 8.dp.toPx()
                )

                val labelAngle = startAngle + sweep / 2f
                val radians = Math.toRadians(labelAngle.toDouble())
                val labelRadius = radius * 0.82f
                val labelCenter = Offset(
                    x = center.x + cos(radians).toFloat() * labelRadius,
                    y = center.y + sin(radians).toFloat() * labelRadius
                )
                drawIntoCanvas { canvas ->
                    val nativeCanvas = canvas.nativeCanvas
                    nativeCanvas.save()
                    nativeCanvas.rotate(labelAngle - 90f, labelCenter.x, labelCenter.y)
                    nativeCanvas.drawText(segment.label, labelCenter.x, labelCenter.y, textPaint)
                    nativeCanvas.restore()
                }
            }

            drawCircle(
                color = separatorColor,
                radius = radius * 0.64f,
                center = center
            )
            drawCircle(
                color = TruthDareYellow,
                radius = radius * 0.58f,
                center = center
            )
        }
    }
}


@Composable
private fun FixedWheelPointer(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(82.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.game_pointer),
            contentDescription = null,
            modifier = Modifier.size(78.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun SpinButton(
    isSpinning: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(width = 154.dp, height = 64.dp)
            .clickable(
                enabled = !isSpinning,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 5.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(Color.Black.copy(alpha = 0.28f))
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(34.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFD774FF), Color(0xFF9A3EEE))
                    )
                )
                .border(5.dp, Color.White, RoundedCornerShape(34.dp))
        ) {
            Text(
                text = if (isSpinning) "..." else "SPIN",
                color = Color.White,
                fontSize = 28.sp,
                fontFamily = FreedokaFontFamily,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    shadow = Shadow(Color.Black, offset = Offset(2f, 3f), blurRadius = 0f)
                )
            )
        }
    }
}

@Composable
private fun TruthDareCallControls(
    onEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 34.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(2.dp, Color.Black, RoundedCornerShape(18.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.call_screen_mic),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.call_screen_speaker),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.call_screen_camera),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            contentScale = ContentScale.Fit
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 88.dp, height = 44.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFFF64A4A))
                .clickable(onClick = onEnd)
        ) {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

private fun formatGameTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "$minutes : ${remainingSeconds.toString().padStart(2, '0')}"
}

private fun detectWheelSegmentIndex(rotation: Float, segmentCount: Int): Int {
    val sweep = 360f / segmentCount
    val pointerAngle = positiveModulo(90f - rotation - WheelSegmentStartOffset, 360f)
    return (pointerAngle / sweep).toInt().coerceIn(0, segmentCount - 1)
}

private fun positiveModulo(value: Float, modulo: Float): Float {
    return ((value % modulo) + modulo) % modulo
}

private fun randomTruthDarePrompt(kind: TruthDareKind): String {
    val prompts = if (kind == TruthDareKind.Truth) {
        listOf(
            "What's the most embarrassing thing you've done in public?",
            "What is one secret you've never told a friend?",
            "Who was your first crush?",
            "What's the funniest lie you ever told?",
            "What is one thing you are scared to admit?"
        )
    } else {
        listOf(
            "Do your best impression of a movie villain for 30 seconds.",
            "Sing the chorus of your favorite song.",
            "Make a funny face and hold it for 10 seconds.",
            "Speak in a dramatic movie voice for one round.",
            "Tell a joke without laughing."
        )
    }
    return prompts.random()
}

@Composable
private fun TruthDareHeartChip(
    hearts: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .size(width = 88.dp, height = 32.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 1.5.dp, y = 2.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .border(1.2.dp, Color.Black, shape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.single_heart),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = hearts,
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun QuestionSparkle(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val path = Path().apply {
            moveTo(center.x, 0f)
            quadraticTo(center.x + 3f, center.y - 3f, size.width, center.y)
            quadraticTo(center.x + 3f, center.y + 3f, center.x, size.height)
            quadraticTo(center.x - 3f, center.y + 3f, 0f, center.y)
            quadraticTo(center.x - 3f, center.y - 3f, center.x, 0f)
            close()
        }
        drawPath(path, color = Color(0xFFFFD33F))
        drawPath(path, color = Color.Black, style = Stroke(width = 1.5f))
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
//@Composable
//private fun TruthDareScreenPreview() {
//    BffAndroidTheme {
//        TruthDareScreen()
//    }
//}
@Composable
private fun TruthDareGameScreenPreview() {
    TruthDareGameScreen(
        onBack = {}
    )
}
