plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.2"
}

group = "org.kirisame.mc"
version = "1.2"

ext {
    set("bytebuddy","1.18.1")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    compileOnly("net.bytebuddy:byte-buddy:${rootProject.ext.get("bytebuddy")}")

    //logging
    implementation("org.tinylog:tinylog-api:2.7.0")
    implementation("org.tinylog:tinylog-impl:2.7.0")

    implementation("com.typesafe:config:1.4.3")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("com.google.guava:guava:33.5.0-jre")

    implementation("io.github.classgraph:classgraph:4.8.184")

    // https://mvnrepository.com/artifact/commons-io/commons-io
    implementation("commons-io:commons-io:2.21.0")

    implementation(project(":minecraft-api"))

}

tasks {
    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    shadowJar {
        archiveBaseName.set("kirisame")
        archiveClassifier.set("all")
        archiveVersion.set(version.toString())

        manifest {
            attributes["Main-Class"] = "org.kirisame.mc.Main" // 改成你的主类
        }

        mergeServiceFiles()

        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }

    build {
        dependsOn(shadowJar)
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Copy>("release"){
    dependsOn(
        tasks.build,
        project(":Kirisame-Agent").tasks.build,
        project(":examplePlugin").tasks.build,
        project(":launcher").tasks.build,
    )

    from(
        tasks.shadowJar.get().outputs,
        project(":Kirisame-Agent").tasks.shadowJar.get().outputs,
        project(":examplePlugin").tasks.shadowJar.get().outputs,
        project(":launcher").tasks.shadowJar.get().outputs,
    )

    into("release")
}