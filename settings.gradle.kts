pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            url = uri("https://mirrors.tencent.com/repository/maven-tencent/")
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://mirrors.tencent.com/repository/maven-tencent/")
        }
    }
}

rootProject.name = "kuikly-app-icon"

include(":kuiklyAppIcon")
include(":kuiklyAppIconAndroid")

// 鸿蒙产物构建用：./gradlew -c settings.ohos.gradle.kts :kuiklyAppIcon:publish
