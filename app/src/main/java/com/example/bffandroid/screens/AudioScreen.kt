package com.example.bffandroid.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bffandroid.R
import com.example.bffandroid.ui.theme.BffAndroidTheme
import com.example.bffandroid.ui.theme.GaretFontFamily
import com.example.bffandroid.viewmodel.VoiceVerificationViewModel
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun AudioScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onDone: () -> Unit = {},
    voiceVerificationViewModel: VoiceVerificationViewModel = viewModel()
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    var audioStage by remember { mutableStateOf(AudioStage.Prompt) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var hasAudioPermission by remember {
        mutableStateOf(
            if (isPreview) {
                true
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            }
        )
    }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var outputFile by remember { mutableStateOf<File?>(null) }
    var recordingStartedAt by remember { mutableLongStateOf(0L) }
    val voiceUiState = voiceVerificationViewModel.uiState

    BackHandler(onBack = onBack)

    fun resolveRecording() {
        val durationSeconds = ((SystemClock.elapsedRealtime() - recordingStartedAt) / 1000L).toInt()
        elapsedSeconds = durationSeconds.coerceAtMost(MAX_RECORDING_SECONDS)
        audioStage = if (durationSeconds >= MIN_SUCCESS_SECONDS) {
            AudioStage.Success
        } else {
            AudioStage.Retry
        }
        statusMessage = null
    }

    fun stopRecording() {
        val currentRecorder = recorder ?: return
        runCatching { currentRecorder.stop() }
            .onSuccess { resolveRecording() }
            .onFailure {
                audioStage = AudioStage.Retry
                statusMessage = "No worries! Let's try that again."
            }
        currentRecorder.reset()
        currentRecorder.release()
        recorder = null
    }

    fun startRecording() {
        if (!hasAudioPermission) return

        outputFile?.delete()
        val file = File.createTempFile("voice_sample_", ".m4a", context.cacheDir)
        outputFile = file
        elapsedSeconds = 0
        statusMessage = null

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44_100)
            setAudioEncodingBitRate(96_000)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }

        recordingStartedAt = SystemClock.elapsedRealtime()
        audioStage = AudioStage.Recording
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            runCatching { startRecording() }
                .onFailure {
                    audioStage = AudioStage.Retry
                    statusMessage = "Couldn't start recording. Please try again."
                }
        } else {
            statusMessage = "Microphone access is needed to record your voice."
        }
    }

    LaunchedEffect(audioStage) {
        if (audioStage != AudioStage.Recording) return@LaunchedEffect

        while (audioStage == AudioStage.Recording) {
            delay(200L)
            val elapsed = ((SystemClock.elapsedRealtime() - recordingStartedAt) / 1000L).toInt()
            elapsedSeconds = elapsed.coerceAtMost(MAX_RECORDING_SECONDS)
            if (elapsedSeconds >= MAX_RECORDING_SECONDS) {
                stopRecording()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { recorder?.stop() }
            recorder?.release()
            recorder = null
            outputFile?.delete()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AudioBackground)
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.women_avatar_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        AudioTopCopy(audioStage = audioStage, modifier = Modifier.align(Alignment.TopCenter))

        if (audioStage != AudioStage.Success) {
            AudioSpeechBubble(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 276.dp)
            )
        }

        when (audioStage) {
            AudioStage.Success -> {
                AudioSuccessIllustration(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-6).dp)
                )
                AudioPrimaryButton(
                    text = if (voiceUiState.isSubmitting) "Submitting..." else "Good to go!",
                    enabled = !voiceUiState.isSubmitting,
                    onClick = {
                        voiceVerificationViewModel.submitVoiceVerification(
                            file = outputFile,
                            onSuccess = onDone
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-52).dp)
                )
            }

            AudioStage.Recording -> {
                AudioRecordButton(
                    recording = true,
                    onClick = { stopRecording() },
                    modifier = Modifier.align(Alignment.Center).offset(y = 98.dp)
                )
                Text(
                    text = formatElapsedSeconds(elapsedSeconds),
                    color = Color.White,
                    fontSize = 22.sp,
                    lineHeight = 22.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center).offset(y = 222.dp)
                )
                Text(
                    text = "Recording...",
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Center).offset(y = 258.dp)
                )
                AudioPrivacyLine(modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-34).dp))
            }

            AudioStage.Prompt, AudioStage.Retry -> {
                AudioRecordButton(
                    recording = false,
                    onClick = {
                        if (hasAudioPermission) {
                            runCatching { startRecording() }
                                .onFailure {
                                    audioStage = AudioStage.Retry
                                    statusMessage = "Couldn't start recording. Please try again."
                                }
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier.align(Alignment.Center).offset(y = 98.dp)
                )
                Text(
                    text = if (audioStage == AudioStage.Retry) "Tap to Record again" else "Tap to Record",
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Center).offset(y = 218.dp)
                )
                AudioPrivacyLine(modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-34).dp))
            }
        }

        val messageText = voiceUiState.errorMessage ?: statusMessage
        if (!messageText.isNullOrBlank()) {
            Text(
                text = messageText,
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 48.dp)
                    .offset(y = (-6).dp)
            )
        }
    }
}

