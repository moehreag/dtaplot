package io.github.moehreag.dtaplot.android.ui.view

import android.graphics.Typeface
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.*
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.LegendItem
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.Shape
import io.github.moehreag.dtaplot.Translations
import io.github.moehreag.dtaplot.android.ActivityViewModel
import io.github.moehreag.dtaplot.android.ui.DiscoveryDialog

class PlotView : View {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Draw(viewModel: ActivityViewModel) {
        val options: MutableList<String> = remember { mutableStateListOf() }

        var editSheetOpen by remember { mutableStateOf(false) }

        Scaffold(
            topBar = TopBar.create(Translations.translate("view.plot")) {
                IconButton(onClick = {
                    viewModel.openFile()
                }) {
                    Icon(imageVector = Icons.Outlined.FileOpen, contentDescription = "Open")
                }
                IconButton(onClick = { DiscoveryDialog.open(viewModel) }) {
                    Icon(imageVector = Icons.Outlined.Download, contentDescription = "Download")
                }
            },
            floatingActionButton = {
                if (options.isNotEmpty()) {
                    FloatingActionButton(onClick = {
                        editSheetOpen = true
                    }) {
                        Icon(Icons.Filled.Edit, "Edit Graph")
                    }
                }
            }
        ) { padding ->

            Graph(
                viewModel,
                Modifier
                    .padding(padding)
                    .height(800.dp)
            )

            if (editSheetOpen) {
                ModalBottomSheet(onDismissRequest = {
                    editSheetOpen = false
                }) {
                    LazyColumn(Modifier.padding(4.dp)) {
                        items(options) {
                            Row {
                                Switch(checked = viewModel.isSetDisplayed(it), onCheckedChange = { checked ->
                                    if (checked) {
                                        viewModel.addSet(it)
                                    } else {
                                        viewModel.removeSet(it)
                                    }
                                })
                                Spacer(modifier = Modifier.padding(horizontal = 12.dp))
                                Text(text = it)
                            }
                        }
                    }
                }
            }

            DiscoveryDialog.Draw(viewModel) {
                viewModel.load(it, options)
            }

        }
    }

    @Composable
    fun Graph(model: ActivityViewModel, modifier: Modifier) {
        ProvideVicoTheme(rememberM3VicoTheme(), content = {
            val chart = rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = rememberStartAxis(
                    label = rememberAxisLabelComponent(padding = Dimensions(2f)),
                    line = rememberAxisLineComponent(margins = Dimensions(2f)),
                    tick = rememberAxisTickComponent(),
                    guideline = rememberAxisGuidelineComponent(),
                    title = "°C"
                ),
                bottomAxis = rememberBottomAxis(
                    label = rememberAxisLabelComponent(margins = Dimensions(2f)),
                    line = rememberAxisLineComponent(margins = Dimensions(2f)),
                    tick = rememberAxisTickComponent(),
                    guideline = null,
                    itemPlacer = remember {
                        HorizontalAxis.ItemPlacer.default(
                            spacing = 3,
                            addExtremeLabelPadding = true
                        )
                    },
                    valueFormatter = { x, _, _ ->
                        model.formatValue(x.toLong())
                    }
                ),
                legend = rememberHorizontalLegend(
                    items = model.legendNames().map {
                        LegendItem(label = it, labelComponent = TextComponent(
                            textSizeSp = 12f,
                            typeface = Typeface.MONOSPACE
                        ), icon = ShapeComponent(shape = Shape.Pill))
                    },
                    iconSize = 12.dp,
                    iconPadding = 6.dp
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
                        Text(text = "No data loaded", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = "Use the buttons to load a file",
                            style = MaterialTheme.typography.headlineSmall,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            )
        })
    }
}