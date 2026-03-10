package com.avilesrodriguez.presentation.graphs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.presentation.R
import io.github.koalaplot.core.pie.DefaultSlice
import io.github.koalaplot.core.pie.PieChart
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun DonutGraph(
    values: List<Float>,
    labels: List<String>,
    colors: List<Color>,
    modifier: Modifier = Modifier
){
    val total = values.sum()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(), contentAlignment = Alignment.Center) {
            PieChart(
                values = values,
                label = { index ->
                    Text(
                        text = "${labels[index]}: ${values[index]}",
                        style = MaterialTheme.typography.labelSmall
                    )
                        },
                slice = { index ->
                    DefaultSlice(
                        color = colors[index],
                    )
                },
                holeSize = 0.5f,
                holeContent = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column{
                            Text(
                                text = stringResource(R.string.total),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "%.2f".format(total),
                                style = MaterialTheme.typography.displaySmall
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}