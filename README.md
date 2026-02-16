# Telegram Video Player

An Android application for browsing Telegram channels and playing videos with support for MKV format, playback progress tracking, and video search.

## Features

- 🔐 **Telegram Authentication** - Secure login with phone number and verification code
- 📺 **Channel Browser** - Browse all your Telegram channels in a grid layout
- 🎬 **Video Player** - Full-featured video player with ExoPlayer supporting MKV and other formats
- ⏯️ **Resume Playback** - Automatically save and resume playback position
- 🔍 **Video Search** - Search videos within channels by title or caption
- 💾 **Offline Storage** - Local database caching for faster load times
- 🌙 **Material Design 3** - Modern UI following Material Design guidelines

## Architecture

The app follows **Clean Architecture** principles with MVVM pattern:

```
┌─────────────────────────────────────────────────────────────────┐
│                         PRESENTATION                             │
│  Activities, Fragments, ViewModels, Adapters                    │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                           DOMAIN                                 │
│  Models, Repository Interfaces, Use Cases                       │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                            DATA                                  │
│  Room Database, Telegram TDLib Client, Repository Impl          │
└─────────────────────────────────────────────────────────────────┘
```

## Technologies Used

- **Language**: Kotlin
- **UI**: Material Design 3, ViewBinding
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt (Dagger)
- **Database**: Room
- **Telegram API**: TDLight (TDLib wrapper)
- **Video Player**: ExoPlayer (Media3)
- **Async**: Coroutines + Flow
- **Image Loading**: Coil
- **Navigation**: Navigation Component

## Setup

### Prerequisites

1. Android Studio Hedgehog or later
2. JDK 17
3. Android SDK API 34

### Telegram API Credentials

1. Go to [https://my.telegram.org](https://my.telegram.org)
2. Log in with your phone number
3. Go to "API development tools"
4. Create a new application
5. Copy your `API_ID` and `API_HASH`

### Configuration

Open `app/src/main/java/com/telegram/videoplayer/data/remote/telegram/TelegramClient.kt` and replace:

```kotlin
private val API_ID = 0 // Replace with your API ID
private val API_HASH = "YOUR_API_HASH" // Replace with your API hash
```

### Build

```bash
./gradlew assembleDebug
```

## Usage

1. **Authentication**
   - Launch the app
   - Enter your phone number (with country code, e.g., +1234567890)
   - Enter the verification code sent to your Telegram app

2. **Browse Channels**
   - View all your channels in a grid
   - Pull to refresh to update channel list
   - Tap a channel to view its videos

3. **Watch Videos**
   - Browse videos in grid layout
   - Tap a video to start playing
   - Video automatically downloads and plays
   - Progress is saved every 5 seconds
   - Resume from where you left off

4. **Search Videos**
   - Use the search icon in the video list
   - Search by video title or caption
   - Results update in real-time

## Project Structure

```
app/
├── src/main/
│   ├── java/com/telegram/videoplayer/
│   │   ├── data/
│   │   │   ├── local/          # Room database
│   │   │   ├── remote/         # Telegram API client
│   │   │   └── repository/     # Repository implementations
│   │   ├── domain/
│   │   │   ├── model/          # Domain models
│   │   │   └── repository/     # Repository interfaces
│   │   ├── presentation/
│   │   │   ├── auth/           # Authentication screens
│   │   │   ├── channel/        # Channel list
│   │   │   ├── player/         # Video player
│   │   │   ├── video/          # Video list
│   │   │   └── util/           # UI utilities
│   │   └── di/                 # Dependency injection
│   └── res/                    # Resources (layouts, drawables, etc.)
└── build.gradle.kts
```

## Features in Detail

### Video Player

- **MKV Support**: Full support for MKV files through ExoPlayer
- **Custom Controls**: Fullscreen player with custom UI controls
- **Progress Tracking**: Saves position every 5 seconds
- **Resume Dialog**: Prompts to resume or start over
- **Landscape Mode**: Forced landscape for optimal viewing
- **Keep Screen On**: Prevents screen timeout during playback

### Progress Tracking

- Automatically saves playback position every 5 seconds
- Stores position in local Room database
- Shows progress bar on video thumbnails
- Resume dialog when reopening a video

### Channel Management

- Fetches channels from Telegram
- Caches in local database
- Pull-to-refresh for updates
- Grid layout with channel thumbnails

## Permissions

- `INTERNET` - Required for Telegram API
- `ACCESS_NETWORK_STATE` - Check network connectivity
- `FOREGROUND_SERVICE` - Background media playback (future enhancement)
- `WAKE_LOCK` - Keep device awake during playback

## Future Enhancements

- [ ] Background playback service
- [ ] Picture-in-Picture mode
- [ ] Download videos for offline viewing
- [ ] Multiple quality selection
- [ ] Subtitle support
- [ ] Playlist creation
- [ ] Share videos
- [ ] Video statistics

## License

This project is for educational purposes. Make sure to comply with Telegram's Terms of Service when using their API.

## Troubleshooting

### Authentication Issues

- Make sure you're using the correct phone number format (+countrycode + number)
- Check that your API credentials are correct
- Verify you have an active Telegram account

### Video Playback Issues

- Ensure you have a stable internet connection for downloading
- Some video codecs may not be supported on all devices
- Check storage space for video caching

### Build Issues

- Make sure you have JDK 17 installed
- Sync Gradle files in Android Studio
- Clean and rebuild project if needed

## Contributing

This is a demonstration project. Feel free to fork and modify as needed.

## Credits

- [TDLight](https://github.com/tdlight-team/tdlight-java) - Telegram client library
- [ExoPlayer](https://github.com/google/ExoPlayer) - Media player
- [Material Design](https://material.io/) - UI design guidelines
