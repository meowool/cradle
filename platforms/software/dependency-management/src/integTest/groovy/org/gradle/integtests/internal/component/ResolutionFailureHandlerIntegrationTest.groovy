/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.integtests.internal.component

import org.gradle.api.internal.artifacts.transform.AmbiguousArtifactTransformException
import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.ToBeFixedForConfigurationCache
import org.gradle.internal.component.AbstractVariantSelectionException
import org.gradle.internal.component.AmbiguousArtifactVariantsException
import org.gradle.internal.component.AmbiguousGraphVariantsException
import org.gradle.internal.component.ConfigurationNotFoundException
import org.gradle.internal.component.ExternalConfigurationNotFoundException
import org.gradle.internal.component.IncompatibleArtifactVariantsException
import org.gradle.internal.component.IncompatibleGraphVariantsException
import org.gradle.internal.component.NoMatchingArtifactVariantsException
import org.gradle.internal.component.NoMatchingGraphVariantsException
import org.gradle.test.fixtures.dsl.GradleDsl
import org.gradle.test.fixtures.file.TestFile
import org.gradle.util.GradleVersion
import spock.lang.Ignore

/**
 * These tests demonstrate the behavior of the [ResolutionFailureHandler] when a project has various
 * variant selection failures.
 *
 * It can also build a text report demonstrating all these errors in a single place by running
 * the [generateFailureShowcase] method, which is marked with [spock.lang.Ignore] so it doesn't
 * run as part of a typical test run.  It is useful for viewing and comparing the behavior of
 * different types of failures.
 */
class ResolutionFailureHandlerIntegrationTest extends AbstractIntegrationSpec {
    // region resolution failures
    // region Graph Variant failures
    def "demonstrate ambiguous graph variant selection failure for project"() {
        ambiguousGraphVariantForProject.prepare()

        expect:
        assertResolutionFailsWithException(ambiguousGraphVariantForProject.exception)

        and: "Has error output"
        failure.assertHasDescription("Could not determine the dependencies of task ':forceResolution'.")
        failure.assertHasCause("Could not resolve all task dependencies for configuration ':resolveMe'.")
        failure.assertHasCause("Could not resolve project :.")
        assertFullMessageCorrect("""      > The consumer was configured to find attribute 'color' with value 'blue'. However we cannot choose between the following variants of project ::
          - blueRoundElements
          - blueSquareElements
        All of them match the consumer attributes:
          - Variant 'blueRoundElements' capability :${temporaryFolder.getTestDirectory().getName()}:unspecified declares attribute 'color' with value 'blue':
              - Unmatched attribute:
                  - Provides shape 'round' but the consumer didn't ask for it
          - Variant 'blueSquareElements' capability :${temporaryFolder.getTestDirectory().getName()}:unspecified declares attribute 'color' with value 'blue':
              - Unmatched attribute:
                  - Provides shape 'square' but the consumer didn't ask for it""")
        and: "Helpful resolutions are provided"
        assertSuggestsReviewingAlgorithm()
        assertSuggestsViewingDocs("Ambiguity errors are explained in more detail at https://docs.gradle.org/${GradleVersion.current().version}/userguide/variant_model.html#sub:variant-ambiguity.")
    }

