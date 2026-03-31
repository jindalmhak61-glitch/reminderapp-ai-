pluginManagement {
    repositories {
        google()

        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://www.jitpack.io") } // Necessary for SceneView and other GitHub dependencies
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") } // Necessary for SceneView and other GitHub dependencies
    }
}

rootProject.name = "ReminderApp"
include(":app")
