package com.example.bffandroid.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bffandroid.R
import com.example.bffandroid.ui.theme.BffAndroidTheme
import com.example.bffandroid.ui.theme.FreedokaFontFamily
import com.example.bffandroid.ui.theme.GaretFontFamily
import com.example.bffandroid.viewmodel.LogoutViewModel

private val SettingsPurple = Color(0xFFC471FF)
private val SettingsAccent = Color(0xFF7D3CF0)

private enum class SettingsPage {
    Main,
    Notifications,
    SafetyCenter,
    HelpSupport,
    SuggestFeature,
    AccountManagement
}

private enum class AccountManagementSheet {
    UpdatePhone,
    VerifyPhone,
    PhoneUpdated
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    logoutViewModel: LogoutViewModel = viewModel()
) {
    var page by remember { mutableStateOf(SettingsPage.Main) }

    BackHandler {
        if (page == SettingsPage.Main) {
            onBack()
        } else {
            page = SettingsPage.Main
        }
    }

    when (page) {
        SettingsPage.Main -> SettingsHomeContent(
            onBack = onBack,
            onNotifications = { page = SettingsPage.Notifications },
            onSafetyCenter = { page = SettingsPage.SafetyCenter },
            onHelpSupport = { page = SettingsPage.HelpSupport },
            onSuggestFeature = { page = SettingsPage.SuggestFeature },
            onAccountManagement = { page = SettingsPage.AccountManagement },
            onLogout = { logoutViewModel.logout(onLogout) },
            modifier = modifier
        )

        SettingsPage.Notifications -> NotificationsSettingsContent(
            onBack = { page = SettingsPage.Main },
            modifier = modifier
        )

        SettingsPage.SafetyCenter -> SafetyCenterContent(
            onBack = { page = SettingsPage.Main },
            modifier = modifier
        )

        SettingsPage.HelpSupport -> HelpSupportContent(
            onBack = { page = SettingsPage.Main },
            modifier = modifier
        )

        SettingsPage.SuggestFeature -> SuggestFeatureContent(
            onBack = { page = SettingsPage.Main },
            modifier = modifier
        )

        SettingsPage.AccountManagement -> AccountManagementContent(
            onBack = { page = SettingsPage.Main },
            modifier = modifier
        )
    }
}

@Composable
private fun SettingsHomeContent(
    onBack: () -> Unit,
    onNotifications: () -> Unit,
    onSafetyCenter: () -> Unit,
    onHelpSupport: () -> Unit,
    onSuggestFeature: () -> Unit,
    onAccountManagement: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsBackground(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 34.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(154.dp)
            ) {
                BackButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 20.dp, top = 48.dp)
                )

                Image(
                    painter = painterResource(id = R.drawable.setting_header),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 53.dp)
                        .size(width = 422.32.dp, height = 97.16.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                SettingsGroupCard {
                    SettingsRow(
                        title = "Notifications",
                        iconRes = R.drawable.setting_notification,
                        iconBackground = Color(0xFFFDF6E7),
                        onClick = onNotifications
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "Audio & Effects",
                        iconRes = R.drawable.setting_audio,
                        iconBackground = Color(0xFFE8F5FE),
                        trailing = { SettingsToggle(checked = true) }
                    )
                }

                SettingsGroupCard {
                    SettingsRow(
                        title = "Safety Center",
                        iconRes = R.drawable.setting_safety,
                        iconBackground = Color(0xFFF1EDFC),
                        onClick = onSafetyCenter
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "Help & Support",
                        iconRes = R.drawable.setting_help_support,
                        iconBackground = Color(0xFFEEF8E9),
                        onClick = onHelpSupport
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "Suggest a Feature",
                        iconRes = R.drawable.setting_suggest,
                        iconBackground = Color(0xFFFDEEF5),
                        onClick = onSuggestFeature
                    )
                }

                SettingsGroupCard {
                    SettingsRow(
                        title = "Terms of Service",
                        iconRes = R.drawable.setting_terms,
                        iconBackground = Color(0xFFF3F2F7),
                        showArrow = false
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "Privacy Policy",
                        iconRes = R.drawable.setting_privacy,
                        iconBackground = Color(0xFFF3F2F7),
                        showArrow = false
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "Account Management",
                        iconRes = R.drawable.setting_account,
                        iconBackground = Color(0xFFF3F2F7),
                        onClick = onAccountManagement
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                ShadowButton(
                    text = "Log Out",
                    background = Color(0xFFFF6E73),
                    width = 154.dp,
                    height = 56.dp,
                    onClick = onLogout
                )
            }
        }
    }
}

