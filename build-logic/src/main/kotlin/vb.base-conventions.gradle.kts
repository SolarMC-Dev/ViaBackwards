plugins {
    `java-library`
    `maven-publish`
}

tasks {
    // Variable replacements
    processResources {
        filesMatching(listOf("plugin.yml", "mcmod.info", "fabric.mod.json", "bungee.yml")) {
            expand("version" to project.version, "description" to project.description, "url" to "https://github.com/ViaVersion/ViaBackwards")
        }
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.addAll(listOf("-nowarn", "-Xlint:-unchecked", "-Xlint:-deprecation"))
    }
}

java {
    javaTarget(16) // Solar
    withSourcesJar()
}

// Solar start
tasks.named<JavaCompile>("compileJava") {
    options.release.set(16)
}
// Solar end


publishing {
    publications.create<MavenPublication>("mavenJava") {
        groupId = rootProject.group as String
        artifactId = project.name
        version = rootProject.version as String
    }
    repositories.maven {
// Solar start
        credentials {
            username = System.getenv("REPO_USER")
            password = System.getenv("REPO_PASS")
        }

        name = "solar-repo"
        val base = "https://mvn-repo.solarmc.gg"
        val releasesRepoUrl = base + "/releases"
        val snapshotsRepoUrl = base + "/snapshots"
        url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
/*
        name = "Via"
        url = uri("https://repo.viaversion.com/")
        credentials(PasswordCredentials::class)
        authentication {
            create<BasicAuthentication>("basic")
        }
*/ // Solar end
    }
}
