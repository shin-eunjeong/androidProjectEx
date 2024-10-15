plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.jungexweb"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.jungexweb"
        minSdk = 29
        targetSdk = 34
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    /* implementation group: 'gun0912.ted', name: 'tedpermission', version: '2.0.0' 을 libs.versions.toml에서 등록후 하려했으나 안됨..*/
    /*안드로이드  특정 상황에서 권한 붙는 창을 만들어 낼 수 있다.*/
    implementation("io.github.ParkSangGwon:tedpermission-normal:3.3.0")
    /* 파일 읽어올때 필요, 보안패치 적용 2.4->2.7로 가져옴. */
    implementation("commons-io:commons-io:2.7")
    //리스트 보여주는 것
    implementation("androidx.recyclerview:recyclerview:1.3.2") //  appcompat = "1.6.1 인데 recyclerView의 안정화 버전 그냥 함"
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}