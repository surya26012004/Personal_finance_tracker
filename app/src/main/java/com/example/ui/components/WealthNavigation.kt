package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CosmicMidnight
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicVoid
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassGlowCyan
import com.example.ui.theme.GlassGlowIndigo
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GlassSurfaceSubtle
import com.example.ui.theme.LunarCyan
import com.example.ui.theme.LunarCyanGlow
import com.example.ui.theme.LunarGold
import com.example.ui.theme.LunarIndigo
import com.example.ui.theme.MoonMuted
import com.example.ui.theme.MoonSilver
import com.example.ui.theme.MoonStarlight
import com.example.ui.viewmodel.WealthTab

fun getTabIcon(tab: WealthTab): ImageVector {
    return when (tab) {
        WealthTab.DASHBOARD -> Icons.Default.Dashboard
        WealthTab.PORTFOLIO -> Icons.Default.ShowChart
        WealthTab.DAILY_UPDATE -> Icons.Default.Update
        WealthTab.TRANSACTIONS -> Icons.Default.ReceiptLong
        WealthTab.CASHFLOW -> Icons.Default.Savings
        WealthTab.GOALS -> Icons.Default.Flag
        WealthTab.ANALYTICS -> Icons.Default.PieChart
        WealthTab.LOANS -> Icons.Default.CreditCard
        WealthTab.WATCHLIST -> Icons.Default.Bookmark
        WealthTab.JOURNAL -> Icons.Default.MenuBook
        WealthTab.SETTINGS -> Icons.Default.Settings
    }
}

@Composable
fun WealthTopScrollableNav(
    currentTab: WealthTab,
    onTabSelected: (WealthTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WealthTab.values().forEach { tab ->
            val isSelected = tab == currentTab
            val isDailyUpdate = tab == WealthTab.DAILY_UPDATE

            val bgBrush = if (isSelected) {
                Brush.horizontalGradient(
                    colors = listOf(
                        LunarIndigo.copy(alpha = 0.35f),
                        LunarCyan.copy(alpha = 0.25f)
                    )
                )
            } else if (isDailyUpdate) {
                Brush.horizontalGradient(
                    colors = listOf(
                        LunarGold.copy(alpha = 0.20f),
                        Color(0x10FFFFFF)
                    )
                )
            } else {
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x18FFFFFF),
                        Color(0x0CFFFFFF)
                    )
                )
            }

            val borderColor = if (isSelected) {
                LunarCyan.copy(alpha = 0.7f)
            } else if (isDailyUpdate) {
                LunarGold.copy(alpha = 0.5f)
            } else {
                GlassBorderSubtle
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgBrush)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = LunarCyan)
                    ) { onTabSelected(tab) }
                    .padding(1.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = borderColor,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                        )
                    }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = getTabIcon(tab),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = if (isSelected) {
                            LunarCyan
                        } else if (isDailyUpdate) {
                            LunarGold
                        } else {
                            MoonSilver
                        }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tab.shortTitle,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MoonStarlight else MoonSilver
                    )
                }
            }
        }
    }
}

@Composable
fun WealthBottomNavBar(
    currentTab: WealthTab,
    onTabSelected: (WealthTab) -> Unit,
    modifier: Modifier = Modifier
) {
    // Primary bottom anchors
    val mainTabs = listOf(
        WealthTab.DASHBOARD,
        WealthTab.PORTFOLIO,
        WealthTab.DAILY_UPDATE,
        WealthTab.TRANSACTIONS,
        WealthTab.ANALYTICS
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Liquid Glass Capsule
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp)),
            color = Color(0x33101835), // Frosted liquid glass backdrop
            shadowElevation = 16.dp,
            border = BorderStroke(
                1.dp,
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0x66FFFFFF), // Top specular highlight
                        Color(0x1AFFFFFF),
                        Color(0x1038BDF8)  // Moon cyan floor reflection
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                mainTabs.forEach { tab ->
                    val isSelected = currentTab == tab
                    val isDaily = tab == WealthTab.DAILY_UPDATE

                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) LunarCyan else MoonMuted,
                        label = "IconTint"
                    )

                    val textTint by animateColorAsState(
                        targetValue = if (isSelected) MoonStarlight else MoonMuted,
                        label = "TextTint"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                if (isSelected) {
                                    Brush.radialGradient(
                                        colors = listOf(
                                            LunarIndigo.copy(alpha = 0.35f),
                                            LunarCyanGlow,
                                            Color.Transparent
                                        )
                                    )
                                } else {
                                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = LunarCyan)
                            ) { onTabSelected(tab) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (isDaily) {
                                BadgedBox(badge = {
                                    Badge(
                                        containerColor = LunarGold,
                                        modifier = Modifier.size(6.dp)
                                    )
                                }) {
                                    Icon(
                                        imageVector = getTabIcon(tab),
                                        contentDescription = tab.title,
                                        tint = iconTint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = getTabIcon(tab),
                                    contentDescription = tab.title,
                                    tint = iconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = tab.shortTitle,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textTint
                            )
                        }
                    }
                }
            }
        }
    }
}


