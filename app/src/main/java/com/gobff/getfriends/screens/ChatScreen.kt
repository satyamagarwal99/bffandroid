package com.gobff.getfriends.screens

import android.graphics.Paint
import android.graphics.Path as AndroidPath
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gobff.getfriends.R
import com.gobff.getfriends.ui.component.BffHeartChip
import com.gobff.getfriends.ui.component.ChatBubbleShape
import com.gobff.getfriends.ui.component.HandDrawnCardShape
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.FreedokaFontFamily
import com.gobff.getfriends.ui.theme.GaretFontFamily

private val ChatBlue = Color(0xFF335FAA)
private val PersonalChatBlue = Color(0xFF335FAA)
private val PersonalChatChromeBlue = Color(0xFF4C7FD0)
private val ChatYellow = Color(0xFFFDCE4E)


@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    walletHearts: Int = 0,
    onBack: () -> Unit = {},
    onRechargeRequested: () -> Unit = {},
    onChatSelected: (String, Int) -> Unit = { _, _ -> },
) {
    BackHandler(onBack = onBack)

    var searchQuery by remember { mutableStateOf("") }
    val messages = remember { chatMessages() }
    val filteredMessages = remember(searchQuery, messages) {
        if (searchQuery.isBlank()) {
            messages
        } else {
            messages.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
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
            Spacer(modifier = Modifier.height(48.dp))
            ChatTopBar(
                walletHearts = walletHearts,
                onBack = onBack,
                onRechargeRequested = onRechargeRequested,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(34.dp))
            ChatHeader()
            Spacer(modifier = Modifier.height(2.dp))
            ChatContentCard(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                messages = filteredMessages,
                onChatSelected = onChatSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 636.dp)
            )
        }
    }
}

@Composable
private fun ChatTopBar(
    walletHearts: Int,
    onBack: () -> Unit,
    onRechargeRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.Black,
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack
                )
        )
        BffHeartChip(
            hearts = walletHearts,
            onClick = onRechargeRequested
        )
    }
}

@Composable
private fun ChatHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            ChatHeaderSparkle()
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CHAT",
                color = ChatBlue,
                fontSize = 32.sp,
                lineHeight = 32.sp,
                letterSpacing = 0.64.sp,
                fontFamily = FreedokaFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "VIBES",
                color = Color(0xFFF7BC36),
                fontSize = 32.sp,
                lineHeight = 32.sp,
                letterSpacing = 0.64.sp,
                fontFamily = FreedokaFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(8.dp))
            ChatHeaderSparkle()
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Where conversations happen",
            color = ChatBlue,
            fontSize = 13.sp,
            lineHeight = 13.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ChatHeaderSparkle() {
    Image(
        painter = painterResource(id = R.drawable.gift_vibe_sparkle),
        contentDescription = null,
        modifier = Modifier.size(22.dp),
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(ChatBlue)
    )
}

@Composable
private fun ChatContentCard(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    messages: List<ChatMessage>,
    onChatSelected: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(ChatBlue)
    ) {
        Image(
            painter = painterResource(id = R.drawable.chat_screen_background_object),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, top = 46.dp, end = 22.dp, bottom = 112.dp)
        ) {
            ChatSearchBar(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth()
            )
            if (messages.isEmpty()) {
                ChatEmptyState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(548.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(42.dp))
                ChatMessageList(
                    messages = messages,
                    onChatSelected = onChatSelected,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ChatSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(51.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 5.dp)
                .clip(shape)
                .background(Color(0xFF163E99))
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .border(1.4.dp, Color(0xFF4B6EFF), shape)
                .padding(horizontal = 18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF7D7D7D),
                modifier = Modifier.size(23.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = "Search by name...",
                        color = Color(0xFFA4A4A4),
                        fontSize = 15.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    cursorBrush = SolidColor(Color(0xFF1B1A1A)),
                    textStyle = TextStyle(
                        color = Color(0xFF1B1A1A),
                        fontSize = 15.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        QuestionSparkle(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 42.dp, y = (-12).dp)
        )
    }
}

@Composable
private fun CurvedChatTagline(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(
            width = 230.dp,
            height = 38.dp
        )
    ) {

        val text = "Where conversations happen"

        val path = AndroidPath().apply {
            moveTo(
                10f,
                size.height * 0.65f
            )

            quadTo(
                size.width / 2f,
                -12f,   // curve depth (increase for more curve)
                size.width - 10f,
                size.height * 0.65f
            )
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = 13.sp.toPx()

            // spacing between letters
            letterSpacing = 0.015f
        }

        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawTextOnPath(
                text,
                path,
                0f,
                0f,
                paint
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
        drawPath(path, color = Color(0xFF4B6EFF), style = Stroke(width = 1.5f))
    }
}

@Composable
private fun ChatMessageList(
    messages: List<ChatMessage>,
    onChatSelected: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Messages",
            color = Color.White,
            fontSize = 17.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            messages.forEach { message ->
                ChatMessageRow(
                    message = message,
                    onClick = { onChatSelected(message.name, message.avatarRes) }
                )
            }
        }
    }
}

