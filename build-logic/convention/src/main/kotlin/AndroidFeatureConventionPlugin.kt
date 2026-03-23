import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/** `:feature:*` 向け: Compose・feature UI バンドル・Navigation3 等を一括適用する Gradle コンベンション。 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("nofy.android.library")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<LibraryExtension> {
                buildFeatures {
                    compose = true
                }
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            dependencies {
                add("implementation", platform(libs.findLibrary("androidx-compose-bom").get()))
                add("implementation", libs.findBundle("feature-ui").get())
                add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
            }

            val verifyNoDirectMaterialUsage = tasks.register("verifyNoDirectMaterialUsage") {
                group = "verification"
                description = "Fails the build if a feature module imports Compose Material APIs directly."

                doLast {
                    val disallowedImports = listOf(
                        "import androidx.compose.material.",
                        "import androidx.compose.material3.",
                        "import androidx.compose.material.icons."
                    )
                    val violations = fileTree(layout.projectDirectory.dir("src")) {
                        include("**/*.kt")
                    }.files.mapNotNull { file ->
                        val violation = file.useLines { lines ->
                            lines.firstOrNull { line ->
                                disallowedImports.any(line::startsWith)
                            }
                        }
                        violation?.let { "${project.path}: ${file.relativeTo(project.projectDir)} -> $it" }
                    }

                    if (violations.isNotEmpty()) {
                        throw GradleException(
                            buildString {
                                appendLine("Direct Compose Material usage is forbidden in feature modules.")
                                appendLine("Use components exposed from :platform:designsystem instead.")
                                append(violations.joinToString(separator = "\n"))
                            }
                        )
                    }
                }
            }

            tasks.matching { it.name == "preBuild" }.configureEach {
                dependsOn(verifyNoDirectMaterialUsage)
            }
        }
    }
}
