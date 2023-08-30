<img src=".github/cradle-thumbnail.png" alt="cradle" width="100%"/>

<div align="center">
  <img height="32" alt="download-badge" src=".github/cradle-download-badge.svg">
  <img height="32" alt="sync-badge" src=".github/cradle-sync-badge.svg">
</div>

<br/>

**A modified version of Gradle with added useful features.** As someone who frequently works with Gradle during the development process, I have encountered many minor frustrations along the way. That's why there's a fork here 🤷‍♂️

> [!IMPORTANT]
> This is an opinionated Gradle distribution that may not be suitable for everyone. If you have no complaints about the Gradle version you are currently using, there is no need for you to continue exploring this project :)

## TLDR?

- [Background - What's this?](#whats-this)
- [Features - What's the difference from Gradle?](#whats-the-difference-from-gradle)
- [Installation - How to use it?](#how-to-use-it)
- [Compatibility - Support the Gradle ecosystem?](#support-the-gradle-ecosystem)
- [Versioning - Following upstream updates?](#following-upstream-updates)
- [License - What license?](#what-license)
- [Acknowledgments](#acknowledgments)

## What's this?

This is a **Gradle distribution** called **Cradle**, which provides a set of modifications aimed at eliminating repetitive boilerplate code. If you play a lot with Gradle scripts, you're probably familiar with how much repetitive boilerplate code we often have to write 😵‍💫

Ah! Are you curious about why it's named **Cradle**? The inspiration for this name comes from the [initial naming idea of the Gradle project by its founder](https://discuss.gradle.org/t/why-is-gradle-called-gradle/3226/2), so I adopted it. Interestingly, he took the "**G**" from Groovy, while I took the "**C**" from my own ID. So, it's a playful "`git revert`" 😄

## What's the difference from Gradle?

#### [Shorthand for adding multiple dependencies to the same configuration](https://github.com/meowool/cradle/commit/7fa72a7c73590ef8540c07935ff2b3cf9c6e8a56)

In **Cradle**, you can do:

```kotlin
dependencies {
  implementationOf(
    "com.example:library:1.0.0",
    "foo.bar:common:0.1.0",
    "a.b:c:0.1.0" config {
      isTransitive = true
    },
  )
}
```

Instead of the verbose approach in **Gradle** :

<details>
  <summary><i>Click to see the equivalent code in Gradle.</i></summary>

```kotlin
dependencies {
  implementation("com.example:library:1.0.0")
  implementation("foo.bar:common:0.1.0")
  implementation("a.b:c:0.1.0") {
    isTransitive = true
  }
}
```

</details>

> [!NOTE]
> This shorthand is implemented by automatic generation to support any registered plugin configuration. For instance, if you have the `kapt` plugin, you can utilize `kaptOf` in a similar way. Likewise, other plugins such as `ksp` offer `kspOf`, and `shadow` provides `shadowOf`, and so forth.

#### [Directly add plugin dependencies from Version Catalog to the configuration](https://github.com/meowool/cradle/commit/7e0aa37c30a902c57ef979d95a8f31930615c480)

In **Cradle**, you can do:

```kotlin
dependencies {
  implementation(libs.plugins.test)
}
```

Instead of the verbose approach in **Gradle** :

<details>
  <summary><i>Click to see the equivalent code in Gradle.</i></summary>

```kotlin
dependencies {
  val plugin = libs.plugins.test.get()
  implementation("${plugin.pluginId}:${plugin.pluginId}.gradle.plugin:${plugin.version}")
}
```

</details>

#### [Support using Version Catalog dependencies as exclusion rules](https://github.com/meowool/cradle/commit/dd86f9c007b8dfb402c98d946bf761405130f106)

In **Cradle**, you can do:

```kotlin
dependencies {
  implementation("com.abc:efg:0.1.0") { exclude(libs.utils) }
}
```

Instead of the verbose approach in **Gradle** :

<details>
  <summary><i>Click to see the equivalent code in Gradle.</i></summary>

```kotlin
dependencies {
  val utils = libs.utils.get()
  implementation("com.abc:efg:0.1.0") {
    exclude(group = utils.group, module = utils.name)
    // or exclude(group = "my.test.library", module = "group")
  }
}
```

</details>

## How to use it?

Switching the Gradle to Cradle distribution in your project is a straightforward process. Simply run the following command in the root directory of your project:

<!--INSTALLATION-START-->

```bash
./gradlew wrapper --gradle-distribution-url=https://github.com/meowool/cradle/releases/download/v8.3.0.1/cradle-v8.3.0.1-bin.zip
```

<!--INSTALLATION-END-->

This command will update the `distributionUrl` property in the `gradle/wrapper/gradle-wrapper.properties` file of your project to point to the download link of the latest release of Cradle. Alternatively, you can manually update this property if you prefer.

>If you wish to use a different version of Cradle, simply modify the link after `--gradle-distribution-url=` to one of the available download links provided below:
>
><details>
>  <summary><i>Click to see all available builds of Cradle.</i></summary>
>
>| Version | Download Links | SHA-256 Checksums |
>| :-- | :-- | --- |
>| [**v8.3.0.1**](https://github.com/meowool/cradle/releases/tag/v8.3.0.1) | <b>-</b> <https://github.com/meowool/cradle/releases/download/v8.3.0.1/cradle-v8.3.0.1-bin.zip><br /><b>-</b> <https://github.com/meowool/cradle/releases/download/v8.3.0.1/cradle-v8.3.0.1-all.zip> | <b>-</b> [_bin:_](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/wrapper/Wrapper.DistributionType.html#BIN) `PLACEHOLDER`<br /><b>-</b> [_all:_](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/wrapper/Wrapper.DistributionType.html#ALL) `PLACEHOLDER` |
>
>**Note:** If you'd like to use a bleeding edge nightly build version, please visit the [Actions ↗](https://github.com/meowool/cradle/actions/workflows/nightly.yml) page to find the latest build and manually copy the download link for its artifact.
>
></details>
>
>------
>
>**Important:** For information on the relationship between Cradle versions and Gradle versions, please refer to [Following upstream updates?](#following-upstream-updates)

## Support the Gradle ecosystem?

#### Can I still enjoy all the features of the Gradle ecosystem after I switch to Cradle?

> Yes, absolutely! Cradle is just a **fork** of Gradle, which means it is fully compatible with the Gradle ecosystem. After switching to Cradle, you can continue to enjoy all the benefits and features provided by the Gradle ecosystem without any limitations.
>

####  What about Renovate?

> TODO
>

## Following upstream updates?

Sure! Our goal is to stay up-to-date with the latest changes from the Gradle project. While we strive to align our updates with the upstream repository, please note that Cradle is an independent distribution and may not always sync immediately with new upstream releases. In the worst case, it may take a few working days to catch up 😢. However, this is rare as Cradle syncs frequently thanks to our bot! It merges changes within hours of a new Gradle version release, ensuring timely releases based on the latest upstream changes!

#### What's the relationship between the Cradle and Gradle versions?

> [!NOTE]
> You may have noticed that the Cradle and Gradle versions are very similar, but the fork version has an additional version component at the end, such as **Gradle version `8.3.0`** and **Cradle version `8.3.0.1`**. This additional component is used to indicate the patch version of this fork.

## What license?

Cradle like Gradle, also uses the [Apache-2.0](./LICENSE) license as its project license.

## Acknowledgments

- [Poe ChatGPT](https://poe.com/ChatGPT) for the excellent README, which was largely written by it.
- [Ideogram AI](https://ideogram.ai/) for providing the inspiration for the project's logo through its generated sketches.
- [Gradle](https://gradle.org/) for providing an excellent build tool that I use every day :)
