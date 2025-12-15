@file:OptIn(ExperimentalGlideComposeApi::class)

package lib.compose.ui.views

import MemberTitle
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.load.model.GlideUrl
import lib.compose.ui.theme.colorTextTertiary
import lib.toolkit.base.R

data class MembersAvatarData(
    val memberId: String,
    val displayName: String,
    val avatarGlideUrl: GlideUrl? = null
)

@Composable
fun MembersRow(
    currentUser: MembersAvatarData,
    users: List<MembersAvatarData>,
    groups: List<MembersAvatarData>,
    onClick: () -> Unit
) {
    val onlyUsers = users.isNotEmpty() && groups.isEmpty()
    val onlyGroups = groups.isNotEmpty() && users.isEmpty()
    val hasBoth = users.isNotEmpty() && groups.isNotEmpty()
    val hasOnlyMe = !(onlyUsers || onlyGroups || hasBoth)

    val usersText = pluralStringResource(
        R.plurals.access_members_users_title, users.size, users.size
    )
    val groupsText = pluralStringResource(
        R.plurals.access_members_groups_title, groups.size, groups.size
    )

    val title = when {
        hasOnlyMe -> currentUser.displayName
        onlyUsers -> stringResource(R.string.access_members_me_and_title, usersText)
        onlyGroups -> stringResource(R.string.access_members_me_and_title, groupsText)
        else -> stringResource(R.string.access_members_me_users_groups_title, usersText, groupsText)
    }

    val membersList: List<MembersAvatarData> = buildList {
        add(currentUser)
        when {
            onlyUsers -> addAll(users.take(2))
            onlyGroups -> addAll(groups.take(2))
            hasBoth -> {
                add(users.first())
                add(groups.first())
            }
        }
    }

    MembersRow(
        membersList = membersList,
        title = title,
        hasOnlyMe = hasOnlyMe,
        onClick = onClick
    )
}

@Composable
private fun MembersRow(
    membersList: List<MembersAvatarData>,
    title: String,
    hasOnlyMe: Boolean,
    onClick: () -> Unit
) {
    val xOffset = 24.dp
    val avatarSize = 40.dp
    val avatarsRowWidth = avatarSize + xOffset * (membersList.size - 1)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable { onClick() }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MultiAvatarRow(
                membersData = membersList,
                xOffset = xOffset,
                avatarSize = avatarSize,
                avatarsRowWidth = avatarsRowWidth
            )
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MemberTitle(
                    name = title,
                    isCurrentUser = hasOnlyMe,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = ImageVector.vectorResource(
                        id = R.drawable.ic_arrow_right
                    ),
                    contentDescription = null,
                    tint = MaterialTheme.colors.colorTextTertiary
                )
            }
        }
        AppDivider(startIndent = 16.dp + avatarsRowWidth + 16.dp)
    }
}

@Composable
private fun MultiAvatarRow(
    membersData: List<MembersAvatarData>,
    xOffset: Dp,
    avatarSize: Dp,
    avatarsRowWidth: Dp,
    modifier: Modifier = Modifier
) {
    Box(modifier.width(avatarsRowWidth)) {
        membersData.reversed().forEachIndexed { index, data ->
            Box(
                modifier = Modifier
                    .offset(x = xOffset * (membersData.size - index - 1))
                    .clip(CircleShape)
                    .size(avatarSize)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colors.colorTextTertiary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                MemberAvatar(data = data)
            }
        }
    }
}
