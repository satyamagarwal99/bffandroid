package com.gobff.getfriends.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gobff.getfriends.R
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.GaretFontFamily
import com.gobff.getfriends.utils.AvatarGender
import com.gobff.getfriends.utils.Constant
import com.gobff.getfriends.utils.toAvatarGender
import com.gobff.getfriends.viewmodel.OnboardingProfileViewModel

@Composable
fun GenderScreen(
    modifier: Modifier = Modifier,
    onAudioStepRequested: () -> Unit = {},
    onHomeRequested: () -> Unit = {},
    savedGender: String? = null,
    savedDisplayName: String? = null,
    profileViewModel: OnboardingProfileViewModel = viewModel()
) {
    val savedAvatarGender = savedGender.toAvatarGender()
    val startOnNickname = savedAvatarGender != null && !savedDisplayName.isUserProvidedDisplayName()
    var showManAvatar by remember(savedAvatarGender) { mutableStateOf(savedAvatarGender == AvatarGender.Male) }
    var showWomenAvatar by remember(savedAvatarGender) { mutableStateOf(savedAvatarGender == AvatarGender.Female) }
    val updateProfileState = profileViewModel.uiState

    fun submitGender(gender: String, onSuccess: () -> Unit) {
        profileViewModel.saveGender(
            gender = gender,
            onSuccess = onSuccess
        )
    }

    fun submitDisplayName(gender: String, nickname: String) {
        profileViewModel.saveDisplayName(
            displayName = nickname,
            gender = gender,
            onSuccess = {
                if (gender == GENDER_FEMALE) {
                    onAudioStepRequested()
                } else {
                    onHomeRequested()
                }
            }
        )
    }

    if (showManAvatar) {
        ManAvatarScreen(
            modifier = modifier,
            onBack = { showManAvatar = false },
            onComplete = { _, nickname ->
                submitDisplayName(GENDER_MALE, nickname)
            },
            isSubmitting = updateProfileState.isLoading,
            submitError = updateProfileState.errorMessage,
            startOnNickname = startOnNickname
        )
        return
    }

    if (showWomenAvatar) {
        WomenAvatarScreen(
            modifier = modifier,
            onBack = { showWomenAvatar = false },
            onComplete = { _, nickname ->
                submitDisplayName(GENDER_FEMALE, nickname)
            },
            isSubmitting = updateProfileState.isLoading,
            submitError = updateProfileState.errorMessage,
            startOnNickname = startOnNickname
        )
        return
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenHeight = maxHeight

        Image(
            painter = painterResource(id = R.drawable.gender_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Text(
            text = "I am a...",
            color = Color.White,
            fontSize = 28.sp,
            lineHeight = 28.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = screenHeight * 0.115f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = screenHeight * 0.215f)
        ) {
            GenderOption(
                imageResId = R.drawable.gender_man,
                label = "Man",
                onClick = {
                    submitGender(GENDER_MALE) {
                        showManAvatar = true
                    }
                }
            )
            Spacer(modifier = Modifier.height(54.dp))
            GenderOption(
                imageResId = R.drawable.gender_women,
                label = "Women",
                onClick = {
                    submitGender(GENDER_FEMALE) {
                        showWomenAvatar = true
                    }
                }
            )
        }
    }
}

@Composable
private fun GenderOption(
    imageResId: Int,
    label: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.size(218.dp)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 4.dp, y = 4.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
            )
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = label,
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 24.sp,
            lineHeight = 24.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun GenderScreenPreview() {
    BffAndroidTheme {
        GenderScreen()
    }
}

private const val GENDER_MALE = "MALE"
private const val GENDER_FEMALE = "FEMALE"

private fun String?.isUserProvidedDisplayName(): Boolean {
    val normalized = this?.trim().orEmpty()
    return normalized.isNotBlank() && !normalized.equals(Constant.DEFAULT_DISPLAY_NAME, ignoreCase = true)
}
