# Setup Required Before Building

This Android project has been created but requires some additional setup before it can be built and run successfully.

## Critical Requirements

### 1. Telegram API Credentials (REQUIRED)

The app uses Telegram's TDLib API which requires official API credentials:

1. Visit [https://my.telegram.org](https://my.telegram.org)
2. Log in with your Telegram account
3. Navigate to "API development tools"
4. Create a new application
5. Copy your `API_ID` and `API_HASH`

**Update the following file:**

`app/src/main/java/com/telegram/videoplayer/data/remote/telegram/TelegramClient.kt`

Replace these lines (around line 20-21):
```kotlin
private val API_ID = 0 // Replace with your actual API ID
private val API_HASH = "YOUR_API_HASH" // Replace with your actual API hash
```

⚠️ **The app will not work without valid Telegram API credentials!**

### 2. Gradle Wrapper JAR (REQUIRED for building)

The Gradle wrapper JAR file is not included in this repository. You have two options:

**Option A: Generate using Android Studio (Recommended)**
1. Open the project in Android Studio
2. Android Studio will automatically download the Gradle wrapper
3. Or run: `gradle wrapper` if you have Gradle installed globally

**Option B: Download manually**
Download from: https://services.gradle.org/distributions/gradle-8.4-bin.zip
Extract and place `gradle-wrapper.jar` in `gradle/wrapper/` directory

### 3. TDLib Native Library

The app uses TDLight (TDLib wrapper) which includes native libraries. The dependency is declared in `build.gradle.kts`:

```kotlin
implementation("it.tdlight:tdlight-java:3.0.3+td.1.8.21")
```

This should be automatically downloaded by Gradle, but if you encounter issues:
- Check that you have internet connectivity
- Ensure JitPack repository is accessible
- May need to manually download TDLib native libraries for your platform

## Build Instructions

Once you've completed the setup above:

```bash
# Sync and build
./gradlew assembleDebug

# Or open in Android Studio and build from there
```

## Optional Configuration

### App Icon

Basic placeholder icons have been generated. For a production app:
- Replace icons in `app/src/main/res/mipmap-*/` directories
- Use Android Studio's Image Asset Studio for better results

### Theme Customization

Modify colors in:
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/themes.xml`

### ProGuard Rules

If you enable minification for release builds, review:
- `app/proguard-rules.pro`

## Known Issues & Limitations

1. **TDLib Loading**: First launch may take time as TDLib initializes
2. **Storage**: Video files are downloaded to cache directory
3. **Permissions**: Make sure to grant storage permissions on devices with Android 10+
4. **MKV Support**: Device-dependent; not all codecs may be supported
5. **Authentication**: Requires active internet connection

## Dependencies Summary

Key libraries used:
- **TDLight 3.0.3**: Telegram client library
- **ExoPlayer (Media3) 1.2.1**: Video playback
- **Room 2.6.1**: Local database
- **Hilt 2.50**: Dependency injection
- **Kotlin Coroutines 1.7.3**: Async operations
- **Coil 2.5.0**: Image loading

## Testing

To test authentication flow:
1. You need a real Telegram account
2. Phone number should be able to receive Telegram verification codes
3. Test on a real device or emulator with internet access

## Troubleshooting

### Build Fails with "tdjni not found"
- TDLib native library loading issue
- Check that the TDLight dependency is properly resolved
- May need to add native library directories to your build configuration

### Authentication Fails
- Verify API credentials are correct
- Check internet connection
- Ensure phone number format includes country code (+1234567890)

### Video Playback Issues
- Check device codec support
- Verify storage permissions
- Ensure sufficient storage space

## Architecture Notes

This is a Clean Architecture implementation with:
- **Presentation Layer**: Activities, Fragments, ViewModels (MVVM)
- **Domain Layer**: Repository interfaces, Use cases, Domain models
- **Data Layer**: Room DB, TDLib client, Repository implementations

All dependencies are managed through Hilt dependency injection.

## Next Steps

After completing the setup:
1. Configure Telegram API credentials
2. Build the project
3. Run on device/emulator
4. Log in with your Telegram account
5. Browse channels and play videos

For more details, see the main [README.md](README.md)
