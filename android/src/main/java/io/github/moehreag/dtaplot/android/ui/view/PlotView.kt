package io.github.moehreag.dtaplot.android.ui.view

import android.util.Log
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.*
import com.patrykandpatrick.vico.compose.cartesian.axis.*
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.common.Dimensions
import io.github.moehreag.dtaplot.android.ActivityViewModel
import io.github.moehreag.dtaplot.android.ui.DiscoveryDialog

class PlotView : View {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Draw(viewModel: ActivityViewModel) {
        Scaffold(
            bottomBar = {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            DiscoveryDialog.open(viewModel)
                        }) {
                            Icon(Icons.Filled.Edit, "Load File")
                        }
                    },
                    actions = {
                        val options = viewModel.getSetNames()
                        var selectedOptionText = options[0]
                        IconButton(onClick = {

                        }) {
                            Icon(Icons.Rounded.Add, "Add to graph")
                        }

                        val expanded = remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded.value,
                            onExpandedChange = {
                                Log.i("DtaPlot/dbg", "dropdown: $expanded")
                                expanded.value = it
                            }
                        ) {
                            TextField(
                                readOnly = true,
                                value = selectedOptionText,
                                onValueChange = { },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = expanded.value
                                    )
                                },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded.value,
                                onDismissRequest = {
                                    expanded.value = false
                                }
                            ) {
                                options.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        onClick = {
                                            selectedOptionText = selectionOption
                                            expanded.value = false
                                        }, text = {
                                            Text(text = selectionOption)
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }) { padding ->

            Graph(
                viewModel,
                Modifier
                    .padding(padding)
                    .height(800.dp)
            )

            DiscoveryDialog.Draw(viewModel) {
                viewModel.load(it)
            }
        }
    }

    @Composable
    fun Graph(model: ActivityViewModel, modifier: Modifier) {
        ProvideVicoTheme(rememberM3VicoTheme(), content = {
            val chart = rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = rememberStartAxis(
                    rememberAxisLabelComponent(padding = Dimensions(2f)),
                    rememberAxisLineComponent(margins = Dimensions(2f)),
                    rememberAxisTickComponent(),
                    guideline = rememberAxisGuidelineComponent(),
                    title = "°C"
                ),
                bottomAxis = rememberBottomAxis(
                    rememberAxisLabelComponent(margins = Dimensions(2f)),
                    rememberAxisLineComponent(margins = Dimensions(2f)),
                    rememberAxisTickComponent(),
                    guideline = null,
                    itemPlacer = remember {
                        AxisItemPlacer.Horizontal.default(
                            spacing = 3,
                            addExtremeLabelPadding = true
                        )
                    },
                    valueFormatter = { x, _, _ ->
                        model.formatValue(x.toLong())
                    }
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
                horizontalLayout = HorizontalLayout.fullWidth()
            )
        })
    }
}