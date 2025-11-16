plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.2"
    application
}

group = "org.kirisame.mc"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    //logging
    implementation("org.tinylog:tinylog-api:2.7.0")
    implementation("org.tinylog:tinylog-impl:2.7.0")

    implementation("com.typesafe:config:1.4.3")
    // https://mvnrepository.com/artifact/commons-io/commons-io
    implementation("commons-io:commons-io:2.21.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    shadowJar {
        archiveBaseName.set("kirisame-launcher")
        archiveClassifier.set("all")
        archiveVersion.set(version.toString())

        manifest {
            attributes["Main-Class"] = "org.kirisame.mc.launcher.Main"
        }

        mergeServiceFiles()

        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }

    build {
        dependsOn(shadowJar)
    }
}

application {
    mainClass.set("org.kirisame.mc.launcher.Main")
}

tasks.register<Copy>("copyKirisame"){
    dependsOn(rootProject.tasks.build, ":Kirisame-Agent:build")

    from("../build/libs/kirisame-"+rootProject.version.toString()+"-all.jar")
    into("workdir")

    from("../Kirisame-Agent/build/libs/Kirisame-Agent-"+project(":Kirisame-Agent").version.toString()+"-all.jar")
    into("workdir")
}

tasks.named<JavaExec>("run") {
    dependsOn("copyKirisame")

    standardInput = System.`in`
    workingDir = file("workdir")
}