    @ToBeFixedForConfigurationCache
    def "demonstrate ambiguous graph variant selection failure for externalDep"() {
        ambiguousGraphVariantForExternalDep.prepare()

        expect:
        assertResolutionFailsWithException(ambiguousGraphVariantForExternalDep.exception)

        and: "Has error output"
        failure.assertHasDescription("Execution failed for task ':forceResolution'")
        failure.assertHasCause("Could not resolve all files for configuration ':resolveMe'.")
        failure.assertHasCause("Could not resolve com.squareup.okhttp3:okhttp:4.4.0.")
        assertFullMessageCorrect("""   > Could not resolve com.squareup.okhttp3:okhttp:4.4.0.
     Required by:
         project :
      > The consumer was configured to find attribute 'org.gradle.category' with value 'documentation'. However we cannot choose between the following variants of com.squareup.okhttp3:okhttp:4.4.0:
          - javadocElements
          - sourcesElements
        All of them match the consumer attributes:
          - Variant 'javadocElements' capability com.squareup.okhttp3:okhttp:4.4.0 declares attribute 'org.gradle.category' with value 'documentation':
              - Unmatched attributes:
                  - Provides org.gradle.dependency.bundling 'external' but the consumer didn't ask for it
                  - Provides org.gradle.docstype 'javadoc' but the consumer didn't ask for it
                  - Provides org.gradle.status 'release' but the consumer didn't ask for it
                  - Provides org.gradle.usage 'java-runtime' but the consumer didn't ask for it
          - Variant 'sourcesElements' capability com.squareup.okhttp3:okhttp:4.4.0 declares attribute 'org.gradle.category' with value 'documentation':
              - Unmatched attributes:
                  - Provides org.gradle.dependency.bundling 'external' but the consumer didn't ask for it
                  - Provides org.gradle.docstype 'sources' but the consumer didn't ask for it
                  - Provides org.gradle.status 'release' but the consumer didn't ask for it
                  - Provides org.gradle.usage 'java-runtime' but the consumer didn't ask for it
        The following variants were also considered but didn't match the requested attributes:
          - Variant 'apiElements' capability com.squareup.okhttp3:okhttp:4.4.0:
              - Incompatible because this component declares attribute 'org.gradle.category' with value 'library' and the consumer needed attribute 'org.gradle.category' with value 'documentation'
          - Variant 'runtimeElements' capability com.squareup.okhttp3:okhttp:4.4.0:
              - Incompatible because this component declares attribute 'org.gradle.category' with value 'library' and the consumer needed attribute 'org.gradle.category' with value 'documentation'""")

        and: "Helpful resolutions are provided"
        assertSuggestsReviewingAlgorithm()
        assertSuggestsViewingDocs("Ambiguity errors are explained in more detail at https://docs.gradle.org/${GradleVersion.current().version}/userguide/variant_model.html#sub:variant-ambiguity.")
    }

    def "demonstrate no matching graph variants selection failure for project"() {
        noMatchingGraphVariantsForProject.prepare()

        expect:
        assertResolutionFailsWithException(noMatchingGraphVariantsForProject.exception)

        and: "Has error output"
        failure.assertHasDescription("Could not determine the dependencies of task ':forceResolution'.")
        failure.assertHasCause("Could not resolve all task dependencies for configuration ':resolveMe'.")
        failure.assertHasCause("Could not resolve project :.")
        assertFullMessageCorrect("""   > Could not resolve project :.
     Required by:
         project :
      > No matching variant of project : was found. The consumer was configured to find attribute 'color' with value 'green' but:
          - Variant 'default' capability :${temporaryFolder.getTestDirectory().getName()}:unspecified:
              - Incompatible because this component declares attribute 'color' with value 'blue' and the consumer needed attribute 'color' with value 'green'""")

        and: "Helpful resolutions are provided"
        assertSuggestsReviewingAlgorithm()
        assertSuggestsViewingDocs("No matching variant errors are explained in more detail at https://docs.gradle.org/${GradleVersion.current().version}/userguide/variant_model.html#sub:variant-no-match.")
    }

    @ToBeFixedForConfigurationCache
    def "demonstrate no matching graph variants selection failure for externalDep"() {
        noMatchingGraphVariantsForExternalDep.prepare()

        expect:
        assertResolutionFailsWithException(noMatchingGraphVariantsForExternalDep.exception)

        and: "Has error output"
        failure.assertHasDescription("Execution failed for task ':forceResolution'.")
        failure.assertHasCause("Could not resolve all files for configuration ':resolveMe'.")
        failure.assertHasCause("Could not resolve com.squareup.okhttp3:okhttp:4.4.0.")
        assertFullMessageCorrect("""      > No matching variant of com.squareup.okhttp3:okhttp:4.4.0 was found. The consumer was configured to find attribute 'org.gradle.category' with value 'non-existent-format' but:
          - Variant 'apiElements' capability com.squareup.okhttp3:okhttp:4.4.0:
              - Incompatible because this component declares attribute 'org.gradle.category' with value 'library' and the consumer needed attribute 'org.gradle.category' with value 'non-existent-format'
          - Variant 'javadocElements' capability com.squareup.okhttp3:okhttp:4.4.0:
              - Incompatible because this component declares attribute 'org.gradle.category' with value 'documentation' and the consumer needed attribute 'org.gradle.category' with value 'non-existent-format'
          - Variant 'runtimeElements' capability com.squareup.okhttp3:okhttp:4.4.0:
              - Incompatible because this component declares attribute 'org.gradle.category' with value 'library' and the consumer needed attribute 'org.gradle.category' with value 'non-existent-format'
          - Variant 'sourcesElements' capability com.squareup.okhttp3:okhttp:4.4.0:
              - Incompatible because this component declares attribute 'org.gradle.category' with value 'documentation' and the consumer needed attribute 'org.gradle.category' with value 'non-existent-format'""")

        and: "Helpful resolutions are provided"
        assertSuggestsReviewingAlgorithm()
        assertSuggestsViewingDocs("No matching variant errors are explained in more detail at https://docs.gradle.org/${GradleVersion.current().version}/userguide/variant_model.html#sub:variant-no-match.")
    }

