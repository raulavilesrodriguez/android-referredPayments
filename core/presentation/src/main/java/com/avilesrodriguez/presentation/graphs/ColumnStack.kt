package com.avilesrodriguez.presentation.graphs

import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.referral.ReferralMetrics
import com.avilesrodriguez.presentation.R
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.cartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.stacked
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberToggleOnTap
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.LegendItem
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import java.text.DecimalFormat

@Composable
fun ColumnStack(
    metrics: ReferralMetrics,
    modifier: Modifier = Modifier
){
    val y =
        mapOf(
            stringResource(R.string.pending) to metrics.pendingReferrals,
            stringResource(R.string.processing) to metrics.processingReferrals,
            stringResource(R.string.rejected) to metrics.rejectedReferrals,
            stringResource(R.string.paid) to metrics.paidReferrals
        )
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(metrics) {
        modelProducer.runTransaction {
            columnSeries {
                y.values.forEach { series(it) }
            }
            extras { it[LegendLabelKey] = y.keys }
        }
    }
    ComposeColumnStack(modelProducer, modifier)


}

private val LegendLabelKey = ExtraStore.Key<Set<String>>()
private val CountFormat = DecimalFormat("#")
private val StartAxisValueFormatter = CartesianValueFormatter.decimal(CountFormat)
private val MarkerValueFormatter = DefaultCartesianMarker.ValueFormatter.default(CountFormat)

@Composable
private fun ComposeColumnStack(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
) {
    val columnColors = listOf(
        Color(0xFFF5AD18),
        Color(0xFF6594B1),
        Color(0XFFDC0E0E),
        Color(0xFF08CB00)
    )
    val legendItemLabelComponent = rememberTextComponent(vicoTheme.textColor)

    val marker = rememberDefaultCartesianMarker(
        label = rememberTextComponent(
            color = MaterialTheme.colorScheme.onSurface,
            background = shapeComponent(fill(MaterialTheme.colorScheme.surface), CorneredShape.Pill),
            padding = insets(8.dp, 4.dp),
            margins = insets(bottom = 4.dp)
        ),
        valueFormatter = MarkerValueFormatter
    )
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider =
                        ColumnCartesianLayer.ColumnProvider.series(
                            columnColors.map { color ->
                                rememberLineComponent(fill = fill(color), thickness = 32.dp)
                            }
                        ),
                    columnCollectionSpacing = 32.dp,
                    mergeMode = { ColumnCartesianLayer.MergeMode.stacked() },
                ),
                startAxis =
                    VerticalAxis.rememberStart(
                        valueFormatter = StartAxisValueFormatter,
                        itemPlacer = VerticalAxis.ItemPlacer.step({1.0}),
                    ),
                layerPadding = { cartesianLayerPadding(scalableStart = 16.dp, scalableEnd = 16.dp) },
                legend =
                    rememberHorizontalLegend(
                        items = { extraStore ->
                            extraStore[LegendLabelKey].forEachIndexed { index, label ->
                                add(
                                    LegendItem(
                                        shapeComponent(
                                            fill(columnColors[index]),
                                            CorneredShape.Pill
                                        ),
                                        legendItemLabelComponent,
                                        label,
                                    )
                                )
                            }
                        },
                        padding = insets(top = 16.dp),
                    ),
                marker = marker,
                markerController = CartesianMarkerController.rememberToggleOnTap(),
            ),
        modelProducer = modelProducer,
        modifier = modifier.height(252.dp),
        zoomState = rememberVicoZoomState(zoomEnabled = false),
    )
}

