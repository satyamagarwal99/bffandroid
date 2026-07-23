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
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gobff.getfriends.data.model.RechargeOption
import com.gobff.getfriends.data.model.RechargePaymentResolution
import com.gobff.getfriends.data.model.RechargeUiState
import com.gobff.getfriends.R
import com.gobff.getfriends.payment.CashfreePaymentLauncher
import com.gobff.getfriends.ui.component.HeartChipShape
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.GaretFontFamily
import com.gobff.getfriends.viewmodel.RechargeViewModel
private val RechargePurple = Color(0xFFAB179C)
private val RechargePink = Color(0xFFFF3F78)
private val RechargeInk = Color(0xFF101010)
private val RechargeMuted = Color(0xFF777777)
private val RechargeProcessPurple = Color(0xFF4D13A5)
private val RechargeButtonShape = GenericShape { size, _ ->
    val corner = size.height * 0.30f
    moveTo(corner * 1.12f, 1.2f)
    cubicTo(size.width * 0.24f, -1.8f, size.width * 0.74f, -0.6f, size.width - corner * 1.08f, 1.2f)
    cubicTo(size.width - corner * 0.34f, 1.8f, size.width - 1.2f, corner * 0.28f, size.width - 1.2f, corner * 0.92f)
    lineTo(size.width - 1.0f, size.height - corner * 0.84f)
    cubicTo(size.width - 1.8f, size.height - corner * 0.24f, size.width - corner * 0.40f, size.height - 1.2f, size.width - corner * 1.16f, size.height - 1.0f)
    cubicTo(size.width * 0.72f, size.height + 1.8f, size.width * 0.27f, size.height + 1.2f, corner * 1.00f, size.height - 1.8f)
    cubicTo(corner * 0.22f, size.height - 2.8f, 1.0f, size.height - corner * 0.52f, 1.0f, size.height - corner * 1.10f)
    lineTo(1.0f, corner * 1.02f)
    cubicTo(1.0f, corner * 0.32f, corner * 0.26f, 3.0f, corner * 1.12f, 1.2f)
    close()
}

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
    val lifecycleOwner = LocalLifecycleOwner.current
    val selectedPack = rechargePacks.firstOrNull { it.id == rechargeUiState.selectedOptionId }

    BackHandler {
        when (stage) {
            RechargeStage.Main -> onBack()
            RechargeStage.Coupon -> stage = RechargeStage.Main
            RechargeStage.UpiPicker -> {
                rechargeViewModel.clearQuoteState()
                stage = RechargeStage.Main
            }
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
            RechargePaymentResolution.Pending,
            RechargePaymentResolution.InProgress -> stage = RechargeStage.PaymentStatus
            null -> Unit
        }
    }

    DisposableEffect(
        lifecycleOwner,
        rechargeUiState.launchedCheckoutKey,
        rechargeUiState.paymentResolution,
        stage
    ) {
        val observer = LifecycleEventObserver { _, event ->
            val shouldRefreshStatus = event == Lifecycle.Event.ON_RESUME &&
                rechargeUiState.launchedCheckoutKey != null &&
                stage in setOf(RechargeStage.Processing, RechargeStage.PaymentStatus) &&
                rechargeUiState.paymentResolution !in setOf(
                    RechargePaymentResolution.Success,
                    RechargePaymentResolution.Failed,
                    RechargePaymentResolution.InProgress
                )

            if (shouldRefreshStatus) {
                rechargeViewModel.refreshActivePaymentStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
                statusMessage = rechargeUiState.purchaseMessage,
                onBackToRecharge = {
                    rechargeViewModel.clearQuoteState()
                    stage = RechargeStage.Main
                }
            )
            RechargeStage.UpiPicker -> RechargeProcessingScreen(
                statusMessage = rechargeUiState.purchaseMessage,
                onBackToRecharge = {
                    rechargeViewModel.clearQuoteState()
                    stage = RechargeStage.Main
                }
            )
            RechargeStage.PaymentStatus -> RechargePaymentStatusScreen(
                resolution = rechargeUiState.paymentResolution ?: RechargePaymentResolution.Pending,
                statusMessage = rechargeUiState.purchaseMessage,
                timerEndsAtMillis = rechargeUiState.statusTimerEndsAtMillis,
                isTimerRunning = rechargeUiState.isStatusPolling,
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
                onDismiss = {
                    rechargeViewModel.clearQuoteState()
                    stage = RechargeStage.Main
                },
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
            RechargePayButton(
                pack = selectedPack,
                modifier = Modifier.padding(horizontal = 18.dp),
                onClick = onPayClick
            )
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
    pack: RechargePack?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isEnabled = pack != null
    val shape = RechargeButtonShape
    val buttonColor = if (isEnabled) RechargePurple else Color(0xFFD184C8)
    val buttonText = pack?.let { "Add \u20B9${it.price} \u2022 ${it.hearts} Hearts" } ?: "Select a pack"
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(enabled = isEnabled, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 4.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(shape)
                .background(buttonColor)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = buttonText,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(22.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
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
    statusMessage: String?,
    onBackToRecharge: () -> Unit
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

    RechargeStatusBackground {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 18.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(154.dp)
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
                    modifier = Modifier.size(66.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Adding your hearts...",
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This usually take a few seconds.",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
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
    timerEndsAtMillis: Long?,
    isTimerRunning: Boolean,
    onBackToRecharge: () -> Unit
) {
    val isFailed = resolution == RechargePaymentResolution.Failed
    if (!isFailed) {
        KeepScreenOnEffect()
    }

    if (resolution == RechargePaymentResolution.InProgress) {
        RechargeInProgressStatus(onBackToRecharge = onBackToRecharge)
        return
    }

    if (!isFailed) {
        PendingPaymentTimerStatus(
            statusMessage = statusMessage,
            timerEndsAtMillis = timerEndsAtMillis,
            isTimerRunning = isTimerRunning
        )
        return
    }

    RechargeStatusBackground {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 2.dp)
                .padding(horizontal = 32.dp)
        ) {
            PaymentStatusIcon(isFailed = isFailed)
            Spacer(modifier = Modifier.height(26.dp))
            Text(
                text = "Payment Failed",
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "We couldn't add your Hearts. Your amount wasn't deducted. Please try again.",
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }

        LargeSuccessButton(
            text = "Try again",
            textColor = RechargeInk,
            onClick = onBackToRecharge,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 36.dp, end = 36.dp, bottom = 56.dp)
        )
    }
}

@Composable
private fun KeepScreenOnEffect() {
    val view = LocalView.current
    DisposableEffect(view) {
        val previousKeepScreenOn = view.keepScreenOn
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = previousKeepScreenOn
        }
    }
}

@Composable
private fun RechargeInProgressStatus(onBackToRecharge: () -> Unit) {
    RechargeStatusBackground {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-28).dp)
                .padding(horizontal = 32.dp)
        ) {
            PaymentStatusIcon(isFailed = false, modifier = Modifier.size(128.dp))
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Recharge in progress",
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "If your payment was successful,\nyour hearts will be added in a few minutes.",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }

        RefundInfoCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 32.dp, end = 32.dp, bottom = 130.dp)
        )

        LargeSuccessButton(
            text = "Got it",
            textColor = RechargeInk,
            onClick = onBackToRecharge,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 36.dp, end = 36.dp, bottom = 46.dp)
        )
    }
}