    def "demonstrate incompatible graph variants selection failure"() {
        incompatibleGraphVariants.prepare()

        expect:
        assertResolutionFailsWithException(incompatibleGraphVariants.exception)

        and: "Has error output"
        failure.assertHasDescription("Could not determine the dependencies of task ':forceResolution'.")
        failure.assertHasCause("Could not resolve all task dependencies for configuration ':resolveMe'.")
        failure.assertHasCause("Could not resolve project :.")
        assertFullMessageCorrect("""     Required by:
         project :
      > Configuration 'mismatch' in project : does not match the consumer attributes
        Configuration 'mismatch':
          - Incompatible because this component declares attribute 'color' with value 'blue' and the consumer needed attribute 'color' with value 'green'
""")

        and: "Helpful resolutions are provided"
        assertSuggestsReviewingAlgorithm()
        assertSuggestsViewingDocs("Incompatible variant errors are explained in more detail at https://docs.gradle.org/${GradleVersion.current().version}/userguide/variant_model.html#sub:variant-incompatible.")
    }

    // region Configuration requested by name
    def "demonstrate configuration not found selection failure"() {
        configurationNotFound.prepare()

        expect:
        assertResolutionFailsWithException(configurationNotFound.exception)

        and: "Has error output"
        failure.assertHasDescription("Could not determine the dependencies of task ':forceResolution'.")
        failure.assertHasCause("Could not resolve all task dependencies for configuration ':resolveMe'.")
        failure.assertHasCause("Could not resolve project :.")
        assertFullMessageCorrect("""     Required by:
         project :
      > A dependency was declared on configuration 'absent' which is not declared in the descriptor for project :.""")

        and: "Helpful resolutions are provided"
        assertSuggestsReviewingAlgorithm()
        // TODO: Nothing specific here
    }

    @Ignore("Is the configuration key in the dependency map just not used for Maven deps?  Should this be an error?")
    def "demonstrate external configuration not found selection failure"() {
        externalConfigurationNotFound.prepare()

        expect:
        assertResolutionFailsWithException(externalConfigurationNotFound.exception)

        and: "Has error output"
        failure.assertHasDescription("Could not determine the dependencies of task ':forceResolution'.")
        failure.assertHasCause("Could not resolve all task dependencies for configuration ':resolveMe'.")
        failure.assertHasCause("Could not resolve project :.")
        assertFullMessageCorrect("A dependency was declared on configuration 'absent' which is not declared in the descriptor for project :.")

        and: "Helpful resolutions are provided"
        assertSuggestsReviewingAlgorithm()
        // TODO: Nothing specific here
    }

    // endregion Configuration requested by name
    // endregion Graph Variant failures

