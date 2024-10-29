plugins {
    id("java")
    application
}

group = "com.yahoo"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass = "yodelr.Main"
}

tasks.test {
    useJUnitPlatform()
}

tasks.run.configure {
    standardInput = System.`in`
}

tasks.jar {
    manifest {
        attributes (
            mapOf(
                "Main-Class" to application.mainClass,
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        )
    }
/*
    from {
        configurations.compile.collect { it.isDirectory() ? it : zipTree(it) }
    }*/
}

