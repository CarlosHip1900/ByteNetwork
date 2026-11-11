plugins {
    id("java")
}

group = "com.carloship.craft"
version = "1.0-SNAPSHOT"


allprojects {
    apply {
        plugin("java-library")
        plugin("java")
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("net.minestom:minestom:2026.10.05-26.2")

        implementation("net.kyori:adventure-text-minimessage:5.1.1")
        implementation("net.kyori:adventure-api:5.1.1")
        implementation("net.kyori:adventure-text-serializer-plain:5.1.1")

        implementation("com.google.code.gson:gson:2.14.0")

        implementation("com.github.ben-manes.caffeine:caffeine:3.2.3")

        implementation("io.lettuce:lettuce-core:7.9.0.RELEASE")
        implementation("org.mongodb:mongodb-driver-sync:5.6.1")
        implementation("com.zaxxer:HikariCP:7.0.2")
        implementation("com.mysql:mysql-connector-j:9.3.0")
        implementation("org.java-websocket:Java-WebSocket:1.6.0")

        implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")
        implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.20.1")
        implementation("org.yaml:snakeyaml:2.5")

        implementation("io.github.classgraph:classgraph:4.8.171")

        compileOnly("org.projectlombok:lombok:1.18.40")

        annotationProcessor("org.projectlombok:lombok:1.18.40")

        runtimeOnly("ch.qos.logback:logback-classic:1.5.18")
    }

    tasks.test {
        useJUnitPlatform()
    }

}