    // region Artifact Variant failures
    def "demonstrate incompatible artifact variants exception"() {
        incompatibleArtifactVariants.prepare()

        expect:
        assertResolutionFailsWithException(incompatibleArtifactVariants.exception)

        and: "Has error output"
        failure.assertHasDescription("Could not determine the dependencies of task ':forceResolution'.")
        failure.assertHasCause("Could not resolve all task dependencies for configuration ':resolveMe'.")
        failure.assertHasCause("Could not resolve project :.")
        assertFullMessageCorrect("""     Required by:
         project :
      > Multiple incompatible variants of org.example:${temporaryFolder.getTestDirectory().getName()}:1.0 were selected:
           - Variant org.example:${temporaryFolder.getTestDirectory().getName()}:1.0 variant blueElementsCapability1 has attributes {color=blue}
           - Variant org.example:${temporaryFolder.getTestDirectory().getName()}:1.0 variant greenElementsCapability2 has attributes {color=green}""")

        and: "Helpful resolutions are provided"
        assertSuggestsReviewingAlgorithm()
        assertSuggestsViewingDocs("Incompatible variant errors are explained in more detail at https://docs.gradle.org/${GradleVersion.current().version}/userguide/variant_model.html#sub:variant-incompatible.")
    }

    def "demonstrate no matching artifact variants exception"() {
        noMatchingArtifactVariants.prepare()

        expect:
        assertResolutionFailsWithException(noMatchingArtifactVariants.exception)

        and: "Has error output"
        failure.assertHasDescription("Could not determine the dependencies of task ':forceResolution'.")
        failure.assertHasCause("Could not resolve all task dependencies for configuration ':resolveMe'.")
        assertFullMessageCorrect("""   > No variants of project : match the consumer attributes:
       - Configuration ':myElements' declares attribute 'color' with value 'blue':
           - Incompatible because this component declares attribute 'artifactType' with value 'jar' and the consumer needed attribute 'artifactType' with value 'dll'
       - Configuration ':myElements' variant secondary declares attribute 'color' with value 'blue':
           - Incompatible because this component declares attribute 'artifactType' with value 'jar' and the consumer needed attribute 'artifactType' with value 'dll'""")

        and: "Helpful resolutions are provided"
        assertSuggestsReviewingAlgorithm()
        assertSuggestsViewingDocs("No matching variant errors are explained in more detail at https://docs.gradle.org/${GradleVersion.current().version}/userguide/variant_model.html#sub:variant-no-match.")
    }

    def "demonstrate ambiguous artifact transforms exception"() {
        ambiguousArtifactTransforms.prepare()

        expect:
        assertResolutionFailsWithException(ambiguousArtifactTransforms.exception)

        and: "Has error output"
        failure.assertHasDescription("Could not determine the dependencies of task ':forceResolution'.")
        failure.assertHasCause("Could not resolve all task dependencies for configuration ':resolveMe'.")
        assertFullMessageCorrect("""   > Found multiple transforms that can produce a variant of project : with requested attributes:
       - color 'red'
       - shape 'round'
     Found the following transforms:
       - From 'configuration ':roundBlueLiquidElements'':
           - With source attributes:
               - color 'blue'
               - shape 'round'
               - state 'liquid'
           - Candidate transform(s):
               - Transform 'BrokenTransform' producing attributes:
                   - color 'red'
                   - shape 'round'
                   - state 'gas'
               - Transform 'BrokenTransform' producing attributes:
                   - color 'red'
                   - shape 'round'
                   - state 'solid'""")

        and: "Helpful resolutions are provided"
        assertSuggestsReviewingAlgorithm()
        assertSuggestsViewingDocs("Transformation failures are explained in more detail at https://docs.gradle.org/${GradleVersion.current().version}/userguide/variant_model.html#sub:transform-ambiguity.")
    }

    def "demonstrate ambiguous artifact variants exception"() {
        ambiguousArtifactVariants.prepare()

        expect:
        assertResolutionFailsWithException(ambiguousArtifactVariants.exception)

        and: "Has error output"
        failure.assertHasDescription("Could not determine the dependencies of task ':forceResolution'.")
        failure.assertHasCause("Could not resolve all task dependencies for configuration ':resolveMe'.")
        assertFullMessageCorrect("""   > More than one variant of project : matches the consumer attributes:
       - Configuration ':default' variant v1
       - Configuration ':default' variant v2""")

        and: "Helpful resolutions are provided"
        assertSuggestsReviewingAlgorithm()
        assertSuggestsViewingDocs("Ambiguity errors are explained in more detail at https://docs.gradle.org/${GradleVersion.current().version}/userguide/variant_model.html#sub:variant-ambiguity.")
    }
    // endregion Artifact Variant failures
    // endregion resolution failures

