package com.gobff.getfriends.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.gobff.getfriends.R
import com.gobff.getfriends.utils.PresenceHeartbeat
import com.gobff.getfriends.ui.component.HandDrawnCardShape
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.FreedokaFontFamily
import com.gobff.getfriends.ui.theme.GaretFontFamily
import com.gobff.getfriends.viewmodel.LogoutViewModel

private val SettingsPurple = Color(0xFFC471FF)
private val SettingsAccent = Color(0xFF7D3CF0)
private val SettingsHeaderText = Color(0xFF2D2D2D)

private enum class SettingsPage {
    Main,
    Notifications,
    SafetyCenter,
    HelpSupport,
    SuggestFeature,
    AccountManagement,
    TermsAndConditions,
    RefundCancellations,
    PrivacyPolicy
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
    hasNotificationAccess: Boolean = true,
    onAlwaysOnlineChanged: (Boolean) -> Unit = {},
    onNotificationAccessRequested: (onAccessReady: () -> Unit) -> Unit = { onAccessReady -> onAccessReady() },
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
            onTermsAndConditions = { page = SettingsPage.TermsAndConditions },
            onRefundCancellations = { page = SettingsPage.RefundCancellations },
            onPrivacyPolicy = { page = SettingsPage.PrivacyPolicy },
            hasNotificationAccess = hasNotificationAccess,
            onAlwaysOnlineChanged = onAlwaysOnlineChanged,
            onNotificationAccessRequested = onNotificationAccessRequested,
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

        SettingsPage.TermsAndConditions -> TermsAndConditionsContent(
            onBack = { page = SettingsPage.Main },
            modifier = modifier
        )

        SettingsPage.RefundCancellations -> RefundCancellationsContent(
            onBack = { page = SettingsPage.Main },
            modifier = modifier
        )

