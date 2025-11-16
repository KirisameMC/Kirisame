plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.2"
}

group = "org.kirisame.mc"
version = rootProject.version

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    compileOnly(project(":"))
    compileOnly(project(":minecraft-api"))

    compileOnly(files("libs/server-25w45a_unobfuscated.jar"))
    compileOnly(files("libs/brigadier-1.3.10.jar"))
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    shadowJar {
        archiveBaseName.set("kirisame-example")
        archiveClassifier.set("all")
        archiveVersion.set(version.toString())

        mergeServiceFiles()

        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }

    build {
        dependsOn(shadowJar)
    }
}

tasks.register<Copy>("applyNewPlugin") {
    dependsOn(tasks.build)

    from(file("build/libs/kirisame-example-$version-all.jar"))
    into("../workdir/kirisame_plugins")
}