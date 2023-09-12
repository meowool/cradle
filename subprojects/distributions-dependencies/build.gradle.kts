import gradlebuild.basics.isBundleGroovy4

/**
 * This project provides the "platform" for the Gradle distribution.
 * We want the versions that are packaged in the distribution to be used everywhere (e.g. in all test scenarios)
 * Hence, we lock the versions down here for all other subprojects.
 *
 * Note:
 * We use strictly here because we do not have any better means to do this at the moment.
 * Ideally we wound be able to say "lock down all the versions of the dependencies resolved for the distribution"
 */
plugins {
    id("gradlebuild.platform")
}

description = "Provides a platform dependency to align all distribution versions"

val antVersion = "1.10.13"
// Don't forget to bump versions in
// subprojects/base-services/src/main/java/org/gradle/internal/classanalysis/AsmConstants.java
// when upgrading ASM.
val asmVersion = "9.5"
val awsS3Version = "1.12.365"
val bouncycastleVersion = "1.68"
val jacksonVersion = "2.14.1"
val jaxbVersion = "3.0.0"
val junit5Version = "5.8.2"
val mavenVersion = "3.9.3"
val nativePlatformVersion = "0.22-milestone-25"
val slf4jVersion = "1.7.30"
val spockVersion = if (isBundleGroovy4) "2.3-groovy-4.0" else "2.3-groovy-3.0"
val tomljVersion = "1.0.0"

// test only
val archunitVersion = "1.0.0-rc1"
val bytebuddyVersion = "1.10.20"
val jettyVersion = "9.4.36.v20210114"
val sshdVersion = "2.0.0" // Upgrade requires changes in package names and tests fail on expectations (but work otherwise)

// For the junit-bom
javaPlatform.allowDependencies()