        SettingsPage.PrivacyPolicy -> PrivacyPolicyContent(
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
    onTermsAndConditions: () -> Unit,
    onRefundCancellations: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    hasNotificationAccess: Boolean,
    onAlwaysOnlineChanged: (Boolean) -> Unit,
    onNotificationAccessRequested: (onAccessReady: () -> Unit) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var alwaysOnline by remember { mutableStateOf(PresenceHeartbeat.isAlwaysOnlineEnabled()) }
    var audioEffects by remember { mutableStateOf(true) }
    val stayOnlineEnabled = alwaysOnline && hasNotificationAccess

    LaunchedEffect(hasNotificationAccess) {
        if (!hasNotificationAccess && alwaysOnline) {
            alwaysOnline = false
            PresenceHeartbeat.setAlwaysOnlineEnabled(false)
            onAlwaysOnlineChanged(false)
        }
    }

    fun toggleAlwaysOnline() {
        val enabled = !stayOnlineEnabled
        if (enabled) {
            onNotificationAccessRequested {
                alwaysOnline = true
                PresenceHeartbeat.setAlwaysOnlineEnabled(true)
                onAlwaysOnlineChanged(true)
            }
        } else {
            alwaysOnline = false
            PresenceHeartbeat.setAlwaysOnlineEnabled(false)
            onAlwaysOnlineChanged(false)
        }
    }

    SettingsBackground(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsWhiteHeader(
                title = "SETTINGS",
                subtitle = "Manage your account",
                onBack = onBack
            )

            SettingsPurpleCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 570.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 34.dp)
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
                            showArrow = false,
                            onClick = { audioEffects = !audioEffects },
                            trailing = { SettingsToggle(checked = audioEffects) }
                        )
                        SettingsDivider()
                        SettingsRow(
                            title = "Stay Online for Calls",
                            iconRes = R.drawable.setting_notification,
                            iconBackground = Color(0xFFF1EDFC),
                            showArrow = false,
                            onClick = ::toggleAlwaysOnline,
                            trailing = {
                                Box(
                                    modifier = Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = ::toggleAlwaysOnline
                                    )
                                ) {
                                    SettingsToggle(checked = stayOnlineEnabled)
                                }
                            }
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
                            title = "Terms & Conditions",
                            iconRes = R.drawable.setting_terms,
                            iconBackground = Color(0xFFF3F2F7),
                            onClick = onTermsAndConditions
                        )
                        SettingsDivider()
                        SettingsRow(
                            title = "Refund & Cancellations",
                            iconRes = R.drawable.setting_terms,
                            iconBackground = Color(0xFFF3F2F7),
                            onClick = onRefundCancellations
                        )
                        SettingsDivider()
                        SettingsRow(
                            title = "Privacy Policy",
                            iconRes = R.drawable.setting_privacy,
                            iconBackground = Color(0xFFF3F2F7),
                            onClick = onPrivacyPolicy
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
            iconRes = R.drawable.setting_help_support,
            title = "Child Safety",
            dotColor = Color(0xFFFF6B6B),
            bullets = listOf(
                "BFF does not allow child sexual abuse or exploitation, grooming, sextortion, trafficking, or sexual content involving minors.",
                "Use Report in the app for safety concerns, abusive behavior, or suspicious users.",
                "For child safety or CSAM concerns, contact team@gocouplemeet.com."
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
                        .clip(HandDrawnCardShape)
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
private fun TermsAndConditionsContent(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsDetailScaffold(
        title = "Terms & Conditions",
        onBack = onBack,
        modifier = modifier
    ) {
        LegalInfoCard(
            title = "Using BFF",
            body = "BFF is a social entertainment app for friendly audio chats, games, gifts, and casual conversations. By using the app, you agree to keep your interactions respectful, lawful, and safe for other users."
        )
        Spacer(modifier = Modifier.height(18.dp))
        LegalInfoCard(
            title = "Account responsibility",
            body = "You are responsible for the activity on your account and for keeping your phone number, login access, and profile information secure. Do not share your account with another person or use someone else's account."
        )
        Spacer(modifier = Modifier.height(18.dp))
        LegalInfoCard(
            title = "Community conduct",
            body = "Do not harass, abuse, threaten, impersonate, scam, or share illegal, explicit, hateful, or unsafe content. BFF may restrict features, suspend access, or remove accounts that break our safety rules."
        )
        Spacer(modifier = Modifier.height(18.dp))
        LegalInfoCard(
            title = "Paid features",
            body = "Hearts, gifts, recharges, and other paid features are provided for use inside the app. Prices, availability, and benefits may change from time to time. Misuse, fraud, or payment disputes may lead to account restrictions."
        )
        Spacer(modifier = Modifier.height(18.dp))
        LegalInfoCard(
            title = "Service changes",
            body = "We may update, pause, or remove app features to improve safety, performance, or user experience. Continued use of BFF after changes means you accept the updated terms."
        )
    }
}

@Composable
private fun RefundCancellationsContent(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsDetailScaffold(
        title = "Refund & Cancellations",
        onBack = onBack,
        modifier = modifier
    ) {
        LegalInfoCard(
            title = "Digital purchases",
            body = "BFF purchases such as Hearts, gifts, recharges, or in-app credits are digital items. Once delivered to your account or used in a chat, game, call, or gift, they are generally non-refundable."
        )
        Spacer(modifier = Modifier.height(18.dp))
        LegalInfoCard(
            title = "Failed or duplicate payment",
            body = "If money is deducted but Hearts or credits are not added, please wait a few minutes and restart the app. For failed, pending, or duplicate payments, contact support with your payment receipt and registered phone number."
        )
        Spacer(modifier = Modifier.height(18.dp))
        LegalInfoCard(
            title = "Cancellation policy",
            body = "Instant purchases cannot be cancelled after successful delivery. If a payment is still pending or has not been delivered, our team will verify the transaction and either add the credits or help process a refund where applicable."
        )
        Spacer(modifier = Modifier.height(18.dp))
        LegalInfoCard(
            title = "Refund review",
            body = "Refund requests are reviewed case by case for technical errors, accidental duplicate charges, or payment gateway issues. Refunds are sent back through the original payment method and may take time based on the bank or payment provider."
        )
        Spacer(modifier = Modifier.height(18.dp))
        LegalInfoCard(
            title = "Abuse prevention",
            body = "Refunds may be declined for used credits, completed gifts, completed calls, promotional rewards, policy violations, or suspicious activity. BFF may limit accounts that misuse refunds or payment systems."
        )
    }
}

@Composable
private fun PrivacyPolicyContent(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsDetailScaffold(
        title = "Privacy Policy",
        onBack = onBack,
        modifier = modifier
    ) {
        LegalInfoCard(
            title = "Information we collect",
            body = "BFF may collect details you provide, such as your phone number, profile information, gender selection, interests, app preferences, support messages, and payment transaction status needed to run the service."
        )
        Spacer(modifier = Modifier.height(18.dp))
        LegalInfoCard(
            title = "App activity",
            body = "We use app activity such as calls, chats, games, gifts, reports, wallet activity, and feature usage to operate BFF, improve matching, prevent abuse, resolve issues, and keep the community safe."
        )
        Spacer(modifier = Modifier.height(18.dp))
        LegalInfoCard(
            title = "Device and notification data",
            body = "We may use device identifiers, app version, network information, crash logs, notification tokens, and permission status to support login, notifications, security, troubleshooting, and app performance."
        )
        Spacer(modifier = Modifier.height(18.dp))
        LegalInfoCard(
            title = "Payments and wallet",
            body = "Payment processing may be handled by payment partners. BFF uses payment status, order IDs, recharge details, and wallet balance information to deliver Hearts, gifts, refunds, and transaction support."
        )
        Spacer(modifier = Modifier.height(18.dp))
        LegalInfoCard(
            title = "Safety and moderation",
            body = "Reports, blocked users, suspicious activity, and safety signals may be reviewed to enforce community rules, prevent fraud, and protect users. We do not ask users to share private banking details inside chats or calls."
        )
        Spacer(modifier = Modifier.height(18.dp))
        LegalInfoCard(
            title = "Your choices",
            body = "You can update account details, control notification access from your device settings, contact support for privacy questions, and request account deletion from Account Management where available."
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsWhiteHeader(
                title = "Account Management",
                subtitle = settingsSubtitleFor("Account Management"),
                onBack = {
                    if (activeSheet != null) {
                        activeSheet = null
                    } else {
                        onBack()
                    }
                }
            )

            SettingsPurpleCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 570.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 34.dp)
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
                                    .clip(HandDrawnCardShape)
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val detailCardMinHeight = (maxHeight - 198.dp).coerceAtLeast(570.dp)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                SettingsWhiteHeader(
                    title = title,
                    subtitle = settingsSubtitleFor(title),
                    onBack = onBack
                )
                SettingsPurpleCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = detailCardMinHeight)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 34.dp),
                        content = content
                    )
                }
            }
        }
    }
}

