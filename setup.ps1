# Scaffold Android Project
$ErrorActionPreference = 'Stop'

$baseDir = "d:\Buddie"
Set-Location $baseDir

# Define folders
$folders = @(
    "gradle/wrapper",
    "app/src/main/java/com/tanmay/buddie/data/local/dao",
    "app/src/main/java/com/tanmay/buddie/data/local/entity",
    "app/src/main/java/com/tanmay/buddie/data/datastore",
    "app/src/main/java/com/tanmay/buddie/data/repository",
    "app/src/main/java/com/tanmay/buddie/domain/model",
    "app/src/main/java/com/tanmay/buddie/domain/repository",
    "app/src/main/java/com/tanmay/buddie/presentation/theme",
    "app/src/main/java/com/tanmay/buddie/presentation/components",
    "app/src/main/java/com/tanmay/buddie/presentation/screens/onboarding",
    "app/src/main/java/com/tanmay/buddie/presentation/screens/home",
    "app/src/main/java/com/tanmay/buddie/presentation/screens/timetable",
    "app/src/main/java/com/tanmay/buddie/presentation/screens/exams",
    "app/src/main/java/com/tanmay/buddie/presentation/screens/settings",
    "app/src/main/java/com/tanmay/buddie/di",
    "app/src/main/res/values",
    "app/src/main/res/xml",
    "app/src/main/res/mipmap-anydpi-v26",
    "app/src/main/res/drawable"
)

foreach ($folder in $folders) {
    New-Item -ItemType Directory -Force -Path $folder | Out-Null
}

# settings.gradle.kts
@"
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Buddie"
include(":app")
"@ | Out-File -FilePath "settings.gradle.kts" -Encoding UTF8

# build.gradle.kts (Project)
@"
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
}
"@ | Out-File -FilePath "build.gradle.kts" -Encoding UTF8

# gradle.properties
@"
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
"@ | Out-File -FilePath "gradle.properties" -Encoding UTF8

# gradle-wrapper.properties
@"
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
"@ | Out-File -FilePath "gradle/wrapper/gradle-wrapper.properties" -Encoding UTF8

# app/build.gradle.kts
@"
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.tanmay.buddie"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tanmay.buddie"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        isCoreLibraryDesugaringEnabled = true
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
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    
    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.4")
    
    // Room
    val roomVersion = "2.6.0"
    implementation("androidx.room:room-runtime:`$roomVersion")
    implementation("androidx.room:room-ktx:`$roomVersion")
    ksp("androidx.room:room-compiler:`$roomVersion")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
"@ | Out-File -FilePath "app/build.gradle.kts" -Encoding UTF8

# AndroidManifest.xml
@"
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:name=".BuddieApp"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Buddie"
        tools:targetApi="31">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.Buddie">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
"@ | Out-File -FilePath "app/src/main/AndroidManifest.xml" -Encoding UTF8

# BuddieApp.kt
@"
package com.tanmay.buddie

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BuddieApp : Application()
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/BuddieApp.kt" -Encoding UTF8

# MainActivity.kt
@"
package com.tanmay.buddie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.tanmay.buddie.presentation.theme.BuddieTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BuddieTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Text("Buddie App Scaffolded")
                }
            }
        }
    }
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/MainActivity.kt" -Encoding UTF8

# Theme.kt
@"
package com.tanmay.buddie.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme()
private val LightColorScheme = lightColorScheme()

@Composable
fun BuddieTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/presentation/theme/Theme.kt" -Encoding UTF8

# Resources
@"
<resources>
    <string name="app_name">Buddie</string>
</resources>
"@ | Out-File -FilePath "app/src/main/res/values/strings.xml" -Encoding UTF8

@"
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.Buddie" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
"@ | Out-File -FilePath "app/src/main/res/values/themes.xml" -Encoding UTF8

@"
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <include domain="sharedpref" path="."/>
</full-backup-content>
"@ | Out-File -FilePath "app/src/main/res/xml/backup_rules.xml" -Encoding UTF8

@"
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-network>
        <allow>
            <include domain="sharedpref" path="."/>
        </allow>
    </cloud-network>
</data-extraction-rules>
"@ | Out-File -FilePath "app/src/main/res/xml/data_extraction_rules.xml" -Encoding UTF8

# Placeholders for mipmap/drawable to avoid compile errors
@"
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
"@ | Out-File -FilePath "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml" -Encoding UTF8
@"
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
"@ | Out-File -FilePath "app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml" -Encoding UTF8

@"
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:fillColor="#3DDC84" android:pathData="M0,0h108v108h-108z"/>
</vector>
"@ | Out-File -FilePath "app/src/main/res/drawable/ic_launcher_background.xml" -Encoding UTF8

@"
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:fillColor="#FFFFFF" android:pathData="M34,42h40v24h-40z"/>
</vector>
"@ | Out-File -FilePath "app/src/main/res/drawable/ic_launcher_foreground.xml" -Encoding UTF8

Write-Host "Scaffolding Complete"
