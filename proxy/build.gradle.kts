plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.1"
}

group = "com.carloship.craft.proxy"
version = "1.0-SNAPSHOT"

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    implementation(project(":sdk"))

    compileOnly("com.velocitypowered:velocity-api:4.3.2-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:4.3.2-SNAPSHOT")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveVersion.set("")
    archiveBaseName.set("core-proxy")

    destinationDirectory.set(file("C:/Devlopment/ByteNetwork/proxy/plugins/"));

}
