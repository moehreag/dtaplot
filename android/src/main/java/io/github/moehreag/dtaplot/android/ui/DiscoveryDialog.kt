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
import java.net.InetAddress
import java.net.InetSocketAddress

object DiscoveryDialog {

    private val dialogOpen = mutableStateOf(false)
    private val options: MutableList<InetSocketAddress?> = mutableStateListOf()
    private var remembered: InetSocketAddress? = null

    fun open(viewModel: ActivityViewModel) {
        Log.i("DtaPlot/DiscoveryDialog", "opening dialog")
        dialogOpen.value = true
        options.clear()
        if (remembered == null) {
            viewModel.discover(options)
        } else {
            Log.i("DtaPlot/DiscoveryDialog", "aborting as a value has been remembered: $remembered")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    @Composable
    fun Draw(viewModel: ActivityViewModel, onConfirm: (InetSocketAddress) -> Unit) {
        if (dialogOpen.value) {
            if (remembered != null) {
                dialogOpen.value = false
                onConfirm.invoke(remembered!!)
            }
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
                    Column(
                        modifier = Modifier
                            .padding(all = 24.dp)
                            .fillMaxWidth()
                    ) {
                        Text(Translations.translate("dialog.message"))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                options.clear()
                                viewModel.discover(options)
                            }) {
                                Icon(Icons.Rounded.Refresh, "Refresh")
                            }
                            if (options.isEmpty()) {
                                Text(
                                    Translations.translate("dialog.loading"),
                                    fontStyle = FontStyle.Italic,
                                    modifier = Modifier.padding(all = 4.dp)
                                )
                            } else if (options[0] == null) {
                                var value by remember { mutableStateOf("") }
                                TextField(value = value, singleLine = true, placeholder = {
                                    Text(
                                        Translations.translate("dialog.enterip"),
                                        fontStyle = FontStyle.Italic,
                                        modifier = Modifier.padding(all = 4.dp)
                                    )
                                }, onValueChange = { s ->
                                    value = s
                                },
                                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                                )
                                runCatching {
                                    val parts = value.let {
                                        if (it.contains(":")) {
                                            it.split(":")
                                        } else listOf(it, "8889")
                                    }
                                    selection = InetSocketAddress(
                                        InetAddress.getByName(parts[0]),
                                        if (parts.size < 2) 8889 else parts[1].toInt()
                                    )
                                }
                            } else {
                                val selectedOption: MutableState<InetSocketAddress> =
                                    remember { options[0]?.let { mutableStateOf(it) }!! }
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
                                        modifier = Modifier
                                            .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                                            .wrapContentWidth()
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
                                                    selectedOption.value = selectionOption!!
                                                    dropDownOpen.value = false
                                                    selection = selectedOption.value
                                                },
                                                text = {
                                                    Text(text = selectionOption!!.hostString)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        val remember = remember { mutableStateOf(false) }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = remember.value, onCheckedChange = {
                                remember.value = it
                            })
                            Text(text = Translations.translate("action.remember"))
                        }

                        FlowRow(
                            modifier = Modifier.align(Alignment.End),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalArrangement = Arrangement.End
                        ) {

                            TextButton(onClick = {
                                dialogOpen.value = false
                            }) {
                                Text(Translations.translate("action.cancel"))
                            }
                            TextButton(onClick = {
                                Log.i("DtaPlot/DiscoveryDialog", "Selected: ${selection?.hostString}")
                                dialogOpen.value = false
                                if (selection != null) {
                                    if (remember.value) {
                                        remembered = selection!!
                                    }
                                    onConfirm.invoke(selection!!)
                                }
                            }) {
                                Text(Translations.translate("action.select"))
                            }
                        }

                    }
                }
            }
        }
    }

}