    // region dependencyInsight failures
    /**
     * Running the dependencyInsight report can also generate a variant selection failure, but this
     * does <strong>NOT</strong> cause the task to fail.
     */
    def "demonstrate dependencyInsight report no matching capabilities failure"() {
        setupDependencyInsightFailure()

        expect:
        succeeds "dependencyInsight", "--configuration", "compileClasspath", "--dependency", "gson"

        String basicOutput = """   Failures:
      - Could not resolve com.google.code.gson:gson:2.8.5."""
        String fullOutput = """          - Unable to find a variant of com.google.code.gson:gson:2.8.5 providing the requested capability com.google.code.gson:gson-test-fixtures:
               - Variant compile provides com.google.code.gson:gson:2.8.5
               - Variant runtime provides com.google.code.gson:gson:2.8.5
               - Variant sources provides com.google.code.gson:gson:2.8.5
               - Variant javadoc provides com.google.code.gson:gson:2.8.5
               - Variant platform-compile provides com.google.code.gson:gson-derived-platform:2.8.5
               - Variant platform-runtime provides com.google.code.gson:gson-derived-platform:2.8.5
               - Variant enforced-platform-compile provides com.google.code.gson:gson-derived-enforced-platform:2.8.5
               - Variant enforced-platform-runtime provides com.google.code.gson:gson-derived-enforced-platform:2.8.5"""

        outputContains(basicOutput)
        outputContains(fullOutput)
    }
    // endregion dependencyInsight failures

    // region error showcase
    @spock.lang.Ignore("This test is used to generate a summary of all possible errors, it shouldn't usually be run as part of testing")
    def "generate resolution failure showcase report"() {
        given:
        // Escape to the root of the dependency-management project, to put these reports in build output dir so it isn't auto-deleted when test completes
        File reportFile = testDirectory.parentFile.parentFile.parentFile.parentFile.file("reports/tests/resolution-failure-showcase.txt")

        when:
        generateFailureShowcase(reportFile)

        then:
        assertAllExceptionsInShowcase(reportFile)

        println("Resolution error showcase report available at: ${reportFile.toURI()}")
    }

    private void generateFailureShowcase(TestFile reportFile) {
        StringBuilder reportBuilder = new StringBuilder()
        demonstrations.each {
            buildKotlinFile.text = ""
            it.prepare()

            fails "forceResolution"

            reportBuilder.append("----------------------------------------------------------------------------------------------------\n")
            reportBuilder.append("${it.name}\n")
            reportBuilder.append(result.getError())
            reportBuilder.append("\n")
        }

        reportFile.text = reportBuilder.toString()
    }

    private void assertAllExceptionsInShowcase(TestFile reportFile) {
        String report = reportFile.text
        demonstrations.each {
            report.contains(it.name)
            report.contains("Caused by: " + it.exception.getName())
        }
    }

    private static class Demonstration {
        private final String name
        private final Class<AbstractVariantSelectionException> exception
        private final Closure setup

        private Demonstration(String name, Class<AbstractVariantSelectionException> exception, Closure setup) {
            this.name = name
            this.exception = exception
            this.setup = setup
        }

        void prepare() {
            setup.call()
        }
    }

    private final Demonstration ambiguousGraphVariantForProject = new Demonstration("Ambiguous graph variant (project)", AmbiguousGraphVariantsException.class, this.&setupAmbiguousGraphVariantFailureForProject)
    private final Demonstration ambiguousGraphVariantForExternalDep = new Demonstration("Ambiguous graph variant (external)", AmbiguousGraphVariantsException.class, this.&setupAmbiguousGraphVariantFailureForExternalDep)
    private final Demonstration noMatchingGraphVariantsForProject = new Demonstration("No matching graph variants (project dependency)", NoMatchingGraphVariantsException.class, this.&setupNoMatchingGraphVariantsFailureForProject)
    private final Demonstration noMatchingGraphVariantsForExternalDep = new Demonstration("No matching graph variants (external dependency)", NoMatchingGraphVariantsException.class, this.&setupNoMatchingGraphVariantsFailureForExternalDep)
    private final Demonstration incompatibleGraphVariants = new Demonstration("Incompatible graph variants", IncompatibleGraphVariantsException.class, this.&setupIncompatibleGraphVariantsFailureForProject)

