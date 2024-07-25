rootProject.name = "DocumentsAndroidWithoutEditors"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../app_manager/gradle/libs.versions.toml"))
        }
    }
}

include(
    ":appmanager",
    ":core",
    ":core:model",
    ":core:network",
    ":core:database",
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
project(":core:model").projectDir = File(settingsDir, "../app_manager/core/model")
project(":core:network").projectDir = File(settingsDir, "../app_manager/core/network")
project(":core:database").projectDir = File(settingsDir, "../app_manager/core/database")