@Composable
private fun PendingPaymentTimerStatus(
    statusMessage: String?,
    timerEndsAtMillis: Long?,
    isTimerRunning: Boolean
) {
    var remainingSeconds by remember(timerEndsAtMillis) {
        mutableStateOf(timerEndsAtMillis.remainingPaymentTimerSeconds())
    }

    LaunchedEffect(timerEndsAtMillis) {
        if (timerEndsAtMillis == null) return@LaunchedEffect
        while (true) {
            remainingSeconds = timerEndsAtMillis.remainingPaymentTimerSeconds()
            if (remainingSeconds <= 0) break
            kotlinx.coroutines.delay(250L)
        }
    }

    RechargeStatusBackground {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 18.dp)
                .padding(horizontal = 30.dp)
        ) {
            PendingPaymentTimerIcon(isRunning = isTimerRunning && remainingSeconds > 0)
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Payment is pending",
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusMessage?.takeIf { it.isNotBlank() }
                    ?: "We are still confirming your payment. Your hearts will be added once it is confirmed.",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
            if (timerEndsAtMillis != null) {
                Spacer(modifier = Modifier.height(34.dp))
                GlassStatusPill(text = remainingSeconds.formatPaymentTimer())
            }
        }
    }
}

private fun Long?.remainingPaymentTimerSeconds(): Int {
    if (this == null) return 0
    val remainingMillis = this - System.currentTimeMillis()
    return kotlin.math.ceil(remainingMillis.coerceAtLeast(0L) / 1000.0).toInt()
}

