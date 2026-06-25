package com.example.bffandroid.screens

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import com.example.bffandroid.R
import com.example.bffandroid.ui.theme.BffAndroidTheme
import com.example.bffandroid.ui.theme.GaretFontFamily

private val ChatBlue = Color(0xFF335FAA)

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onChatSelected: (String, Int) -> Unit = { _, _ -> },
    onConnectSelected: () -> Unit = {},
    onGamesSelected: () -> Unit = {},
    onHistorySelected: () -> Unit = {}
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
            .background(ChatBlue)
    ) {
        Image(
            painter = painterResource(id = R.drawable.chat_screen_background_object),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 112.dp)
        ) {
            ChatHeader()
            ChatSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 22.dp)
                    .offset(y = 174.dp)
            )

            if (filteredMessages.isEmpty()) {
                ChatEmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 278.dp)
                )
            } else {
                ChatMessageList(
                    messages = filteredMessages,
                    onChatSelected = onChatSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .padding(top = 255.dp)
                )
            }
        }

        ChatBottomBar(
            selectedTab = ChatNavTab.Chat,
            onTabSelected = { tab ->
                when (tab) {
                    ChatNavTab.Connect -> onConnectSelected()
                    ChatNavTab.Games -> onGamesSelected()
                    ChatNavTab.History -> onHistorySelected()
                    else -> Unit
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ChatHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.chat_screen_message),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 25.dp, y = 108.dp)
                .size(width = 42.52.dp, height = 36.21.dp),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.chat_screen_legs),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 240.dp, y = 48.dp)
                .size(width = 23.dp, height = 24.dp)
                .graphicsLayer { rotationZ = 11.46f },
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.chat_screen_eyes),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 78.dp, y = 51.dp)
                .size(width = 31.59.dp, height = 24.dp)
                .graphicsLayer { rotationZ = 11.46f },
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.chat_screen_hearder),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 78.dp)
                .size(width = 332.dp, height = 42.dp),
            contentScale = ContentScale.Fit
        )
        CurvedChatTagline(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 126.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.chat_screen_filled_sparkle),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 22.dp, y = 79.dp)
                .size(width = 38.32.dp, height = 18.dp),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.chat_screen_sparkle),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 326.dp, y = 110.dp)
                .size(width = 18.32.dp, height = 18.dp),
            contentScale = ContentScale.Fit
        )
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

            Spacer(modifier = Modifier.height(14.dp))

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFEFEFE))
    ) {
        Image(
            painter = painterResource(id = R.drawable.chat_screen_background_object),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.055f },
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
                .padding(top = 42.dp, bottom = 98.dp)
        ) {
            PersonalChatHeader(
                personName = personName,
                avatarRes = avatarRes,
                onBack = onBack
            )
            Spacer(modifier = Modifier.height(58.dp))
            PersonalMessageList()
        }

        PersonalChatComposer(
            value = messageText,
            onValueChange = { messageText = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 26.dp)
        )
    }
}

@Composable
private fun PersonalChatHeader(
    personName: String,
    avatarRes: Int,
    onBack: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
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
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Online now",
                color = Color(0xFF15A849),
                fontSize = 11.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
        PersonalHeaderIcon(resId = R.drawable.chat_screen_phone)
        Spacer(modifier = Modifier.width(14.dp))
        PersonalHeaderIcon(resId = R.drawable.chat_screen_gift)
    }
}

@Composable
private fun PersonalHeaderIcon(resId: Int) {
    val shape = RoundedCornerShape(9.dp)
    Box(modifier = Modifier.size(32.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 2.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color(0xFFFFC431))
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
private fun PersonalMessageList() {
    Column(
        verticalArrangement = Arrangement.spacedBy(30.dp),
        modifier = Modifier.fillMaxWidth()
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
        val bubbleShape = RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = if (isMine) 18.dp else 4.dp,
            bottomEnd = if (isMine) 4.dp else 18.dp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isMine) 0.78f else 0.78f)
                .then(if (isMine) Modifier.align(Alignment.End) else Modifier.align(Alignment.Start))
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 4.dp, y = 5.dp)
                    .clip(bubbleShape)
                    .background(Color.Black)
            )
            Text(
                text = text,
                color = if (isMine) Color.Black else Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(bubbleShape)
                    .background(if (isMine) Color.White else ChatBlue)
                    .border(1.2.dp, Color.Black, bubbleShape)
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = time,
            color = Color(0xFF6D6D6D),
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
                    .offset(x = 4.dp, y = 5.dp)
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
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(ChatBlue)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun ChatBottomBar(
    selectedTab: ChatNavTab,
    onTabSelected: (ChatNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(99.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(88.dp)
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(Color.White)
        )
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(88.dp)
        ) {
            ChatNavTab.entries.forEach { tab ->
                ChatBottomBarItem(
                    tab = tab,
                    isSelected = selectedTab == tab,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

@Composable
private fun ChatBottomBarItem(
    tab: ChatNavTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bubbleColor by animateColorAsState(
        targetValue = if (isSelected) tab.tint.copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = spring(stiffness = 380f),
        label = "chatBubbleColor"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 420f),
        label = "chatIconScale"
    )
    val circleSize by animateDpAsState(
        targetValue = if (isSelected) 56.dp else 40.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 450f),
        label = "chatCircleSize"
    )
    val itemTopOffset by animateDpAsState(
        targetValue = if (isSelected) (-11).dp else 12.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 450f),
        label = "chatItemTopOffset"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) Color.Black else Color(0xFF7A7A7A),
        label = "chatLabelColor"
    )

    Box(
        modifier = Modifier
            .size(width = 74.dp, height = 99.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = itemTopOffset)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(circleSize)
                    .clip(CircleShape)
                    .background(if (isSelected) bubbleColor else Color.Transparent)
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(if (isSelected) 46.dp else 40.dp)
                        .clip(CircleShape)
                        .background(tab.tint)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = Color.White,
                        modifier = Modifier
                            .size(if (isSelected) 22.dp else 21.dp)
                            .scale(iconScale)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tab.label,
                color = labelColor,
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

private enum class ChatNavTab(
    val label: String,
    val icon: ImageVector,
    val tint: Color
) {
    Connect("Connect", Icons.Default.Phone, Color(0xFFF5BE2E)),
    Games("Games", Icons.Default.SportsEsports, Color(0xFF8D32F7)),
    Chat("Chat", Icons.Default.ChatBubbleOutline, Color(0xFF196DFF)),
    History("History", Icons.Default.History, Color(0xFFFF9518))
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
