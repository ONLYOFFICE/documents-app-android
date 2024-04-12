package app.editors.manager.ui.fragments.share.link

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.editors.manager.R
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppListItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTopBar
import lib.toolkit.base.managers.utils.TimeUtils
import java.util.Calendar
import java.util.Date

sealed interface SharedLinkLifeTimeWithAmount {

    val amount: Int
    val field: Int
}

sealed class SharedLinkLifeTime(val title: Int) {

    data object Hours12 : SharedLinkLifeTime(lib.toolkit.base.R.plurals.hours), SharedLinkLifeTimeWithAmount {

        override val amount: Int
            get() = 12

        override val field: Int
            get() = Calendar.HOUR
    }

    data object Days1 : SharedLinkLifeTime(lib.toolkit.base.R.plurals.days), SharedLinkLifeTimeWithAmount {

        override val amount: Int
            get() = 1

        override val field: Int
            get() = Calendar.DAY_OF_MONTH
    }

    data object Days7 : SharedLinkLifeTime(lib.toolkit.base.R.plurals.days), SharedLinkLifeTimeWithAmount {

        override val amount: Int
            get() = 7

        override val field: Int
            get() = Calendar.DAY_OF_MONTH
    }

    data object Unlimited : SharedLinkLifeTime(R.string.rooms_share_lifetime_unlimited)
    data class Custom(val date: Date) : SharedLinkLifeTime(R.string.rooms_share_lifetime_custom)

    companion object {

        val all: List<SharedLinkLifeTime> = listOf(
            Hours12, Days1, Days7, Unlimited, Custom(Date())
        )
    }
}

@Composable
fun SharedLinkLifeTimeScreen(onBack: () -> Unit, onSetLifeTime: (SharedLinkLifeTime) -> Unit) {
    AppScaffold(
        topBar = {
            AppTopBar(title = R.string.rooms_share_link_life_time, backListener = onBack)
        }
    ) {
        Column {
            val context = LocalContext.current
            SharedLinkLifeTime.all.forEach { item ->
                AppListItem(
                    title = getLifeTimeString(item),
                    onClick = {
                        when (item) {
                            is SharedLinkLifeTime.Custom -> {
                                TimeUtils.showDateTimePickerDialog(context) {
                                    onSetLifeTime.invoke(SharedLinkLifeTime.Custom(it))
                                    onBack.invoke()
                                }
                            }
                            else -> {
                                onSetLifeTime.invoke(item)
                                onBack.invoke()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun getLifeTimeString(item: SharedLinkLifeTime): String {
    return when (item) {
        is SharedLinkLifeTimeWithAmount -> "${item.amount} " + pluralStringResource(item.title, item.amount)
        else -> stringResource(id = item.title)
    }
}

@Preview
@Composable
private fun SharedLinkLifeTimeScreenPreview() {
    ManagerTheme {
        SharedLinkLifeTimeScreen({}) {}
    }
}