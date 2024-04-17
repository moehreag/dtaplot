package io.github.moehreag.dtaplot.android.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.moehreag.dtaplot.Translations
import io.github.moehreag.dtaplot.android.ActivityViewModel
import java.net.InetSocketAddress
import java.util.*

object DiscoveryDialog {

    private var dialogOpen = mutableStateOf(false)
    private val options: MutableList<InetSocketAddress> = mutableStateListOf()

    fun open(viewModel: ActivityViewModel) {
        Log.i("DtaPlot/DiscoveryDialog", "opening dialog")
        dialogOpen.value = true
        options.clear()
        viewModel.discover(options)
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    @Composable
    fun draw(viewModel: ActivityViewModel, onConfirm: (InetSocketAddress) -> Unit) {
        if (dialogOpen.value) {
            var address: InetSocketAddress? = remember { null }
            Dialog(onDismissRequest = {
                dialogOpen.value = false
            }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    val dropDownOpen = remember { mutableStateOf(false) }
                    var selection: InetSocketAddress? = remember { null }
                    Column(modifier = Modifier.padding(all = 24.dp)) {
                        Text(Translations.translate("dialog.message"))
                        if (options.isEmpty()) {
                            Text(
                                Translations.translate("dialog.loading"),
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(all = 4.dp)
                            )
                        } else {
                            Row {
                                IconButton(onClick = {
                                    options.clear()
                                    viewModel.discover(options)
                                }, modifier = Modifier.padding(top = 5.dp)) {
                                    Icon(Icons.Rounded.Refresh, "Refresh")
                                }
                                val selectedOption: MutableState<InetSocketAddress> =
                                    remember { mutableStateOf(options[0]) }
                                selection = selectedOption.value
                                ExposedDropdownMenuBox(
                                    modifier = Modifier.wrapContentWidth(),
                                    expanded = dropDownOpen.value,
                                    onExpandedChange = {
                                        dropDownOpen.value = it
                                    }
                                ) {
                                    TextField(
                                        readOnly = true,
                                        value = selectedOption.value.hostString,
                                        onValueChange = { },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = dropDownOpen.value
                                            )
                                        },
                                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                        modifier = Modifier.menuAnchor().wrapContentWidth()
                                    )
                                    ExposedDropdownMenu(
                                        modifier = Modifier.wrapContentWidth(),
                                        expanded = dropDownOpen.value,
                                        onDismissRequest = {
                                            dropDownOpen.value = false
                                        }
                                    ) {
                                        options.forEach { selectionOption ->
                                            DropdownMenuItem(
                                                onClick = {
                                                    selectedOption.value = selectionOption
                                                    dropDownOpen.value = false
                                                    selection = selectedOption.value
                                                },
                                                text = {
                                                    Text(text = selectionOption.hostString)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        FlowRow(
                            modifier = Modifier.align(Alignment.End),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalArrangement = Arrangement.End
                        ) {

                            TextButton(onClick = {
                                dialogOpen.value = false
                            }) {
                                Text(Translations.translate("dialog.cancel"))
                            }
                            TextButton(onClick = {
                                address = selection
                                Log.i("DtaPlot/DiscoveryDialog", "Selected: ${address?.hostString}")
                                dialogOpen.value = false
                                onConfirm.invoke(selection!!)
                            }) {
                                Text(Translations.translate("dialog.confirm"))
                            }
                        }

                    }
                }
            }
        }
    }

}