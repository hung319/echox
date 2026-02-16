# GitHub Actions Workflows

This directory contains CI/CD workflows for the Telegram Video Player Android app.

## 📁 Workflow Files

### 1. `android-build.yml` - Main CI/CD Pipeline
**Triggers:**
- Push to `main`, `master`, or `develop` branches
- Pull requests to `main` or `master`
- Manual dispatch (workflow_dispatch)
- Tags starting with `v*`

**What it does:**
- ✅ Sets up JDK 17 and Android SDK
- ✅ Builds Debug APK
- ✅ Builds Release APK (unsigned)
- ✅ Runs unit tests
- ✅ Uploads APKs as artifacts (30-90 days retention)
- ✅ Creates/updates "continuous" release
- ✅ Updates "latest" release

**Outputs:**
- Debug APK: `telegram-video-player-debug.apk`
- Release APK: `telegram-video-player-release.apk`
- Test results

---

### 2. `test-build.yml` - Quick Validation
**Triggers:**
- Push to branches with `test-*` or `feature-*` prefix
- Manual dispatch

**What it does:**
- ✅ Validates project structure
- ✅ Checks Gradle configuration
- ✅ Verifies AndroidManifest.xml
- ✅ Counts source files
- ✅ Validates package structure
- ✅ Generates build summary

**Outputs:**
- Build summary report

**Note:** This workflow does NOT build an APK - it's for quick validation only.

---

### 3. `release.yml` - Official Release Pipeline
**Triggers:**
- GitHub release created/published
- Manual dispatch with version input

**What it does:**
- ✅ Builds optimized Release APK
- ✅ Builds Debug APK
- ✅ Signs APK (if keystore configured)
- ✅ Renames APKs with version number
- ✅ Generates SHA256 checksums
- ✅ Creates detailed release notes
- ✅ Creates GitHub release with APKs

**Outputs:**
- `telegram-video-player-v{version}-release.apk`
- `telegram-video-player-v{version}-debug.apk`
- `checksums.txt`

---

## 🚀 Quick Start

### First Time Setup

1. **Add GitHub Secrets:**
   - Go to: `Settings` → `Secrets and variables` → `Actions`
   - Click `New repository secret`
   - Add these secrets:

   | Name | Value | Required |
   |------|-------|----------|
   | `TELEGRAM_API_ID` | Your Telegram API ID | ✅ Yes |
   | `TELEGRAM_API_HASH` | Your Telegram API Hash | ✅ Yes |

2. **Get Telegram Credentials:**
   - Visit https://my.telegram.org
   - Log in with your phone number
   - Go to "API Development Tools"
   - Create a new application
   - Copy `API_ID` and `API_HASH`

3. **Push to trigger build:**
   ```bash
   git push origin main
   ```

4. **Check build status:**
   - Go to `Actions` tab in your repository
   - Click on the running workflow
   - Wait for completion (~5-10 minutes)

5. **Download APK:**
   - **Option A:** From workflow artifacts (click on completed workflow)
   - **Option B:** From Releases → "latest" tag

---

## 📥 How to Download APK

### Method 1: From Workflow Artifacts
1. Go to `Actions` tab
2. Click on a completed workflow run
3. Scroll to `Artifacts` section
4. Download:
   - `telegram-video-player-debug` for testing
   - `telegram-video-player-release` for production

⚠️ **Note:** Artifacts are temporary (30-90 days)

### Method 2: From Releases (Recommended)
1. Go to `Releases` tab
2. Find the release:
   - **latest** → Most recent build
   - **continuous-main** → Latest from main branch
   - **v1.x.x** → Specific version
3. Download APK from `Assets` section

✅ **Note:** Release APKs are permanent

---

## 🔧 Usage Examples

### Example 1: Auto-build on Push
```bash
# Make changes to code
git add .
git commit -m "feat: add new feature"
git push origin main

# Workflow runs automatically
# APK available in ~5-10 minutes
```

### Example 2: Manual Build
1. Go to `Actions` tab
2. Select `Android CI/CD Build`
3. Click `Run workflow` dropdown
4. Select branch (e.g., `main`)
5. Click `Run workflow` button
6. Wait for completion
7. Download from artifacts

### Example 3: Create Release Build
```bash
# Create a version tag
git tag v1.0.0
git push origin v1.0.0

# Or manually:
# 1. Actions → Release Build → Run workflow
# 2. Enter version: 1.0.0
# 3. Check "Create GitHub Release"
# 4. Run workflow
```

### Example 4: Test Branch (No Build)
```bash
# Create test branch
git checkout -b test-my-feature
git push origin test-my-feature

# Quick validation runs (no APK)
# Check Actions → Test Build
```

---

## 🎯 Workflow Comparison

| Feature | android-build.yml | test-build.yml | release.yml |
|---------|-------------------|----------------|-------------|
| Builds APK | ✅ Yes | ❌ No | ✅ Yes |
| Runs Tests | ✅ Yes | ❌ No | ❌ No |
| Creates Release | ✅ Yes | ❌ No | ✅ Yes |
| Speed | ~5-10 min | ~30 sec | ~5-10 min |
| When to use | Every commit | Quick check | Official release |

---

## 📊 Build Time Optimization

Our workflows use several optimizations:

✅ **Gradle Cache** - Saves 2-5 minutes per build
✅ **Dependency Cache** - Speeds up repeated builds
✅ **Parallel Jobs** - Multiple workflows can run simultaneously
✅ **Conditional Steps** - Skip unnecessary steps

Average build time: **5-10 minutes**

---

## 🐛 Troubleshooting

### Build fails with "TELEGRAM_API_ID not found"
**Solution:** Add secrets to repository settings

### Build fails with "SDK not found"
**Solution:** Check if `setup-android` action completed successfully

### APK not in artifacts
**Solution:** 
- Check build logs for errors
- Ensure build step completed successfully
- Look for "Upload" step in logs

### Workflow doesn't trigger
**Solution:**
- Check branch name matches trigger conditions
- Verify you have push permissions
- Check Actions are enabled in Settings

### Release not created
**Solution:**
- Ensure workflow completed successfully
- Check token permissions
- Verify release step didn't error

---

## 📝 Customization

### Change Trigger Branches
Edit `on.push.branches` in workflow file:

```yaml
on:
  push:
    branches: [ main, develop, staging ]
```

### Add More Build Variants
Add steps to workflow:

```yaml
- name: Build Staging APK
  run: ./gradlew assembleStagingDebug
```

### Custom Artifact Retention
Change `retention-days`:

```yaml
- uses: actions/upload-artifact@v4
  with:
    retention-days: 60  # Keep for 60 days
```

### Add Code Signing
See `release.yml` for signing example with keystore.

---

## 🔒 Security Notes

✅ Secrets are encrypted and not visible in logs
✅ Environment variables are injected at build time
✅ APKs are scanned by GitHub
⚠️ Release APKs are unsigned by default

To add signing, configure keystore secrets in repository settings.

---

## 📚 Additional Resources

- [Complete Guide](./.github/GITHUB_ACTIONS_GUIDE.md)
- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Android Build Actions](https://github.com/android-actions)
- [Project README](../../README.md)

---

## ✅ Checklist

Before first build:
- [ ] Add `TELEGRAM_API_ID` secret
- [ ] Add `TELEGRAM_API_HASH` secret
- [ ] Push to main/master branch
- [ ] Check Actions tab for workflow
- [ ] Wait for build completion
- [ ] Download APK from artifacts or releases

---

**Need Help?** Check [GITHUB_ACTIONS_GUIDE.md](../GITHUB_ACTIONS_GUIDE.md) for detailed documentation.
