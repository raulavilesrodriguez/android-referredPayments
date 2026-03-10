package com.avilesrodriguez.presentation.graphs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.presentation.R
import io.github.koalaplot.core.bar.BulletGraphs
import io.github.koalaplot.core.style.KoalaPlotTheme
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel

@Composable
private fun TwoLineLabel(line1: String, line2: String) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(end = KoalaPlotTheme.sizes.gap)
    ) {
        Text(line1, textAlign = TextAlign.End, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Text(line2, textAlign = TextAlign.End, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    }
}
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun BulletsGraphClient(
    moneyEarned: Float,
    winByReferral: Float,
    modifier: Modifier = Modifier
){
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        BulletGraphs(modifier = Modifier.padding(16.dp)) {
            val maxRangeIncome = moneyEarned * 1.5f
            bullet(FloatLinearAxisModel(0f..maxRangeIncome)){
                label { TwoLineLabel(stringResource(R.string.income), "USD $moneyEarned")}
                axis { labels { Text("${it.toInt()}", style = MaterialTheme.typography.labelSmall) } }
                featuredMeasureBar(moneyEarned)
                ranges(0f, maxRangeIncome*0.25f, maxRangeIncome*0.75f, maxRangeIncome)

            }
            val rangeWinReferral = winByReferral * 2f
            bullet(FloatLinearAxisModel(0f..rangeWinReferral)){
                label{ TwoLineLabel(stringResource(R.string.profit_by_referred), "USD $winByReferral")}
                axis { labels { Text("${it.toInt()}", style = MaterialTheme.typography.labelSmall) } }
                featuredMeasureBar(winByReferral)
                ranges(0f, rangeWinReferral*0.25f, rangeWinReferral*0.75f, rangeWinReferral)
            }
        }
    }
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun BulletsGraphProvider(
    moneyPaid: Float,
    referralConversion: Float,
    costByReferral: Float,
    modifier: Modifier = Modifier
){
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        BulletGraphs(modifier = Modifier.padding(16.dp)) {
            val maxRangePayments = moneyPaid * 1.5f
            bullet(FloatLinearAxisModel(0f..maxRangePayments)){
                label { TwoLineLabel(stringResource(R.string.money_paid), "USD $moneyPaid")}
                axis { labels { Text("${it.toInt()}", style = MaterialTheme.typography.labelSmall) } }
                featuredMeasureBar(moneyPaid)
                ranges(0f, maxRangePayments*0.25f, maxRangePayments*0.75f, maxRangePayments)
            }
            val costRange = costByReferral * 2f
            bullet(FloatLinearAxisModel(0f..costRange)){
                label{TwoLineLabel(stringResource(R.string.cost_by_referred), "USD $costByReferral")}
                axis { labels { Text("${it.toInt()}", style = MaterialTheme.typography.labelSmall) } }
                featuredMeasureBar(costByReferral)
                ranges(0f, costRange*0.25f, costRange*0.75f, costRange)
            }
            bullet(FloatLinearAxisModel(0f..1f)){
                label { TwoLineLabel(stringResource(R.string.referrals_conversion), "${(referralConversion * 100).toInt()}%")}
                axis { labels { Text("${it.toInt()}", style = MaterialTheme.typography.labelSmall) } }
                featuredMeasureBar(referralConversion)
                ranges(0f, 0.25f, 0.75f, 1f)
            }
        }
    }
}


@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun BulletsGraphPercentages(
    percentagesMetrics: List<Pair<Float, String>>,
    modifier: Modifier = Modifier
){
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        BulletGraphs(modifier = Modifier.padding(16.dp)) {
            percentagesMetrics.forEach { pair ->
                bullet(FloatLinearAxisModel(0f..1f)){
                    label { TwoLineLabel(pair.second, "${(pair.first * 100).toInt()}%")}
                    axis { labels { Text("$it", style = MaterialTheme.typography.labelSmall) } }
                    featuredMeasureBar(pair.first)
                    ranges(0f, 0.25f, 0.75f, 1f)
                }
            }
        }
    }
}