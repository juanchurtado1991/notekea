rootProject.name = "notekea"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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

include(":app")
include(":ktlint-rules")
include(":test-buddy-crash")
include(":test-buddy")
include(":auto-test")
