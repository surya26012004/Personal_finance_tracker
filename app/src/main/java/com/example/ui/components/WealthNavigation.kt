package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmOrange
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

            FilterChip(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) IndigoAccent.copy(alpha = 0.5f) else SlateBorder
                ),
                leadingIcon = {
                    Icon(
                        imageVector = getTabIcon(tab),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSelected) {
                            IndigoPrimary
                        } else if (isDailyUpdate) {
                            WarmAmber
                        } else {
                            SlateTextSecondary
                        }
                    )
                },
                label = {
                    Text(
                        text = tab.shortTitle,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IndigoContainer,
                    selectedLabelColor = IndigoPrimary,
                    containerColor = if (isDailyUpdate) WarmAmber.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                    labelColor = SlateTextSecondary
                ),
                modifier = Modifier.height(34.dp)
            )
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

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, SlateBorderSubtle)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            mainTabs.forEach { tab ->
                val isSelected = currentTab == tab
                val isDaily = tab == WealthTab.DAILY_UPDATE

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        if (isDaily) {
                            BadgedBox(badge = {
                                Badge(
                                    containerColor = WarmAmber,
                                    modifier = Modifier.size(6.dp)
                                )
                            }) {
                                Icon(
                                    imageVector = getTabIcon(tab),
                                    contentDescription = tab.title
                                )
                            }
                        } else {
                            Icon(
                                imageVector = getTabIcon(tab),
                                contentDescription = tab.title
                            )
                        }
                    },
                    label = {
                        Text(
                            text = tab.shortTitle,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IndigoPrimary,
                        selectedTextColor = IndigoPrimary,
                        unselectedIconColor = SlateTextMuted,
                        unselectedTextColor = SlateTextMuted,
                        indicatorColor = IndigoContainer
                    )
                )
            }
        }
    }
}

