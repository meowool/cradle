// tag::container-api[]
tasks.create("greeting") {
    doLast { println("Hello, World!") }
}
// end::container-api[]

// tag::typed-container-api[]
tasks.create<Zip>("docZip") {
    archiveFileName = "doc.zip"
    from("doc")
}
// end::typed-container-api[]
