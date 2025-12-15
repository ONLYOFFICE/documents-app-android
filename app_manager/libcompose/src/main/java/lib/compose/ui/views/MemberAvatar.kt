package lib.compose.ui.views
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.model.GlideUrl
import lib.compose.ui.theme.colorTextSecondary
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.StringUtils

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MemberAvatar(
    modifier: Modifier = Modifier,
    avatarUrl: GlideUrl?,
    displayName: String
) {
    Box(modifier) {
        if (avatarUrl != null) {
            GlideImage(
                modifier = Modifier.fillMaxSize(),
                model = avatarUrl,
                contentDescription = null,
                loading = placeholder(R.drawable.ic_avatar_default),
                failure = placeholder(R.drawable.ic_avatar_default)
            )
        } else {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.colorIconBackground))
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = StringUtils.getAvatarName(displayName),
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.colorTextSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MemberAvatar(
    modifier: Modifier = Modifier,
    data: MemberData
) {
    MemberAvatar(
        modifier = modifier,
        avatarUrl = data.avatarGlideUrl,
        displayName = data.displayName
    )
}
