enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "viabackwards-parent"

dependencyResolutionManagement {
    repositories {
// Solar start
        maven("https://repo.viaversion.com")
        maven("https://nexus.velocitypowered.com/repository/maven-public")
        mavenCentral()
        maven("https://mvn-repo.solarmc.gg/releases")
        maven("https://mvn-repo.solarmc.gg/snapshots")
        maven("https://mvn-repo.arim.space/lesser-gpl3")
        maven("https://mvn-repo.arim.space/gpl3")
// Solar end
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
    plugins {
        id("net.kyori.blossom") version "1.2.0"
        id("com.github.johnrengelman.shadow") version "7.0.0"
    }
}

includeBuild("build-logic")

// Solar start
setupViaSubproject("common")
setupViaSubproject("bukkit")
//setupViaSubproject("bungee")
setupViaSubproject("velocity")
//setupViaSubproject("sponge")
//setupViaSubproject("fabric")
// Solar end

setupSubproject("viabackwards") {
    projectDir = file("universal")
}

fun setupViaSubproject(name: String) {
    setupSubproject("viabackwards-$name") {
        projectDir = file(name)
    }
}

inline fun setupSubproject(name: String, block: ProjectDescriptor.() -> Unit) {
    include(name)
    project(":$name").apply(block)
}
