package com.gobff.getfriends.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gobff.getfriends.R
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.GaretFontFamily

private val FriendListBlue = Color(0xFF4D6AF3)
private val FriendListYellow = Color(0xFFF5B120)
private val FriendListInk = Color(0xFF1B1A1A)

@Composable
fun FriendsListScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    BackHandler(onBack = onBack)

    Box(
        modifier = modifier
            .fillMaxSize()
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
                .padding(top = 46.dp, bottom = 28.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onBack)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Image(
                    painter = painterResource(id = R.drawable.friend_list_sqaud_icon),
                    contentDescription = null,
                    modifier = Modifier.size(width = 423.dp, height = 106.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            FriendsSearchBar()
            Spacer(modifier = Modifier.height(30.dp))

            FriendsSectionTitle(text = "Favorites (2)")
            Spacer(modifier = Modifier.height(14.dp))
            FavoriteFriends.forEach { friend ->
                FriendRow(friend = friend)
                Spacer(modifier = Modifier.height(14.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            FriendsSectionTitle(text = "All Friends (12)")
            Spacer(modifier = Modifier.height(14.dp))
            AllFriends.forEach { friend ->
                FriendRow(friend = friend)
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}



@Composable
private fun FriendsSearchBar() {
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
                .background(Color(0xFF14237A))
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
private fun FriendRow(friend: FriendItem) {
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
                .background(Color(0xFF172A8E))
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
                    text = friend.name,
                    color = FriendListInk,
                    fontSize = 14.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = friend.status,
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
private fun FriendAvatar(friend: FriendItem) {
    Box(modifier = Modifier.size(48.dp)) {
        Image(
            painter = painterResource(id = friend.avatarRes),
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(Color(0xFF8BBEFF)),
            contentScale = ContentScale.Crop
        )
        if (friend.isOnline) {
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

private data class FriendItem(
    val name: String,
    val status: String,
    val avatarRes: Int,
    val isOnline: Boolean
)

private val FavoriteFriends = listOf(
    FriendItem("Anshu", "Online", R.drawable.home_screen_avatar, true),
    FriendItem("Mira", "Last seen 2h ago", R.drawable.women_avatar11, false)
)

private val AllFriends = listOf(
    FriendItem("Jatin", "Last seen 30m ago", R.drawable.women_avatar1, false),
    FriendItem("Riya", "Online", R.drawable.women_avatar9, true),
    FriendItem("Karan", "Online", R.drawable.women_avatar2, true),
    FriendItem("Nisha", "Online", R.drawable.women_avatar7, true),
    FriendItem("Sahil", "Last seen 1h ago", R.drawable.women_avatar3, false),
    FriendItem("Kabir", "Online", R.drawable.man_avatar1, true)
)

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun FriendsListScreenPreview() {
    BffAndroidTheme {
        FriendsListScreen()
    }
}
