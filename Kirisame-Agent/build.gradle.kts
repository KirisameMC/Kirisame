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

    implementation("net.bytebuddy:byte-buddy:${rootProject.ext.get("bytebuddy")}")

    compileOnly(project(":"))
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    shadowJar {
        archiveBaseName.set("kirisame-agent")
        archiveClassifier.set("all")
        archiveVersion.set(version.toString())

        manifest {
            attributes["Premain-Class"] = "org.kirisame.mc.agent.Agent"
        }

        mergeServiceFiles()

        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }

    build {
        dependsOn(shadowJar)
    }
}

tasks.register<Copy>("applyNewAgent"){
    dependsOn(tasks.build)

    from(tasks.shadowJar.get().archiveFile.get().asFile)
    into("../workdir")
}