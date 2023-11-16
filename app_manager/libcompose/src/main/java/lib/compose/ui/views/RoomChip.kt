package lib.compose.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.ManagerTheme
import java.util.UUID

@Immutable
data class ChipData(
    val text: String,
    val id: String = UUID.randomUUID().toString()
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RoomChip(
    data: ChipData,
    onDeleteClick: () -> Unit
) {
    Chip(
        modifier = Modifier,
        shape = RoundedCornerShape(50),
        enabled = true,
        onClick = {}
    ) {
        Text(
            text = data.text,
            modifier = Modifier.weight(1f, fill = false),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.body2
        )
        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
        Icon(
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    onDeleteClick()
                }
                .background(Color.Black.copy(alpha = .4f))
                .size(16.dp)
                .padding(2.dp),
            imageVector = Icons.Filled.Close,
            tint = Color(0xFFE0E0E0),
            contentDescription = null
        )
    }
}

@Preview
@Composable
private fun ChipPreview() {
    ManagerTheme {
        RoomChip(data = ChipData("Hello")) {}
    }
}