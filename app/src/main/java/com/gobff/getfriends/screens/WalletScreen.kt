package com.gobff.getfriends.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gobff.getfriends.R
import com.gobff.getfriends.data.model.WalletWithdrawalItem
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.FreedokaFontFamily
import com.gobff.getfriends.ui.theme.GaretFontFamily
import com.gobff.getfriends.viewmodel.WalletViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val WalletCoral = Color(0xFFFF7171)
private val WalletYellow = Color(0xFFFFCC4D)

@Composable
fun WalletScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    walletViewModel: WalletViewModel = viewModel()
) {
    var sheet by remember { mutableStateOf<WalletSheet?>(null) }
    var panVerified by remember { mutableStateOf(false) }
    var resultState by remember { mutableStateOf(WithdrawalResult.Success) }
    var verifiedName by remember { mutableStateOf("") }
    var verifiedPan by remember { mutableStateOf("") }
    val walletUiState = walletViewModel.uiState
    val balanceAmount = walletUiState.amountInr
    val balanceAmountPaise = walletUiState.amountPaise
    val coinBalance = walletUiState.coins
    val currentCoinValuePaise = walletUiState.currentCoinValuePaise

    BackHandler {
        if (sheet != null) sheet = null else onBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WalletHeader(
                amount = balanceAmount,
                amountPaise = balanceAmountPaise,
                coins = coinBalance,
                currentCoinValuePaise = currentCoinValuePaise,
                onBack = onBack,
                onRedeem = {
                    if (coinBalance > MIN_REDEEM_COINS) {
                        sheet = if (panVerified) WalletSheet.Withdraw else WalletSheet.PanVerification
                    }
                }
            )
            WalletTransactionHistory(
                withdrawals = walletUiState.withdrawals,
                isLoading = walletUiState.isWithdrawalsLoading,
                errorMessage = walletUiState.withdrawalErrorMessage
            )
        }

        if (sheet != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { sheet = null }
            )
        }

        when (sheet) {
            WalletSheet.PanVerification -> PanVerificationSheet(
                onSubmit = { name, pan ->
                    verifiedName = name
                    verifiedPan = pan
                    panVerified = true
                    sheet = WalletSheet.PanVerified
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            WalletSheet.PanVerified -> {
                LaunchedEffect(Unit) {
                    delay(1300)
                    sheet = WalletSheet.Withdraw
                }
                PanVerifiedSheet(modifier = Modifier.align(Alignment.BottomCenter))
            }

            WalletSheet.Withdraw -> WithdrawRewardsSheet(
                amountPaise = balanceAmountPaise,
                isSubmitting = walletUiState.isSubmittingWithdrawal,
                errorMessage = walletUiState.withdrawalSubmitMessage,
                onWithdraw = { upiId ->
                    walletViewModel.createWithdrawal(
                        name = verifiedName,
                        pan = verifiedPan,
                        upiId = upiId,
                        coinAmount = coinBalance,
                        onSuccess = {
                            resultState = WithdrawalResult.Success
                            sheet = WalletSheet.WithdrawalResult
                        }
                    )
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            WalletSheet.WithdrawalResult -> WithdrawalResultSheet(
                state = resultState,
                amountPaise = walletUiState.lastWithdrawalAmountPaise.takeIf { it > 0 }
                    ?: balanceAmountPaise,
                onDone = {
                    sheet = null
                    if (resultState == WithdrawalResult.Success) {
                        walletViewModel.loadWalletBalance()
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            null -> Unit
        }
    }
}

@Composable
private fun WalletHeader(
    amount: Int,
    amountPaise: Int,
    coins: Int,
    currentCoinValuePaise: Int,
    onBack: () -> Unit,
    onRedeem: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
            .background(WalletCoral)
    ) {
        Image(
            painter = painterResource(id = R.drawable.profile_screen_bg_objects),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        WalletTopBar(
            currentCoinValuePaise = currentCoinValuePaise,
            onBack = onBack
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 124.dp)
        ) {
            Text(
                text = coins.toString(),
                color = Color.White,
                fontSize = 72.sp,
                lineHeight = 72.sp,
                fontFamily = FreedokaFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(10.dp))
            Image(
                painter = painterResource(id = R.drawable.coin_icon),
                contentDescription = null,
                modifier = Modifier.size(54.dp),
                contentScale = ContentScale.Fit
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 220.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.22f))
                .border(1.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
                .padding(horizontal = 13.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Balance: ${amountPaise.formatWalletBalancePaise(amount)}",
                color = Color.Black,
                fontSize = 11.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
        SwipeToRedeem(
            enabled = coins > MIN_REDEEM_COINS,
            onRedeem = onRedeem,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 293.dp)
                .padding(horizontal = 32.dp)
        )
        WalletTrustRow(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )
    }
}

@Composable
private fun WalletTopBar(
    currentCoinValuePaise: Int,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp, start = 20.dp, end = 20.dp)
    ) {

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(22.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack
                )
        )
        Text(
            text = "Reward wallet",
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
        )
        CoinRatePill(
            currentCoinValuePaise = currentCoinValuePaise,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

@Composable
private fun CoinRatePill(
    currentCoinValuePaise: Int,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(25.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
            .padding(horizontal = 9.dp)
    ) {
        Text(
            text = "1",
            color = Color.Black,
            fontSize = 11.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(3.dp))
        Image(
            painter = painterResource(id = R.drawable.coin_icon),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "= ${currentCoinValuePaise.formatCoinRate()}",
            color = Color.Black,
            fontSize = 10.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SwipeToRedeem(
    enabled: Boolean,
    onRedeem: () -> Unit,
    modifier: Modifier = Modifier
) {
    val knobOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var trackWidthPx by remember { mutableStateOf(0f) }
    val sidePaddingPx = with(density) { 8.dp.toPx() }
    val knobSizePx = with(density) { 40.dp.toPx() }
    val maxOffset = (trackWidthPx - knobSizePx - (sidePaddingPx * 2f)).coerceAtLeast(0f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .onSizeChanged { trackWidthPx = it.width.toFloat() }
            .pointerInput(enabled, maxOffset) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        scope.launch {
                            knobOffset.snapTo((offset.x - sidePaddingPx - (knobSizePx / 2f)).coerceIn(0f, maxOffset))
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        scope.launch {
                            knobOffset.snapTo((change.position.x - sidePaddingPx - (knobSizePx / 2f)).coerceIn(0f, maxOffset))
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            if (knobOffset.value > maxOffset * 0.72f) {
                                knobOffset.animateTo(maxOffset, spring())
                                onRedeem()
                            }
                            knobOffset.animateTo(0f, spring())
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            knobOffset.animateTo(0f, spring())
                        }
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (enabled) Color.Black else Color.Transparent)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(16.dp))
                .background(if (enabled) Color.White else Color.White.copy(alpha = 0.40f))
                .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
        )
        Text(
            text = if (enabled) "Swipe to redeem" else "Minimum 100 coins to redeem",
            color = Color(0xFF222222).copy(alpha = if (enabled) 1f else 0.38f),
            fontSize = 13.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
        if (enabled) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset { IntOffset(knobOffset.value.roundToInt(), 0) }
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF98080))
                    .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun WalletTrustRow(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        TrustItem(
            icon = R.drawable.wallet_secure_payment,
            text = "Secure payments"
        )

        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .height(14.dp)
                .width(1.dp)
                .background(Color.White.copy(alpha = 0.55f))
        )

        TrustItem(
            icon = R.drawable.wallet_fast,
            text = "Fast processing"
        )
    }
}


@Composable
private fun TrustItem(
    icon: Int,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(15.dp)
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = text,
            color = Color.Black,
            fontSize = 10.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun WalletTransactionHistory(
    withdrawals: List<WalletWithdrawalItem>,
    isLoading: Boolean,
    errorMessage: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp)
    ) {
        Text(
            text = "Transaction History",
            color = Color(0xFF8C8C8C),
            fontSize = 14.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
        if (isLoading) {
            Text(
                text = "Loading withdrawals...",
                color = Color(0xFF888888),
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 30.dp)
            )
        } else if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color(0xFFE95151),
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 30.dp)
            )
        } else if (withdrawals.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 142.dp)
            ) {
                Text(
                    text = "No withdrawals yet",
                    color = Color(0xFF888888),
                    fontSize = 14.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Start making friends, earn hearts, and cash out\nyour rewards.",
                    color = Color(0xFF999999),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Spacer(modifier = Modifier.height(30.dp))
            withdrawals
                .mapIndexed { index, withdrawal -> withdrawal.toWalletTransaction(index) }
                .forEachIndexed { index, item ->
                TransactionRow(item)
                if (index < withdrawals.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFF1F1F1))
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(item: WalletTransaction) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(item.iconBackground)
        ) {
            Icon(
                painter = painterResource(R.drawable.wallet_withdrawal),
                contentDescription = null,
                tint = item.statusColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.title, color = Color(0xFF000000), fontSize = 12.sp, fontFamily = GaretFontFamily, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(item.statusColor.copy(alpha = 0.12f))
                        .border(1.dp, item.statusColor, RoundedCornerShape(20.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(item.status, color = item.statusColor, fontSize = 7.sp, fontFamily = GaretFontFamily, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.date, color = Color(0xFF999999), fontSize = 10.sp, fontFamily = GaretFontFamily, fontWeight = FontWeight.Medium)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(item.amount, color = Color.Black, fontSize = 13.sp, fontFamily = GaretFontFamily, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.coinDelta, color = WalletYellow, fontSize = 10.sp, fontFamily = GaretFontFamily, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PanVerificationSheet(
    onSubmit: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("") }
    var panNumber by remember { mutableStateOf("") }
    val canSubmit = fullName.isNotBlank() && panNumber.isNotBlank()

    WalletBottomSheet(
        title = "KYC-PAN Card verification",
        modifier = modifier.height(440.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            WalletInput(value = fullName, onValueChange = { fullName = it }, placeholder = "Full name ( as on PAN card )")
            Spacer(modifier = Modifier.height(28.dp))
            WalletInput(value = panNumber, onValueChange = { panNumber = it.uppercase() }, placeholder = "PAN number")
            Spacer(modifier = Modifier.weight(1f))
            WalletYellowButton(
                text = "Submit",
                enabled = canSubmit,
                onClick = { onSubmit(fullName.trim(), panNumber.trim().uppercase()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
            )
        }
    }
}

@Composable
private fun PanVerifiedSheet(modifier: Modifier = Modifier) {
    WalletBottomSheet(
        title = "",
        showTitle = false,
        modifier = modifier.height(440.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.wallet_pan_verified),
                contentDescription = null,
                modifier = Modifier.size(140.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text("PAN Verified! 🎉", color = Color.Black, fontSize = 18.sp, fontFamily = GaretFontFamily, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Your PAN has been verified successfully.\nyou can now withdraw your rewards",
                color = Color(0xFF888888),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WithdrawRewardsSheet(
    amountPaise: Int,
    isSubmitting: Boolean,
    errorMessage: String?,
    onWithdraw: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var upi by remember { mutableStateOf("") }
    val amountText = amountPaise.formatCurrencyPaise()
    val canWithdraw = upi.isNotBlank() && !isSubmitting

    WalletBottomSheet(
        title = "Withdraw Rewards",
        modifier = modifier.height(455.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF8E5))
                    .border(1.2.dp, Color.Black, RoundedCornerShape(12.dp))
            ) {
                Text(amountText, color = Color.Black, fontSize = 24.sp, fontFamily = GaretFontFamily, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text("Enter your UPI ID", color = Color.Black, fontSize = 11.sp, fontFamily = GaretFontFamily, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Box {
                WalletInput(value = upi, onValueChange = { upi = it }, placeholder = "example@paytm")
                if (upi.isNotBlank()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF35B45D))
                    ) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
            }
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage,
                    color = Color(0xFFE95151),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            WalletYellowButton(
                text = if (isSubmitting) "Withdrawing..." else "Withdraw $amountText",
                enabled = canWithdraw,
                onClick = { onWithdraw(upi.trim()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
            )
        }
    }
}

@Composable
private fun WithdrawalResultSheet(
    state: WithdrawalResult,
    amountPaise: Int,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val amountText = amountPaise.formatCurrencyPaise()
    val image = when (state) {
        WithdrawalResult.Success -> R.drawable.withdrawal_success
        WithdrawalResult.Process -> R.drawable.withdrawal_process
        WithdrawalResult.Failed -> R.drawable.withdrawal_failed
    }
    val title = when (state) {
        WithdrawalResult.Success -> "Withdrawal Successful"
        WithdrawalResult.Process -> "Withdrawal In Progress"
        WithdrawalResult.Failed -> "Withdrawal Failed"
    }
    val color = when (state) {
        WithdrawalResult.Success -> Color(0xFF249842)
        WithdrawalResult.Process -> Color(0xFFFF8D1A)
        WithdrawalResult.Failed -> Color(0xFFFF474D)
    }
    val message = when (state) {
        WithdrawalResult.Success -> "$amountText will be credited to your account\nwithin 2 hours"
        WithdrawalResult.Process -> "Your withdrawal is being processed.\nIf the transfer fails, $amountText will be returned to your wallet."
        WithdrawalResult.Failed -> "We couldn't process your withdrawal\nright now. Please check your UPI ID and try\nagain."
    }
    val button = when (state) {
        WithdrawalResult.Success -> "Awesome!"
        WithdrawalResult.Process -> "Got it"
        WithdrawalResult.Failed -> "Try again"
    }

    WalletBottomSheet(title = "", showTitle = false, modifier = modifier.height(470.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 34.dp)
        ) {
            Spacer(modifier = Modifier.height(22.dp))
            Image(
                painter = painterResource(id = image),
                contentDescription = null,
                modifier = Modifier.size(128.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(title, color = color, fontSize = 18.sp, fontFamily = GaretFontFamily, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = Color(0xFF6C6C6C),
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(1f))
            WalletYellowButton(
                text = button,
                enabled = true,
                onClick = onDone,
                modifier = Modifier.width(272.dp)
            )
        }
    }
}

@Composable
private fun WalletBottomSheet(
    title: String,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    content: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .size(width = 64.dp, height = 6.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFD6D6D6))
        )
        if (showTitle) {
            Spacer(modifier = Modifier.height(28.dp))
            Text(title, color = Color.Black, fontSize = 16.sp, fontFamily = GaretFontFamily, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(58.dp))
        } else {
            Spacer(modifier = Modifier.height(18.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            content()
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun WalletInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.2.dp, Color.Black, RoundedCornerShape(10.dp))
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = Color(0xFFB4B4B4),
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            cursorBrush = SolidColor(Color.Black),
            textStyle = TextStyle(
                color = Color(0xFF222222),
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun WalletYellowButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(9.dp)
    Box(modifier = modifier.height(42.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 3.dp)
                .clip(shape)
                .background(Color.Black.copy(alpha = if (enabled) 1f else 0.25f))
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(WalletYellow.copy(alpha = if (enabled) 1f else 0.38f))
                .clickable(
                    enabled = enabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            Text(
                text = text,
                color = Color.Black.copy(alpha = if (enabled) 1f else 0.25f),
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private enum class WalletSheet {
    PanVerification,
    PanVerified,
    Withdraw,
    WithdrawalResult
}

private enum class WithdrawalResult {
    Success,
    Process,
    Failed
}

private data class WalletTransaction(
    val title: String,
    val date: String,
    val amount: String,
    val coinDelta: String,
    val status: String,
    val statusColor: Color,
    val iconBackground: Color
)

private fun WalletWithdrawalItem.toWalletTransaction(index: Int): WalletTransaction {
    val normalizedStatus = status.trim().uppercase(Locale.ENGLISH)
    val isFailed = normalizedStatus.contains("FAIL") || normalizedStatus == "REJECTED"
    val isCompleted = normalizedStatus.contains("COMPLETE") ||
        normalizedStatus == "SUCCESS" ||
        normalizedStatus == "PAID"
    val statusLabel = when {
        isCompleted -> "Completed"
        isFailed -> "Failed"
        normalizedStatus.isBlank() -> "Pending"
        else -> status.lowercase(Locale.ENGLISH).replaceFirstChar { it.titlecase(Locale.ENGLISH) }
    }
    val statusColor = when {
        isCompleted -> Color(0xFF39B86B)
        isFailed -> Color(0xFFE95151)
        else -> Color(0xFFFF8D1A)
    }
    val backgrounds = listOf(
        Color(0xFFE3F7E9),
        Color(0xFFF0E7FF),
        Color(0xFFFFF0C8),
        Color(0xFFDDF9F5)
    )
    return WalletTransaction(
        title = title.ifBlank { "Withdrawal" },
        date = createdAt.formatWithdrawalDate(),
        amount = amountPaise.formatCurrencyPaise(),
        coinDelta = "-$coinAmount Coins",
        status = statusLabel,
        statusColor = statusColor,
        iconBackground = backgrounds[index % backgrounds.size]
    )
}

private fun String?.formatWithdrawalDate(): String {
    if (isNullOrBlank()) return ""
    val outputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
    runCatching {
        return Instant.parse(this)
            .atZone(ZoneId.systemDefault())
            .format(outputFormatter)
    }
    runCatching {
        return LocalDateTime.parse(this)
            .atZone(ZoneId.systemDefault())
            .format(outputFormatter)
    }
    return this
}

private fun Int.formatWalletBalance(): String = formatCurrencyPaise()

private fun Int.formatCurrencyPaise(): String {
    val paise = coerceAtLeast(0)
    val rupees = paise / 100
    val fractionalPaise = (paise % 100).toString().padStart(2, '0')
    return "\u20B9$rupees.$fractionalPaise"
}

private fun Int.formatWalletBalancePaise(fallbackAmountInr: Int): String {
    val paise = if (this > 0) this else fallbackAmountInr * 100
    return paise.formatCurrencyPaise()
}

private fun Int.formatCoinRate(): String {
    val paise = takeIf { it > 0 } ?: 0
    return paise.formatCurrencyPaise()
}

private const val MIN_REDEEM_COINS = 100

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun WalletScreenPreview() {
    BffAndroidTheme {
        WalletScreen()
    }
}
