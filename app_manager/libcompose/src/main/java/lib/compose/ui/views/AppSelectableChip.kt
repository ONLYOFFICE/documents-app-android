package lib.compose.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
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
        modifier = modifier.height(32.dp),
        selected = selected,
        border = BorderStroke(0.5.dp, colorResource(id = R.color.colorOutline)).takeIf { selected },
        colors = ChipDefaults.filterChipColors(
            selectedBackgroundColor = MaterialTheme.colors.primary,
            selectedContentColor = MaterialTheme.colors.onPrimary
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