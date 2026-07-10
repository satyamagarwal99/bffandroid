package com.gobff.getfriends.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gobff.getfriends.R
import com.gobff.getfriends.data.model.LoginMethod
import com.gobff.getfriends.data.model.LoginUiState
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.GaretFontFamily
import com.gobff.getfriends.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onSkipLogin: () -> Unit = {},
    onAuthenticated: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    LaunchedEffect(viewModel.uiState.isAuthenticated) {
        if (viewModel.uiState.isAuthenticated) {
            onAuthenticated()
        }
    }

    LoginScreenContent(
        uiState = viewModel.uiState,
        onMobileNumberChange = viewModel::onMobileNumberChange,
        onOtpCodeChange = viewModel::onOtpCodeChange,
        onLoginClick = viewModel::onLoginClick,
        onContinueClick = viewModel::onContinueClick,
        onGoogleSignInClick = viewModel::onGoogleSignInClick,
        onSkipLogin = onSkipLogin,
        modifier = modifier
    )
}

@Composable
private fun LoginScreenContent(
    uiState: LoginUiState,
    onMobileNumberChange: (String) -> Unit,
    onOtpCodeChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onContinueClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onSkipLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loginMethod = uiState.loginCountry.loginMethod
    val formProgress by animateFloatAsState(
        targetValue = if (uiState.showOtp) 0f else 1f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "loginFormProgress"
    )
    val otpProgress by animateFloatAsState(
        targetValue = if (uiState.showOtp) 1f else 0f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "loginOtpProgress"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(LoginPurple)
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val bottomHeight = screenHeight * 0.385f

        Image(
            painter = painterResource(id = R.drawable.login_screen_objects),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        if (uiState.showOtp) {
            OtpTopContent(
                otpCode = uiState.otpCode,
                otpDebugText = uiState.otpDebugText,
                authStatusText = uiState.authStatusText,
                onOtpCodeChange = onOtpCodeChange,
                modifier = Modifier
                    .matchParentSize()
                    .loginCrossfadeMotion(otpProgress, slideY = 22f)
            )

            RaisedLoginButton(
                text = "Continue",
                width = 112.dp,
                onClick = onContinueClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-50).dp, y = 307.dp)
            )
        } else {
            TopArrowButton(
                onClick = onSkipLogin,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-17).dp, y = 48.dp)
            )

            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 94.dp)
                    .width(screenWidth * 0.68f)
                    .loginCrossfadeMotion(formProgress, slideY = 18f)
            ) {
                Text(
                    text = "Let's start talking",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (loginMethod == LoginMethod.MobileNumber) {
                        "Enter your mobile number"
                    } else {
                        "Continue with Google"
                    },
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }

            if (loginMethod == LoginMethod.MobileNumber) {
                LoginPhoneTextField(
                    value = uiState.mobileNumber,
                    onValueChange = onMobileNumberChange,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = screenHeight * 0.22f)
                        .width(screenWidth * 0.77f)
                        .loginCrossfadeMotion(formProgress, slideY = 22f)
                )

                PrivacyLine(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = screenHeight * 0.33f)
                        .width(screenWidth * 0.72f)
                        .loginCrossfadeMotion(formProgress, slideY = 18f)
                )

                LoginStatusText(
                    text = uiState.authStatusText,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = screenHeight * 0.37f)
                        .width(screenWidth * 0.72f)
                        .loginCrossfadeMotion(formProgress, slideY = 14f)
                )

                RaisedLoginButton(
                    text = if (uiState.isOtpRequestLoading) "Sending" else "Login",
                    onClick = onLoginClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = -(screenWidth * 0.16f), y = screenHeight * 0.41f)
                        .loginCrossfadeMotion(formProgress, slideY = 18f)
                )

                DashedCurve(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 352.dp, y = 219.dp)
                        .size(width = 32.dp, height = 151.42578125.dp)
                )
            } else {
                GoogleSignInButton(
                    onClick = onGoogleSignInClick,
                    isLoading = uiState.isGoogleAuthLoading,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = screenHeight * 0.22f)
                        .width(screenWidth * 0.77f)
                        .loginCrossfadeMotion(formProgress, slideY = 22f)
                )

                LoginStatusText(
                    text = uiState.authStatusText,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = screenHeight * 0.31f)
                        .width(screenWidth * 0.72f)
                        .loginCrossfadeMotion(formProgress, slideY = 14f)
                )
            }
        }

        LoginBottomPager(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomHeight)
        )
    }
}

private fun Modifier.loginCrossfadeMotion(progress: Float, slideY: Float): Modifier =
    graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * slideY
        scaleX = 0.98f + progress * 0.02f
        scaleY = 0.98f + progress * 0.02f
    }

@Composable
private fun LoginStatusText(
    text: String?,
    modifier: Modifier = Modifier
) {
    if (text.isNullOrBlank()) return

    Text(
        text = text,
        color = Color.White,
        fontSize = 12.sp,
        lineHeight = 12.sp,
        fontFamily = GaretFontFamily,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        modifier = modifier
    )
}

