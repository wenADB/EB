#!/bin/bash
set -e

APP_FOLDER="${APP_FOLDER:-app}"
echo "Locating APK in ${APP_FOLDER}..."

APK_FILES=$(find "${APP_FOLDER}/build/outputs/apk" -type f -name "*.apk" 2>/dev/null || true)

if [ -z "$APK_FILES" ]; then
  echo "Warning: No APK found directly in ${APP_FOLDER}/build/outputs/apk, checking root..."
  APK_FILES=$(find . -type f -name "*.apk" 2>/dev/null || true)
fi

echo "Found APK files:"
echo "$APK_FILES"

TAG="${GITHUB_REF#refs/tags/}"
if [ "$TAG" = "$GITHUB_REF" ] || [ -z "$TAG" ]; then
  TAG="v1.0.0-build.${GITHUB_RUN_NUMBER:-1}"
fi

echo "Publishing release for tag: $TAG"

if [ -n "$APK_FILES" ]; then
  gh release create "$TAG" $APK_FILES --title "$TAG" --generate-notes || \
  gh release upload "$TAG" $APK_FILES --clobber
  echo "Release successfully created with APKs."
else
  echo "No APK files found to upload."
fi