@Composable
private fun NotificationsSettingsContent(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var audio by remember { mutableStateOf(true) }
    var messages by remember { mutableStateOf(true) }
    var gifts by remember { mutableStateOf(true) }
    var promos by remember { mutableStateOf(true) }

    SettingsDetailScaffold(
        title = "Notifications",
        onBack = onBack,
        modifier = modifier
    ) {
        SettingsGroupCard {
            NotificationRow("Audio & Effects", audio) { audio = !audio }
            SettingsDivider()
            NotificationRow("New Messages", messages) { messages = !messages }
            SettingsDivider()
            NotificationRow("Gift Alerts", gifts) { gifts = !gifts }
            SettingsDivider()
            NotificationRow("Updates & Promos", promos) { promos = !promos }
        }
    }
}

@Composable
private fun SafetyCenterContent(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsDetailScaffold(
        title = "Safety Center",
        onBack = onBack,
        modifier = modifier
    ) {
        SafetyInfoCard(
            iconRes = R.drawable.safety_center_community_rules,
            title = "Community Rules",
            dotColor = Color(0xFFB77CFF),
            bullets = listOf(
                "Keep it fun & friendly: Treat everyone with respect. No hate speech or bullying allowed.",
                "Consent is key: Don't force anyone into a dare they don't want to do.",
                "Keep it clean: No nudity or sexually explicit behavior on camera."
            )
        )
        Spacer(modifier = Modifier.height(26.dp))
        SafetyInfoCard(
            iconRes = R.drawable.safety_center_safety_tips,
            title = "Safety Tips",
            dotColor = Color(0xFFEAC85B),
            bullets = listOf(
                "Guard your info: Never share your phone number, address, or banking details.",
                "Watch your background: Make sure nothing private is visible behind you during video dares.",
                "Trust your gut: If a conversation feels weird, just leave the lobby!"
            )
        )
    }
}

@Composable
private fun HelpSupportContent(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsDetailScaffold(
        title = "Help & Support",
        onBack = onBack,
        modifier = modifier
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            HelpActionCard(
                iconRes = R.drawable.help_bug_report,
                title = "Report a Bug",
                iconBackground = Color(0xFFE7F5D9),
                modifier = Modifier.weight(1f)
            )
            HelpActionCard(
                iconRes = R.drawable.help_contact_us,
                title = "Contact us",
                iconBackground = Color(0xFFFEE4EC),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(26.dp))
        FaqCard()
    }
}

@Composable
private fun SuggestFeatureContent(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsDetailScaffold(
        title = "Suggest A Feature",
        onBack = onBack,
        modifier = modifier
    ) {
        SettingsGroupCard {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 26.dp)
            ) {
                Text(
                    text = "Got a brilliant idea ?",
                    color = Color.Black,
                    fontSize = 17.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(218.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF4F4F4))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "What should we build next?\nA new game? A crazy dare?\nLet us know...",
                        color = Color(0xFFAAAAAA),
                        fontSize = 13.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        ShadowButton(
            text = "Send Idea",
            background = SettingsAccent,
            width = 130.dp,
            height = 48.dp,
            onClick = {}
        )
    }
}

