package app.editors.manager.ui.activities.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.editors.manager.R
import app.editors.manager.app.appComponent
import kotlinx.coroutines.launch
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppScaffold
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.capitalize
import lib.toolkit.base.R as Base

class OnBoardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ManagerTheme {
                OnBoardingScreen(
                    onClose = {
                        appComponent.preference.onBoarding = true
                        MainActivity.show(this)
                    }
                )
            }
        }
    }

    companion object {

        val TAG: String = OnBoardingActivity::class.java.simpleName

        fun show(activity: Activity) {
            activity.startActivity(Intent(activity, OnBoardingActivity::class.java))
        }
    }
}

private data class OnBoardingData(
    val image: Int,
    val header: Int,
    val info: Int,
)

private val onBoardingListData: List<OnBoardingData> = listOf(
    OnBoardingData(
        image = R.drawable.image_on_boarding_screen1,
        header = R.string.on_boarding_welcome_header,
        info = R.string.on_boarding_welcome_info
    ),
    OnBoardingData(
        image = R.drawable.image_on_boarding_screen2,
        header = R.string.on_boarding_edit_header,
        info = R.string.on_boarding_edit_info
    ),
    OnBoardingData(
        image = R.drawable.image_on_boarding_screen3,
        header = R.string.on_boarding_locally_header,
        info = R.string.on_boarding_locally_info
    ),
    OnBoardingData(
        image = R.drawable.image_on_boarding_screen4,
        header = R.string.on_boarding_collaborate_header,
        info = R.string.on_boarding_collaborate_info
    ),
    OnBoardingData(
        image = R.drawable.image_on_boarding_screen5,
        header = R.string.on_boarding_third_party_header,
        info = R.string.on_boarding_third_party_info
    )
)

@Composable
private fun OnBoardingScreen(onClose: () -> Unit) {
    AppScaffold(useTablePaddings = false) {
        Column {
            val pagerState = rememberPagerState { onBoardingListData.size }
            val context = LocalContext.current
            val isPortrait = UiUtils.isPortrait(context)
            val isTablet = UiUtils.isTablet(context)
            val coroutineScope = rememberCoroutineScope()
            val currentPage = pagerState.currentPage

            fun next() {
                if (currentPage == pagerState.pageCount - 1) {
                    onClose()
                    return
                }
                if (currentPage < pagerState.pageCount) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(currentPage + 1)
                    }
                }
            }

            fun back() {
                if (currentPage == 0) {
                    onClose()
                    return
                }
                if (currentPage > 0) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(currentPage - 1)
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) { page ->
                if (!isPortrait && !isTablet) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ImageBlock(modifier = Modifier.weight(1f), page = page)
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            TextBlock(
                                modifier = Modifier
                                    .widthIn(max = 360.dp)
                                    .padding(16.dp),
                                page = page
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(max = 360.dp)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ImageBlock(modifier = Modifier.padding(bottom = 60.dp), page = page)
                            TextBlock(page = page)
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),

                ) {
                Box(
                    modifier = Modifier
                        .weight(.5f)
                ) {
                    TextButton(::back) {
                        Text(
                            if (currentPage == 0) {
                                stringResource(R.string.on_boarding_skip_button).capitalize()
                            } else {
                                stringResource(R.string.on_boarding_back_button).capitalize()
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(.5f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        repeat(pagerState.pageCount) { index ->
                            val color = animateColorAsState(
                                targetValue = if (index == currentPage) {
                                    MaterialTheme.colors.colorTextSecondary
                                } else {
                                    colorResource(Base.color.colorOutline)
                                },
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(8.dp)
                                    .background(color.value)
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier.weight(.5f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TextButton(::next) {
                        Text(
                            if (currentPage < pagerState.pageCount - 1) {
                                stringResource(R.string.on_boarding_next_button).capitalize()
                            } else {
                                stringResource(R.string.on_boarding_finish_button).capitalize()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageBlock(modifier: Modifier = Modifier, page: Int) {
    Image(
        modifier = modifier,
        imageVector = ImageVector.vectorResource(onBoardingListData[page].image),
        contentDescription = null
    )
}

@Composable
private fun TextBlock(modifier: Modifier = Modifier, page: Int) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = stringResource(onBoardingListData[page].header),
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = stringResource(onBoardingListData[page].info),
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.colorTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Preview(device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait")
@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
private fun OnBoardingScreenPreview() {
    ManagerTheme {
        OnBoardingScreen {}
    }
}
