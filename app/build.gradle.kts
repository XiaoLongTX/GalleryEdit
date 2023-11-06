import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
fun getLocalProperty(key: String) = gradleLocalProperties(rootDir).getProperty(key)


android {
    namespace = "com.bianxl.galleryedit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bianxl.galleryedit"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("MySigningConfig") {
            storeFile = file(getLocalProperty("keypath") ?: "")
            keyAlias = getLocalProperty("keyAlias")?.toString()
            keyPassword = getLocalProperty("keyPassword")?.toString()
            storePassword = getLocalProperty("storePassword")?.toString()
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs["MySigningConfig"]
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 输出类型
            android.applicationVariants.all {
                // 编译类型
                val buildType = this.buildType.name
                if (buildType == "release") {
                    this.outputs.filterIsInstance<com.android.build.gradle.internal.api.ApkVariantOutputImpl>()
                        .forEach {
                            it.outputFileName =
                                 "${defaultConfig.applicationId}_${defaultConfig.versionName}_$buildType.apk"
                        }
                }
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0-alpha13")
    implementation("androidx.media3:media3-exoplayer:1.1.1")
    implementation("androidx.media3:media3-transformer:1.1.1")
    implementation("androidx.media3:media3-effect:1.1.1")
    implementation("androidx.media3:media3-common:1.1.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.vmadalin:easypermissions-ktx:1.0.0")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")

}