package lib.compose.ui.views

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorButtonBackground
import lib.compose.ui.theme.colorTextTertiary

@Composable
fun ActivityIndicatorView(title: String? = null) {
    Surface(color = MaterialTheme.colors.background) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = 500,
                    easing = LinearEasing)
                )
            ) {
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(36.dp),
                        color = MaterialTheme.colors.colorTextTertiary,
                        backgroundColor = MaterialTheme.colors.colorButtonBackground,
                        strokeCap = StrokeCap.Round,
                        strokeWidth = 3.dp
                    )

                    Spacer(Modifier.size(16.dp))

                    title?.let {
                        Text(
                            modifier = Modifier.padding(bottom = 16.dp),
                            text = it,
                            color = MaterialTheme.colors.colorTextTertiary,
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.size(64.dp))
                }
            }
        }
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    ManagerTheme {
        AppScaffold {
            ActivityIndicatorView(title = "Loading")
        }
    }
}

@Preview
@Composable
private fun Preview2() {
    ManagerTheme {
        AppScaffold {
            ActivityIndicatorView(title = "Loading")
        }
    }
}