@Composable
private fun AccountManagementContent(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSheet by remember { mutableStateOf<AccountManagementSheet?>(null) }
    var newPhone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("1234") }

    BackHandler {
        if (activeSheet != null) {
            activeSheet = null
        } else {
            onBack()
        }
    }

    SettingsBackground(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 48.dp)
        ) {
            BackButton(onClick = {
                if (activeSheet != null) {
                    activeSheet = null
                } else {
                    onBack()
                }
            })
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Account Management",
                color = Color(0xFF252525),
                fontSize = 20.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(top = 138.dp)
        ) {
            SettingsGroupCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Phone number",
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontFamily = GaretFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "+91 97983 23456",
                            color = Color(0xFF7D7D7D),
                            fontSize = 14.sp,
                            fontFamily = GaretFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(SettingsAccent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { activeSheet = AccountManagementSheet.UpdatePhone }
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Update",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = GaretFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            SettingsGroupCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF0F2))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color(0xFFFF5B63),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(18.dp))
                    Text(
                        text = "Delete account",
                        color = Color(0xFFFF5B63),
                        fontSize = 15.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (activeSheet != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
            )
        }

        when (activeSheet) {
            AccountManagementSheet.UpdatePhone -> UpdatePhoneSheet(
                currentPhone = "+91 97983 23456",
                newPhone = newPhone,
                onNewPhoneChange = { newPhone = it },
                onSendOtp = { activeSheet = AccountManagementSheet.VerifyPhone },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            AccountManagementSheet.VerifyPhone -> VerifyPhoneSheet(
                otp = otp,
                onOtpChange = { otp = it.take(4) },
                onBack = { activeSheet = AccountManagementSheet.UpdatePhone },
                onVerify = { activeSheet = AccountManagementSheet.PhoneUpdated },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            AccountManagementSheet.PhoneUpdated -> PhoneUpdatedSheet(
                onDismiss = { activeSheet = null },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            null -> Unit
        }
    }
}

@Composable
private fun SettingsDetailScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    SettingsBackground(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 48.dp)
        ) {
            BackButton(onClick = onBack)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = Color(0xFF252525),
                fontSize = 24.sp,
                fontFamily = FreedokaFontFamily,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 138.dp, bottom = 32.dp),
            content = content
        )
    }
}

@Composable
private fun SettingsBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SettingsPurple)
    ) {
        Image(
            painter = painterResource(id = R.drawable.settings_bg_object),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        content()
    }
}

