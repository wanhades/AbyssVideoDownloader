import proguard.gradle.ProGuardTask

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.gradleup.shadow") version "8.3.3"
    application
}


group = "com.abmo"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.abmo.MainKt")
}


repositories {
    mavenCentral()
}
buildscript {
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.6.0")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.mashape.unirest:unirest-java:1.4.9")
    implementation("org.mozilla:rhino:1.7.15")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jsoup:jsoup:1.18.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}


tasks {
    shadowJar {
        minimize {
            exclude(dependency("org.mozilla:.*:.*"))
            exclude(dependency("org.apache.httpcomponents:httpcore:.*"))
            exclude(dependency("org.apache.httpcomponents:httpclient:.*"))
        }
        archiveFileName = "abyss-dl.jar"
    }
}

tasks.register<ProGuardTask>("proguard") {
    val buildDir = layout.buildDirectory.get()
    dependsOn("shadowJar")

    injars("$buildDir/libs/abyss-dl.jar")
    outjars("$buildDir/libs/abyss-dl-obfuscated.jar")

    configuration("proguard-rules.pro")


    libraryjars("${System.getProperty("java.home")}/lib/rt-fs.jar")
    libraryjars("${System.getProperty("java.home")}/jmods")
    printmapping("${buildDir}/libs/mapping.txt")
}

tasks.build {
    dependsOn(tasks.named("proguard"))
}
