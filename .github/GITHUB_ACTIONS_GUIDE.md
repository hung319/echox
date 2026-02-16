# GitHub Actions CI/CD Guide

This guide explains how to use the GitHub Actions workflows for building and releasing the Telegram Video Player Android app.

## 📋 Available Workflows

### 1. **Android CI/CD Build** (`android-build.yml`)
- **Triggers:** Push to main/master/develop, Pull Requests, Manual dispatch
- **Purpose:** Automated build for every push and PR
- **Outputs:** 
  - Debug APK (always)
  - Release APK (if configured)
  - Test results
  - Automatic release to "continuous" and "latest" tags

### 2. **Test Build** (`test-build.yml`)
- **Triggers:** Push to test-*/feature-* branches, Manual dispatch
- **Purpose:** Quick validation without building APK
- **Outputs:** 
  - Project structure validation
  - Build configuration check
  - Build summary report

### 3. **Release Build** (`release.yml`)
- **Triggers:** Release events, Manual dispatch
- **Purpose:** Official release builds
- **Outputs:** 
  - Signed Release APK (if keystore configured)
  - Debug APK
  - SHA256 checksums
  - GitHub Release with detailed notes

## 🔧 Setup Requirements

### 1. Configure GitHub Secrets

Go to your repository → Settings → Secrets and variables → Actions → New repository secret

Add the following secrets:

#### Required Secrets

| Secret Name | Description | How to Get |
|-------------|-------------|------------|
| `TELEGRAM_API_ID` | Telegram API ID | https://my.telegram.org → API Development Tools |
| `TELEGRAM_API_HASH` | Telegram API Hash | https://my.telegram.org → API Development Tools |

#### Optional Secrets (for signed releases)

| Secret Name | Description | How to Get |
|-------------|-------------|------------|
| `RELEASE_KEYSTORE_BASE64` | Base64 encoded keystore | `base64 -w 0 your-release.keystore` |
| `RELEASE_KEYSTORE_PASSWORD` | Keystore password | Your keystore password |
| `RELEASE_KEY_ALIAS` | Key alias | Your key alias |
| `RELEASE_KEY_PASSWORD` | Key password | Your key password |

### 2. Update TelegramClient.kt

The workflows will inject API credentials via environment variables. Update your `TelegramClient.kt`:

```kotlin
private const val API_ID = System.getenv("TELEGRAM_API_ID")?.toIntOrNull() ?: 0
private const val API_HASH = System.getenv("TELEGRAM_API_HASH") ?: "YOUR_API_HASH"
```

Or use BuildConfig:

```kotlin
// In app/build.gradle.kts
android {
    defaultConfig {
        buildConfigField("int", "TELEGRAM_API_ID", 
            System.getenv("TELEGRAM_API_ID") ?: "0")
        buildConfigField("String", "TELEGRAM_API_HASH", 
            "\"${System.getenv("TELEGRAM_API_HASH") ?: "YOUR_API_HASH"}\"")
    }
}

// In TelegramClient.kt
private const val API_ID = BuildConfig.TELEGRAM_API_ID
private const val API_HASH = BuildConfig.TELEGRAM_API_HASH
```

## 🚀 How to Use

### Quick Test (No APK Build)

1. Create a branch with prefix `test-` or `feature-`:
   ```bash
   git checkout -b test-my-changes
   git push origin test-my-changes
   ```

2. Check Actions tab → "Test Build" workflow
3. View build summary artifact

### Build Debug APK

1. Push to main/master/develop branch:
   ```bash
   git push origin main
   ```

2. Go to Actions tab → "Android CI/CD Build"
3. Wait for build to complete
4. Download APK from:
   - **Artifacts** section (temporary, 30 days)
   - **Releases** → "continuous-main" tag (permanent)
   - **Releases** → "latest" tag (always newest)

### Manual Build

1. Go to Actions tab
2. Select "Android CI/CD Build" workflow
3. Click "Run workflow" button
4. Select branch and click "Run workflow"
5. Download APK from artifacts

### Create Official Release

#### Option 1: Using Workflow Dispatch

1. Go to Actions tab
2. Select "Release Build" workflow
3. Click "Run workflow"
4. Enter version number (e.g., 1.0.0)
5. Check "Create GitHub Release"
6. Click "Run workflow"
7. Download APK from Releases page

#### Option 2: Using Git Tags

1. Create and push a tag:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. Workflow will automatically build and create release

#### Option 3: Create Release via GitHub UI

1. Go to Releases → "Draft a new release"
2. Create new tag (e.g., v1.0.0)
3. Add title and description
4. Click "Publish release"
5. Workflow will automatically build and attach APKs

## 📦 Download Built APKs

### From Workflow Artifacts

1. Go to Actions tab
2. Click on a completed workflow run
3. Scroll to "Artifacts" section
4. Download:
   - `telegram-video-player-debug` - Debug APK
   - `telegram-video-player-release` - Release APK

**Note:** Artifacts expire after 30-90 days

### From Releases

1. Go to Releases tab
2. Select a release:
   - **latest** - Always the newest build
   - **continuous-{branch}** - Latest build from specific branch
   - **v1.x.x** - Official versioned releases
3. Download APK from Assets section

