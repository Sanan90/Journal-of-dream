plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt") // Для работы с Room

    id("com.google.gms.google-services") version "4.4.2"
}

android {
    namespace = "com.example.journalofdream"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.journalofdream"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
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
    }
    composeOptions {
        // Версия Kotlin Compiler Extension
        // Для Compose 1.5.1 это корректно
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }
}

dependencies {
    // -------------------------------------------------------------------------
    // Firebase BoM + Firebase
    // -------------------------------------------------------------------------
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.6.0")

    // -------------------------------------------------------------------------
    // ТУТ ваши другие зависимости
    // -------------------------------------------------------------------------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")

    implementation(libs.androidx.activity.compose)

    // ------------------- Compose BOM (из version catalogs) -------------------
    // Вы уже используете BOM: implementation(platform(libs.androidx.compose.bom))
    // Это хорошо, значит версии компонентов Compose будут синхронизированы.
    implementation(platform(libs.androidx.compose.bom))

    // Старые пакеты Compose (UI и прочее):
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // --------------------------------------------
    // Material3 (M3) - рекомендуемая библиотека
    // --------------------------------------------
    implementation(libs.androidx.material3)

    // Если вам нужен Navigation Compose:
    implementation(libs.androidx.navigation.compose)

    // ВАЖНО: libs.material (скорее всего com.google.android.material:material),
    // Это M2 (обычный Material Components для View, а не Compose).
    // Если вы используете его только для M2, и переходите на M3 Compose -
    // возможно, можно убрать, если не нужен. Если где-то используете View-тулкит, оставляйте.
    implementation(libs.material)

    // ViewPager2 (для скролла экранов)
    implementation(libs.androidx.viewpager2)

    // -------------------------------------------------------------------------
    // Room
    // -------------------------------------------------------------------------
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // -------------------------------------------------------------------------
    // Testing
    // -------------------------------------------------------------------------
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // тут тоже BOM для UI test:
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
