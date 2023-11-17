import org.gradle.api.internal.FeaturePreviews

pluginManagement {
    repositories {
        maven {
            url = uri("https://repo.gradle.org/gradle/enterprise-libs-release-candidates")
            content {
                val rcAndMilestonesPattern = "\\d{1,2}?\\.\\d{1,2}?(\\.\\d{1,2}?)?-((rc-\\d{1,2}?)|(milestone-\\d{1,2}?))"
                // GE plugin marker artifact
                includeVersionByRegex("com.gradle.enterprise", "com.gradle.enterprise.gradle.plugin", rcAndMilestonesPattern)
                // GE plugin jar
                includeVersionByRegex("com.gradle", "gradle-enterprise-gradle-plugin", rcAndMilestonesPattern)
            }
        }
        maven {
            name = "Gradle public repository"
            url = uri("https://repo.gradle.org/gradle/public")
            content {
                includeModule("org.openmbee.junit", "junit-xml-parser")
            }
        }
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.enterprise").version("3.15.1") // Sync with `build-logic-commons/build-platform/build.gradle.kts`
    id("io.github.gradle.gradle-enterprise-conventions-plugin").version("0.7.6")
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.7.0")
}

includeBuild("build-logic-commons")
includeBuild("build-logic")

apply(from = "gradle/shared-with-buildSrc/mirrors.settings.gradle.kts")

// If you include a new subproject here, you will need to execute the
// ./gradlew generateSubprojectsInfo
// task to update metadata about the build for CI

unassigned {
    subproject("distributions-dependencies") // platform for dependency versions
    subproject("core-platform")              // platform for Gradle distribution core
}

// Gradle Distributions - for testing and for publishing a full distribution
unassigned {
    subproject("distributions-core")
    subproject("distributions-basics")
    subproject("distributions-full")
}

// Gradle implementation projects
unassigned {
    subproject("core")
    subproject("plugins")
    subproject("build-events")
    subproject("diagnostics")
    subproject("installation-beacon")
    subproject("composite-builds")
    subproject("core-api")
    subproject("build-profile")
    subproject("instrumentation-declarations")
}

// Core Runtime Platform
platform("core-runtime") {
    subproject("base-annotations")
    subproject("base-services")
    subproject("bootstrap")
    subproject("build-operations")
    subproject("build-option")
    subproject("cli")
    subproject("file-temp")
    subproject("files")
    subproject("functional")
    subproject("instrumentation-agent")
    subproject("internal-instrumentation-api")
    subproject("internal-instrumentation-processor")
    subproject("launcher")
    subproject("logging")
    subproject("logging-api")
    subproject("messaging")
    subproject("native")
    subproject("process-services")
    subproject("worker-services")
    subproject("wrapper")
    subproject("wrapper-shared")
}

// Core Configuration Platform
platform("core-configuration") {
    subproject("api-metadata")
    subproject("base-services-groovy")
    subproject("configuration-cache")
    subproject("file-collections")
    subproject("input-tracking")
    subproject("kotlin-dsl")
    subproject("kotlin-dsl-provider-plugins")
    subproject("kotlin-dsl-tooling-builders")
    subproject("kotlin-dsl-tooling-models")
    subproject("kotlin-dsl-plugins")
    subproject("kotlin-dsl-integ-tests")
    subproject("model-core")
    subproject("model-groovy")
}

// Core Execution Platform
platform("core-execution") {
    subproject("build-cache")
    subproject("build-cache-base")
    subproject("build-cache-http")
    subproject("build-cache-packaging")
    subproject("file-watching")
    subproject("execution")
    subproject("hashing")
    subproject("persistent-cache")
    subproject("snapshots")
    subproject("worker-processes")
    subproject("workers")
}

// Extensibility Platform
platform("extensibility") {
    subproject("plugin-use")
    subproject("plugin-development")
    subproject("test-kit")
}

// IDE Platform
platform("ide") {
    subproject("base-ide-plugins")
    subproject("ide")
    subproject("ide-native")
    subproject("ide-plugins")
    subproject("problems")
    subproject("problems-api")
    subproject("tooling-api")
    subproject("tooling-api-builders")
}

// Native Platform
platform("native") {
    subproject("distributions-native")
    subproject("platform-native")
    subproject("language-native")
    subproject("tooling-native")
    subproject("testing-native")
}

// Software Platform
platform("software") {
    subproject("antlr")
    subproject("build-init")
    subproject("dependency-management")
    subproject("plugins-distribution")
    subproject("distributions-publishing")
    subproject("ivy")
    subproject("maven")
    subproject("platform-base")
    subproject("plugins-version-catalog")
    subproject("publish")
    subproject("resources")
    subproject("resources-http")
    subproject("resources-gcs")
    subproject("resources-s3")
    subproject("resources-sftp")
    subproject("reporting")
    subproject("security")
    subproject("signing")
    subproject("testing-base")
    subproject("test-suites-base")
    subproject("version-control")
}

// JVM Platform
platform("jvm") {
    subproject("code-quality")
    subproject("distributions-jvm")
    subproject("ear")
    subproject("jacoco")
    subproject("jvm-services")
    subproject("language-groovy")
    subproject("language-java")
    subproject("language-jvm")
    subproject("toolchains-jvm")
    subproject("java-compiler-plugin")
    subproject("java-platform")
    subproject("normalization-java")
    subproject("platform-jvm")
    subproject("plugins-groovy")
    subproject("plugins-java")
    subproject("plugins-java-base")
    subproject("plugins-jvm-test-fixtures")
    subproject("plugins-jvm-test-suite")
    subproject("plugins-test-report-aggregation")
    subproject("scala")
    subproject("testing-jvm")
    subproject("testing-jvm-infrastructure")
    subproject("testing-junit-platform")
    subproject("war")
}

// Develocity Platform
platform("enterprise") {
    subproject("enterprise")
    subproject("enterprise-logging")
    subproject("enterprise-operations")
    subproject("enterprise-plugin-performance")
    subproject("enterprise-workers")
}

// Internal utility and verification projects
unassigned {
    subproject("docs")
    subproject("docs-asciidoctor-extensions-base")
    subproject("docs-asciidoctor-extensions")
    subproject("samples")
    subproject("architecture-test")
    subproject("internal-testing")
    subproject("internal-integ-testing")
    subproject("internal-performance-testing")
    subproject("internal-architecture-testing")
    subproject("internal-build-reports")
    subproject("integ-test")
    subproject("distributions-integ-tests")
    subproject("soak")
    subproject("smoke-test")
    subproject("performance")
    subproject("precondition-tester")
}

rootProject.name = "gradle"

FeaturePreviews.Feature.values().forEach { feature ->
    if (feature.isActive) {
        enableFeaturePreview(feature.name)
    }
}

fun remoteBuildCacheEnabled(settings: Settings) = settings.buildCache.remote?.isEnabled == true

fun getBuildJavaHome() = System.getProperty("java.home")

gradle.settingsEvaluated {
    if ("true" == System.getProperty("org.gradle.ignoreBuildJavaVersionCheck")) {
        return@settingsEvaluated
    }

    if (!JavaVersion.current().isJava11) {
        throw GradleException("This build requires JDK 11. It's currently ${getBuildJavaHome()}. You can ignore this check by passing '-Dorg.gradle.ignoreBuildJavaVersionCheck=true'.")
    }
}

// region platform include DSL

fun platform(platformName: String, platformConfiguration: PlatformScope.() -> Unit) =
    PlatformScope("platforms/$platformName").platformConfiguration()

fun unassigned(platformConfiguration: PlatformScope.() -> Unit) =
    PlatformScope("subprojects").platformConfiguration()

class PlatformScope(
    private val basePath: String
) {
    fun subproject(projectName: String) {
        include(projectName)
        project(":$projectName").projectDir = file("$basePath/$projectName")
    }
}

// endregion
