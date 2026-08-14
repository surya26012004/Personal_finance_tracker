package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AllocationSlice
import com.example.domain.model.FinancialCalculator
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.NaturalBlue
import com.example.ui.theme.NaturalPurple
import com.example.ui.theme.NaturalTeal
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmOrange

private val SliceColors = listOf(
    NaturalBlue,          // Blue
    WarmOrange,           // Warm Orange
    WarmAmber,            // Gold/Amber
    NaturalTeal,          // Teal
    NaturalPurple,        // Purple
    EmeraldProfit,        // Emerald
    Color(0xFFE11D48),    // Rose
    Color(0xFF0284C7),    // Sky
    Color(0xFF64748B)     // Slate
)

@Composable
fun AllocationDonutChart(
    slices: List<AllocationSlice>,
    currencySymbol: String = "₹",
    title: String = "Asset Allocation",
    modifier: Modifier = Modifier
) {
    var selectedSliceIndex by remember { mutableStateOf<Int?>(null) }
    val anim = remember { Animatable(0f) }

    LaunchedEffect(slices) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(700))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SlateSurface
        ),
        border = BorderStroke(1.dp, SlateBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (slices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No allocation data available",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTextMuted
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Donut Canvas
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(120.dp)) {
                            val strokeWidth = 22.dp.toPx()
                            var startAngle = -90f

                            slices.forEachIndexed { index, slice ->
                                val sweepAngle = (slice.percentage.toFloat() / 100f * 360f) * anim.value
                                val color = SliceColors[index % SliceColors.size]
                                val isSelected = selectedSliceIndex == index

                                drawArc(
                                    color = if (isSelected) color else color.copy(alpha = 0.9f),
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(
                                        width = if (isSelected) strokeWidth + 4.dp.toPx() else strokeWidth,
                                        cap = StrokeCap.Butt
                                    )
                                )
                                startAngle += sweepAngle
                            }
                        }

                        // Center text displaying top or selected slice
                        val activeSlice = selectedSliceIndex?.let { slices.getOrNull(it) } ?: slices.firstOrNull()
                        if (activeSlice != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format("%.1f%%", activeSlice.percentage),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SlateTextPrimary
                                )
                                Text(
                                    text = if (selectedSliceIndex != null) "Selected" else "Largest",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateTextMuted,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Legend List
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        slices.take(5).forEachIndexed { index, slice ->
                            val color = SliceColors[index % SliceColors.size]
                            val isSelected = selectedSliceIndex == index

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SlateSurfaceVariant else Color.Transparent)
                                    .clickable {
                                        selectedSliceIndex = if (selectedSliceIndex == index) null else index
                                    }
                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(color, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = slice.categoryName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SlateTextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = String.format("%.1f%%", slice.percentage),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SlateTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

