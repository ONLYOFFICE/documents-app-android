package lib.compose.ui.views

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.toolkit.base.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppSelectableChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    selected: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    FilterChip(
        modifier = modifier.height(28.dp),
        selected = selected,
        border = BorderStroke(0.5.dp, colorResource(id = R.color.colorOutline)).takeIf { selected },
        colors = ChipDefaults.filterChipColors(
            selectedBackgroundColor = MaterialTheme.colors.primary,
            selectedContentColor = MaterialTheme.colors.onPrimary,
            leadingIconColor = MaterialTheme.colors.colorTextSecondary,
            selectedLeadingIconColor = MaterialTheme.colors.onPrimary
        ),
        onClick = onClick,
        leadingIcon = leadingIcon,
        trailingIcon = {
            if (selected) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_chip_close),
                    contentDescription = null
                )
            }
        },
        content = content
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    ManagerTheme {
        Surface(color = MaterialTheme.colors.background) {
            Column(modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppSelectableChip(
                    onClick = {},
                    selected = true,
                    content = {
                        Text("Text", color = Color.Unspecified)
                    }
                )
                AppSelectableChip(
                    onClick = {},
                    selected = false,
                    leadingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_add_small),
                            contentDescription = null
                        )
                    },
                    content = {
                        Text("Text")
                    }
                )
            }
        }
    }
}