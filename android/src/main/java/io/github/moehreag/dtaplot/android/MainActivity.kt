package io.github.moehreag.dtaplot.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.StackedLineChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.moehreag.dtaplot.Main
import io.github.moehreag.dtaplot.Translations
import io.github.moehreag.dtaplot.android.ui.view.PlotView
import io.github.moehreag.dtaplot.android.ui.view.TcpView
import io.github.moehreag.dtaplot.android.ui.view.WsView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Main.init()
        Translations.loadLanguage(Locale.current.language, Locale.current.region)
        setContent {
            App()
        }
    }
}

@Preview(widthDp = 250)
@Composable
private fun App() {
    val viewModel = remember { ActivityViewModel() }
    MaterialTheme {
        BodyContent(viewModel)
    }

}

@Composable
fun BodyContent(model: ActivityViewModel) {

    val view = remember { mutableStateOf(View.PLOT) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Title")
                HorizontalDivider()
                NavigationDrawerItem(label = { Text(text = "Plot") }, selected = view.value == View.PLOT, onClick = {
                    view.value = View.PLOT
                })
                NavigationDrawerItem(label = { Text(text = "TCP") }, selected = view.value == View.TCP, onClick = {
                    view.value = View.TCP
                })
                NavigationDrawerItem(label = { Text(text = "WS") }, selected = view.value == View.WS, onClick = {
                    view.value = View.WS
                })
            }
        }) {

        view.value.Draw(model)
    }

    /*Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                actions = {
                    FilledIconButton(onClick = {
                        view.value = View.PLOT
                    }, enabled = view.value != View.PLOT) {
                        Icon(Icons.Filled.StackedLineChart, "Plot")
                    }
                    FilledIconButton(onClick = {
                        view.value = View.TCP
                    }, enabled = view.value != View.TCP) {
                        Icon(Icons.Outlined.SettingsEthernet, "TCP")
                    }
                    FilledIconButton(onClick = {
                        view.value = View.WS
                    }, enabled = view.value != View.WS) {
                        Icon(Icons.Filled.Language, "WS")
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        when (view.value) {
                            View.PLOT -> {
                                dialogOpen.value = true
                                model.openDialog() //TODO
                            }
                            View.WS -> {

                            }
                            View.TCP -> {

                            }
                        }
                    }) {

                        Icon(Icons.Filled.Edit, "Load File")
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxWidth().padding(innerPadding)) {
                Graph(model)
                Button(onClick = {
                    selectionSheet.value = true
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Select entries")
                }
            }
        }

        if (dialogOpen.value) {
            DiscoveryDialog {
                dialogOpen.value = false
            }
        }
        if (selectionSheet.value) {
            SelectionSheet { selectionSheet.value = false }
        }
    }*/
}
enum class View(private val instance: io.github.moehreag.dtaplot.android.ui.view.View) {
    PLOT(PlotView()), TCP(TcpView()), WS(WsView());

    @Composable
    fun Draw(activityViewModel: ActivityViewModel) {
        instance.Draw(activityViewModel)
    }
}


