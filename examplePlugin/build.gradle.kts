plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.2"
}

group = "org.kirisame.mc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    compileOnly(project(":"))

    compileOnly(files("libs/server-25w45a_unobfuscated.jar"))
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
        archiveClassifier.set("")
        archiveVersion.set("1.0.0")

        mergeServiceFiles()

        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }

    build {
        dependsOn(shadowJar)
    }
}