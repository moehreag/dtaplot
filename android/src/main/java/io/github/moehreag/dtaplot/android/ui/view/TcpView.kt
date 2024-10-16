package io.github.moehreag.dtaplot.android.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.moehreag.dtaplot.Translations
import io.github.moehreag.dtaplot.android.ActivityViewModel
import io.github.moehreag.dtaplot.android.ui.DiscoveryDialog
import io.github.moehreag.dtaplot.socket.TcpSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TcpView: TableView() {

    @Composable
    override fun Draw(viewModel: ActivityViewModel) {
        val scope = rememberCoroutineScope()
        var currentAction = remember { Action.NONE }

        Scaffold (
            topBar = TopBar.create(Translations.translate("view.tcp")),
            bottomBar = {
                BottomAppBar {
                    Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        TextButton(onClick = {
                            currentAction = Action.READ
                            DiscoveryDialog.open(viewModel)
                        }) {
                            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector =Icons.Rounded.Refresh, contentDescription ="")
                                Text(text = "Load", )
                            }
                        }
                        if (hasContent()) {
                            TextButton(onClick = {
                                currentAction = Action.WRITE_CONFIRM
                            }) {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(imageVector = Icons.Rounded.Upload, contentDescription = "")
                                    Text(text = "Write")
                                }
                            }
                        }
                    }
                }
            }
        ) {padding ->
            if (currentAction == Action.WRITE_CONFIRM){
                AlertDialog(onDismissRequest = {
                    currentAction = Action.NONE
                }, confirmButton = {
                    TextButton(onClick = {
                        currentAction = Action.WRITE
                    }) {
                        Text(text = Translations.translate("action.confirm"))
                    }
                })
            }

            DiscoveryDialog.Draw(viewModel = viewModel, onConfirm = {
                when (currentAction) {
                    Action.READ -> {
                        currentAction = Action.NONE
                        scope.launch(Dispatchers.IO) {
                            load(TcpSocket.readAll(it))
                        }
                    }
                    Action.WRITE -> {
                        currentAction = Action.NONE
                        scope.launch(Dispatchers.IO) {
                            TcpSocket.write(it)
                        }
                    }
                    else -> {}
                }
            })
            super.DrawTable(padding)
        }
    }

    enum class Action {
        READ, WRITE_CONFIRM, WRITE, NONE
    }
}