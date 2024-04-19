package io.github.moehreag.dtaplot.android.ui.view

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

object TopBar {

    lateinit var openDrawer: () -> Unit

    @OptIn(ExperimentalMaterial3Api::class)
    fun create(title: String, actions: @Composable RowScope.() -> Unit = {}): @Composable () -> Unit {
        return {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                Text(text = title)
            }, navigationIcon = {
                IconButton(onClick = {
                    openDrawer.invoke()
                }) {
                    Icon(imageVector = Icons.Rounded.Menu, contentDescription = "Menu")
                }
            }, actions = actions)

        }
    }
}