package app.editors.manager.mvp.models.states

import kotlinx.serialization.Serializable

@Serializable
data class PasscodeLockState(
    val passcode: String? = null,
    val fingerprintEnabled: Boolean = false,
    val appUnlocked: Boolean = true,
    val disabledTimestamp: Long? = null,
) {

    val enabled: Boolean get() = !passcode.isNullOrEmpty()
}