private fun Int.formatPaymentTimer(): String {
    val boundedSeconds = coerceAtLeast(0)
    val minutes = boundedSeconds / 60
    val seconds = boundedSeconds % 60
    return "$minutes :${seconds.toString().padStart(2, '0')}"
}

@Composable
private fun PendingPaymentTimerIcon(isRunning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pending-payment")
    val progressSweep by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 320f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pending-progress-sweep"
    )
    val progressRotation by infiniteTransition.animateFloat(
        initialValue = -90f,
        targetValue = 270f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pending-progress-rotation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(154.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = size.minDimension / 2.45f
            )
            drawArc(
                color = Color.White,
                startAngle = if (isRunning) progressRotation else 35f,
                sweepAngle = if (isRunning) progressSweep else 110f,
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
            modifier = Modifier.size(66.dp)
        )
    }
}

@Composable
private fun RechargeStatusBackground(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RechargePurple)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.035f),
                radius = size.width * 0.58f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.82f, size.height * 0.13f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.03f),
                radius = size.width * 0.38f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.86f, size.height * 0.13f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.025f),
                radius = size.width * 0.18f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.93f, size.height * 0.12f)
            )
            drawOval(
                color = Color.White.copy(alpha = 0.025f),
                topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.30f, size.height * 0.76f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.55f, size.height * 0.22f)
            )
            drawOval(
                color = Color.White.copy(alpha = 0.018f),
                topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.40f, size.height * 0.83f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.30f, size.height * 0.11f)
            )
        }
        content()
    }
}

@Composable
private fun GlassStatusPill(text: String) {
    val shape = RoundedCornerShape(51.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(shape)
            .background(Color.White.copy(alpha = 0.1f))
            .border(1.dp, Color.White.copy(alpha = 0.55f), shape)
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PaymentStatusIcon(
    isFailed: Boolean,
    modifier: Modifier = Modifier.size(146.dp)
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(Color.White, radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * 0.16f, size.height * 0.18f))
            drawCircle(Color(0xFFFF6D9A), radius = 6.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.83f))
            drawCircle(
                Color.White,
                radius = 4.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(size.width * 0.04f, size.height * 0.80f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )
        }
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = Color(0xFFD678FF),
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = 8.dp)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(111.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = if (isFailed) Icons.Default.Close else Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color(0xFFC13AB2),
                    modifier = Modifier.size(38.dp)
                )
            }
        }
    }
}

