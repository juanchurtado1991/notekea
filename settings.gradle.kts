rootProject.name = "notekea"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

include(":composeApp")
include(":ktlint-rules")
include(":test-buddy")
include(":selftest")
