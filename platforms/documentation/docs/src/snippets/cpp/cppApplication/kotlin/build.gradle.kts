// tag::apply-plugin[]
plugins {
    `cpp-application`
}
// end::apply-plugin[]

// tag::configure-target-machines[]
application {
    targetMachines = listOf(machines.linux.x86_64,
        machines.windows.x86, machines.windows.x86_64,
        machines.macOS.x86_64)
}
// end::configure-target-machines[]
