package app.editors.manager.ui.fragments.share.link

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.editors.manager.R
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppListItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.DropdownMenuButton
import lib.compose.ui.views.DropdownMenuItem
import lib.toolkit.base.managers.utils.TimeUtils
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

sealed interface SharedLinkLifeTimeWithAmount {

    val amount: Int
    val field: Int
}

sealed class SharedLinkLifeTime(val title: Int) {

    data object Hours12 : SharedLinkLifeTime(lib.toolkit.base.R.plurals.hours),
        SharedLinkLifeTimeWithAmount {

        override val amount: Int
            get() = 12

        override val field: Int
            get() = Calendar.HOUR
    }

    data object Days1 : SharedLinkLifeTime(lib.toolkit.base.R.plurals.days),
        SharedLinkLifeTimeWithAmount {

        override val amount: Int
            get() = 1

        override val field: Int
            get() = Calendar.DAY_OF_MONTH
    }

    data object Days7 : SharedLinkLifeTime(lib.toolkit.base.R.plurals.days),
        SharedLinkLifeTimeWithAmount {

        override val amount: Int
            get() = 7

        override val field: Int
            get() = Calendar.DAY_OF_MONTH
    }

    data object Unlimited : SharedLinkLifeTime(R.string.rooms_share_lifetime_unlimited)

    data class Custom(val date: Date) : SharedLinkLifeTime(R.string.rooms_share_lifetime_custom)

    fun getFormattedDateTime(): String? {
        var calendar: Calendar? = Calendar.getInstance()
        calendar?.timeZone = TimeZone.getTimeZone("gmt")

        when (this) {
            Unlimited -> calendar = null
            is Custom -> calendar?.time = date
            is SharedLinkLifeTimeWithAmount -> calendar?.add(field, amount)
        }

        return calendar?.let { TimeUtils.DEFAULT_GMT_FORMAT.format(calendar.time) }
    }

    companion object {

        val all: List<SharedLinkLifeTime> = listOf(
            Hours12, Days1, Days7, Unlimited, Custom(Date())
        )
    }
}

@Composable
fun LinkLifeTimeListItem(
    expirationDate: String?,
    onSetLifeTime: (SharedLinkLifeTime) -> Unit,
) {
    AppListItem(
        title = stringResource(R.string.rooms_share_link_life_time),
        endContent = {
            val dropdownMenuState = remember { mutableStateOf(false) }
            val context = LocalContext.current

            DropdownMenuButton(
                state = dropdownMenuState,
                onDismiss = { dropdownMenuState.value = false },
                title = if (expirationDate != null) {
                    TimeUtils.getDateTimeLeft(context, expirationDate)
                        ?: context.getString(R.string.rooms_info_link_expired)
                } else {
                    context.getString(R.string.rooms_share_lifetime_unlimited)
                },
                items = {
                    SharedLinkLifeTime.all.forEach { item ->
                        DropdownMenuItem(
                            title = getLifeTimeString(item),
                            selected = false,
                            onClick = {
                                when (item) {
                                    is SharedLinkLifeTime.Custom -> {
                                        TimeUtils.showDateTimePickerDialog(context) {
                                            onSetLifeTime.invoke(SharedLinkLifeTime.Custom(it))
                                        }
                                    }
                                    else -> onSetLifeTime.invoke(item)
                                }
                                dropdownMenuState.value = false
                            }
                        )
                    }
                }
            ) {
                dropdownMenuState.value = true
            }
        }
    )
}

@Composable
private fun getLifeTimeString(item: SharedLinkLifeTime): String {
    return when (item) {
        is SharedLinkLifeTimeWithAmount -> "${item.amount} " + pluralStringResource(
            item.title,
            item.amount
        )

        else -> stringResource(id = item.title)
    }
}

@Preview
@Composable
private fun SharedLinkLifeTimeScreenPreview() {
    ManagerTheme {
        AppScaffold {
            LinkLifeTimeListItem(
                expirationDate = "2024-12-24T21:52:31.6150000Z",
                onSetLifeTime = {}
            )
        }
    }
}