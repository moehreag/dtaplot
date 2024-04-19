package io.github.moehreag.dtaplot.android

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.moehreag.dtaplot.Main
import io.github.moehreag.dtaplot.Translations
import io.github.moehreag.dtaplot.android.ui.view.PlotView
import io.github.moehreag.dtaplot.android.ui.view.TcpView
import io.github.moehreag.dtaplot.android.ui.view.TopBar
import io.github.moehreag.dtaplot.android.ui.view.WsView
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Main.init()
        Translations.loadLanguage("en")
        Translations.loadLanguage(Locale.current.language)
        setContent {
            App()
        }
    }

    @Preview(widthDp = 250)
    @Composable
    private fun App() {
        val viewModel = remember { ActivityViewModel() }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val lightColors: ColorScheme = dynamicLightColorScheme(applicationContext)
            val darkColors: ColorScheme = dynamicDarkColorScheme(applicationContext)
            MaterialTheme(
                if (isSystemInDarkTheme()) {
                    darkColors
                } else {
                    lightColors
                }
            ) {
                BodyContent(viewModel)
            }
        } else {
            MaterialTheme {
                BodyContent(viewModel)
            }
        }

    }

    @Composable
    fun BodyContent(model: ActivityViewModel) {

        val view = rememberSaveable { mutableStateOf(View.PLOT) }
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        TopBar.openDrawer = {
            scope.launch {
                drawerState.open()
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(modifier = Modifier.padding(top = 150.dp))
                    Text(
                        text = "DtaPlot (${
                            applicationContext.packageManager.getPackageInfo(
                                applicationContext.packageName,
                                0
                            ).versionName
                        })", fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text(text = Translations.translate("view.plot")) },
                        selected = view.value == View.PLOT,
                        onClick = {
                            view.value = View.PLOT
                            scope.launch {
                                drawerState.close()
                            }
                        })
                    NavigationDrawerItem(
                        label = { Text(text = Translations.translate("view.tcp")) },
                        selected = view.value == View.TCP,
                        onClick = {
                            view.value = View.TCP
                            scope.launch {
                                drawerState.close()
                            }
                        })
                    NavigationDrawerItem(
                        label = { Text(text = Translations.translate("view.ws")) },
                        selected = view.value == View.WS,
                        onClick = {
                            view.value = View.WS
                            scope.launch {
                                drawerState.close()
                            }
                        })
                }
            }) {

            view.value.Draw(model)
        }
    }
}


enum class View(private val instance: io.github.moehreag.dtaplot.android.ui.view.View) {
    PLOT(PlotView()), TCP(TcpView()), WS(WsView());

    @Composable
    fun Draw(activityViewModel: ActivityViewModel) {
        instance.Draw(activityViewModel)
    }
}


