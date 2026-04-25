package com.avilesrodriguez.presentation.graphs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.presentation.R
import io.github.koalaplot.core.bar.DefaultBar
import io.github.koalaplot.core.bar.VerticalBarPlot
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.AxisContent
import io.github.koalaplot.core.xygraph.AxisStyle
import io.github.koalaplot.core.xygraph.rememberFloatLinearAxisModel


@Composable
fun ColumnVerticalGraph(
    values: List<Float>,
    labels: List<String>,
    colors: List<Color>,
    modifier: Modifier = Modifier
){
    if (values.size != labels.size) return
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(), contentAlignment = Alignment.Center) {
            val xAxisModel = remember(labels) { CategoryAxisModel(labels) }
            val maxValue = remember(values) { values.maxOrNull() ?: 0f }
            val safeMax = if (maxValue == 0f) 1f else maxValue
            val range = 0f..(safeMax *1.2f)
            val yAxisModel = rememberFloatLinearAxisModel(range, minorTickCount = 0)

            XYGraph<String, Float>(
                xAxisModel = xAxisModel,
                yAxisModel = yAxisModel,
                xAxisContent = AxisContent(
                    labels = { Text(it, style = MaterialTheme.typography.labelSmall) },
                    title = { Text(stringResource(R.string.status), style = MaterialTheme.typography.labelMedium) },
                    style = AxisStyle()
                ),
                yAxisContent = AxisContent(
                    labels = { Text(it.toInt().toString(), style = MaterialTheme.typography.labelSmall) },
                    title = { Text(stringResource(R.string.value), style = MaterialTheme.typography.labelMedium) },
                    style = AxisStyle()
                ),
            ) {
                VerticalBarPlot(
                    xData = labels,
                    yData = values,
                    bar = { index, _, _ ->
                        Box(contentAlignment = Alignment.TopCenter) {
                            DefaultBar(
                                brush = SolidColor(colors[index]),
                                modifier = Modifier
                                    .fillMaxWidth(),
                            )
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.offset(y = (-32).dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = values[index].toInt().toString(),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
