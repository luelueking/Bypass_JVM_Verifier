plugins {
    java
    kotlin("jvm") version "1.3.61"
}

group = "org.vidar"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.hackery.site/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    arrayOf("asm", "asm-tree", "asm-commons").forEach {
        implementation(group = "org.ow2.asm", name = it, version = "7.2")
    }

    implementation("codes.som.anthony:koffee:7.1.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
