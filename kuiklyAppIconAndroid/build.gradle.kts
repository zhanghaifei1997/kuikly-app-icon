plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("signing")
}

// 发布坐标（io.github.zhanghaifei1997，见 docs/PUBLISH.md）
group = project.findProperty("kuiklyAppIcon.group") as String? ?: "com.kuiklyext.appicon"
version = project.findProperty("kuiklyAppIcon.version") as String? ?: "0.1.0"

android {
    namespace = "com.kuikly.appicon.android"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Kuikly 产物版本为复合格式（Kuikly版本-Kotlin版本），见 gradle.properties
    compileOnly("com.tencent.kuikly-open:core-render-android:${project.findProperty("kuiklyCoreVersion")}")
}

// ---------- Maven Central 发布 ----------

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from("src/main/kotlin")
    from("src/main/java")
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                from(components.findByName("release"))
            }
            artifact(sourcesJar.get())
            artifact(javadocJar.get())
            pom {
                name.set("kuikly-app-icon-android")
                description.set("Android native implementation of kuikly-app-icon (activity-alias based)")
                url.set("https://github.com/zhanghaifei1997/kuikly-app-icon")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("zhanghaifei1997")
                        name.set("Zhanghaifei")
                        email.set("2428669833@qq.com")
                    }
                }
                scm {
                    url.set("https://github.com/zhanghaifei1997/kuikly-app-icon")
                    connection.set("scm:git:git://github.com/zhanghaifei1997/kuikly-app-icon.git")
                    developerConnection.set("scm:git:ssh://github.com/zhanghaifei1997/kuikly-app-icon.git")
                }
            }
        }
    }
    repositories {
        mavenLocal()
        maven {
            name = "CentralStaging"
            url = uri(layout.buildDirectory.dir("staging-repo"))
        }
    }
}

if (project.hasProperty("signing.keyId")) {
    signing {
        useGpgCmd()
        sign(publishing.publications)
    }
}
