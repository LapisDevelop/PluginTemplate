val paperApiVersion = project.property("paper-api-version").toString()
val pluginName = "Lapis" + project.property("plugin-name").toString()
val pluginPackage = pluginName.lowercase().removePrefix("lapis")
val lapisDeps = project.property("lapis-deps").toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
val useAcf = project.property("use-acf").toString().toBoolean()
val website = project.property("website").toString()
val authors = project.property("authors").toString()
val loadType = project.property("load-type").toString()
val customRepos = project.property("custom-repos").toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
val customDeps = project.property("custom-deps").toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }

plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.4.2"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    if (useAcf) {
        maven("https://repo.aikar.co/content/groups/aikar/")
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    }
    if (lapisDeps.isNotEmpty()) {
        maven("https://jitpack.io")
    }
    for (repo in customRepos) {
        maven(repo)
    }
}

var paperDependency = "io.papermc.paper:paper-api:" + paperApiVersion + ".build.+"
if (paperApiVersion.startsWith("1.")) { // use legacy versioning scheme for pre-26.1
    paperDependency = "io.papermc.paper:paper-api:" + paperApiVersion + "-R0.1-SNAPSHOT"
    if (paperApiVersion.split(".")[1].toInt() < 17) { // used to be com.destroystokyo.paper:paper-api for 1.16 and below
        paperDependency = "com.destroystokyo.paper:paper-api:" + paperApiVersion + "-R0.1-SNAPSHOT"
    }
}

dependencies {
    compileOnly(paperDependency)

    for (lapisDep in lapisDeps) {
        implementation("com.github.LapisDevelop:$lapisDep")
    }

    if (useAcf) {
        implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    }
    for (customDep in customDeps) {
        implementation(customDep)
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
    options.forkOptions.executable = System.getProperty("java.home") + "/bin/javac"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("26.1.2")
        jvmArgs("-Xms2G", "-Xmx2G", "-Dcom.mojang.eula.agree=true")
    }

    processResources {
        val props = mapOf(
            "pluginName" to pluginName,
            "version" to version,
            "description" to project.description,
            "paperApiVersion" to paperApiVersion,
            "pluginPackage" to pluginPackage,
            "group" to project.group,
            "website" to website,
            "authors" to authors,
            "loadType" to loadType
        )
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}
