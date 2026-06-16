plugins {
    kotlin("jvm")
    id("maven-publish")
    id("com.gradleup.shadow")
    id("gg.essential.multi-version")
    id("gg.essential.defaults")
}

group = "org.zephy.zkeys"
version = "1.0.0"

configurations.all {
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-json-jvm")
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-core-jvm")
}

val embed by configurations.creating
configurations.getByName("implementation").extendsFrom(embed)

tasks {
    shadowJar {
        configurations.set(listOf(embed))
        exclude("gg/essential/**")
    }
    withType<net.fabricmc.loom.task.RemapJarTask>().configureEach {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.flatMap { it.archiveFile })
    }

    processResources {
        val version = project.version
        val fabricApiVersion = project.platform.fabricApiVersion ?: throw IllegalStateException("fabricApiVersion is not set for platform ${project.platform}")
        val fabricKotlinVersion = project.platform.fabricKotlinVersion ?: throw IllegalStateException("fabricKotlinVersion is not set for platform ${project.platform}")

        inputs.property("version", version)
        inputs.property("fabric_api_version", fabricApiVersion)
        inputs.property("fabric_kotlin_version", fabricKotlinVersion)
        filesMatching("fabric.mod.json") {
            expand(
                "version" to version,
                "fabric_api_version" to fabricApiVersion,
                "fabric_kotlin_version" to fabricKotlinVersion,
            )
        }
    }
}

afterEvaluate {
    val hasRemapJar = tasks.findByName("remapJar") != null
    val outputTaskName = if (hasRemapJar) "remapJar" else "shadowJar"

    tasks.register<Copy>("collectJars") {
        group = "build"
        description = "Copies this version's non-shadowed JARs to main/jars"

        val outputDir = projectDir.resolve("../../jars").normalize()
        dependsOn(outputTaskName)

        from(tasks.named(outputTaskName)) {
            include("*.jar")
            exclude { it.name.contains(" 1.2") && it.name.contains("-all") }
            rename {
                "${rootProject.name}-${version}+${project.platform.mcVersionStr}.jar"
            }
        }
        into(outputDir)
    }

    tasks.named("build") {
        finalizedBy("collectJars")
    }

    configurations.named("default") {
        isCanBeConsumed = true
        isCanBeResolved = false
    }

    artifacts {
        add("default", tasks.named(outputTaskName))
    }
}
