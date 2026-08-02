# VTT CABS - Release Notes

## Build Status: SUCCESS

### APK Downloads

The following APKs have been built successfully:

| APK | Size | Description |
|-----|------|-------------|
| app-debug.apk | 26.9 MB | Main VTT CABS app |
| app-release.apk | 19.3 MB | Main app (release build) |
| customer-app-debug.apk | 21.5 MB | Customer-only app |
| driver-app-debug.apk | 17.8 MB | Driver-only app |
| admin-app-debug.apk | 17.9 MB | Admin-only app |

### Build Information

- Gradle Version: 8.11.1
- Java Version: 21.0.12
- Android SDK: 34/35/36

### APK Locations

```
VTT-CABS/release/
├── app-debug.apk
├── app-release.apk
├── customer-app-debug.apk
├── driver-app-debug.apk
└── admin-app-debug.apk
```

### How to Generate APKs

```bash
cd VTT-CABS
export JAVA_HOME=/path/to/jdk-21
export ANDROID_HOME=/path/to/android-sdk
./gradlew assembleDebug
```
