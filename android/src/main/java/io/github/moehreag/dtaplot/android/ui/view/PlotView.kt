package io.github.moehreag.dtaplot.android.ui.view

import android.graphics.Typeface
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.*
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.data.rememberExtraLambda
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.LegendItem
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import io.github.moehreag.dtaplot.Translations
import io.github.moehreag.dtaplot.android.ActivityViewModel
import io.github.moehreag.dtaplot.android.ui.DiscoveryDialog

class PlotView : View {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Draw(viewModel: ActivityViewModel) {
        val options: MutableList<String> = viewModel.setNames

        var editSheetOpen by remember { mutableStateOf(false) }

        Scaffold(
            topBar = TopBar.create(Translations.translate("view.plot")),
            bottomBar = {
                BottomAppBar {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        TextButton(onClick = {
                            viewModel.openFile()
                        }) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Outlined.FileOpen, contentDescription = "Open File")
                                Text(text = "Open File")
                            }
                        }
                        TextButton(onClick = {
                            DiscoveryDialog.open(viewModel)
                        }) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Outlined.Download, contentDescription = "Download")
                                Text(text = "Download")
                            }
                        }
                        if (options.isNotEmpty()) {
                            TextButton(onClick = {editSheetOpen = true}) {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Outlined.Edit, "Edit Graph")
                                    Text("Edit Graph")
                                }
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Graph(
                viewModel,
                Modifier
                    .padding(padding)
                    .height(800.dp),
                options
            )

            if (editSheetOpen) {
                ModalBottomSheet(onDismissRequest = {
                    editSheetOpen = false
                }) {
                    LazyColumn(Modifier.padding(4.dp).fillMaxWidth()) {
                        items(options) {
                            Card(
                                colors = CardDefaults.cardColors(),
                                elevation = CardDefaults.elevatedCardElevation(),
                                modifier = Modifier.padding(12.dp).fillMaxWidth()
                            ) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                    Text(text = it)
                                    Switch(checked = viewModel.isSetDisplayed(it), onCheckedChange = { checked ->
                                        if (checked) {
                                            viewModel.addSet(it)
                                        } else {
                                            viewModel.removeSet(it)
                                        }
                                    }, modifier = Modifier.scale(0.8f))
                                }
                            }
                        }
                    }
                }
            }

            DiscoveryDialog.Draw(viewModel) {
                viewModel.load(it)
            }

        }
    }

    val chartColors = listOf(
        0xff0000,  // red
        0x0000ff,  // blue
        0x00aaaa,  // cyan-ish
        0xffa500,  // orange
        0x53868b,  // cadetblue4
        0xff7f50,  // coral
        0x45ab1f,  // dark green-ish
        0x90422d,  // sienna-ish
        0xa0a0a0,  // grey-ish
        0x14ff14,  // green-ish
    ).map { it.rgb() }

    private fun Int.rgb(): Color {
        return Color(0xff000000 or this.toLong())
    }

    @Composable
    fun Graph(model: ActivityViewModel, modifier: Modifier, options: MutableList<String>) {
        val textColor = MaterialTheme.colorScheme.primary.toArgb()
        val chartColors = MaterialTheme.colorScheme.primary.run {
            chartColors.map { it.compositeOver(this) }
        }
        ProvideVicoTheme(rememberM3VicoTheme(), content = {
            val chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    LineCartesianLayer.LineProvider.series(
                        chartColors
                            .map {
                                LineCartesianLayer.rememberLine(fill = remember {
                                    LineCartesianLayer.LineFill.single(
                                        fill(it)
                                    )
                                })
                            }
                    )
                ),
                startAxis = VerticalAxis.rememberStart(
                    label = rememberAxisLabelComponent(padding = Dimensions(2f)),
                    line = rememberAxisLineComponent(margins = Dimensions(2f)),
                    tick = rememberAxisTickComponent(),
                    guideline = rememberAxisGuidelineComponent(),
                    title = "°C"
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = rememberAxisLabelComponent(margins = Dimensions(2f)),
                    line = rememberAxisLineComponent(margins = Dimensions(2f)),
                    tick = rememberAxisTickComponent(),
                    guideline = null,
                    itemPlacer = remember {
                        HorizontalAxis.ItemPlacer.aligned(
                            spacing = 3,
                            addExtremeLabelPadding = true
                        )
                    },
                    valueFormatter = { _, x, _ ->
                        model.formatValue(x.toLong())
                    }
                ),
                legend = rememberHorizontalLegend(
                    items = rememberExtraLambda {
                        model.legendNames().mapIndexed { i, s ->
                            LegendItem(
                                label = s,
                                labelComponent = TextComponent(
                                    textSizeSp = 12f,
                                    typeface = Typeface.MONOSPACE,
                                    color = textColor
                                ), icon = ShapeComponent(
                                    shape = CorneredShape.Pill,
                                    color = chartColors[i % chartColors.size.coerceAtLeast(1)].toArgb()
                                )
                            )
                        }.forEach { add(it) }
                    },
                    iconSize = 12.dp,
                    iconPadding = 4.dp,
                    padding = Dimensions(10f),
                    spacing = 10.dp,
                    lineSpacing = 12.dp
                )
            )
            val scrollState = rememberVicoScrollState()
            val zoomState = rememberVicoZoomState()
            CartesianChartHost(
                chart = chart,
                scrollState = scrollState,
                zoomState = zoomState,
                modelProducer = model.modelProducer,
                modifier = modifier,
                runInitialAnimation = false,
                placeholder = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (options.isEmpty()) {
                            Text(text = "No data loaded", style = MaterialTheme.typography.headlineMedium)
                            Text(
                                text = "Use the buttons to load a file",
                                style = MaterialTheme.typography.headlineSmall,
                                fontStyle = FontStyle.Italic
                            )
                        } else {
                            Text("No data selected", style = MaterialTheme.typography.headlineMedium)
                            /*Text("Use the \"Edit Graph\" button to select data",
                                style = MaterialTheme.typography.headlineSmall,
                                fontStyle = FontStyle.Italic
                            )*/
                        }
                    }
                }
            )
        })
    }
}