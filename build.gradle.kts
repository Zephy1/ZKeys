plugins {
    kotlin("jvm")
    id("maven-publish")
    id("gg.essential.multi-version")
    id("gg.essential.defaults")
}

group = "org.zephy.zkeys"
version = "1.0.0"

configurations.all {
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-json-jvm")
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-core-jvm")
}

tasks {
    withType<net.fabricmc.loom.task.RemapJarTask>().configureEach {
        dependsOn(jar)
        inputFile.set(jar.flatMap { it.archiveFile })
    }

    processResources {
        val version = project.version
        inputs.property("version", version)
        filesMatching("fabric.mod.json") {
            expand("version" to version)
        }

        val javaVersion = project.java.toolchain.languageVersion.get().asInt()
        inputs.property("compatibilityLevel", javaVersion)
        filesMatching("zkeys.mixins.json") {
            filter { line -> line.replace("\$compatibilityLevel", "JAVA_${javaVersion}") }
        }
    }
}

afterEvaluate {
    val hasRemapJar = tasks.findByName("remapJar") != null
    val outputTaskName = if (hasRemapJar) "remapJar" else "jar"

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
