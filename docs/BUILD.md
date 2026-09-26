# Build

## Requirements

- JDK 17 for Kotlin; Gradle provisions its own daemon JVM.
- Android SDK with platform 37.
- Xcode 26 and [XcodeGen](https://github.com/yonaskolb/XcodeGen) for iOS.

## Android

```bash
./gradlew :apps:androidApp:assembleDebug
```

The release build runs R8 and resource shrinking; it is signed with the debug key unless a
signing config is added locally.

## iOS

The Xcode project is generated and not committed:

```bash
cd apps/iosApp && xcodegen generate
```

Open `apps/iosApp/Swishy.xcodeproj` and run the `Swishy` scheme. A build phase calls
`./gradlew :shared:app:embedAndSignAppleFrameworkForXcode`, which builds the `SwishyKit`
framework for the selected target.

To run on a device, copy `apps/iosApp/Signing.xcconfig.example` to
`apps/iosApp/Signing.xcconfig` and set your own team id. The real file is ignored by git.

## Checks

```bash
./scripts/check-conventions.sh
./gradlew detekt \
    :shared:core:media:testAndroidHostTest \
    :shared:core:decisions:testAndroidHostTest \
    :shared:design-system:testAndroidHostTest
```

## Version

`config/version.properties` holds the version name. The Android version code is the number of
commits on the branch; on iOS it is set when the build is uploaded.

## Gradle

Configuration cache, build cache and parallel execution are on. Convention plugins live in
`build-logic/`:

| Plugin | Applies |
| --- | --- |
| `com.disspear574.swishy.kmplibrary` | Kotlin Multiplatform with Android and iOS targets |
| `com.disspear574.swishy.compose-multiplatform` | the above plus Compose and Compose resources |
| `com.disspear574.swishy.androidApplication` | the Android app host |
