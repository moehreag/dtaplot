package io.github.moehreag.dtaplot.android.ui.view

import androidx.compose.runtime.Composable
import io.github.moehreag.dtaplot.android.ActivityViewModel

interface View {
    @Composable
    fun Draw(viewModel: ActivityViewModel)
}