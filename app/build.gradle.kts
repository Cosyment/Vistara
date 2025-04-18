import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gradle.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.service)
}

val keystorePropertiesFile = rootProject.file("gradle.properties")
val keystoreProperties = Properties()
keystoreProperties.load(keystorePropertiesFile.inputStream())

android {
    namespace = "com.vistara.aestheticwalls"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vistara.aestheticwalls"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "UNSPLASH_API_KEY", "\"WnVAinP7jaof1NjifR_hULHSod66MMdr2bspQxyeyhw\"")
            buildConfigField("String", "UNSPLASH_SECRET_KEY", "\"-IBwR1mET4I7C4fp9XMgozKmRw7Fu7Oyttdt5iQ2Ca4\"")
            buildConfigField("String", "PEXELS_API_KEY", "\"3Hu4ltF8QgCdrqZTxZPbC7M6LipoqYF41dCaRH7iYvgchtCRBpGPH4D0\"")
            buildConfigField("String", "PIXABAY_API_KEY", "\"49629695-35e6ee8fb0f82cc4b4ed4b6a2\"")
            buildConfigField("String", "WALLHAVEN_API_KEY", "\"QMbjLhJGSPHVIPQ91HCFUL4UOJtRjTk8\"")
            buildConfigField("boolean", "IS_DEV_MODE", "false")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            versionNameSuffix = ".test"
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "UNSPLASH_API_KEY", "\"WnVAinP7jaof1NjifR_hULHSod66MMdr2bspQxyeyhw\"")
            buildConfigField("String", "UNSPLASH_SECRET_KEY", "\"-IBwR1mET4I7C4fp9XMgozKmRw7Fu7Oyttdt5iQ2Ca4\"")
            buildConfigField("String", "PEXELS_API_KEY", "\"3Hu4ltF8QgCdrqZTxZPbC7M6LipoqYF41dCaRH7iYvgchtCRBpGPH4D0\"")
            buildConfigField("String", "PIXABAY_API_KEY", "\"49629695-35e6ee8fb0f82cc4b4ed4b6a2\"")
            buildConfigField("String", "WALLHAVEN_API_KEY", "\"QMbjLhJGSPHVIPQ91HCFUL4UOJtRjTk8\"")
            buildConfigField("boolean", "IS_DEV_MODE", "true")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material) // 添加 Material 依赖，用于 PullRefresh
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.constraint.compose)
    implementation(libs.androidx.material.icons.core)

    // StaggeredGrid for waterfall layout
    implementation(libs.compose.glide)
    implementation(libs.accompanist.flowlayout)
    implementation(libs.accompanist.swiperefresh)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.appcompat)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Coil
    implementation(libs.coil.compose)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Room
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Accompanist
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.drawablepainter)

    // Media3 for video playback
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)

    // Image Cropping
    implementation(libs.imagecropper)

    // Google Play Billing
    implementation(libs.google.play.billing)
    implementation(libs.google.play.billing.ktx)

    // Google Auth
    implementation(libs.google.auth)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.androidx.datastore.preferences.core)
    
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.hilt.android)
}

// 自定义任务：编译、安装并启动应用
tasks.register("buildInstallAndRun") {
    dependsOn("assembleDebug", "installDebug")
    doLast {
        // 启动应用
        try {
            providers.exec {
                commandLine("adb", "shell", "am", "start", "-n", "com.vistara.aestheticwalls/.ui.MainActivity")
                isIgnoreExitValue = true // 忽略退出代码
            }
            println("\n\n应用已成功编译、安装并启动\n\n")
        } catch (e: Exception) {
            println("\n\n启动应用时出错: ${e.message}\n\n")
        }
    }
}