package io.github.moehreag.dtaplot.android.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.moehreag.dtaplot.Translations
import io.github.moehreag.dtaplot.android.ActivityViewModel
import io.github.moehreag.dtaplot.android.ui.DiscoveryDialog
import io.github.moehreag.dtaplot.android.socket.WebSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class WsView : TableView() {
    @Composable
    override fun Draw(viewModel: ActivityViewModel) {
        val scope = rememberCoroutineScope()

        Scaffold(
            topBar = TopBar.create(Translations.translate("view.ws")),
            bottomBar = {
                BottomAppBar {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        TextButton(onClick = {
                            if (WebSocket.isConnected()) {
                                scope.launch(Dispatchers.IO) {
                                    WebSocket.disconnect()
                                }
                            } else {
                                DiscoveryDialog.open(viewModel)
                            }
                        }) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (WebSocket.isConnected()) {
                                    Icon(imageVector = Icons.Rounded.LinkOff, contentDescription = "")
                                    Text(text = "Disconnect")
                                } else {
                                    Icon(imageVector = Icons.Outlined.Download, contentDescription = "")
                                    Text(text = "Connect")
                                }
                            }
                        }
                    }
                }
            }
        ) { padding ->
            super.DrawTable(padding)

            DiscoveryDialog.Draw(viewModel = viewModel) {
                scope.launch(Dispatchers.IO) {
                    WebSocket.read(it, {
                        Optional.of("")
                    }, {load(it)})
                }
            }
        }
    }
}