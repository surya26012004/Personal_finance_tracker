package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DailyPortfolioSnapshotEntity
import com.example.domain.model.FinancialCalculator
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.EmeraldProfitLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseLoss
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary

@Composable
fun PortfolioLineChart(
    snapshots: List<DailyPortfolioSnapshotEntity>,
    currencySymbol: String = "₹",
    selectedRange: String = "1M",
    onRangeSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(snapshots, selectedRange) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, tween(durationMillis = 600))
        selectedIndex = null
    }

    // Filter snapshots based on selected range
    val filteredSnapshots = remember(snapshots, selectedRange) {
        if (snapshots.isEmpty()) return@remember emptyList()
        val sorted = snapshots.sortedBy { it.dateString }
        when (selectedRange) {
            "1W" -> sorted.takeLast(7)
            "1M" -> sorted.takeLast(30)
            "3M" -> sorted.takeLast(90)
            "6M" -> sorted.takeLast(180)
            "1Y" -> sorted.takeLast(365)
            else -> sorted
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Range selector chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val ranges = listOf("1W", "1M", "3M", "6M", "1Y", "ALL")
            ranges.forEach { range ->
                val isSelected = range == selectedRange
                FilterChip(
                    selected = isSelected,
                    onClick = { onRangeSelected(range) },
                    label = {
                        Text(
                            text = range,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IndigoPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        // Active inspection header / tooltip
        val activeSnapshot = if (selectedIndex != null && selectedIndex!! in filteredSnapshots.indices) {
            filteredSnapshots[selectedIndex!!]
        } else {
            filteredSnapshots.lastOrNull()
        }

        if (activeSnapshot != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PORTFOLIO VALUE (${activeSnapshot.dateString})",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateTextMuted
                    )
                    Text(
                        text = FinancialCalculator.formatCurrency(activeSnapshot.totalCurrentValue, currencySymbol),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextPrimary
                    )
                }

                val plColor = if (activeSnapshot.totalProfitLoss >= 0) EmeraldProfit else RoseLoss
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TOTAL RETURN",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateTextMuted
                    )
                    Text(
                        text = "${FinancialCalculator.formatCurrency(activeSnapshot.totalProfitLoss, currencySymbol)} (${FinancialCalculator.formatPercent(activeSnapshot.totalReturnPercent)})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = plColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Canvas Interactive Graph
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 8.dp)
        ) {
            if (filteredSnapshots.size < 2) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add daily records to plot historical performance trend",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTextMuted
                    )
                }
            } else {
                val minVal = filteredSnapshots.minOf { it.totalCurrentValue }
                val maxVal = filteredSnapshots.maxOf { it.totalCurrentValue }
                val valRange = if (maxVal > minVal) maxVal - minVal else 1.0

                val isPositiveTrend = (filteredSnapshots.last().totalCurrentValue >= filteredSnapshots.first().totalCurrentValue)
                val lineColor = if (isPositiveTrend) EmeraldProfitLight else RoseLoss

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(filteredSnapshots) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val count = filteredSnapshots.size
                                    if (count > 1) {
                                        val step = size.width / (count - 1)
                                        val idx = (offset.x / step).toInt().coerceIn(0, count - 1)
                                        selectedIndex = idx
                                    }
                                }
                            )
                        }
                        .pointerInput(filteredSnapshots) {
                            detectDragGestures(
                                onDrag = { change, _ ->
                                    val count = filteredSnapshots.size
                                    if (count > 1) {
                                        val step = size.width / (count - 1)
                                        val idx = (change.position.x / step).toInt().coerceIn(0, count - 1)
                                        selectedIndex = idx
                                    }
                                }
                            )
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val paddingBottom = 16f
                    val paddingTop = 16f
                    val chartHeight = height - paddingTop - paddingBottom
                    val count = filteredSnapshots.size
                    val stepX = width / (count - 1)

                    val points = filteredSnapshots.mapIndexed { index, item ->
                        val x = index * stepX
                        val normalizedY = ((item.totalCurrentValue - minVal) / valRange).toFloat()
                        val y = height - paddingBottom - (normalizedY * chartHeight * animationProgress.value)
                        Offset(x, y)
                    }

                    // Draw grid lines
                    val gridColor = SlateBorder.copy(alpha = 0.5f)
                    val gridLines = 3
                    for (i in 0..gridLines) {
                        val gridY = paddingTop + (chartHeight * (i.toFloat() / gridLines))
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, gridY),
                            end = Offset(width, gridY),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    }

                    // Build smooth cubic bezier curve
                    val path = Path()
                    val fillPath = Path()
                    if (points.isNotEmpty()) {
                        path.moveTo(points.first().x, points.first().y)
                        fillPath.moveTo(points.first().x, height)
                        fillPath.lineTo(points.first().x, points.first().y)

                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val cx = (p0.x + p1.x) / 2
                            path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            fillPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                        }

                        fillPath.lineTo(points.last().x, height)
                        fillPath.close()

                        // Gradient fill under the curve
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    lineColor.copy(alpha = 0.25f),
                                    lineColor.copy(alpha = 0.0f)
                                )
                            )
                        )

                        // Stroke line
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(
                                width = 2.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                    }

                    // Draw scrub indicator if user touches
                    selectedIndex?.let { idx ->
                        if (idx in points.indices) {
                            val activePt = points[idx]
                            // Vertical dotted guide
                            drawLine(
                                color = SlateTextMuted.copy(alpha = 0.6f),
                                start = Offset(activePt.x, 0f),
                                end = Offset(activePt.x, height),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                            )

                            // Outer pulse circle
                            drawCircle(
                                color = lineColor.copy(alpha = 0.25f),
                                radius = 10.dp.toPx(),
                                center = activePt
                            )
                            // Inner filled node
                            drawCircle(
                                color = lineColor,
                                radius = 5.dp.toPx(),
                                center = activePt
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.5.dp.toPx(),
                                center = activePt
                            )
                        }
                    }
                }
            }
        }
    }
}
