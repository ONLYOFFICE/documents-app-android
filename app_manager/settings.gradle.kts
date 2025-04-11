rootProject.name = "app_manager"

val withEditors: String? by settings  // parameter -PincludeEditors=true/false

fun shouldIncludeEditors(): Boolean? {
    return withEditors?.toBoolean() ?: true
}

include(
    ":appmanager",
    ":core",
    ":core:network",
    ":libcompose",
    ":libtoolkit",
    ":core:model",
    ":core:database"
)

//  Check the existence of editor modules before connecting
fun includeEditorModule(path: String, projectPath: String) {
    val projectDir = File(settingsDir, path)
    if (projectDir.exists()) {
        include(projectPath)
        project(projectPath).projectDir = projectDir
    } else {
        println("⚠️ The $projectPath editor module is missing (path: ${projectDir.absolutePath})")
    }
}

if (shouldIncludeEditors() == true) {
    includeEditorModule("../../core/X2tConverter/build/Android/libx2t", ":libx2t")
    includeEditorModule("../../core-ext/native_base/android_base/libeditors", ":libeditors")
    includeEditorModule("../../core-ext/cell_android/libcells", ":libcells")
    includeEditorModule("../../core-ext/word_android/libdocs", ":libdocs")
    includeEditorModule("../../core-ext/slide_android/libslides", ":libslides")
    includeEditorModule("../../document-android-editors/editors_base/libgeditors", ":libgeditors")
    includeEditorModule("../../document-android-editors/editors_cells/libgcells", ":libgcells")
    includeEditorModule("../../document-android-editors/editors_docs/libgdocs", ":libgdocs")
    includeEditorModule("../../document-android-editors/editors_slides/libgslides", ":libgslides")
}

project(":libtoolkit").projectDir = File(settingsDir, "../toolkit/libtoolkit")