# androidframework

Builds the android framework for LiteWave.

## Source Code Changes

- Need to remove from the activity block in the app/manifests/AndroidManifest.xml file the following for each activity:

```
<activity
  ...
  android:theme="@style/AppTheme"
</activity>
```

## Build

- Open View|Tool Windows|Gradle
- From within the Gradle projects window open: androidframework|:litewave_lib|Tasks|build
- Run asselmbleRelease

## Release

- outputed release aar will be found out: litewave_lib/build/outputs/aar/litewave_lib-release.aar

## Signing

- open build.gradle
- within the android block include the following:

```
android {
  ...
  signingConfigs {
    config {
      keyAlias 'LiteWave'
      keyPassword 'litewavepassword'
      storeFile file('keys/litewave.jks')
      storePassword 'litewavepassword'
    }
  }
}
```

- add the signingConfig to the build type within the android block:

```
android {
  ...
  release {
    signingConfig signingConfigs.config
  }
}
```



