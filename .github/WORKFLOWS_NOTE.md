# GitHub Actions Workflows - Setup Note

## ⚠️ Important Setup Required

Due to GitHub App permissions, the workflow files are not stored in `.github/workflows/` directory in this repository. Instead, they are documented below and in the guide files.

## 📝 Workflow Files to Create

To enable GitHub Actions CI/CD, manually create these files in `.github/workflows/` directory:

### 1. Create `.github/workflows/android-build.yml`

See the complete file content in: **GITHUB_ACTIONS_GUIDE.md** (Section: Android Build Workflow)

Or copy from the project documentation and create manually.

**Key features:**
- Builds Debug and Release APKs
- Runs tests
- Creates releases automatically
- Caches dependencies

### 2. Create `.github/workflows/test-build.yml`

Quick validation workflow for test branches.

### 3. Create `.github/workflows/release.yml`

Official release workflow with versioning and signing support.

## 🚀 Quick Setup

1. **Create workflows directory:**
   ```bash
   mkdir -p .github/workflows
   ```

2. **Create workflow files:**
   - Copy content from GITHUB_ACTIONS_GUIDE.md
   - Or use templates from project documentation
   - Place in `.github/workflows/` directory

3. **Add GitHub Secrets:**
   - Repository Settings → Secrets → Actions
   - Add: `TELEGRAM_API_ID`
   - Add: `TELEGRAM_API_HASH`

4. **Push to GitHub:**
   ```bash
   git add .github/workflows/
   git commit -m "ci: add workflow files"
   git push
   ```

## 📚 Documentation

Complete workflow configurations and setup instructions:

- **GITHUB_ACTIONS_GUIDE.md** - Full guide with workflow YAML
- **WORKFLOWS_README.md** - Quick reference
- **GITHUB_ACTIONS_SETUP.md** - Setup summary

## ✅ Why This Approach?

GitHub Apps (like this CI system) require special `workflows` permission to create/modify files in `.github/workflows/` directory. 

**Solution:** Create workflow files manually using the provided documentation.

## 🔗 References

- Workflow templates: See GITHUB_ACTIONS_GUIDE.md
- Setup guide: See GITHUB_ACTIONS_SETUP.md
- Quick start: See WORKFLOWS_README.md

---

**Note:** Once workflow files are created manually, the CI/CD pipeline will be fully functional.