    private final Demonstration configurationNotFound = new Demonstration("Configuration not found", ConfigurationNotFoundException.class, this.&setupConfigurationNotFound)
    private final Demonstration externalConfigurationNotFound = new Demonstration("External configuration not found", ExternalConfigurationNotFoundException.class, this.&setupExternalConfigurationNotFound)

    private final Demonstration incompatibleArtifactVariants = new Demonstration("Incompatible artifact variants", IncompatibleArtifactVariantsException.class, this.&setupIncompatibleArtifactVariantsFailureForProject)
    private final Demonstration noMatchingArtifactVariants = new Demonstration("No matching artifact variants", NoMatchingArtifactVariantsException.class, this.&setupNoMatchingArtifactVariantsFailureForProject)
    private final Demonstration ambiguousArtifactTransforms = new Demonstration("Ambiguous artifact transforms", AmbiguousArtifactTransformException.class, this.&setupAmbiguousArtifactTransformFailureForProject)
    private final Demonstration ambiguousArtifactVariants = new Demonstration("Ambiguous artifact variants", AmbiguousArtifactVariantsException.class, this.&setupAmbiguousArtifactVariantsFailureForProject)

    private final List<Demonstration> demonstrations = [
        ambiguousGraphVariantForProject,
        ambiguousGraphVariantForExternalDep,
        noMatchingGraphVariantsForProject,
        noMatchingGraphVariantsForExternalDep,
        incompatibleGraphVariants,
        incompatibleArtifactVariants,
        noMatchingArtifactVariants,
        ambiguousArtifactTransforms,
        ambiguousArtifactVariants,
        configurationNotFound
    ]
    // endregion error showcase

    // region setup
    private void setupExternalConfigurationNotFound() {
        buildKotlinFile << """
            ${mavenCentralRepository(GradleDsl.KOTLIN)}

            configurations {
                dependencyScope("myLibs")

                resolvable("resolveMe") {
                    extendsFrom(configurations.getByName("myLibs"))
                }
            }

            dependencies {
                add("myLibs", module(mapOf("group" to "com.squareup.okhttp3", "name" to "okhttp", "version" to "4.4.0", "configuration" to "absent")))
            }

            ${forceConsumerResolution()}
        """
    }

    private void setupConfigurationNotFound() {
        buildKotlinFile << """
            configurations {
                dependencyScope("myDependencies")

                resolvable("resolveMe") {
                    extendsFrom(configurations.getByName("myDependencies"))
                }
            }

            dependencies {
                add("myDependencies", project(":", "absent"))
            }

            ${forceConsumerResolution()}
        """
    }

    private void setupAmbiguousArtifactVariantsFailureForProject() {
        buildKotlinFile << """
            configurations {
                consumable("default") {
                    outgoing {
                        variants {
                            val v1 by creating { }
                            val v2 by creating { }
                        }
                    }
                }

                dependencyScope("myDependencies")

                resolvable("resolveMe") {
                    extendsFrom(configurations.getByName("myDependencies"))
                }
            }

            dependencies {
                add("myDependencies", project(":"))
            }

            ${forceConsumerResolution()}
        """
    }

    private void setupAmbiguousArtifactTransformFailureForProject() {
        buildKotlinFile <<  """
            val color = Attribute.of("color", String::class.java)
            val shape = Attribute.of("shape", String::class.java)
            val matter = Attribute.of("state", String::class.java)

            configurations {
                consumable("roundBlueLiquidElements") {
                    attributes.attribute(shape, "round")
                    attributes.attribute(color, "blue")
                    attributes.attribute(matter, "liquid")
                }

                dependencyScope("myDependencies")

                resolvable("resolveMe") {
                    extendsFrom(configurations.getByName("myDependencies"))
                    // Initially request only round
                    attributes.attribute(shape, "round")
                }
            }

            abstract class BrokenTransform : TransformAction<TransformParameters.None> {
                override fun transform(outputs: TransformOutputs) {
                    throw AssertionError("Should not actually be selected to run")
                }
            }

            dependencies {
                add("myDependencies", project(":"))

                // Register 2 transforms that both will move blue -> red, but also do
                // something else to another irrelevant attribute in order to make them
                // unique from each other
                registerTransform(BrokenTransform::class.java) {
                    from.attribute(color, "blue")
                    to.attribute(color, "red")
                    from.attribute(matter, "liquid")
                    to.attribute(matter, "solid")
                }
                registerTransform(BrokenTransform::class.java) {
                    from.attribute(color, "blue")
                    to.attribute(color, "red")
                    from.attribute(matter, "liquid")
                    to.attribute(matter, "gas")
                }
            }

            val forceResolution by tasks.registering {
                inputs.files(configurations.getByName("resolveMe").incoming.artifactView {
                    attributes.attribute(color, "red")
                }.artifacts.artifactFiles)

                doLast {
                    inputs.files.files.forEach { println(it) }
                }
            }
        """
    }