@Composable
private fun SettingsGroupCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(26.dp)
    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 3.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Color.White)
                .border(1.5.dp, Color.Black, shape)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            content = content
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    iconRes: Int,
    iconBackground: Color,
    showArrow: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable(
                enabled = onClick != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick?.invoke() }
            )
    ) {
        SettingsIconBubble(iconRes = iconRes, background = iconBackground)
        Spacer(modifier = Modifier.width(18.dp))
        Text(
            text = title,
            color = Color.Black,
            fontSize = 14.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        when {
            trailing != null -> trailing()
            showArrow -> Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFB9B3C3),
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun SettingsIconBubble(
    iconRes: Int,
    background: Color,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(start = 58.dp)
            .background(Color(0xFFF0EEF4))
    )
}

@Composable
private fun SettingsToggle(
    checked: Boolean,
    modifier: Modifier = Modifier
) {
    val trackColor = if (checked) SettingsAccent else Color(0xFFE2DDEC)
    Box(
        modifier = modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(trackColor)
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
private fun NotificationRow(
    title: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
    ) {
        Text(
            text = title,
            color = Color.Black,
            fontSize = 14.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
        ) {
            SettingsToggle(checked = checked)
        }
    }
}

@Composable
private fun SafetyInfoCard(
    iconRes: Int,
    title: String,
    dotColor: Color,
    bullets: List<String>
) {
    SettingsGroupCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = Color.Black,
                fontSize = 16.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.32.sp
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        bullets.forEach { bullet ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = bullet,
                    color = Color(0xFF222222),
                    fontSize = 13.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun HelpActionCard(
    iconRes: Int,
    title: String,
    iconBackground: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Box(modifier = modifier.height(78.dp)) {
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
                .border(1.3.dp, Color.Black, shape)
                .padding(horizontal = 12.dp)
        ) {
            SettingsIconBubble(
                iconRes = iconRes,
                background = iconBackground,
                modifier = Modifier.size(38.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                color = Color(0xFF4D4D4D),
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FaqCard() {
    SettingsGroupCard {
        Text(
            text = "Frequently Asked Questions",
            color = Color.Black,
            fontSize = 15.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(18.dp))
        FaqItem(
            question = "I bought Hearts but didn't get them.",
            expandedText = "Don't worry! Sometimes network delays happen. Try restarting your app. If your Hearts still don't appear after 5 minutes, tap the Contact us button above with your receipt!",
            expanded = true
        )
        FaqItem("How do I earn free Hearts?")
        FaqItem("How do I skip a dare I don't want to do?")
        FaqItem("Can I suggest my own dares?")
        FaqItem("How do I change my vibe or interests?")
        FaqItem("How do I delete my account permanently?")
    }
}

@Composable
private fun FaqItem(
    question: String,
    expandedText: String? = null,
    expanded: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Text(
                text = question,
                color = Color.Black,
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }
        if (expanded && expandedText != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF4F4F4))
                    .padding(14.dp)
            ) {
                Text(
                    text = expandedText,
                    color = Color(0xFF333333),
                    fontSize = 12.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFF0EEF4))
        )
    }
}

@Composable
private fun UpdatePhoneSheet(
    currentPhone: String,
    newPhone: String,
    onNewPhoneChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    modifier: Modifier = Modifier
) {
    BottomSheetSurface(modifier = modifier) {
        Text(
            text = "Update phone number",
            color = Color.Black,
            fontSize = 18.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "We'll send an OTP to your new number\nfor verification.",
            color = Color(0xFF8F8F8F),
            fontSize = 13.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(34.dp))
        BottomSheetLabel("Current number")
        Spacer(modifier = Modifier.height(10.dp))
        SettingsInputField(
            value = currentPhone,
            onValueChange = {},
            placeholder = "",
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF8F8F8F),
                    modifier = Modifier.size(20.dp)
                )
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        BottomSheetLabel("New phone number")
        Spacer(modifier = Modifier.height(10.dp))
        SettingsInputField(
            value = newPhone,
            onValueChange = onNewPhoneChange,
            placeholder = "Enter new phone number",
            keyboardType = KeyboardType.Phone
        )
        Spacer(modifier = Modifier.height(36.dp))
        PurpleActionButton(
            text = "Send OTP",
            onClick = onSendOtp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun VerifyPhoneSheet(
    otp: String,
    onOtpChange: (String) -> Unit,
    onBack: () -> Unit,
    onVerify: () -> Unit,
    modifier: Modifier = Modifier
) {
    BottomSheetSurface(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            BackButton(onClick = onBack)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Verify your new number",
                color = Color.Black,
                fontSize = 18.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Enter the 4-digit code sent to your\nnew phone number.",
            color = Color(0xFF8F8F8F),
            fontSize = 13.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(34.dp))
        OtpBoxes(
            otp = otp,
            onOtpChange = onOtpChange,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(28.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Didn't get the OTP? ",
                color = Color.Black,
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Resend",
                color = Color(0xFF8F8F8F),
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline
            )
        }
        Spacer(modifier = Modifier.height(42.dp))
        PurpleActionButton(
            text = "Verify",
            onClick = onVerify,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun PhoneUpdatedSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BottomSheetSurface(modifier = modifier) {
        Spacer(modifier = Modifier.height(14.dp))
        Image(
            painter = painterResource(id = R.drawable.wallet_pan_verified),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(120.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Phone number updated!",
            color = Color.Black,
            fontSize = 18.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Your new phone number has been verified\nand saved successfully.",
            color = Color(0xFF8F8F8F),
            fontSize = 13.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(34.dp))
        PurpleActionButton(
            text = "Done",
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun BottomSheetSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp, vertical = 12.dp)
                .padding(bottom = 18.dp),
            content = content
        )
    }
}

@Composable
private fun BottomSheetLabel(text: String) {
    Text(
        text = text,
        color = Color.Black,
        fontSize = 13.sp,
        fontFamily = GaretFontFamily,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SettingsInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(shape)
            .background(Color.White)
            .border(1.3.dp, Color.Black, shape)
            .padding(horizontal = 18.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color(0xFFB5B5B5),
                    fontSize = 14.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = readOnly,
                singleLine = true,
                cursorBrush = SolidColor(Color.Black),
                textStyle = TextStyle(
                    color = Color(0xFF666666),
                    fontSize = 14.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(10.dp))
            trailingIcon()
        }
    }
}

@Composable
private fun OtpBoxes(
    otp: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val boxShape = RoundedCornerShape(14.dp)
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        repeat(4) { index ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 62.dp, height = 66.dp)
                    .clip(boxShape)
                    .background(Color.White)
                    .border(1.2.dp, Color.Black, boxShape)
            ) {
                Text(
                    text = otp.getOrNull(index)?.toString().orEmpty(),
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    BasicTextField(
        value = otp,
        onValueChange = onOtpChange,
        singleLine = true,
        cursorBrush = SolidColor(Color.Transparent),
        textStyle = TextStyle(color = Color.Transparent),
        visualTransformation = VisualTransformation.None,
        modifier = Modifier.size(1.dp)
    )
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Back",
        tint = Color.Black,
        modifier = modifier
            .size(26.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    )
}

@Composable
private fun PurpleActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .height(54.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 3.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(SettingsAccent)
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ShadowButton(
    text: String,
    background: Color,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 3.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(background)
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 15.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun SettingsScreenPreview() {
    BffAndroidTheme {
        SettingsScreen()
    }
}