@Composable
private fun AudioTopCopy(audioStage: AudioStage, modifier: Modifier = Modifier) {
    val title = when (audioStage) {
        AudioStage.Prompt, AudioStage.Recording -> "Let's hear your voice"
        AudioStage.Retry -> "We couldn't recognize\nyour voice :("
        AudioStage.Success -> "Voice verified"
    }
    val subtitle = when (audioStage) {
        AudioStage.Prompt, AudioStage.Recording -> "Record a few seconds of your voice"
        AudioStage.Retry -> "No worries! Let's try that again."
        AudioStage.Success -> "Thanks! You're all set"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(top = 76.dp)
            .padding(horizontal = 32.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 28.sp,
            lineHeight = 40.sp,
            textAlign = TextAlign.Center,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = subtitle,
            color = Color.White,
            fontSize = 16.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AudioSpeechBubble(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 334.dp, height = 134.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.audio_chat_bubble),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .offset(y = (-8).dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AudioWaveIcon()
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Please say this sentence",
                    color = Color(0xFFFF6666),
                    fontSize = 15.sp,
                    lineHeight = 15.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "\"Hi, I'm excited to meet\nnew people!\"",
                color = Color.Black,
                fontSize = 18.sp,
                lineHeight = 30.sp,
                textAlign = TextAlign.Center,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AudioWaveIcon() {
    Canvas(modifier = Modifier.size(width = 22.dp, height = 18.dp)) {
        val lineColor = Color(0xFFFF6666)
        val stroke = 2.dp.toPx()
        val heights = listOf(0.35f, 0.7f, 1f, 0.55f, 0.8f)
        val spacing = size.width / 6f
        heights.forEachIndexed { index, factor ->
            val x = spacing * (index + 1)
            val barHeight = size.height * factor
            drawLine(
                color = lineColor,
                start = Offset(x, (size.height - barHeight) / 2f),
                end = Offset(x, (size.height + barHeight) / 2f),
                strokeWidth = stroke
            )
        }
    }
}

@Composable
private fun AudioRecordButton(
    recording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        if (recording) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            )
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f))
            )
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFFFF6F74))
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .border(6.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }
        }
    }
}

@Composable
private fun AudioPrivacyLine(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Your voice is safe with us and\nwill never be shared.",
            color = Color.White,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AudioSuccessIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(width = 356.dp, height = 291.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.audio_sparkles),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .border(width = 8.dp, color = Color.White, shape = CircleShape)
                .background(Color(0xFFF6D8DD)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = Color(0xFFF56286),
                modifier = Modifier.size(53.dp)
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.tick_mark),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-110).dp, y = 90.dp)
                .size(32.dp)
        )
    }
}

@Composable
private fun AudioPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier.clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(width = 204.dp, height = 58.dp)
                .offset(x = 4.dp, y = 5.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Box(
            modifier = Modifier
                .size(width = 204.dp, height = 58.dp)
                .clip(shape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.Black,
                fontSize = 18.sp,
                lineHeight = 18.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatElapsedSeconds(seconds: Int): String {
    val clamped = seconds.coerceIn(0, MAX_RECORDING_SECONDS)
    return "0:${clamped.toString().padStart(2, '0')}"
}

private enum class AudioStage {
    Prompt,
    Recording,
    Retry,
    Success
}

private const val MIN_SUCCESS_SECONDS = 3
private const val MAX_RECORDING_SECONDS = 6
private val AudioBackground = Color(0xFFFF7171)

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AudioScreenPreview() {
    BffAndroidTheme {
        AudioScreen()
    }
}
