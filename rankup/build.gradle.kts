plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.1"
}

group = "com.carloship.craft.rankup"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":sdk"))
}


tasks.assemble {
    dependsOn(tasks.shadowJar)
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "com.carloship.craft.rankup.Main"
        }
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveVersion.set("")
    archiveBaseName.set("server")

    destinationDirectory.set(file("C:/Devlopment/ByteNetwork/rankup/"));

    from("spawn") {
        into("spawn")
    }
}