@Composable
private fun ChatMessageRow(
    message: ChatMessage,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(modifier = Modifier.size(54.dp)) {
            Image(
                painter = painterResource(id = message.avatarRes),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            if (message.isOnline) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF31C75B))
                        .border(2.dp, Color.White, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = message.name,
                color = Color.White,
                fontSize = 15.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message.preview,
                color = Color.White.copy(alpha = 0.52f),
                fontSize = 13.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.height(60.dp)
        ) {
            Text(
                text = message.time,
                color = if (message.unreadCount > 0)
                    Color(0xFFFFC43B)
                else
                    Color.White.copy(alpha = 0.46f),
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (message.unreadCount > 0) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFC43B))
                ) {
                    Text(
                        text = message.unreadCount.toString(),
                        color = Color.Black,
                        fontSize = 11.sp,
                        lineHeight = 11.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatEmptyState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(bottom = 18.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.chat_screen_people),
            contentDescription = null,
            modifier = Modifier.size(width = 233.95.dp, height = 200.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(34.dp))
        Text(
            text = "Connect with people who match your",
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "vibe and start a conversation.",
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(28.dp))
        ChatPrimaryButton(text = "Make a New Friend")
    }
}

@Composable
private fun ChatPrimaryButton(text: String) {
    val shape = RoundedCornerShape(14.dp)
    Box(modifier = Modifier.size(width = 214.dp, height = 54.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 5.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color(0xFFFFCC48))
        ) {
            Text(
                text = text,
                color = Color.Black,
                fontSize = 15.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PersonalChatScreen(
    personName: String,
    avatarRes: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    BackHandler(onBack = onBack)

    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PersonalChatBlue)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(106.dp)
                .background(PersonalChatChromeBlue)
        ) {
            PersonalChatHeader(
                personName = personName,
                avatarRes = avatarRes,
                onBack = onBack,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(PersonalChatBlue)
        ) {
            Image(
                painter = painterResource(id = R.drawable.chat_screen_background_object),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            PersonalMessageList(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 42.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(PersonalChatChromeBlue)
        ) {
            PersonalChatComposer(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun PersonalChatHeader(
    personName: String,
    avatarRes: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.Black,
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack
                )
        )
        Spacer(modifier = Modifier.width(18.dp))
        Image(
            painter = painterResource(id = avatarRes),
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = personName,
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Online now",
                color = Color(0xFF8DE81E),
                fontSize = 11.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
        PersonalHeaderIcon(resId = R.drawable.chat_screen_phone)
        Spacer(modifier = Modifier.width(14.dp))
        PersonalHeaderIcon(    backgroundColor = Color(0xFFFF5A9D)
                ,resId = R.drawable.chat_screen_gift)
    }
}

@Composable
private fun PersonalHeaderIcon(
    resId: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFFFC431)
) {
    val shape = RoundedCornerShape(9.dp)

    Box(
        modifier = modifier.size(32.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 1.5.dp, y = 2.dp)
                .clip(shape)
                .background(Color.Black)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(backgroundColor)
                .border(1.4.dp, Color.Black, shape)
        ) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun PersonalMessageList(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(30.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        PersonalMessageBubble(
            text = "Hi, How are you doing? did you\nget a change to watch the movie?",
            time = "9:32 AM",
            isMine = false
        )
        PersonalMessageBubble(
            text = "Yeah!, i watched the movie. the\nmovie is fantastic.",
            time = "9:38 AM",
            isMine = true
        )
        PersonalMessageBubble(
            text = "That's great to hear! Which part of\nthe movie did you like the most?",
            time = "9:45 AM",
            isMine = false
        )
        PersonalMessageBubble(
            text = "I loved plot twists kept me hooked",
            time = "9:50 AM",
            isMine = true
        )
    }
}

@Composable
private fun PersonalMessageBubble(
    text: String,
    time: String,
    isMine: Boolean
) {
    Column(
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        val bubbleShape = ChatBubbleShape
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isMine) 0.78f else 0.78f)
                .then(if (isMine) Modifier.align(Alignment.End) else Modifier.align(Alignment.Start))
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 2.dp, y = 2.dp)
                    .clip(bubbleShape)
                    .background(Color.Black)
            )
            Text(
                text = text,
                color = Color.Black,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(bubbleShape)
                    .background(if (isMine) Color.White else ChatYellow)
                    .border(1.2.dp, Color.Black, bubbleShape)
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = time,
            color = Color.White,
            fontSize = 11.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
    }
}

@Composable
private fun PersonalChatComposer(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        val shape = RoundedCornerShape(26.dp)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 2.dp, y = 2.dp)
                    .clip(shape)
                    .background(Color.Black)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .background(Color.White)
                    .border(1.2.dp, Color.Black, shape)
                    .padding(horizontal = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SentimentSatisfied,
                    contentDescription = null,
                    tint = Color(0xFF777777),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Type your message...",
                            color = Color(0xFFAAAAAA),
                            fontSize = 14.sp,
                            fontFamily = GaretFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        cursorBrush = SolidColor(Color.Black),
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontFamily = GaretFontFamily,
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = null,
                    tint = Color(0xFF777777),
                    modifier = Modifier.size(22.dp)
                )
            }
            QuestionSparkle(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 42.dp, y = (-12).dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Box(
            modifier = Modifier.size(48.dp)
        ) {

            // Shadow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 1.dp, y = 2.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
            )

            // Main Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(Color(0xFFFFC431)) // Yellow
                    .border(1.dp, Color.Black, CircleShape)
            ) {

                Image(
                    painter = painterResource(id = R.drawable.chat_mic), // Your drawable
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit
                )

            }
        }
    }
}

