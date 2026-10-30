plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("maven-publish")
    id("signing")
}

// 发布坐标（io.github.zhanghaifei1997，见 docs/PUBLISH.md）
group = project.findProperty("kuiklyAppIcon.group") as String? ?: "com.kuiklyext.appicon"
version = project.findProperty("kuiklyAppIcon.version") as String? ?: "0.1.0"

val kuiklyVersion: String by project
val kuiklyCoreVersion: String by project

android {
    namespace = "com.kuikly.appicon"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
    }
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Kuikly dynamic app icon module"
        homepage = "https://github.com/zhanghaifei1997/kuikly-app-icon"
        ios.deploymentTarget = "15.0"
        framework {
            baseName = "kuiklyAppIcon"
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Kuikly 产物版本为复合格式（Kuikly版本-Kotlin版本），见 gradle.properties
            api("com.tencent.kuikly-open:core:$kuiklyCoreVersion")
        }
    }

    // 本 Module 不需要平台专属 Kotlin 代码，Android/iOS/鸿蒙的原生实现分别在
    // kuiklyAppIconAndroid / kuiklyAppIconIOS / kuiklyAppIconOhos 模块中。
}

// ---------- Maven Central 发布 ----------

// sources jar 由 KMP 插件自动生成并挂到各 target publication；
// javadoc jar Central 校验必需，KMP 无 javadoc 文档，附空 jar
val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        artifact(emptyJavadocJar.get())
        pom {
            name.set("kuikly-app-icon")
            description.set("Kuikly dynamic app icon module: switch Android/iOS/HarmonyOS app icons from Kotlin")
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
    repositories {
        mavenLocal()
        // Central Portal 暂存目录：publishAllPublicationsToCentralStagingRepository 后
        // 将 staging-repo 打 zip 经 central.sonatype.com API 上传（见 docs/PUBLISH.md）
        maven {
            name = "CentralStaging"
            url = uri(layout.buildDirectory.dir("staging-repo"))
        }
    }
}

// 本机 ~/.gradle/gradle.properties 配置了 signing.keyId 时启用签名（无密钥环境跳过，便于本地开发）
if (project.hasProperty("signing.keyId")) {
    signing {
        useGpgCmd()
        sign(publishing.publications)
    }
}