    private void setupNoMatchingArtifactVariantsFailureForProject() {
        buildKotlinFile << """
            val artifactType = Attribute.of("artifactType", String::class.java)
            val color = Attribute.of("color", String::class.java)

            configurations {
                consumable("myElements") {
                    attributes.attribute(color, "blue")

                    outgoing {
                        variants {
                            val secondary by creating {
                                // Without artifacts on the variant, we would get a AmbiguousArtifactVariantsException - need a mismatch with the derived artifact type of "jar"
                                artifact(file("secondary.jar"))
                            }
                        }

                        artifacts {
                            artifact(file("implicit.jar"))
                        }
                    }
                }
            }

            configurations {
                dependencyScope("myDependencies")

                resolvable("resolveMe") {
                    extendsFrom(configurations.getByName("myDependencies"))

                    // We need to match the "myElements" configuration, then the AV will look at its variants
                    attributes.attribute(color, "blue")
                    // Without requesting this special attribute that mismatches the derived value of "jar", we would get a successful result that pulls implicit.jar
                    attributes.attribute(artifactType, "dll")
                }
            }

            dependencies {
                add("myDependencies", project(":"))
            }

            ${forceConsumerResolution()}
        """
    }

    private void setupAmbiguousGraphVariantFailureForProject() {
        buildKotlinFile <<  """
            val color = Attribute.of("color", String::class.java)
            val shape = Attribute.of("shape", String::class.java)

            configurations {
                consumable("blueRoundElements") {
                    attributes.attribute(color, "blue")
                    attributes.attribute(shape, "round")
                }
                consumable("blueSquareElements") {
                    attributes.attribute(color, "blue")
                    attributes.attribute(shape, "square")
                }

                dependencyScope("blueFilesDependencies")

                resolvable("resolveMe") {
                    extendsFrom(configurations.getByName("blueFilesDependencies"))
                    attributes.attribute(color, "blue")
                }
            }

            dependencies {
                add("blueFilesDependencies", project(":"))
            }

            ${forceConsumerResolution()}
        """
    }

    private void setupAmbiguousGraphVariantFailureForExternalDep() {
        buildKotlinFile <<  """
            ${mavenCentralRepository(GradleDsl.KOTLIN)}

            configurations {
                dependencyScope("myLibs")

                resolvable("resolveMe") {
                    extendsFrom(configurations.getByName("myLibs"))
                    attributes {
                        attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category::class.java, Category.DOCUMENTATION))
                    }
                }
            }

            dependencies {
                add("myLibs", "com.squareup.okhttp3:okhttp:4.4.0")
            }

            ${forceConsumerResolution()}
        """
    }

    private void setupNoMatchingGraphVariantsFailureForProject() {
        buildKotlinFile << """
            plugins {
                id("base")
            }

            val color = Attribute.of("color", String::class.java)

            configurations {
                val default by getting {
                    attributes.attribute(color, "blue")
                }

                dependencyScope("defaultDependencies")

                resolvable("resolveMe") {
                    extendsFrom(configurations.getByName("defaultDependencies"))
                    attributes.attribute(color, "green")
                }
            }

            dependencies {
                add("defaultDependencies", project(":"))
            }

            ${forceConsumerResolution()}
        """
    }

