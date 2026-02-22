buildscript {
    repositories {
        mavenLocal()
    }

}

plugins {
    id("java")
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "buloshnaya"
version = "0.0.1-SNAPSHOT"
description = "NotificationService"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:3.3.4")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.3.4")
    implementation("org.springframework.kafka:spring-kafka:3.2.4")
    implementation("org.springframework.boot:spring-boot-starter-mail:3.3.4")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    runtimeOnly("org.postgresql:postgresql:42.7.4")

    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.4")
    testImplementation("org.springframework.kafka:spring-kafka-test:3.2.4")
}

springBoot {
    mainClass = "buloshnaya.Main"
}