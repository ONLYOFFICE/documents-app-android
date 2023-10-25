rootProject.name = "DocumentsAndroidWithoutEditors"

include(
    ":appmanager",
    ":core",
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

project(":appmanager").projectDir = File(settingsDir, "../app_manager/appmanager")
project(":libcompose").projectDir = File(settingsDir, "../app_manager/libcompose")
project(":libtoolkit").projectDir = File(settingsDir, "../toolkit/libtoolkit")
project(":core").projectDir = File(settingsDir, "../app_manager/core")

