dependencies {
    compileOnlyApi(libs.viaverVelocity) // Solar
    implementation(projects.viabackwardsCommon)
    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)
}