@Composable
private fun RefundInfoCard(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.55f), shape)
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Icon(
                imageVector = Icons.Default.CurrencyRupee,
                contentDescription = null,
                tint = Color(0xFFC13AB2),
                modifier = Modifier.size(17.dp)
            )
        }
        Text(
            text = buildAnnotatedString {
                append("Recharge failed after payment? You'll receive a refund within ")
                withStyle(
                    SpanStyle(
                        color = Color(0xFFFEE185),
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("2-4 working days")
                }
            },
            color = Color.White,
            fontSize = 11.sp,
            lineHeight = 17.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
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
            .background(RechargePurple)
    ) {
        Image(
            painter = painterResource(id = R.drawable.process_screen_oject),
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .alpha(0.22f),
            contentScale = ContentScale.Crop
        )
        SuccessConfetti(modifier = Modifier.matchParentSize())
        Text(
            text = "Yay!",
            color = Color.White,
            fontSize = 32.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 78.dp)
        )
        Text(
            text = "Recharge successful",
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 130.dp)
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color.White,
            modifier = Modifier
                .size(32.dp)
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
                .offset(y = (-30).dp),
            contentScale = ContentScale.FillWidth
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp, start = 36.dp, end = 36.dp)
        ) {
            BalanceAvailableChip(balance = balance)
            Spacer(modifier = Modifier.height(28.dp))
            LargeSuccessButton(
                text = "Start talking now",
                textColor = RechargeProcessPurple,
                height = 48.dp,
                shape = RoundedCornerShape(12.dp),
                onClick = onStartTalking
            )
        }
    }
}

@Composable
private fun SuccessConfetti(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "success-confetti")
    val fallProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "success-confetti-fall"
    )

    Canvas(modifier = modifier) {
        fun normalizedY(base: Float, speed: Float): Float = (base + fallProgress * speed) % 1f

        fun strip(index: Int, xBase: Float, yBase: Float, color: Color) {
            val speed = 0.22f + (index % 7) * 0.035f
            val y = normalizedY(yBase, speed)
            val sway = kotlin.math.sin((fallProgress * 6.28f + index) * 1.7f).toFloat() * 0.018f
            val x = (xBase + sway).coerceIn(0.01f, 0.99f)
            val tilt = if (index % 2 == 0) 0.014f else -0.014f
            drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(size.width * x, size.height * y),
                end = androidx.compose.ui.geometry.Offset(size.width * (x + tilt), size.height * (y - 0.014f)),
                strokeWidth = (3 + index % 3).dp.toPx(),
                cap = StrokeCap.Square
            )
        }

        val colors = listOf(
            Color(0xFFFF5C82),
            Color(0xFFFFCB26),
            Color(0xFF8F73FF),
            Color(0xFF23D665),
            Color.White
        )
        val particles = listOf(
            0.05f to 0.07f, 0.14f to 0.13f, 0.24f to 0.04f, 0.34f to 0.16f, 0.47f to 0.08f,
            0.60f to 0.14f, 0.72f to 0.05f, 0.84f to 0.17f, 0.94f to 0.10f, 0.09f to 0.30f,
            0.19f to 0.39f, 0.31f to 0.27f, 0.43f to 0.35f, 0.56f to 0.29f, 0.69f to 0.40f,
            0.81f to 0.31f, 0.93f to 0.44f, 0.03f to 0.55f, 0.16f to 0.63f, 0.28f to 0.51f,
            0.41f to 0.60f, 0.53f to 0.49f, 0.66f to 0.58f, 0.79f to 0.52f, 0.91f to 0.65f
        )

        particles.forEachIndexed { index, particle ->
            if (index % 4 == 0) {
                val y = normalizedY(particle.second, 0.24f + (index % 5) * 0.04f)
                val x = particle.first + kotlin.math.sin((fallProgress * 6.28f + index) * 1.3f).toFloat() * 0.014f
                drawCircle(
                    colors[index % colors.size],
                    radius = (2 + index % 4).dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(size.width * x.coerceIn(0.01f, 0.99f), size.height * y)
                )
            } else {
                strip(index, particle.first, particle.second, colors[index % colors.size])
            }
        }
    }
}

@Composable
private fun BalanceAvailableChip(balance: Int) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = Modifier
            .size(width = 286.dp, height = 48.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF4F80).copy(alpha = 0.58f),
                            Color.White.copy(alpha = 0.24f),
                            Color(0xFF7C1BC8).copy(alpha = 0.36f)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.72f), shape)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.single_heart),
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "$balance hearts available",
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LargeSuccessButton(
    text: String,
    textColor: Color = RechargeProcessPurple,
    height: Dp = 48.dp,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
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
                color = textColor,
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