dependencies {
    api(platform("org.junit:junit-bom:${junit5Version}!!"))

    constraints {
        api(libs.ansiControlSequenceUtil) { version { strictly("0.3") }}
        api(libs.ant)                   { version { strictly(antVersion) }}
        api(libs.antLauncher)           { version { strictly(antVersion) }}
        api(libs.asm)                   { version { strictly(asmVersion) }}
        api(libs.asmAnalysis)           { version { strictly(asmVersion) }}
        api(libs.asmCommons)            { version { strictly(asmVersion) }}
        api(libs.asmTree)               { version { strictly(asmVersion) }}
        api(libs.asmUtil)               { version { strictly(asmVersion) }}
        api(libs.assertj)               { version { strictly("3.16.1") }}
        api(libs.awsS3Core)             { version { strictly(awsS3Version) }}
        api(libs.awsS3Kms)              { version { strictly(awsS3Version) }}
        api(libs.awsS3S3)               { version { strictly(awsS3Version) }}
        api(libs.awsS3Sts)              { version { strictly(awsS3Version) }}
        api(libs.bouncycastlePgp)       { version { strictly(bouncycastleVersion) }}
        api(libs.bouncycastlePkix)      { version { strictly(bouncycastleVersion) }}
        api(libs.bouncycastleProvider)  { version { strictly(bouncycastleVersion) }}
        api(libs.bsh)                   { version { strictly("2.0b6") }}
        api(libs.capsule)               { version { strictly("0.6.3") }}
        api(libs.commonsCodec)          { version { strictly("1.15") }}
        api(libs.commonsCompress)       { version { strictly("1.21") }}
        api(libs.commonsHttpclient)     { version { strictly("4.5.13") }}
        api(libs.commonsIo)             { version { strictly("2.11.0") }}
        api(libs.commonsLang)           { version { strictly("2.6") }}
        api(libs.commonsLang3)          { version { strictly("3.12.0") }}
        api(libs.commonsMath)           { version { strictly("3.6.1") }}
        api(libs.gradleProfiler)        { version { strictly("0.20.0") }}
        api(libs.gradleEnterpriseTestAnnotation) { version { strictly("1.0") }}
        api(libs.gcs)                   { version { strictly("v1-rev20220705-1.32.1") }}
        api(libs.googleApiClient)       { version { strictly("1.34.0"); because("our GCS version requires 1.34.0") }}
        api(libs.guava)                 { version { strictly("32.1.2-jre"); because("our Google API Client version requires at least 31.1-jre")  }}
        api(libs.googleHttpClientGson)  { version { strictly("1.42.2"); because("our Google API Client version requires 1.42.2")  }}
        api(libs.googleHttpClientApacheV2) { version { strictly("1.42.2"); because("our Google API Client version requires 1.42.2")  }}
        api(libs.googleHttpClient)      { version { strictly("1.42.2"); because("our Google API Client version requires 1.42.2") }}
        api(libs.googleOauthClient)     { version { strictly("1.34.1"); because("our Google API Client version requires 1.34.1") }}
        api(libs.groovy)                { version { strictly(libs.groovyVersion) }}
        api(libs.groovyAnt)             { version { strictly(libs.groovyVersion) }}
        api(libs.groovyAstbuilder)      { version { strictly(libs.groovyVersion) }}
        api(libs.groovyConsole)         { version { strictly(libs.groovyVersion) }}
        api(libs.groovySql)             { version { strictly(libs.groovyVersion) }}
        api(libs.groovyDatetime)        { version { strictly(libs.groovyVersion) }}
        api(libs.groovyDateUtil)        { version { strictly(libs.groovyVersion) }}
        api(libs.groovyNio)             { version { strictly(libs.groovyVersion) }}
        api(libs.groovyDoc)             { version { strictly(libs.groovyVersion) }}
        api(libs.groovyJson)            { version { strictly(libs.groovyVersion) }}
        api(libs.groovyTemplates)       { version { strictly(libs.groovyVersion) }}
        api(libs.groovyTest)            { version { strictly(libs.groovyVersion) }}
        api(libs.groovyXml)             { version { strictly(libs.groovyVersion) }}
        api(libs.gson)                  { version { strictly("2.8.9") }}
        api(libs.h2Database)            { version { strictly("2.2.220") }}
        api(libs.hamcrest)              { version { strictly("1.3"); because("2.x changes the API") }}
        api(libs.hikariCP)              { version { strictly("4.0.3"); because("5.x requires Java 11+") }}
        api(libs.httpcore)              { version { strictly("4.4.14") }}
        api(libs.inject)                { version { strictly("1") }}
        api(libs.ivy)                   { version { strictly("2.5.2") }}
        api(libs.jacksonAnnotations)    { version { strictly(jacksonVersion) }}
        api(libs.jacksonCore)           { version { strictly(jacksonVersion) }}
        api(libs.jacksonDatabind)       { version { strictly(jacksonVersion) }}
        api(libs.jacksonKotlin)         { version { strictly(jacksonVersion) }}
        api(libs.jakartaActivation)     { version { strictly("2.0.0") }}
        api(libs.jakartaXmlBind)        { version { strictly("3.0.0") }}
        api(libs.jansi)                 { version { strictly("1.18"); because("2.x changes the API") }}
        api(libs.jatl)                  { version { strictly("0.2.3") }}
        api(libs.javaPoet)              { version { strictly("1.13.0") } }
        api(libs.jaxbCore)              { version { strictly(jaxbVersion) }}
        api(libs.jaxbImpl)              { version { strictly(jaxbVersion) }}
        api(libs.jcifs)                 { version { strictly("1.3.17") }}
        api(libs.jclToSlf4j)            { version { strictly(slf4jVersion) }}
        api(libs.jcommander)            { version { strictly("1.78") }}
        api(libs.jetbrainsAnnotations)  { version { strictly("24.0.1") }}
        api(libs.jgit)                  { version { strictly("5.7.0.202003110725-r"); because("Upgrade has breaking API changes") }}
        api(libs.joda)                  { version { strictly("2.10.4") }}
        api(libs.joptSimple)            { version { strictly("5.0.4"); because("needed to create profiler in Gradle profiler API") }}
        api(libs.jsch)                  { version { strictly("0.1.55") }}
        api(libs.jsoup)                 { version { strictly("1.15.3") }}
        api(libs.jsr305)                { version { strictly("3.0.2") }}
        api(libs.julToSlf4j)            { version { strictly(slf4jVersion) }}
        api(libs.junit)                 { version { strictly("4.13.2") }}
        api(libs.junit5JupiterApi)      { version { strictly(junit5Version) }}
        api(libs.junit5Vintage)         { version { strictly(junit5Version) }}
        api(libs.junitPlatform)         { version { strictly("1.8.2") }}
        api(libs.jzlib)                 { version { strictly("1.1.3") }}
        api(libs.kryo)                  { version { strictly("2.24.0") }}
        api(libs.log4jToSlf4j)          { version { strictly(slf4jVersion) }}
        api(libs.maven3BuilderSupport)  { version { strictly(mavenVersion); because("required to load/build poms and repository settings") }}
        api(libs.maven3Model)           { version { strictly(mavenVersion); because("required to load/build poms and repository settings") }}
        api(libs.maven3RepositoryMetadata) { version { strictly(mavenVersion); because("required to load/build poms and repository settings") }}
        api(libs.maven3Settings)        { version { strictly(mavenVersion); because("required to load/build poms and repository settings") }}
        api(libs.maven3SettingsBuilder) { version { strictly(mavenVersion); because("required to load/build poms and repository settings") }}
        api(libs.minlog)                { version { strictly("1.2") }}
        api(libs.nativePlatform)        { version { strictly(nativePlatformVersion) }}
        api(libs.nativePlatformFileEvents) { version { strictly(nativePlatformVersion) }}
        api(libs.objenesis)             { version { strictly("2.6") }}
        api(libs.plexusCipher)          { version { strictly("2.0"); because("transitive dependency of Maven modules to process POM metadata") }}
        api(libs.plexusInterpolation)   { version { strictly("1.26"); because("transitive dependency of Maven modules to process POM metadata") }}
        api(libs.plexusSecDispatcher)   { version { strictly("2.0"); because("transitive dependency of Maven modules to process POM metadata") }}
        api(libs.plexusUtils)           { version { strictly("3.5.1"); because("transitive dependency of Maven modules to process POM metadata") }}
        api(libs.plist)                 { version { strictly("1.21") }}
        api(libs.servletApi)            { version { strictly("3.1.0") }}
        api(libs.slf4jApi)              { version { strictly(slf4jVersion) }}
        api(libs.snakeyaml)             { version { strictly("2.0") }}
        api(libs.testng)                { version { strictly("6.3.1"); because("later versions break test cross-version test filtering") }}
        api(libs.tomlj)                 { version { strictly(tomljVersion) }}
        api(libs.trove4j)               { version { strictly("1.0.20200330") }}
        api(libs.jna)                   { version { strictly("5.10.0") }}
        // TODO upgrade this AGP version to recent version
        api(libs.agp)                   { version { strictly("3.0.0"); because("We use 3.0.0 for internal performance test") }}
        api(libs.xbeanReflect)          { version { strictly("3.7") }}
        api(libs.xmlApis)               { version { strictly("1.4.01"); because("2.0.x has a POM with relocation Gradle does not handle well") }}

        // compile only
        api(libs.maven3Compat)          { version { strictly(mavenVersion); because("required for maven2gradle in init plugin") }}
        api(libs.maven3PluginApi)       { version { strictly(mavenVersion); because("required for maven2gradle in init plugin") }}

        // test only
        api(libs.aircompressor)         { version { strictly("0.8") }}
        api(libs.archunit)              { version { strictly(archunitVersion) }}
        api(libs.archunitJunit5)        { version { strictly(archunitVersion) }}
        api(libs.awaitility)            { version { strictly("3.1.6") }}
        api(libs.bytebuddy)             { version { strictly(bytebuddyVersion) }}
        api(libs.bytebuddyAgent)        { version { strictly(bytebuddyVersion) }}
        api(libs.cglib)                 { version { strictly("3.2.6") }}
        api(libs.compileTesting)        { version { strictly("0.21.0")}}
        api(libs.equalsverifier)        { version { strictly("2.1.6") }}
        api(libs.guice)                 { version { strictly("4.2.3") }}
        api(libs.httpmime)              { version { strictly("4.5.10") }}
        api(libs.javaParser)            { version { strictly("3.17.0") }}
        api(libs.jetty)                 { version { strictly(jettyVersion) }}
        api(libs.jettySecurity)         { version { strictly(jettyVersion) }}
        api(libs.jettyWebApp)           { version { strictly(jettyVersion) }}
        api(libs.jtar)                  { version { strictly("2.3") }}
        api(libs.kotlinCoroutines)      { version { strictly("1.5.2") }}
        api(libs.kotlinCoroutinesDebug) { version { strictly("1.5.2") }}
        api(libs.littleproxy)           { version { strictly("2.0.5") }}
        api(libs.mina)                  { version { strictly("2.0.17") }}
        api(libs.mockitoCore)           { version { strictly("3.7.7") }}
        api(libs.mockitoKotlin)         { version { strictly("1.6.0") }}
        api(libs.mockitoKotlin2)        { version { strictly("2.2.0") }}
        api(libs.mySqlConnector)        { version { strictly("8.0.17") }}
        api(libs.samplesCheck)          { version { strictly("1.0.0") }}
        api(libs.snappy)                { version { strictly("0.4") }}
        api(libs.socksProxy)            { version { strictly("2.0.0") }}
        api(libs.spock)                 { version { strictly(spockVersion) }}
        api(libs.spockJUnit4)           { version { strictly(spockVersion) }}
        api(libs.sshdCore)              { version { strictly(sshdVersion) }}
        api(libs.sshdScp)               { version { strictly(sshdVersion) }}
        api(libs.sshdSftp)              { version { strictly(sshdVersion) }}
        api(libs.testcontainersSpock)   { version { strictly("1.12.5") }}
        api(libs.typesafeConfig)        { version { strictly("1.3.3") }}
        api(libs.xerces)                { version { strictly("2.12.0") }}
        api(libs.xmlunit)               { version { strictly("1.6") }}
    }
}
