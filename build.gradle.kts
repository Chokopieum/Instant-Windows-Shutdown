plugins {
    kotlin("multiplatform") version "2.4.0"
}

repositories {
    mavenCentral()
}

kotlin {

    mingwX64("mingw") {
        binaries {
            executable {

                entryPoint = "main"


                baseName = "shutdown"
            }
        }
    }
}