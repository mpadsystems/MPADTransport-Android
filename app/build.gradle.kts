
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.devtools.ksp") version "2.1.20-1.0.31" apply true
}

android {
    namespace = "com.wneth.mpadtransport"
    compileSdk = 35

    defaultConfig {applicationId = "com.wneth.mpadtransport"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.9"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }

        buildConfigField("String", "SERVER_URL", "\"${findProperty("server_url") ?: ""}\"")
        buildConfigField("String", "API_URL", "\"${findProperty("api_url") ?: ""}\"")

        buildConfigField("String", "MPAD_URL", "\"${findProperty("mpad_url") ?: ""}\"")

        buildConfigField("String", "PAY_SERVER_URL", "\"${findProperty("pay_server_url") ?: ""}\"")
        buildConfigField("String", "PAY_API_URL", "\"${findProperty("pay_api_url") ?: ""}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        viewBinding = true
        buildConfig = true
        aidl = true
        //dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.webkit)
    implementation(libs.gson)


    //Scanner
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.vision.common)
    implementation(libs.play.services.mlkit.barcode.scanning)
    implementation(libs.play.services.code.scanner)


    // ROOM Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.runtime.android)
    implementation(libs.protolite.well.known.types)
    implementation(libs.firebase.crashlytics.buildtools)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Dagger
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)

    // OKHttp
    implementation(libs.okhttp)

    // Backup Dependency
    implementation(libs.androidx.work.runtime.ktx)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}