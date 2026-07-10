package com.gobff.getfriends.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gobff.getfriends.R
import com.gobff.getfriends.data.model.FriendListUserResponse
import com.gobff.getfriends.ui.component.BffHeartChip
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.FreedokaFontFamily
import com.gobff.getfriends.ui.theme.GaretFontFamily
import com.gobff.getfriends.viewmodel.FriendsListViewModel

private val FriendListBlue = Color(0xFF4D6AF3)
private val FriendListYellow = Color(0xFFF5B120)
private val FriendListInk = Color(0xFF1B1A1A)
private val FriendListShadow = Color(0xFF14237A)

@Composable
fun FriendsListScreen(
    walletHearts: Int = 0,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onRechargeRequested: () -> Unit = {},
    friendsListViewModel: FriendsListViewModel = viewModel()
) {
    BackHandler(onBack = onBack)

    LaunchedEffect(Unit) {
        friendsListViewModel.loadFriends()
    }

    val uiState = friendsListViewModel.uiState
    var searchQuery by remember { mutableStateOf("") }
    val filteredFriends = remember(uiState.friends, searchQuery) {
        val query = searchQuery.trim()
        if (query.isBlank()) {
            uiState.friends
        } else {
            uiState.friends.filter { it.displayNameForList().contains(query, ignoreCase = true) }
        }
    }
    val favoriteFriends = filteredFriends.filter { it.isFavoriteFriend() }
    val allFriends = filteredFriends

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FriendsHeader(
                walletHearts = walletHearts,
                onBack = onBack,
                onRechargeRequested = onRechargeRequested
            )
            FriendsContentCard(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                favoriteFriends = favoriteFriends,
                allFriends = allFriends,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FriendsHeader(
    walletHearts: Int,
    onBack: () -> Unit,
    onRechargeRequested: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(227.dp)
            .background(Color.White)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.Black,
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
        BffHeartChip(
            hearts = walletHearts,
            onClick = onRechargeRequested,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 20.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 122.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.gift_vibe_sparkle),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FRIEND",
                    color = Color(0xFF3762AF),
                    fontSize = 32.sp,
                    lineHeight = 32.sp,
                    letterSpacing = 0.64.sp,
                    fontFamily = FreedokaFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LIST",
                    color = Color(0xFFF6B93B),
                    fontSize = 32.sp,
                    lineHeight = 32.sp,
                    letterSpacing = 0.64.sp,
                    fontFamily = FreedokaFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.gift_vibe_sparkle),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Friends just a tap away",
                color = Color(0xFF303030),
                fontSize = 15.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FriendsContentCard(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    favoriteFriends: List<FriendListUserResponse>,
    allFriends: List<FriendListUserResponse>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(FriendListBlue)
    ) {
        Image(
            painter = painterResource(id = R.drawable.friend_list_background_object),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 52.dp, bottom = 28.dp)
        ) {
            FriendsSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange
            )
            Spacer(modifier = Modifier.height(30.dp))

            when {
                isLoading -> FriendsStatusMessage(text = "Loading friends...")
                allFriends.isEmpty() -> FriendsEmptyState(errorMessage = errorMessage)
                else -> {
                    if (favoriteFriends.isNotEmpty()) {
                        FriendsSectionTitle(text = "Favorites (${favoriteFriends.size})")
                        Spacer(modifier = Modifier.height(14.dp))
                        favoriteFriends.forEach { friend ->
                            FriendRow(friend = friend)
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    FriendsSectionTitle(text = "All Friends (${allFriends.size})")
                    Spacer(modifier = Modifier.height(14.dp))
                    allFriends.forEach { friend ->
                        FriendRow(friend = friend)
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val shape = RoundedCornerShape(32.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 5.dp)
                .clip(shape)
                .background(FriendListShadow)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .padding(horizontal = 20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF777777),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    cursorBrush = SolidColor(Color(0xFF3762AF)),
                    textStyle = TextStyle(
                        color = FriendListInk,
                        fontSize = 16.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (query.isBlank()) {
                    Text(
                        text = "Search by name...",
                        color = Color(0xFFAAAAAA),
                        fontSize = 16.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        QuestionSparkle(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 45.dp, y = (-12).dp)
        )
    }
}

@Composable
private fun FriendsSectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 17.sp,
        fontFamily = GaretFontFamily,
        fontWeight = FontWeight.Bold
    )
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
private fun FriendRow(friend: FriendListUserResponse) {
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 5.dp)
                .clip(shape)
                .background(FriendListShadow)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .padding(start = 18.dp, end = 14.dp)
        ) {
            FriendAvatar(friend = friend)
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = friend.displayNameForList(),
                    color = FriendListInk,
                    fontSize = 14.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = friend.statusForList(),
                    color = Color(0xFF8F8F8F),
                    fontSize = 10.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
            FriendActionButton(icon = Icons.Default.Phone, contentDescription = "Call")
            Spacer(modifier = Modifier.width(12.dp))
            FriendActionButton(icon = Icons.Default.ChatBubbleOutline, contentDescription = "Chat")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = Color.Black,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun FriendAvatar(friend: FriendListUserResponse) {
    Box(modifier = Modifier.size(48.dp)) {
        Image(
            painter = painterResource(id = friend.avatarUrl.toFriendAvatarRes(friend.identitySeed())),
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(Color(0xFF8BBEFF)),
            contentScale = ContentScale.Crop
        )
        if (friend.isOnlineFriend()) {
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color(0xFF28C461))
                    .border(1.5.dp, Color.White, CircleShape)
            )
        }
    }
}

@Composable
private fun FriendActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = 48.dp, height = 36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(FriendListYellow)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.Black,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun FriendsEmptyState(errorMessage: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.no_host_connect_screen),
            contentDescription = null,
            modifier = Modifier.size(width = 250.dp, height = 210.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = errorMessage ?: "No friends found right now",
            color = Color.White,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FriendsStatusMessage(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 17.sp,
        fontFamily = GaretFontFamily,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 72.dp)
    )
}

private fun FriendListUserResponse.displayNameForList(): String =
    displayName?.takeIf { it.isNotBlank() }
        ?: name?.takeIf { it.isNotBlank() }
        ?: "Someone"

private fun FriendListUserResponse.isOnlineFriend(): Boolean = online == true || isOnline == true

private fun FriendListUserResponse.isFavoriteFriend(): Boolean = favorite == true || isFavorite == true

private fun FriendListUserResponse.statusForList(): String {
    if (isOnlineFriend()) return "Online"
    return when {
        !lastSeenAt.isNullOrBlank() -> "Last seen recently"
        !lastOnlineAt.isNullOrBlank() -> "Last seen recently"
        !lastTalkedAt.isNullOrBlank() -> "Last talked recently"
        else -> "Offline"
    }
}

private fun FriendListUserResponse.identitySeed(): String =
    userId ?: id ?: displayName ?: name ?: avatarUrl.orEmpty()

private fun String?.toFriendAvatarRes(seed: String): Int {
    return when (this?.substringAfterLast("/")?.substringBeforeLast(".")?.trim()?.lowercase()) {
        "home_screen_avatar" -> R.drawable.home_screen_avatar
        "women_avatar1" -> R.drawable.women_avatar1
        "women_avatar2" -> R.drawable.women_avatar1
        "women_avatar3" -> R.drawable.women_avatar1
        "women_avatar4" -> R.drawable.women_avatar1
        "women_avatar5" -> R.drawable.women_avatar1
        "women_avatar6" -> R.drawable.women_avatar1
        "women_avatar7" -> R.drawable.women_avatar1
        "women_avatar8" -> R.drawable.women_avatar1
        "women_avatar9" -> R.drawable.women_avatar1
        "women_avatar10" -> R.drawable.women_avatar1
        "women_avatar11" -> R.drawable.women_avatar1
        "women_avatar12" -> R.drawable.women_avatar1
        "man_avatar1" -> R.drawable.man_avatar1
        "man_avatar2" -> R.drawable.man_avatar1
        "man_avatar3" -> R.drawable.man_avatar1
        "man_avatar4" -> R.drawable.man_avatar1
        "man_avatar5" -> R.drawable.man_avatar1
        "man_avatar6" -> R.drawable.man_avatar1
        "man_avatar7" -> R.drawable.man_avatar1
        "man_avatar8" -> R.drawable.man_avatar1
        "man_avatar9" -> R.drawable.man_avatar1
        "man_avatar10" -> R.drawable.man_avatar1
        "man_avatar11" -> R.drawable.man_avatar1
        "man_avatar12" -> R.drawable.man_avatar1
        else -> {
            val avatars = listOf(
                R.drawable.home_screen_avatar,
                R.drawable.women_avatar1,
                R.drawable.women_avatar1,
                R.drawable.women_avatar1,
                R.drawable.women_avatar1,
                R.drawable.women_avatar1,
                R.drawable.women_avatar1,
                R.drawable.man_avatar1
            )
            val index = (seed.hashCode() and Int.MAX_VALUE) % avatars.size
            avatars[index]
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun FriendsListScreenPreview() {
    BffAndroidTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            FriendsHeader(
                walletHearts = 30,
                onBack = {},
                onRechargeRequested = {}
            )
            FriendsContentCard(
                searchQuery = "",
                onSearchQueryChange = {},
                isLoading = false,
                errorMessage = null,
                favoriteFriends = PreviewFriends.take(2),
                allFriends = PreviewFriends,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private val PreviewFriends = listOf(
    FriendListUserResponse(
        userId = "preview_1",
        displayName = "Anshu",
        avatarUrl = "home_screen_avatar",
        online = true,
        favorite = true
    ),
    FriendListUserResponse(
        userId = "preview_2",
        displayName = "Mira",
        avatarUrl = "women_avatar11",
        online = false,
        favorite = true
    ),
    FriendListUserResponse(
        userId = "preview_3",
        displayName = "Jatin",
        avatarUrl = "women_avatar1",
        online = false
    ),
    FriendListUserResponse(
        userId = "preview_4",
        displayName = "Riya",
        avatarUrl = "women_avatar9",
        online = true
    )
)
