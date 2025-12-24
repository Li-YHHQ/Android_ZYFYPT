plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.zyfypt613lsl"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.zyfypt613lsl"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")     //Retrofit
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")     //Retrofit
    implementation ("com.squareup.retrofit2:converter-scalars:2.11.0")  //Retrofit
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")  //OkHttp日志
    implementation ("com.squareup.picasso:picasso:2.8")     //网络图片加载
    implementation ("com.github.mhiew:android-pdf-viewer:3.2.0-beta.3") // PDF 查看
}