    private void setupNoMatchingGraphVariantsFailureForExternalDep() {
        buildKotlinFile <<  """
            ${mavenCentralRepository(GradleDsl.KOTLIN)}

            configurations {
                dependencyScope("myLibs")

                resolvable("resolveMe") {
                    extendsFrom(configurations.getByName("myLibs"))
                    attributes {
                        attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category::class.java, "non-existent-format"))
                    }
                }
            }

            dependencies {
                add("myLibs", "com.squareup.okhttp3:okhttp:4.4.0")
            }

            ${forceConsumerResolution()}
        """
    }

    private void setupIncompatibleGraphVariantsFailureForProject() {
        buildKotlinFile <<  """
            plugins {
                id("base")
            }

            val color = Attribute.of("color", String::class.java)

            configurations {
                val mismatch by configurations.creating {
                    attributes.attribute(color, "blue")
                }

                dependencyScope("defaultDependencies")

                resolvable("resolveMe") {
                    extendsFrom(configurations.getByName("defaultDependencies"))
                    attributes.attribute(color, "green")
                }
            }

            dependencies {
                add("defaultDependencies", project(":", "mismatch"))
            }

            ${forceConsumerResolution()}
        """
    }

    private void setupIncompatibleArtifactVariantsFailureForProject() {
        buildKotlinFile <<  """
            group = "org.example"
            version = "1.0"

            val color = Attribute.of("color", String::class.java)

            // TODO: Can't use dependencyScope here yet, as it doesn't support capabilities
            val incompatible: Configuration by configurations.creating {
                isCanBeDeclared = true
                isCanBeConsumed = false
                isCanBeResolved = false
            }

            configurations {
                consumable("blueElementsCapability1") {
                    attributes.attribute(color, "blue")
                    outgoing {
                        capability("org.example:cap1:1.0")
                    }
                }
                consumable("blueElementsCapability2") {
                    attributes.attribute(color, "blue")
                    outgoing {
                        capability("org.example:cap2:1.0")
                    }
                }

                consumable("greenElementsCapability1") {
                    attributes.attribute(color, "green")
                    outgoing {
                        capability("org.example:cap1:1.0")
                    }
                }
                consumable("greenElementsCapability2") {
                    attributes.attribute(color, "green")
                    outgoing {
                        capability("org.example:cap2:1.0")
                    }
                }

                resolvable("resolveMe") {
                    extendsFrom(incompatible)
                }
            }

            dependencies {
                add("incompatible", project(":")) {
                    attributes {
                        attribute(color, "blue")
                    }
                    capabilities {
                        requireCapability("org.example:cap1:1.0")
                    }
                }
                add("incompatible", project(":")) {
                    attributes {
                        attribute(color, "green")
                    }
                    capabilities {
                        requireCapability("org.example:cap2:1.0")
                    }
                }
            }

            ${forceConsumerResolution()}
        """
    }

    private void setupDependencyInsightFailure() {
        buildKotlinFile <<  """
            plugins {
                `java-library`
                `java-test-fixtures`
            }

            ${mavenCentralRepository(GradleDsl.KOTLIN)}

            dependencies {
                // Adds a dependency on the test fixtures of Gson, however this
                // project doesn't publish such a thing
                implementation(testFixtures("com.google.code.gson:gson:2.8.5"))
            }
        """

    }

    private String forceConsumerResolution() {
        return """
            abstract class ForceResolution : DefaultTask() {
                @get:InputFiles
                abstract val resolvedFiles: ConfigurableFileCollection
            }

            val forceResolution by tasks.registering(ForceResolution::class) {
                resolvedFiles.from(configurations.getByName("resolveMe"))
                doLast {
                    resolvedFiles.forEach { println(it) }
                }
            }
        """
    }
    // endregion setup

    private void assertFullMessageCorrect(String identifyingFragment) {
        failure.assertHasErrorOutput(identifyingFragment)
    }

    private void assertSuggestsReviewingAlgorithm() {
        assertSuggestsViewingDocs("Review the variant matching algorithm at https://docs.gradle.org/${GradleVersion.current().version}/userguide/variant_attributes.html#sec:abm_algorithm.")
    }

    private void assertSuggestsViewingDocs(String resolution) {
        failure.assertHasResolution(resolution)
    }

    private void assertResolutionFailsWithException(Class<? extends RuntimeException> exception) {
        fails "forceResolution", "--stacktrace"
        failure.assertHasErrorOutput("Caused by: " + exception.getName())
    }
}
