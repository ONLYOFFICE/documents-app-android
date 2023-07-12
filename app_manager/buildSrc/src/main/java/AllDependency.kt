object Dagger {
    private const val version = "2.46.1"

    const val dagger = "com.google.dagger:dagger:$version"
    const val daggerCompiler = "com.google.dagger:dagger-compiler:$version"
}

object Moxy {
    private const val version = "2.2.2"

    const val moxyAndroid = "com.github.moxy-community:moxy-androidx:$version"
    const val moxyMaterial = "com.github.moxy-community:moxy-material:$version"
    const val moxyCompiler = "com.github.moxy-community:moxy-compiler:$version"
    const val moxyKtx = "com.github.moxy-community:moxy-ktx:$version"
}

object Koin {
    private const val version = "3.4.1"
    private const val versionCompose = "3.4.5"

    const val koinAndroid = "io.insert-koin:koin-android:$version"
    const val koinCompose = "io.insert-koin:koin-androidx-compose:$versionCompose"
}

object AndroidX {
    private const val androidxVersion = "1.6.1"
    private const val recyclerViewVersion = "1.3.0"
    private const val recyclerViewSelectionVersion = "1.1.0"
    private const val kotlinKtxVersion = "1.10.1"

    const val appCompat = "androidx.appcompat:appcompat:$androidxVersion"
    const val appCompatResources = "androidx.appcompat:appcompat-resources:$androidxVersion"
    const val composeActivity = "androidx.activity:activity-compose:1.7.2"
    const val recyclerView = "androidx.recyclerview:recyclerview:$recyclerViewVersion"
    const val recyclerViewSelection = "androidx.recyclerview:recyclerview-selection:$recyclerViewSelectionVersion"
    const val cardView = "androidx.cardview:cardview:1.0.0"
    const val constraint = "androidx.constraintlayout:constraintlayout:2.1.4"
    const val ktx = "androidx.core:core-ktx:$kotlinKtxVersion"
    const val biometric = "androidx.biometric:biometric:1.1.0"

}

object Retrofit {
    private const val retrofitVersion = "2.9.0"

    const val retrofit = "com.squareup.retrofit2:retrofit:$retrofitVersion"
    const val retrofitGson = "com.squareup.retrofit2:converter-gson:$retrofitVersion"
    const val retrofitXml = "com.squareup.retrofit2:converter-simplexml:$retrofitVersion"
    const val retrofitRx = "com.squareup.retrofit2:adapter-rxjava2:$retrofitVersion"
    const val retrofitKotlinSerialization = "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0"

}

object Kotlin {
    const val version = "1.7.10"
    const val kspVersion = "1.7.10-1.0.6"
    private const val coroutinesVersion = "1.6.0"
    private const val serializationVersion = "1.3.2"

    const val kotlinCore = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
    const val coroutineCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"
    const val coroutineAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion"
    const val kotlinSerialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion"
}

object Google {
    private const val playServicesAuthVersion = "20.5.0"
    private const val playCoreVersion = "1.10.3"
    private const val materialVersion = "1.9.0"
    private const val gsonVersion = "2.9.1"
    private const val safetynetVersion = "18.0.1"


    const val playServiceAuth = "com.google.android.gms:play-services-auth:$playServicesAuthVersion"
    const val playCore = "com.google.android.play:core:$playCoreVersion"
    const val material = "com.google.android.material:material:$materialVersion"
    const val gson = "com.google.code.gson:gson:$gsonVersion"
    const val safetynet = "com.google.android.gms:play-services-safetynet:$safetynetVersion"

}

object Firebase {
    private const val firebaseCoreVersion = "21.1.1"
    private const val firebaseConfigVersion = "21.4.0"
    private const val firebaseMessagingVersion = "23.1.2"
    private const val firebaseCrashlyticsVersion = "18.3.7"

    const val firebaseCore = "com.google.firebase:firebase-core:$firebaseCoreVersion"
    const val firebaseConfig = "com.google.firebase:firebase-config:$firebaseConfigVersion"
    const val firebaseCrashlytics = "com.google.firebase:firebase-crashlytics:$firebaseCrashlyticsVersion"
    const val firebaseMessaging = "com.google.firebase:firebase-messaging-ktx:$firebaseMessagingVersion"
}

object Room {
    private const val roomVersion = "2.5.1"

    const val roomRuntime = "androidx.room:room-runtime:$roomVersion"
    const val roomKtx = "androidx.room:room-ktx:$roomVersion"
    const val roomCompiler = "androidx.room:room-compiler:$roomVersion"
}

object Rx {
    private const val version = "2.1.1"

    const val androidRx = "io.reactivex.rxjava2:rxandroid:$version"
    const val rxRelay = "com.jakewharton.rxrelay2:rxrelay:$version"
}

object Lifecycle {
    private const val version = "2.6.1"

    const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
    const val liveData = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
    const val runtime = "androidx.lifecycle:lifecycle-runtime-ktx:$version"
}

object Compose {
    const val version = "1.3.3"
    const val versionMaterial = "1.3.1"
    const val versionCompiler = "1.3.1"
    private const val navigationVersion = "2.5.3"

    const val ui = "androidx.compose.ui:ui:$version"
    const val material = "androidx.compose.material:material:$versionMaterial"
    const val preview = "androidx.compose.ui:ui-tooling-preview:$version"
    const val tooling = "androidx.compose.ui:ui-tooling:$version"
    const val navigation = "androidx.navigation:navigation-compose:$navigationVersion"
    const val liveData = "androidx.compose.runtime:runtime-livedata:$version"
}

object Jackson {
    private const val version = "2.14.3"
    const val core = "com.fasterxml.jackson.core:jackson-core:$version"
    const val annotations = "com.fasterxml.jackson.core:jackson-annotations:$version"
    const val databind = "com.fasterxml.jackson.core:jackson-databind:$version"
}

object Libs {
    const val phoneNumber = "io.michaelrocks:libphonenumber-android:8.12.24"
    const val facebookLogin = "com.facebook.android:facebook-login:16.0.1"
    const val pageIndicator = "com.github.romandanylyk:PageIndicatorView:b1bad589b5"
    const val glide = "com.github.bumptech.glide:glide:4.15.1"
    const val photoView = "com.github.chrisbanes:PhotoView:2.3.0"
    const val androidWork = "androidx.work:work-runtime:2.8.1"
    const val documentFile = "androidx.documentfile:documentfile:1.0.1"
    const val pdfView = "com.github.TalbotGooday:AndroidPdfViewer:3.1.0-beta.3"
    const val colorPicker = "com.github.skydoves:colorpickerview:2.2.4"
    const val dropboxSdk = "com.dropbox.core:dropbox-core-sdk:5.2.0"
}