@Composable
private fun LegalInfoCard(
    title: String,
    body: String
) {
    SettingsGroupCard {
        Text(
            text = title,
            color = Color.Black,
            fontSize = 15.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = body,
            color = Color(0xFF333333),
            fontSize = 13.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            lineHeight = 19.sp
        )
    }
}

private fun settingsSubtitleFor(title: String): String =
    when (title) {
        "Notifications" -> "Control your alerts and updates"
        "Help & Support" -> "We're here when you need us"
        "Suggest A Feature" -> "We'd love to hear your suggestions"
        "Account Management" -> "Keep your account up to date"
        "Safety Center" -> "Stay safe while having fun"
        "Terms & Conditions" -> "Rules for using BFF"
        "Refund & Cancellations" -> "Payments, refunds, and cancellations"
        "Privacy Policy" -> "How BFF handles your information"
        else -> ""
    }

@Composable
private fun SettingsWhiteHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(198.dp)
            .background(Color.White)
    ) {
        BackButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 48.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
                .padding(horizontal = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.gift_vibe_sparkle),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = SettingsHeaderText,
                    fontSize = 32.sp,
                    lineHeight = 32.sp,
                    fontFamily = FreedokaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.gift_vibe_sparkle),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    contentScale = ContentScale.Fit
                )
            }
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFF454545),
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SettingsPurpleCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(SettingsPurple)
    ) {
        Image(
            painter = painterResource(id = R.drawable.settings_bg_object),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        content()
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
            .background(Color.White)
    ) {
        content()
    }
}

@Composable
private fun SettingsGroupCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = HandDrawnCardShape
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
    val shape = HandDrawnCardShape
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
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    SettingsGroupCard {
        Text(
            text = "Frequently Asked Questions",
            color = Color.Black,
            fontSize = 15.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(18.dp))
        settingsFaqItems.forEachIndexed { index, faq ->
            FaqItem(
                question = faq.question,
                answer = faq.answer,
                expanded = expandedIndex == index,
                onClick = {
                    expandedIndex = if (expandedIndex == index) null else index
                }
            )
        }
    }
}

@Composable
private fun FaqItem(
    question: String,
    answer: String,
    expanded: Boolean,
    onClick: () -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "faqArrowRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 240))
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
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(arrowRotation)
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(durationMillis = 240)) +
                fadeIn(animationSpec = tween(durationMillis = 180)),
            exit = shrinkVertically(animationSpec = tween(durationMillis = 220)) +
                fadeOut(animationSpec = tween(durationMillis = 140))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(HandDrawnCardShape)
                    .background(Color(0xFFF4F4F4))
                    .padding(14.dp)
            ) {
                Text(
                    text = answer,
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

private data class SettingsFaq(
    val question: String,
    val answer: String
)

private val settingsFaqItems = listOf(
    SettingsFaq(
        question = "I purchased Hearts, but they haven't been added.",
        answer = "Hearts are usually added instantly. If they don't appear within a few minutes, please restart the app. If the issue persists, contact Support with your payment receipt."
    ),
    SettingsFaq(
        question = "How do I start a voice or video call?",
        answer = "Drag down the user's profile to begin a voice or video conversation."
    ),
    SettingsFaq(
        question = "Can I send messages during a call?",
        answer = "Yes. You can exchange messages while you're on a call."
    ),
    SettingsFaq(
        question = "What does \"Stay Online\" do?",
        answer = "Enabling Stay Online lets other users know you're available to receive calls."
    ),
    SettingsFaq(
        question = "What is Live Hangout?",
        answer = "Live Hangout allows you to join live sessions, interact with others, and participate in community conversations."
    ),
    SettingsFaq(
        question = "How do I block or report a user?",
        answer = "You can block or report a user from their profile or by using the safety options available during a call."
    ),
    SettingsFaq(
        question = "How do I update my profile?",
        answer = "Go to Profile > Edit Profile to update your photo and personal information."
    ),
    SettingsFaq(
        question = "How do I recharge Hearts?",
        answer = "Open the recharge section to purchase Hearts using one of the available payment methods."
    )
)

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
    val shape = HandDrawnCardShape
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
    val boxShape = HandDrawnCardShape
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
    val shape = HandDrawnCardShape
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
    val shape = HandDrawnCardShape
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