@Composable
private fun LoginPhoneTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(80.dp)
    val borderColor = Color(0xFF1B1A1A)

    Box(modifier = modifier.wrapContentSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .offset(x = 4.dp, y = 7.dp)
                .clip(shape)
                .background(borderColor)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(shape)
                .background(Color.White)
                .border(width = 1.5.dp, color = borderColor, shape = shape)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IndiaFlag(modifier = Modifier.size(width = 21.dp, height = 15.dp))
                DropdownTriangle(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(width = 10.dp, height = 6.dp)
                )
                Box(
                    modifier = Modifier
                        .padding(start = 17.dp, end = 14.dp)
                        .width(1.dp)
                        .height(34.dp)
                        .background(Color(0xFFDCDCDC))
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = "",
                            fontSize = 17.sp,
                            fontFamily = GaretFontFamily,
                            color = Color(0xFFAAAAAA)
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        cursorBrush = SolidColor(borderColor),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        textStyle = TextStyle(
                            fontSize = 17.sp,
                            color = borderColor,
                            fontFamily = GaretFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
private fun GoogleSignInButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(80.dp)
    val borderColor = Color(0xFF1B1A1A)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 520f),
        label = "googleButtonPress"
    )

    Box(modifier = modifier.wrapContentSize().scale(pressScale)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .offset(x = 4.dp, y = 5.dp)
                .clip(shape)
                .background(borderColor)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(shape)
                .background(Color.White)
                .border(width = 1.5.dp, color = borderColor, shape = shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google_icon),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Box(
                    modifier = Modifier
                        .padding(start = 17.dp, end = 15.dp)
                        .width(1.dp)
                        .height(30.dp)
                        .background(Color(0xFFDCDCDC))
                )
                Text(
                    text = if (isLoading) "Signing in..." else "Sign in with Google",
                    color = borderColor,
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    letterSpacing = 0.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TopArrowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 540f),
        label = "topArrowPress"
    )
    Box(
        modifier = modifier
            .size(width = 52.dp, height = 27.dp)
            .scale(pressScale)
            .clip(RoundedCornerShape(50))
            .background(Color.Black)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Next",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}
@Composable
private fun PrivacyLine(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        LockIcon(modifier = Modifier.size(25.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Worry not, Your privacy is safe with us.",
            color = Color.White,
            fontSize = 12.sp,
            lineHeight = 12.sp,
            letterSpacing = 0.12.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun OtpTopContent(
    otpCode: String,
    otpDebugText: String?,
    authStatusText: String?,
    onOtpCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 88.dp)
                .width(296.5.dp)
                .height(58.dp)
        ) {
            Text(
                text = "Let's get you in",
                color = Color.White,
                fontSize = 22.sp,
                lineHeight = 22.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Enter the code",
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
        }

        OtpCodeField(
            value = otpCode,
            onValueChange = onOtpCodeChange,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 158.dp)
                .size(width = 296.495849609375.dp, height = 64.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 246.dp)
                .size(width = 210.dp, height = 24.dp)
        ) {
            Text(
                text = "Didn't get the OTP?",
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Resend",
                color = Color(0xFFD7B7DF),
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline
            )
        }

        val helperText = otpDebugText ?: authStatusText
        if (!helperText.isNullOrBlank()) {
            Text(
                text = helperText,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 13.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 274.dp)
                    .width(296.5.dp)
            )
        }
    }
}

@Composable
private fun OtpCodeField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = { input ->
            onValueChange(input.filter { it.isDigit() }.take(4))
        },
        singleLine = true,
        cursorBrush = SolidColor(Color.Transparent),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        textStyle = TextStyle(
            color = Color.Transparent,
            fontSize = 1.sp
        ),
        modifier = modifier,
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    OtpDigitBox(value = value.getOrNull(index)?.toString().orEmpty())
                }
            }
        }
    )
}

