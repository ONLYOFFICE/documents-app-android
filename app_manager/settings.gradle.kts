rootProject.name = "app_manager"

include(
    ":appmanager",
    ":core",
    ":core:network",
    ":libcompose",
    ":libtoolkit",
    ":libx2t",
    ":libeditors",
    ":libcells",
    ":libdocs",
    ":libslides",
    ":libgeditors",
    ":libgcells",
    ":libgdocs",
    ":libgslides"
)

project(":libtoolkit").projectDir = File(settingsDir, "../toolkit/libtoolkit")
project(":libx2t").projectDir = File(settingsDir, "../../core/X2tConverter/build/Android/libx2t")
project(":libeditors").projectDir = File(settingsDir, "../../core-ext/native_base/android_base/libeditors")
project(":libcells").projectDir = File(settingsDir, "../../core-ext/cell_android/libcells")
project(":libdocs").projectDir = File(settingsDir, "../../core-ext/word_android/libdocs")
project(":libslides").projectDir = File(settingsDir, "../../core-ext/slide_android/libslides")
project(":libgeditors").projectDir = File(settingsDir, "../../document-android-editors/editors_base/libgeditors")
project(":libgcells").projectDir = File(settingsDir, "../../document-android-editors/editors_cells/libgcells")
project(":libgdocs").projectDir = File(settingsDir, "../../document-android-editors/editors_docs/libgdocs")
project(":libgslides").projectDir = File(settingsDir, "../../document-android-editors/editors_slides/libgslides")