package com.gobff.getfriends.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gobff.getfriends.data.model.RechargeOption
import com.gobff.getfriends.data.model.RechargePaymentResolution
import com.gobff.getfriends.data.model.RechargeUiState
import com.gobff.getfriends.R
import com.gobff.getfriends.payment.CashfreePaymentLauncher
import com.gobff.getfriends.ui.component.HandDrawnCardShape
import com.gobff.getfriends.ui.component.HeartChipShape
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.GaretFontFamily
import com.gobff.getfriends.viewmodel.RechargeViewModel
private val RechargePurple = Color(0xFFAB179C)
private val RechargePink = Color(0xFFFF3F78)
private val RechargeInk = Color(0xFF101010)
private val RechargeMuted = Color(0xFF777777)
private val RechargeProcessPurple = Color(0xFF4D13A5)

@Composable
fun RechargeScreen(
    modifier: Modifier = Modifier,
    walletHearts: Int = 0,
    onBack: () -> Unit = {},
    onRechargeSuccess: () -> Unit = {},
    rechargeViewModel: RechargeViewModel = viewModel()
) {
    val rechargeUiState = rechargeViewModel.uiState
    val rechargePacks = remember(rechargeUiState.options) {
        rechargeUiState.options.mapIndexed { index, option ->
            option.toRechargePack(index)
        }
    }
    val paymentMethods = remember { paymentMethods() }
    var selectedPayment by remember { mutableStateOf(paymentMethods.first()) }
    var stage by remember { mutableStateOf(RechargeStage.Main) }
    var appliedCouponCode by remember { mutableStateOf("") }
    var pendingCouponCode by remember { mutableStateOf("") }
    var availableUpiApps by remember { mutableStateOf(emptyList<CashfreePaymentLauncher.UpiPaymentApp>()) }
    val context = LocalContext.current
    val selectedPack = rechargePacks.firstOrNull { it.id == rechargeUiState.selectedOptionId }
        ?: rechargePacks.firstOrNull()

    BackHandler {
        when (stage) {
            RechargeStage.Main -> onBack()
            RechargeStage.Coupon -> stage = RechargeStage.Main
            RechargeStage.UpiPicker -> stage = RechargeStage.Processing
            RechargeStage.Processing,
            RechargeStage.PaymentStatus -> {
                rechargeViewModel.clearQuoteState()
                stage = RechargeStage.Main
            }
            RechargeStage.Success -> {
                rechargeViewModel.clearQuoteState()
                stage = RechargeStage.Main
            }
        }
    }

    LaunchedEffect(rechargeUiState.paymentResolution) {
        when (rechargeUiState.paymentResolution) {
            RechargePaymentResolution.Success -> {
                onRechargeSuccess()
                stage = RechargeStage.Success
            }
            RechargePaymentResolution.Failed,
            RechargePaymentResolution.Pending -> stage = RechargeStage.PaymentStatus
            null -> Unit
        }
    }

    LaunchedEffect(stage, rechargeUiState.isPurchaseLoading, rechargeUiState.checkout) {
        if (stage == RechargeStage.Processing &&
            !rechargeUiState.isPurchaseLoading &&
            !rechargeUiState.isPurchaseSuccessful &&
            rechargeUiState.purchaseMessage == null
        ) {
            rechargeViewModel.purchaseRecharge(couponCode = pendingCouponCode)
            return@LaunchedEffect
        }

        val checkout = rechargeUiState.checkout
        if (stage == RechargeStage.Processing &&
            checkout != null &&
            rechargeUiState.launchedCheckoutKey != checkout.launchKey
        ) {
            val activity = context as? Activity
            if (activity == null) {
                rechargeViewModel.markCheckoutLaunchFailed("Unable to open payment page")
                stage = RechargeStage.Main
                return@LaunchedEffect
            }

            if (checkout.hasCashfreeSession) {
                val upiApps = CashfreePaymentLauncher.installedUpiApps(activity)
                if (upiApps.isNotEmpty()) {
                    availableUpiApps = upiApps
                    stage = RechargeStage.UpiPicker
                    return@LaunchedEffect
                }
            }

            when (val result = CashfreePaymentLauncher.fallback(
                activity = activity,
                checkout = checkout,
                onReturn = rechargeViewModel::markPaymentReturned,
                onFailure = rechargeViewModel::markPaymentReturnFailed
            )) {
                CashfreePaymentLauncher.LaunchResult.Launched -> {
                    rechargeViewModel.markCheckoutLaunched(checkout.launchKey)
                }
                is CashfreePaymentLauncher.LaunchResult.Failure -> {
                    rechargeViewModel.markCheckoutLaunchFailed(result.message)
                    stage = RechargeStage.Main
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (stage) {
            RechargeStage.Main, RechargeStage.Coupon -> RechargeMainContent(
                modifier = if (stage == RechargeStage.Coupon) Modifier.blur(8.6.dp) else Modifier,
                selectedPack = selectedPack,
                selectedPayment = selectedPayment,
                walletHearts = walletHearts,
                rechargePacks = rechargePacks,
                rechargeUiState = rechargeUiState,
                paymentMethods = paymentMethods,
                onPackSelected = { rechargeViewModel.selectOption(it.id) },
                onPaymentSelected = { selectedPayment = it },
                onRetry = rechargeViewModel::loadRechargeOptions,
                onBack = onBack,
                onCouponClick = { stage = RechargeStage.Coupon },
                onPayClick = {
                    if (selectedPack != null) {
                        rechargeViewModel.clearQuoteState()
                        pendingCouponCode = appliedCouponCode.trim()
                        stage = RechargeStage.Processing
                    }
                }
            )
            RechargeStage.Processing -> RechargeProcessingScreen(
                statusMessage = rechargeUiState.purchaseMessage
            )
            RechargeStage.UpiPicker -> RechargeProcessingScreen(
                statusMessage = rechargeUiState.purchaseMessage
            )
            RechargeStage.PaymentStatus -> RechargePaymentStatusScreen(
                resolution = rechargeUiState.paymentResolution ?: RechargePaymentResolution.Pending,
                statusMessage = rechargeUiState.purchaseMessage,
                onBackToRecharge = {
                    rechargeViewModel.clearQuoteState()
                    stage = RechargeStage.Main
                }
            )
            RechargeStage.Success -> RechargeSuccessScreen(
                balance = walletHearts,
                onDismiss = {
                    rechargeViewModel.clearQuoteState()
                    stage = RechargeStage.Main
                },
                onStartTalking = {
                    rechargeViewModel.clearQuoteState()
                    onBack()
                }
            )
        }

        if (stage == RechargeStage.Coupon) {
            CouponOverlay(
                couponCode = appliedCouponCode,
                onCouponCodeChange = { appliedCouponCode = it },
                onApply = {
                    appliedCouponCode = it.trim()
                    stage = RechargeStage.Main
                },
                onDismiss = { stage = RechargeStage.Main }
            )
        }

        if (stage == RechargeStage.UpiPicker) {
            UpiAppsOverlay(
                apps = availableUpiApps,
                onDismiss = { stage = RechargeStage.Processing },
                onAppSelected = { app ->
                    val activity = context as? Activity
                    val checkout = rechargeUiState.checkout
                    if (activity == null || checkout == null) {
                        rechargeViewModel.markCheckoutLaunchFailed("Unable to open payment app")
                        stage = RechargeStage.Main
                        return@UpiAppsOverlay
                    }

                    when (val result = CashfreePaymentLauncher.launchUpiIntent(
                        activity = activity,
                        checkout = checkout,
                        selectedPackageName = app.packageName,
                        onReturn = rechargeViewModel::markPaymentReturned,
                        onFailure = rechargeViewModel::markPaymentReturnFailed
                    )) {
                        CashfreePaymentLauncher.LaunchResult.Launched -> {
                            rechargeViewModel.markCheckoutLaunched(checkout.launchKey)
                            stage = RechargeStage.Processing
                        }
                        is CashfreePaymentLauncher.LaunchResult.Failure -> {
                            rechargeViewModel.markCheckoutLaunchFailed(result.message)
                            stage = RechargeStage.Main
                        }
                    }
                },
                onFallback = {
                    val activity = context as? Activity
                    val checkout = rechargeUiState.checkout
                    if (activity == null || checkout == null) {
                        rechargeViewModel.markCheckoutLaunchFailed("Unable to open payment page")
                        stage = RechargeStage.Main
                        return@UpiAppsOverlay
                    }

                    when (val result = CashfreePaymentLauncher.fallback(
                        activity = activity,
                        checkout = checkout,
                        onReturn = rechargeViewModel::markPaymentReturned,
                        onFailure = rechargeViewModel::markPaymentReturnFailed
                    )) {
                        CashfreePaymentLauncher.LaunchResult.Launched -> {
                            rechargeViewModel.markCheckoutLaunched(checkout.launchKey)
                            stage = RechargeStage.Processing
                        }
                        is CashfreePaymentLauncher.LaunchResult.Failure -> {
                            rechargeViewModel.markCheckoutLaunchFailed(result.message)
                            stage = RechargeStage.Main
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun RechargeMainContent(
    modifier: Modifier = Modifier,
    selectedPack: RechargePack?,
    selectedPayment: PaymentMethod,
    walletHearts: Int,
    rechargePacks: List<RechargePack>,
    rechargeUiState: RechargeUiState,
    paymentMethods: List<PaymentMethod>,
    onPackSelected: (RechargePack) -> Unit,
    onPaymentSelected: (PaymentMethod) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onCouponClick: () -> Unit,
    onPayClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp)
        ) {
            RechargeHeader(
                balance = walletHearts,
                onBack = onBack
            )
            Spacer(modifier = Modifier.height(22.dp))
            when {
                rechargeUiState.isLoading -> {
                    RechargeOptionsStatus(
                        title = "Loading recharge options...",
                        actionLabel = null,
                        onAction = null,
                        modifier = Modifier.padding(horizontal = 30.dp)
                    )
                }
                rechargePacks.isEmpty() -> {
                    RechargeOptionsStatus(
                        title = rechargeUiState.errorMessage ?: "No recharge options available",
                        actionLabel = "Retry",
                        onAction = onRetry,
                        modifier = Modifier.padding(horizontal = 30.dp)
                    )
                }
                else -> {
                    RechargePackGrid(
                        packs = rechargePacks,
                        selectedPack = selectedPack,
                        onPackSelected = onPackSelected,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(34.dp))
            CouponOfferCard(
                modifier = Modifier.padding(horizontal = 24.dp),
                onClick = onCouponClick
            )
            Spacer(modifier = Modifier.height(28.dp))
            PaymentMethodRow(
                methods = paymentMethods,
                selectedPayment = selectedPayment,
                onPaymentSelected = onPaymentSelected,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(42.dp))
            selectedPack?.let { pack ->
                RechargePayButton(
                    pack = pack,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    onClick = onPayClick
                )
            }
        }
    }
}

@Composable
private fun RechargeHeader(
    balance: Int,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .background(RechargePurple)
    ) {
        Image(
            painter = painterResource(id = R.drawable.recharge_screen_background),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .offset(y = (-6).dp),
            contentScale = ContentScale.FillBounds
        )
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(36.dp)
        ) {
            val path = Path().apply {
                moveTo(0f, size.height * 0.2f)
                quadraticTo(
                    size.width * 0.5f,
                    size.height * 1.08f,
                    size.width,
                    size.height * 0.2f
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path = path, color = Color.White)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onBack)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Recharge Now",
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 30.dp)
                .size(24.dp)
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 56.dp)
        ) {
            Text(
                text = "Don't feel lonely",
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Recharge & keep talking",
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
        BalanceChip(
            balance = balance,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 50.dp)
        )
    }
}

@Composable
private fun BalanceChip(
    balance: Int,
    modifier: Modifier = Modifier
) {
    val shape = HeartChipShape
    Box(modifier = modifier.size(width = 152.dp, height = 56.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 2.5.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .border(1.3.dp, Color.Black, shape)
                .padding(horizontal = 14.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.single_heart),
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Your Balance",
                    color = RechargeMuted,
                    fontSize = 11.sp,
                    lineHeight = 10.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = balance.toString(),
                    color = RechargeInk,
                    fontSize = 14.sp,
                    lineHeight = 13.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RechargePackGrid(
    packs: List<RechargePack>,
    selectedPack: RechargePack?,
    onPackSelected: (RechargePack) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        packs.chunked(3).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(13.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { pack ->
                    RechargePackCard(
                        pack = pack,
                        isSelected = selectedPack?.id == pack.id,
                        onClick = { onPackSelected(pack) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RechargeOptionsStatus(
    title: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF8F8F8))
            .border(1.dp, Color(0xFFE5E5E5), RoundedCornerShape(18.dp))
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = title,
            color = Color.Black,
            fontSize = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium
        )
        if (!actionLabel.isNullOrBlank() && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = actionLabel,
                color = RechargePurple,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

@Composable
private fun RechargePackCard(
    pack: RechargePack,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = HeartChipShape
    Box(
        modifier = modifier
            .height(132.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = if (isSelected) 1.5.dp else 0.dp, y = if (isSelected) 2.dp else 0.dp)
                .clip(shape)
                .background(if (isSelected) Color.Black else Color.Transparent)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(if (isSelected) Color(0xFFFFEDF3) else Color.White)
                .border(
                    width = if (isSelected) 1.2.dp else 0.8.dp,
                    color = if (isSelected) Color.Black else Color(0xFFE5E5E5),
                    shape = shape
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(top = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = pack.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(pack.iconSize),
                    contentScale = ContentScale.Fit
                )
                if (isSelected) {
                    SelectedCheck(modifier = Modifier.align(Alignment.TopEnd).padding(end = 10.dp))
                }
            }
            Text(
                text = pack.hearts.toString(),
                color = Color.Black,
                fontSize = 16.sp,
                lineHeight = 11.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Hearts",
                color = RechargeMuted,
                fontSize = 10.sp,
                lineHeight = 11.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(7.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(if (isSelected) RechargePink else Color(0xFFF7F7F7))
            ) {
                Text(
                    text = "₹${pack.price}",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (pack.isPopular) {
            PopularPill(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-12).dp)
            )
        }
    }
}

@Composable
private fun SelectedCheck(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(Color(0xFFFF5B86))
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun PopularPill(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2EB4A4))
            .padding(horizontal = 6.dp, vertical = 1.dp)
    ) {
        Text(
            text = "Popular",
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CouponOfferCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF1F0FF))
            .padding(start = 12.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Coupons & offers",
                color = Color(0xFF08033D),
                fontSize = 16.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier= Modifier.height(3.dp))
            Text(
                text = buildAnnotatedString {
                    append("Grab the ")
                    withStyle(SpanStyle(color = RechargePink, fontWeight = FontWeight.Bold)) {
                        append("best deals")
                    }
                    append(" before\nthey're gone!")
                },
                color = Color(0xFF777777),
                fontSize = 10.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Normal,
                lineHeight = 10.sp
            )
        }
        Image(
            painter = painterResource(id = R.drawable.coupan_icon),
            contentDescription = null,
            modifier = Modifier.size(width = 114.dp, height = 84.dp),
            contentScale = ContentScale.Fit
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(end = 12.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFFAB179C))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@Composable
private fun PaymentMethodRow(
    methods: List<PaymentMethod>,
    selectedPayment: PaymentMethod,
    onPaymentSelected: (PaymentMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.width(314.dp)
    ) {
        methods.forEach { method ->
            PaymentMethodItem(
                method = method,
                isSelected = selectedPayment == method,
                onClick = { onPaymentSelected(method) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PaymentMethodItem(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.size(44.dp)
        ) {
            val shape = HeartChipShape
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(39.dp)
                    .clip(shape)
                    .background(Color.White)
                    .border(1.dp, Color.Black, shape)
                    .clickable(onClick = onClick)
            ) {
                Image(
                    painter = painterResource(id = method.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(width = method.iconWidth, height = method.iconHeight),
                    contentScale = ContentScale.Fit
                )
            }
            if (isSelected) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(14.4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2FBF64))
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.8.dp)
                    )
                }
            }
        }
        Text(
            text = method.label,
            color = Color.Black,
            fontSize = 11.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun RechargePayButton(
    pack: RechargePack,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = HandDrawnCardShape
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 2.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color(0xFFD184C8))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = "Pay ₹${pack.price}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun UpiAppsOverlay(
    apps: List<CashfreePaymentLauncher.UpiPaymentApp>,
    onDismiss: () -> Unit,
    onAppSelected: (CashfreePaymentLauncher.UpiPaymentApp) -> Unit,
    onFallback: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.50f))
            .clickable(onClick = onDismiss)
    ) {
        val sheetShape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(sheetShape)
                .background(Color.White)
                .clickable(enabled = false, onClick = {})
                .padding(start = 24.dp, top = 22.dp, end = 24.dp, bottom = 30.dp)
        ) {
            Text(
                text = "Pay with UPI",
                color = RechargeInk,
                fontSize = 18.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Choose an app to complete the payment",
                color = RechargeMuted,
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(18.dp))
            apps.take(8).forEach { app ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { onAppSelected(app) }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F0FF))
                    ) {
                        Text(
                            text = app.label.firstOrNull()?.uppercase() ?: "U",
                            color = RechargePurple,
                            fontSize = 14.sp,
                            fontFamily = GaretFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = app.label,
                        color = RechargeInk,
                        fontSize = 14.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = RechargeMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Use another payment method",
                color = RechargePurple,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable(onClick = onFallback)
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun CouponOverlay(
    couponCode: String,
    onCouponCodeChange: (String) -> Unit,
    onApply: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.50f))
            .clickable(onClick = onDismiss)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .height(510.dp)
        ) {
            val sheetShape = RoundedCornerShape(26.dp)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 4.dp, y = 5.dp)
                    .clip(sheetShape)
                    .background(Color.Black)
            )
            Column(
                modifier = Modifier
                    .matchParentSize()
                    .clip(sheetShape)
                    .background(Color.White)
                    .clickable(enabled = false, onClick = {})
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp)
                        .background(RechargePurple)
                ) {
                    val couponInputHeight = 48.dp
                    val waveHeight = 34.dp
                    Image(
                        painter = painterResource(id = R.drawable.recharge_screen_background),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                    Canvas(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(waveHeight)
                    ) {
                        val path = Path().apply {
                            moveTo(0f, size.height * 0.25f)
                            quadraticTo(size.width * 0.5f, size.height, size.width, size.height * 0.25f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(path = path, color = Color.White)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (138.dp - waveHeight - couponInputHeight) / 2)
                            .width(300.dp)
                            .height(couponInputHeight)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .padding(horizontal = 18.dp)
                    ) {
                        TextField(
                            value = couponCode,
                            onValueChange = onCouponCodeChange,
                            placeholder = {
                                Text(
                                    text = "Enter coupon code",
                                    color = Color(0xFF777777),
                                    fontSize = 14.sp,
                                    fontFamily = GaretFontFamily
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFF404040),
                                fontSize = 14.sp,
                                fontFamily = GaretFontFamily,
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Apply",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontFamily = GaretFontFamily,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                onApply(couponCode)
                            }
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 18.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.no_coupon_available),
                        contentDescription = null,
                        modifier = Modifier.size(width = 230.dp, height = 210.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "No coupons available",
                        color = Color(0xFF7E7E7E),
                        fontSize = 14.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RechargeProcessingScreen(
    statusMessage: String?
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recharge-process")
    val progressSweep by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 320f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress-sweep"
    )
    val progressRotation by infiniteTransition.animateFloat(
        initialValue = -90f,
        targetValue = 270f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress-rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RechargeProcessPurple)
    ) {
        Image(
            painter = painterResource(id = R.drawable.process_screen_oject),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(170.dp)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = size.minDimension / 2.45f
                    )
                    drawArc(
                        color = Color.White,
                        startAngle = progressRotation,
                        sweepAngle = progressSweep,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 10.dp.toPx(),
                            cap = StrokeCap.Round
                        ),
                        topLeft = androidx.compose.ui.geometry.Offset(
                            x = size.width * 0.16f,
                            y = size.height * 0.16f
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            width = size.width * 0.68f,
                            height = size.height * 0.68f
                        )
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.single_heart),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Adding your hearts...",
                color = Color.White,
                fontSize = 22.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This usually take a few seconds.",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Normal
            )
            if (!statusMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = statusMessage,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun RechargePaymentStatusScreen(
    resolution: RechargePaymentResolution,
    statusMessage: String?,
    onBackToRecharge: () -> Unit
) {
    val isFailed = resolution == RechargePaymentResolution.Failed
    val title = if (isFailed) "Payment failed" else "Payment pending"
    val fallbackMessage = if (isFailed) {
        "Your payment could not be completed. No hearts were added."
    } else {
        "We are still confirming your payment. We'll update your hearts once it is confirmed."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RechargeProcessPurple)
    ) {
        Image(
            painter = painterResource(id = R.drawable.process_screen_oject),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 30.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(118.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isFailed) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(46.dp)
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 24.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = statusMessage?.takeIf { it.isNotBlank() } ?: fallbackMessage,
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(30.dp))
            LargeSuccessButton(
                text = "Back to recharge",
                onClick = onBackToRecharge
            )
        }
    }
}

@Composable
private fun RechargeSuccessScreen(
    balance: Int,
    onDismiss: () -> Unit,
    onStartTalking: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RechargeProcessPurple)
    ) {
        Image(
            painter = painterResource(id = R.drawable.process_screen_oject),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
        Text(
            text = "Yay!",
            color = Color.White,
            fontSize = 34.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 88.dp)
        )
        Text(
            text = "Recharge successful",
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 140.dp)
        )
        Text(
            text = "x",
            color = Color.White,
            fontSize = 34.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 56.dp, end = 28.dp)
                .clickable(onClick = onDismiss)
        )
        Image(
            painter = painterResource(id = R.drawable.success_screen_people),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .offset(y = (-24).dp),
            contentScale = ContentScale.FillWidth
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 28.dp, end = 28.dp)
        ) {
            Text(
                text = "You can now talk for up to",
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "20 Minutes",
                color = Color(0xFFFFE16A),
                fontSize = 34.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(26.dp))
            BalanceAvailableChip(balance = balance)
            Spacer(modifier = Modifier.height(24.dp))
            LargeSuccessButton(
                text = "Start talking now",
                onClick = onStartTalking
            )
        }
    }
}

@Composable
private fun BalanceAvailableChip(balance: Int) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = Modifier
            .size(width = 210.dp, height = 34.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.28f),
                            Color.White.copy(alpha = 0.14f)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.35f), shape)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.matchParentSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.single_heart),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$balance hearts available",
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LargeSuccessButton(
    text: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable(onClick = onClick)
    ) {
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
                .background(Color.White)
        ) {
            Text(
                text = text,
                color = RechargeProcessPurple,
                fontSize = 18.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class RechargePack(
    val id: String,
    val hearts: Int,
    val price: Int,
    val iconRes: Int,
    val iconSize: androidx.compose.ui.unit.Dp,
    val isPopular: Boolean = false
)

private data class PaymentMethod(
    val label: String,
    val iconRes: Int,
    val iconWidth: androidx.compose.ui.unit.Dp,
    val iconHeight: androidx.compose.ui.unit.Dp
)

private enum class RechargeStage {
    Main,
    Coupon,
    UpiPicker,
    Processing,
    PaymentStatus,
    Success
}

private fun RechargeOption.toRechargePack(index: Int): RechargePack {
    val iconRes = when (index % 6) {
        0 -> R.drawable.single_heart
        1 -> R.drawable.double_heart
        2 -> R.drawable.triple_heart
        3 -> R.drawable.bucket_hearts
        4 -> R.drawable.box_hearts
        else -> R.drawable.safe_hearts
    }
    return RechargePack(
        id = id,
        hearts = hearts,
        price = price,
        iconRes = iconRes,
        iconSize = 46.dp,
        isPopular = isPopular
    )
}

private fun paymentMethods() = listOf(
    PaymentMethod("G pay", R.drawable.recharge_gpay, 22.67.dp, 19.2.dp),
    PaymentMethod("Phonepe", R.drawable.recharge_phonepe, 19.2.dp, 19.2.dp),
    PaymentMethod("Other apps", R.drawable.recharge_upi, 22.4.dp, 8.36.dp),
    PaymentMethod("Card", R.drawable.recharge_debit_card, 19.2.dp, 12.22.dp)
)

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun RechargeScreenPreview() {
    val previewOptions = listOf(
        RechargeOption(id = "pack_1", packCode = "HEARTS_90", hearts = 90, price = 29),
        RechargeOption(id = "pack_2", packCode = "HEARTS_180", hearts = 180, price = 49, isPopular = true),
        RechargeOption(id = "pack_3", packCode = "HEARTS_360", hearts = 360, price = 99),
        RechargeOption(id = "pack_4", packCode = "HEARTS_720", hearts = 720, price = 199),
        RechargeOption(id = "pack_5", packCode = "HEARTS_1500", hearts = 1500, price = 399),
        RechargeOption(id = "pack_6", packCode = "HEARTS_3000", hearts = 3000, price = 799)
    )
    val rechargePacks = previewOptions.mapIndexed { index, option -> option.toRechargePack(index) }
    val paymentMethods = paymentMethods()
    var selectedPack by remember { mutableStateOf(rechargePacks[1]) }
    var selectedPayment by remember { mutableStateOf(paymentMethods.first()) }

    BffAndroidTheme {
        RechargeMainContent(
            selectedPack = selectedPack,
            selectedPayment = selectedPayment,
            walletHearts = 10940,
            rechargePacks = rechargePacks,
            rechargeUiState = RechargeUiState(
                isLoading = false,
                options = previewOptions,
                selectedOptionId = selectedPack.id
            ),
            paymentMethods = paymentMethods,
            onPackSelected = { selectedPack = it },
            onPaymentSelected = { selectedPayment = it },
            onRetry = {},
            onBack = {},
            onCouponClick = {},
            onPayClick = {}
        )
    }
}

//@Preview(showBackground = true, widthDp = 393, heightDp = 852)
//@Composable
//private fun CouponOverlayPreview() {
//    BffAndroidTheme {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(RechargePurple)
//        ) {
//            Image(
//                painter = painterResource(id = R.drawable.recharge_screen_background),
//                contentDescription = null,
//                modifier = Modifier.fillMaxSize(),
//                contentScale = ContentScale.Crop
//            )
//            CouponOverlay(onDismiss = {})
//        }
//    }
//}
