plugins {
    base
    id("vb.build-logic")
}

allprojects {
    group = "gg.solarmc.viaversion" // Solar
    version = "4.0.2-SNAPSHOT"
    description = "Allow older clients to join newer server versions."
}

val platforms = setOf(
        projects.viabackwardsBukkit,
/* Solar start
        projects.viabackwardsBungee,
        projects.viabackwardsFabric,
        projects.viabackwardsSponge,
*/ // Solar end
        projects.viabackwardsVelocity
).map { it.dependencyProject }

// Would otherwise contain api/depdency modules if at some point needed
val special = setOf(
        projects.viabackwards
).map { it.dependencyProject }

subprojects {
    when (this) {
        in platforms -> plugins.apply("vb.platform-conventions")
        in special -> plugins.apply("vb.base-conventions")
        else -> plugins.apply("vb.standard-conventions")
    }
}
