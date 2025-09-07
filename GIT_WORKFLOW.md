# Git Workflow Guide

## 🌿 Branch Structure

This project follows a **Git Flow** branching model with the following branches:

### Main Branches
- **`main`** - Production-ready code, tagged releases only
- **`develop`** - Integration branch for ongoing development

### Feature Branches
- **`feature/feature-name`** - New features and enhancements
- **`bugfix/bug-description`** - Bug fixes
- **`hotfix/critical-fix`** - Critical production fixes

## 🚀 Release Workflow

### Current Release Status
- **Current Version**: `v1.0.0`
- **Release Date**: Initial release with Ollama integration
- **Main Branch**: Protected for releases only

### Creating a New Release

1. **Switch to develop branch**:
   ```bash
   git checkout develop
   git pull origin develop
   ```

2. **Create feature branch**:
   ```bash
   git checkout -b feature/new-feature-name
   ```

3. **Develop your feature**:
   - Make commits with clear messages
   - Test thoroughly
   - Update documentation if needed

4. **Merge back to develop**:
   ```bash
   git checkout develop
   git merge feature/new-feature-name
   git push origin develop
   ```

5. **Create release branch** (when ready for release):
   ```bash
   git checkout -b release/v1.1.0
   git push origin release/v1.1.0
   ```

6. **Final testing and preparation**:
   - Run all tests
   - Update version numbers
   - Update CHANGELOG.md
   - Final testing

7. **Merge to main and tag**:
   ```bash
   git checkout main
   git merge release/v1.1.0
   git tag -a v1.1.0 -m "Release v1.1.0: Feature description"
   git push origin main
   git push origin v1.1.0
   ```

8. **Clean up**:
   ```bash
   git checkout develop
   git merge release/v1.1.0
   git branch -d release/v1.1.0
   git push origin develop
   ```

## 🔧 Development Commands

### Daily Development
```bash
# Start new feature
git checkout develop
git pull origin develop
git checkout -b feature/my-new-feature

# Work on feature
git add .
git commit -m "feat: add new functionality"

# Push feature branch
git push origin feature/my-new-feature

# Merge to develop when ready
git checkout develop
git merge feature/my-new-feature
git push origin develop
```

### Hotfix for Production
```bash
# Create hotfix from main
git checkout main
git pull origin main
git checkout -b hotfix/critical-bug-fix

# Fix the issue
git add .
git commit -m "fix: resolve critical production issue"

# Merge to main and develop
git checkout main
git merge hotfix/critical-bug-fix
git tag -a v1.0.1 -m "Hotfix v1.0.1: Critical bug fix"
git push origin main
git push origin v1.0.1

git checkout develop
git merge hotfix/critical-bug-fix
git push origin develop
```

## 📋 Commit Message Convention

Use conventional commits for clear history:

- `feat:` - New features
- `fix:` - Bug fixes
- `docs:` - Documentation changes
- `style:` - Code style changes (formatting, etc.)
- `refactor:` - Code refactoring
- `test:` - Adding or updating tests
- `chore:` - Maintenance tasks

### Examples:
```bash
git commit -m "feat: add user authentication system"
git commit -m "fix: resolve memory leak in conversation service"
git commit -m "docs: update API documentation"
git commit -m "test: add unit tests for chat handler"
```

## 🏷️ Version Tagging

### Semantic Versioning (SemVer)
- **MAJOR** (1.0.0): Breaking changes
- **MINOR** (0.1.0): New features, backward compatible
- **PATCH** (0.0.1): Bug fixes, backward compatible

### Creating Tags
```bash
# Annotated tag (recommended)
git tag -a v1.1.0 -m "Release v1.1.0: Enhanced chat functionality"

# Push tag to remote
git push origin v1.1.0

# List all tags
git tag -l

# Show tag details
git show v1.1.0
```

## 🔒 Branch Protection Rules

### Main Branch Protection
- **Direct pushes**: Disabled
- **Required reviews**: 1 reviewer minimum
- **Status checks**: All tests must pass
- **Branch up-to-date**: Required before merging

### Develop Branch Protection
- **Direct pushes**: Allowed for developers
- **Required reviews**: Recommended for major changes
- **Status checks**: All tests must pass

## 🚨 Emergency Procedures

### Critical Production Fix
1. Create hotfix branch from main
2. Fix the issue immediately
3. Test thoroughly
4. Merge to main and tag
5. Merge to develop
6. Deploy immediately

### Rollback Procedure
```bash
# Rollback to previous version
git checkout main
git reset --hard v1.0.0
git push --force-with-lease origin main

# Create rollback tag
git tag -a v1.1.1-rollback -m "Rollback to v1.0.0"
git push origin v1.1.1-rollback
```

## 📊 Branch Status

### Current Branches
- ✅ `main` - Production ready (v1.0.0)
- ✅ `develop` - Development integration
- 🔄 `feature/*` - Active feature development

### Release History
- **v1.0.0** - Initial release with Ollama integration
  - Features: Chat interface, model management, web UI
  - Status: ✅ Released and stable

## 🛠️ Development Setup

### Prerequisites
- Git 2.30+
- Java 17+
- Gradle 7.0+
- Docker (for Ollama)

### Initial Setup
```bash
# Clone repository
git clone <repository-url>
cd koog-agent-deep-research

# Setup branches
git checkout develop
git pull origin develop

# Install dependencies
./gradlew build
```

### Daily Workflow
1. Checkout develop branch
2. Pull latest changes
3. Create feature branch
4. Develop and test
5. Merge back to develop
6. Create release when ready

## 📝 Best Practices

1. **Always pull before starting work**
2. **Use descriptive commit messages**
3. **Test before merging**
4. **Keep feature branches small and focused**
5. **Regularly sync with develop**
6. **Document breaking changes**
7. **Use pull requests for code review**
8. **Tag releases immediately after merging**

## 🆘 Troubleshooting

### Common Issues
- **Merge conflicts**: Resolve locally, test, then push
- **Failed tests**: Fix issues before merging
- **Branch divergence**: Rebase or merge as appropriate
- **Lost commits**: Use `git reflog` to recover

### Getting Help
- Check Git documentation
- Review this workflow guide
- Ask team members for assistance
- Use `git log --oneline` to understand history