## 🔍 Workflow Details

### Build Process

```
┌─────────────────┐
│  Checkout Code  │
└────────┬────────┘
         ↓
┌─────────────────┐
│  Setup JDK 17   │
└────────┬────────┘
         ↓
┌─────────────────┐
│ Setup Android   │
│      SDK        │
└────────┬────────┘
         ↓
┌─────────────────┐
│ Cache Gradle    │
└────────┬────────┘
         ↓
┌─────────────────┐
│  Build Debug    │
│      APK        │
└────────┬────────┘
         ↓
┌─────────────────┐
│  Build Release  │
│      APK        │
└────────┬────────┘
         ↓
┌─────────────────┐
│  Upload APKs    │
└────────┬────────┘
         ↓
┌─────────────────┐
│ Create Release  │
└─────────────────┘
```

### APK Types

| Type | Filename | Description | Use Case |
|------|----------|-------------|----------|
| Debug | `app-debug.apk` | Debug build with logging | Development, testing |
| Release | `app-release-unsigned.apk` | Optimized, unsigned | Testing release builds |
| Release Signed | `app-release.apk` | Optimized, signed | Production distribution |

## 🐛 Troubleshooting

### Build Fails: "TELEGRAM_API_ID not found"

**Solution:** Add `TELEGRAM_API_ID` and `TELEGRAM_API_HASH` secrets to repository settings.

### Build Fails: "SDK not found"

**Solution:** This should be automatic. Check if `setup-android` action completed successfully.

### Build Fails: Gradle errors

**Solution:** 
- Clear cache: Re-run workflow
- Check Gradle wrapper: Ensure `gradlew` has execute permissions
- Check dependencies: Ensure all repositories are accessible

### APK not in Artifacts

**Solution:**
- Check build logs for errors
- Ensure build completed successfully
- Check if APK was created: Look for "Build Debug APK" step output

### Release not Created

**Solution:**
- Ensure you have write permissions to repository
- Check if `GITHUB_TOKEN` has sufficient permissions
- Verify release workflow completed successfully

## 📊 Build Status Badges

Add these to your README.md:

```markdown
![Android CI](https://github.com/USERNAME/REPO/workflows/Android%20CI%2FCD%20Build/badge.svg)
![Release](https://github.com/USERNAME/REPO/workflows/Release%20Build/badge.svg)
```

Replace `USERNAME` and `REPO` with your GitHub username and repository name.

## 🔒 Security Best Practices

1. **Never commit secrets** to repository
2. **Use GitHub Secrets** for sensitive data
3. **Rotate API credentials** periodically
4. **Review workflow permissions** in Settings → Actions → General
5. **Enable branch protection** for main/master branches
6. **Review pull requests** before merging

## ⚙️ Advanced Configuration

### Custom Build Variants

Edit `android-build.yml`:

```yaml
- name: Build Custom Variant
  run: ./gradlew assembleCustomVariant --stacktrace
```

### Add Code Signing

See `release.yml` for signing example. You'll need:

1. Generate release keystore
2. Convert to Base64: `base64 -w 0 release.keystore > keystore.txt`
3. Add to GitHub Secrets
4. Update workflow to use signing config

### Deploy to Play Store

Add after build step:

```yaml
- name: Upload to Play Store
  uses: r0adkll/upload-google-play@v1
  with:
    serviceAccountJsonPlainText: ${{ secrets.PLAY_STORE_JSON }}
    packageName: com.telegram.videoplayer
    releaseFiles: app/build/outputs/apk/release/*.apk
    track: internal
```

### Run on Specific Events

```yaml
on:
  push:
    branches: [ main ]
    paths-ignore:
      - '**.md'
      - 'docs/**'
  schedule:
    - cron: '0 0 * * 0'  # Weekly on Sunday
```

## 📝 Workflow Files Location

```
.github/
└── workflows/
    ├── android-build.yml    # Main CI/CD workflow
    ├── test-build.yml       # Quick test workflow
    └── release.yml          # Release workflow
```

## 🎯 Best Practices

1. **Test locally first** before pushing
2. **Use feature branches** for development
3. **Create PRs** for main branch changes
4. **Tag releases** with semantic versioning (v1.0.0)
5. **Keep workflows simple** and maintainable
6. **Monitor build times** and optimize as needed
7. **Clean up old artifacts** periodically

## 📚 Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Android Build Actions](https://github.com/android-actions)
- [Gradle Build Cache](https://docs.gradle.org/current/userguide/build_cache.html)
- [Android Signing](https://developer.android.com/studio/publish/app-signing)

## 💡 Tips

- **Parallel builds:** Actions run in parallel on different branches
- **Matrix builds:** Build multiple variants simultaneously
- **Caching:** Gradle cache saves ~2-5 minutes per build
- **Artifacts retention:** Adjust retention days based on needs
- **Workflow badges:** Show build status in README

## 🆘 Getting Help

If you encounter issues:

1. Check workflow logs in Actions tab
2. Review this guide
3. Check GitHub Actions documentation
4. Open an issue with:
   - Workflow name
   - Error message
   - Build logs
   - Steps to reproduce

---

**Last Updated:** 2024-02-16  
**Maintained by:** Project Team
