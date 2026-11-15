#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

export JAVA_HOME="/usr/lib/jvm/java-21-amazon-corretto"

# 1. Resolve Keystore Passwords from CLI arguments or Environment Variables
# CLI Argument 1: Store Password (or fallback to env variable REMINDFLOW_STORE_PASSWORD)
# CLI Argument 2: Key Password (or fallback to env variable REMINDFLOW_KEY_PASSWORD)
STORE_PASS="${1:-${REMINDFLOW_STORE_PASSWORD:-remindflow123}}"
KEY_PASS="${2:-${REMINDFLOW_KEY_PASSWORD:-$STORE_PASS}}"

echo "============================================="
echo "Building RemindFlow APKs (Release & Debug)..."
echo "============================================="
echo "ℹ️  Using signing credentials from CLI / Environment variables:"
echo "   REMINDFLOW_STORE_PASSWORD: [PROTECTED - ${#STORE_PASS} chars]"
echo "   REMINDFLOW_KEY_PASSWORD:   [PROTECTED - ${#KEY_PASS} chars]"
echo "---------------------------------------------"

# 2. Invoke Gradle passing credentials securely via CLI project properties
./gradlew assembleDebug assembleRelease \
    -PREMINDFLOW_STORE_PASSWORD="$STORE_PASS" \
    -PREMINDFLOW_KEY_PASSWORD="$KEY_PASS"

# Read version from gradle.properties (fallback to 1.0.0 if missing)
VERSION=$(grep -E '^[[:space:]]*version[[:space:]]*=' "$DIR/gradle.properties" | cut -d'=' -f2 | tr -d '[:space:]')
VERSION="${VERSION:-1.0.0}"

# 3. Organize APKs: version-specific files inside preview/apks/<version>/ and latest in preview/apks/
APK_DIR="$DIR/preview/apks"
VERSION_DIR="$APK_DIR/$VERSION"
mkdir -p "$VERSION_DIR"

# Clean up any obsolete loose APKs in project root, preview root, or directly in apks root
rm -f "$DIR"/*.apk "$DIR/preview"/*.apk "$APK_DIR"/remindflow-[0-9]*.apk

# Prod / Release APK
if [ -f "$DIR/app/build/outputs/apk/release/remindflow-${VERSION}.apk" ]; then
    # Version folder
    cp "$DIR/app/build/outputs/apk/release/remindflow-${VERSION}.apk" "$VERSION_DIR/remindflow-${VERSION}.apk"
    # Latest in apks folder
    cp "$DIR/app/build/outputs/apk/release/remindflow-${VERSION}.apk" "$APK_DIR/remindflow-latest.apk"
fi

# Debug APK
if [ -f "$DIR/app/build/outputs/apk/debug/remindflow-${VERSION}-debug.apk" ]; then
    # Version folder
    cp "$DIR/app/build/outputs/apk/debug/remindflow-${VERSION}-debug.apk" "$VERSION_DIR/remindflow-${VERSION}-debug.apk"
    # Latest in apks folder
    cp "$DIR/app/build/outputs/apk/debug/remindflow-${VERSION}-debug.apk" "$APK_DIR/remindflow-latest-debug.apk"
fi

echo ""
echo "✅ Both APKs built successfully!"
echo "--------------------------------------------------------"
echo "Version Folder (preview/apks/${VERSION}/):"
ls -lh "$VERSION_DIR"
echo ""
echo "Latest (preview/apks/):"
ls -lh "$APK_DIR"/*.apk
echo "--------------------------------------------------------"
