plugins {
    id("org.myorg.url-verifier")        // <1>
}

verification {
    url = "https://www.google.com/"  // <2>
}