private data class ChatMessage(
    val name: String,
    val preview: String,
    val time: String,
    val unreadCount: Int,
    val avatarRes: Int,
    val isOnline: Boolean
)

private fun chatMessages() = listOf(
    ChatMessage(
        name = "Anshu",
        preview = "Thank you for the sharing the l...",
        time = "10:42 AM",
        unreadCount = 2,
        avatarRes = R.drawable.women_avatar3,
        isOnline = true
    ),
    ChatMessage(
        name = "Priya",
        preview = "Could you please clarify the ...",
        time = "11:03 AM",
        unreadCount = 1,
        avatarRes = R.drawable.women_avatar3,
        isOnline = false
    ),
    ChatMessage(
        name = "Raj",
        preview = "I've updated the document acc...",
        time = "11:15 AM",
        unreadCount = 3,
        avatarRes = R.drawable.women_avatar3,
        isOnline = true
    ),
    ChatMessage(
        name = "Sweety",
        preview = "Hi, Good afternoon",
        time = "1:05 PM",
        unreadCount = 0,
        avatarRes = R.drawable.women_avatar3,
        isOnline = false
    )
)

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun ChatScreenPreview() {
    BffAndroidTheme {
        ChatScreen()
    }
}
//@Composable
//fun PersonalChatScreenPreview() {
//        PersonalChatScreen(
//            personName = "Sophia",
//            avatarRes = R.drawable.man_avatar3, // Replace with your drawable
//            onBack = {}
//        )
//}
