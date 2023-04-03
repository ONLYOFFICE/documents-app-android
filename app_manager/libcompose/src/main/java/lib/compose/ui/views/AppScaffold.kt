package lib.compose.ui.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppScaffold(modifier: Modifier = Modifier, topBar: @Composable () -> Unit = {}, content: @Composable () -> Unit) {
    Scaffold(modifier = modifier, topBar = topBar) { padding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(padding),
            color = MaterialTheme.colors.surface,
            content = content
        )
    }
}