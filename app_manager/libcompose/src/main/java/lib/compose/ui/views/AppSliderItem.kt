package lib.compose.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.ManagerTheme

@Composable
fun AppSliderItem(
    title: Int? = null,
    header: Int? = null,
    initValue: Float = 0f,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    digitsAfterPoint: Int = 0,
    steps: Int = 0,
    unit: String? = null,
    enabled: Boolean = true,
    dividerVisible: Boolean = true,
    onValueChange: (Float) -> Unit = {},
    onValueChangeFinished: (Float) -> Unit = {}
) {
    var value by remember { mutableStateOf(initValue) }
    val formattedValue by remember { derivedStateOf { String.format("%.0${digitsAfterPoint}f", value) } }

    Column {
        header?.let { AppHeaderItem(stringResource(header)) }
        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            title?.let { Text(modifier = Modifier.padding(end = 8.dp), text = stringResource(title)) }
            Slider(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                value = value,
                enabled = enabled,
                valueRange = valueRange,
                steps = steps,
                onValueChange = {
                    onValueChange(it)
                    value = it
                },
                onValueChangeFinished = {
                    onValueChangeFinished.invoke(value)
                })
            Box() {
                Text(
                    modifier = Modifier.alpha(0f),
                    text = "${String.format("%.0${digitsAfterPoint}f", valueRange.endInclusive)} ${unit.orEmpty()}"
                )
                Text(
                    modifier = Modifier.alpha(1f),
                    text = "$formattedValue ${unit.orEmpty()}"
                )
            }
        }
        if (dividerVisible) {
            AppDivider(startIndent = 16.dp)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ManagerTheme {
        Surface(color = MaterialTheme.colors.background) {
            Column {
                AppSliderItem(
                    header = lib.toolkit.base.R.string.app_title,
                    unit = "cm",
                    onValueChange = {},
                    onValueChangeFinished = {}
                )
                AppSliderItem(
                    header = lib.toolkit.base.R.string.app_title,
                    title = lib.toolkit.base.R.string.sizes_bytes,
                    unit = "cm",
                    onValueChange = {},
                    onValueChangeFinished = {}
                )
            }
        }
    }
}