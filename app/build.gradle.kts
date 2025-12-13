import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.nio.file.Files
import java.util.Properties

val secrets = Properties()
Files.newBufferedReader(project.rootDir.resolve("secrets.properties").toPath()).use {
    secrets.load(it)
}
val mockitoAgent = configurations.create("mockitoAgent")

plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.sonarqube)
}

sonarqube {
    properties {
        property("sonar.projectKey", "tlscontact_notifier")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.login", secrets["SONAR_TOKEN"] as String)
    }
}


kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_23)
    }
}

android {
    namespace = "com.tlscontact"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tlscontact"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "v1.0.4"

        testInstrumentationRunner = "org.mockito.junit.MockitoJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file(secrets["KEYSTORE_FILE"] as String)
            storePassword = secrets["KEYSTORE_PASSWORD"] as String
            keyAlias = secrets["KEY_ALIAS"] as String
            keyPassword = secrets["KEY_PASSWORD"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_23
        targetCompatibility = JavaVersion.VERSION_23
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    @Suppress("UnstableAPIUsage")
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
    }

    testOptions {
        unitTests.all {
            it.jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
        }
    }
}

android.applicationVariants.configureEach {
    outputs.all {
        val appName = rootProject.name
        val version = android.defaultConfig.versionName
        val buildType = buildType.name

        val newApkName = "$appName-$version-$buildType.apk"
        (this as BaseVariantOutputImpl).outputFileName = newApkName
    }
}

dependencies {
    implementation(libs.hilt.android)
    implementation(libs.hilt.android.compiler)
    ksp(libs.hilt.android.compiler)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.jsoup)

    implementation(libs.coroutines)

    implementation(libs.datastore)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.slf4j.simple)

    testImplementation(libs.mockito.kotlin)
    mockitoAgent(libs.mockito.kotlin) {
        isTransitive = false
    }

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.moshi)

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
}