@Composable
private fun OtpDigitBox(value: String) {
    val shape = RoundedCornerShape(10.dp)

    Box(modifier = Modifier.wrapContentSize()) {
        Box(
            modifier = Modifier
                .size(width = 62.12396240234375.dp, height = 64.dp)
                .offset(x = 2.dp, y = 2.dp)
                .clip(shape)
                .background(Color(0xFF2B1A12))
        )
        Box(
            modifier = Modifier
                .size(width = 62.12396240234375.dp, height = 64.dp)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, Color(0xFF2B1A12), shape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                color = Color(0xFF2B1A12),
                fontSize = 24.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RaisedLoginButton(
    text: String,
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = 90.dp,
    height: androidx.compose.ui.unit.Dp = 48.dp,
    onClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(12.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.64f, stiffness = 540f),
        label = "raisedLoginPress"
    )

    Box(modifier = modifier.wrapContentSize().scale(pressScale)) {
        Box(
            modifier = Modifier
                .size(width = width, height = height)
                .offset(x = 3.dp, y = 4.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Box(
            modifier = Modifier
                .size(width = width, height = height)
                .clip(shape)
                .background(Color.White)
                .border(1.5.dp, Color.Black, shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.Black,
                fontSize = 16.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.16.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LoginBottomPager(modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = { 3 })

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(Color.White)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> LoginPageOne()
                1 -> LoginPageTwo()
                else -> LoginPageThree()
            }
        }

        PageIndicator(
            currentPage = pagerState.currentPage,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 15.dp, end = 18.dp)
        )
    }
}

@Composable
private fun LoginPageOne() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 45.dp, end = 40.dp)
        ) {
            Text(
                text = "...feeling lonely?",
                color = LoginPurpleDark,
                fontSize = 18.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "and have no one to talk?",
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
        }

        Image(
            painter = painterResource(id = R.drawable.login_screen1_person),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-8).dp, y = 12.dp)
                .fillMaxHeight(),
            contentScale = ContentScale.Fit
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun LoginPageTwo() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 42.dp)
        ) {
            Text(
                text = "Talk to a Friend",
                color = LoginPurpleDark,
                fontSize = 20.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "100% Safe and Secure",
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
        }

        Image(
            painter = painterResource(R.drawable.login_screen_phone),
            contentDescription = "Phone",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 16.dp)
                .size(40.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.login_screen2_boy),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-22).dp, y = 0.dp)
                .size(height = 218.dp, width = 236.dp),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.login_screen2_women),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 18.dp, y = 22.dp)
                .size(height = 227.dp, width = 194.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun LoginPageThree() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "...Hi, Kaise ho?",
            color = LoginPurpleDark,
            fontSize = 18.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 32.dp, top = 56.dp)
        )

        Text(
            text = "Hi, Main Thik hue\nAp kaise ho?",
            color = Color(0xFF321D11),
            fontSize = 18.sp,
            lineHeight = 29.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 35.dp, top = 80.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.login_screen2_women),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-4).dp, y = 20.dp)
                .size(height = 234.dp, width = 200.dp)
                .scale(scaleX = -1f, scaleY = 1f),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.login_screen2_boy),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 18.dp, y = 12.dp)
                .size(height = 194.dp, width = 210.dp)
                .scale(scaleX = -1f, scaleY = 1f),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun PageIndicator(
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(3) { page ->
            val isSelected = page == currentPage
            val dotWidth by animateDpAsState(
                targetValue = if (isSelected) 28.dp else 8.dp,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "loginPagerDotWidth"
            )
            Box(
                modifier = Modifier
                    .size(width = dotWidth, height = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) LoginPurpleDark else Color(0xFFD3D3D3))
            )
        }
    }
}

@Composable
private fun IndiaFlag(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stripeHeight = size.height / 3f
        drawRect(Color(0xFFFF9933), size = Size(size.width, stripeHeight))
        drawRect(
            Color.White,
            topLeft = Offset(0f, stripeHeight),
            size = Size(size.width, stripeHeight)
        )
        drawRect(
            Color(0xFF138808),
            topLeft = Offset(0f, stripeHeight * 2f),
            size = Size(size.width, stripeHeight)
        )
        drawCircle(
            color = Color(0xFF1A3C8B),
            radius = size.height * 0.11f,
            center = Offset(size.width / 2f, size.height / 2f)
        )
    }
}

@Composable
private fun DropdownTriangle(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width / 2f, size.height)
            close()
        }
        drawPath(path, color = Color.Black)
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

@Composable
private fun LockIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 3.2f)
        drawArc(
            color = Color.White,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(size.width * 0.25f, size.height * 0.02f),
            size = Size(size.width * 0.5f, size.height * 0.58f),
            style = stroke
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(size.width * 0.16f, size.height * 0.42f),
            size = Size(size.width * 0.68f, size.height * 0.5f),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
        drawCircle(
            color = LoginPurple,
            radius = size.width * 0.07f,
            center = Offset(size.width / 2f, size.height * 0.64f)
        )
        drawRect(
            color = LoginPurple,
            topLeft = Offset(size.width * 0.47f, size.height * 0.65f),
            size = Size(size.width * 0.06f, size.height * 0.15f)
        )
    }
}



@Composable
private fun DashedCurve(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val curve = Path().apply {
            moveTo( 0.08f, 2f)
            cubicTo(
                size.width * 1.06f,
                size.height * 0.02f,
                size.width * 0.96f,
                size.height * 0.28f,
                size.width * 0.96f,
                size.height * 0.43f
            )
            cubicTo(
                size.width * 0.94f,
                size.height * 0.65f,
                size.width * 1.05f,
                size.height * 0.82f,
                size.width * 1.32f,
                size.height
            )
        }

        drawPath(
            path = curve,
            color = Color.White,
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(8.dp.toPx(), 8.dp.toPx())
                )
            )
        )
    }
}

private val LoginPurple = Color(0xFF9435A3)
private val LoginPurpleDark = Color(0xFF8E2AA0)

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun LoginScreenPreview() {
    BffAndroidTheme {
        LoginScreenContent(
            uiState = LoginUiState(),
            onMobileNumberChange = {},
            onOtpCodeChange = {},
            onLoginClick = {},
            onContinueClick = {},
            onGoogleSignInClick = {},
            onSkipLogin = {}
        )
    }
}
