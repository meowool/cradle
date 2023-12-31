// tag::apply-swift-plugin[]
plugins {
    `swift-application` // or `swift-library`
}

version = "1.2.1"
// end::apply-swift-plugin[]

// tag::swift-dependency-mgmt[]
application {
    dependencies {
        implementation(project(":common"))
    }
}
// end::swift-dependency-mgmt[]

// tag::swift-compiler-options-all-variants[]
tasks.withType(SwiftCompile::class.java).configureEach {
    // Define a preprocessor macro for every binary
    macros.add("NDEBUG")

    // Define a compiler options
    compilerArgs.add("-O")
}
// end::swift-compiler-options-all-variants[]

// tag::swift-compiler-options-per-variants[]
application {
    binaries.configureEach(SwiftStaticLibrary::class.java) {
        // Define a preprocessor macro for every binary
        compileTask.get().macros.add("NDEBUG")

        // Define a compiler options
        compileTask.get().compilerArgs.add("-O")
    }
}
// end::swift-compiler-options-per-variants[]

// tag::swift-select-target-machines[]
application {
    targetMachines = listOf(machines.linux.x86_64, machines.macOS.x86_64)
}
// end::swift-select-target-machines[]
