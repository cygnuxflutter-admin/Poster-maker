# Create/Update .gitignore for Android Project

The current `.gitignore` is generic and lacks specific rules for Android, Gradle, and local environment files. This can lead to unnecessary or sensitive files being tracked in the repository.

## User Review Required

> [!IMPORTANT]
> The updated `.gitignore` will include entries for `local.properties` and `key.properties`. If you have already committed these files, you will need to manually remove them from the Git index using `git rm --cached <file>` for the ignore rules to take effect.

## Proposed Changes

### Project Root

#### [MODIFY] [.gitignore](file:///E:/Aayush/android/Poster-maker/.gitignore)
Update the root `.gitignore` with comprehensive rules for:
- Gradle build artifacts (`.gradle/`, `build/`)
- Local configuration (`local.properties`, `key.properties`)
- Android Studio / IntelliJ IDE files (`.idea/`, `*.iml`)
- Keystore files (`*.jks`, `*.keystore`, `*.pepk`)
- OS-specific files (`.DS_Store`, `Thumbs.db`)
- Tool-specific artifacts (`.artifacts/`)

## Verification Plan

### Manual Verification
- Check the project root for files that should be ignored but are still tracked.
- Verify that `git status` no longer shows ignored patterns (if they weren't already tracked).
