package app.documents.core.model.login

import kotlinx.serialization.Serializable

@Serializable
data class RequestDeviceToken(val firebaseDeviceToken : String)

@Serializable
data class RequestPushSubscribe(val firebaseDeviceToken : String, val isSubscribed: Boolean)