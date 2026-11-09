import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * The versions the build itself uses, read from `gradle/libs.versions.toml`, the one place they are written. A milestone
 * build passes `-PpackageVersion=1.1.2 -PreleaseName=m1-rc2` (`qa/README.md`), so each release candidate installs over
 * the last.
 */
object NotekeaVersions {
    fun jdk(project: Project): Int = catalog(project = project, name = "jdk").toInt()

    fun ktlint(project: Project): String = catalog(project = project, name = "ktlint-cli")

    /** The plain version the installers need. */
    fun packageVersion(
        project: Project
    ): String = project.providers.gradleProperty("packageVersion")
        .getOrElse(catalog(project = project, name = "notekea-package"))

    /** The version people see, in About and crash reports. */
    fun displayVersion(project: Project): String = catalog(project = project, name = "notekea") +
        project.providers.gradleProperty("releaseName").map { " ($it)" }.getOrElse("")

    private fun catalog(
        project: Project,
        name: String
    ): String = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
        .findVersion(name).get().requiredVersion
}
