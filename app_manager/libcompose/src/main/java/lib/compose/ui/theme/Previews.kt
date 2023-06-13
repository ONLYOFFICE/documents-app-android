package lib.compose.ui.theme

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

annotation class Previews {

    @Preview(device = PHONE_SPEC, name = "Phone")
    @Preview(device = PHONE_SPEC_LANDSCAPE, name = "Phone Landscape")
    @Preview(device = PHONE_SPEC, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Phone Dark")
    annotation class Phone

    @Preview(device = TABLET_SPEC, name = "Tablet")
    @Preview(device = TABLET_SPEC_LANDSCAPE, name = "Tablet Landscape")
    @Preview(device = TABLET_SPEC, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Tablet Dark")
    annotation class Tablet

    @Phone
    @Tablet
    annotation class All

    companion object {
        private const val PHONE_WIDTH = 360
        private const val PHONE_HEIGHT = 640
        private const val TABLET_WIDTH = 768
        private const val TABLET_HEIGHT = 1024

        private const val PHONE_SPEC = "spec:width=${PHONE_WIDTH}dp,height=${PHONE_HEIGHT}dp"
        private const val PHONE_SPEC_LANDSCAPE = "spec:width=${PHONE_HEIGHT}dp,height=${PHONE_WIDTH}dp"
        private const val TABLET_SPEC = "spec:width=${TABLET_WIDTH}dp,height=${TABLET_HEIGHT}dp"
        private const val TABLET_SPEC_LANDSCAPE = "spec:width=${TABLET_HEIGHT}dp,height=${TABLET_WIDTH}dp"
    }
}