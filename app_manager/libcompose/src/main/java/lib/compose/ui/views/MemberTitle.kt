import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.colorTextSecondary
import lib.toolkit.base.R

@Composable
fun MemberTitle(
    name: String,
    modifier: Modifier = Modifier,
    isCurrentUser: Boolean = false,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.body1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, false)
        )
        if (isCurrentUser) {
            Text(
                text = stringResource(R.string.access_members_me_subtitle),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.colorTextSecondary,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}
