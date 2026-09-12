#!/usr/bin/env bash
#
# Idempotent development-environment bootstrap for the Rhetorica Android app.
#
# Installs the Android command-line tools + SDK packages required to build the
# app with the Gradle wrapper, then writes local.properties so Gradle can find
# the SDK. Safe to run repeatedly: already-present components are left as-is.
#
# Building the app (./gradlew assembleDebug) and running the unit tests
# (./gradlew test) only need the components installed here. Running the app in
# an emulator additionally requires the "emulator" and a "system-images;..."
# package plus a working /dev/kvm; see the note at the bottom of this file.
set -euo pipefail

ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
CMDLINE_TOOLS_VERSION="11076708"
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip"

# SDK packages needed to build/test the app. Keep these aligned with
# app/build.gradle.kts (compileSdk / build-tools) when they change.
PLATFORM_PACKAGE="platforms;android-35"
BUILD_TOOLS_PACKAGE="build-tools;35.0.0"

echo "==> Ensuring Android SDK command-line tools are installed at ${ANDROID_HOME}"
SDKMANAGER="${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager"
if [ ! -x "${SDKMANAGER}" ]; then
  echo "    Downloading command-line tools..."
  tmp_zip="$(mktemp --suffix=.zip)"
  tmp_dir="$(mktemp -d)"
  curl -fsSL -o "${tmp_zip}" "${CMDLINE_TOOLS_URL}"
  unzip -q -o "${tmp_zip}" -d "${tmp_dir}"
  mkdir -p "${ANDROID_HOME}/cmdline-tools"
  rm -rf "${ANDROID_HOME}/cmdline-tools/latest"
  mv "${tmp_dir}/cmdline-tools" "${ANDROID_HOME}/cmdline-tools/latest"
  rm -rf "${tmp_zip}" "${tmp_dir}"
else
  echo "    Command-line tools already present."
fi

echo "==> Accepting SDK licenses"
yes | "${SDKMANAGER}" --sdk_root="${ANDROID_HOME}" --licenses >/dev/null 2>&1 || true

echo "==> Installing SDK packages (platform-tools, ${PLATFORM_PACKAGE}, ${BUILD_TOOLS_PACKAGE})"
"${SDKMANAGER}" --sdk_root="${ANDROID_HOME}" \
  "platform-tools" \
  "${PLATFORM_PACKAGE}" \
  "${BUILD_TOOLS_PACKAGE}" >/dev/null

echo "==> Writing local.properties (git-ignored, machine-specific)"
echo "sdk.dir=${ANDROID_HOME}" > "$(dirname "$0")/../local.properties"

echo "==> Android SDK ready. Build with: ./gradlew assembleDebug   Test with: ./gradlew test"

# --- Optional: running the app in an emulator ---------------------------------
# The build/test flow above does NOT require an emulator. To run the app
# interactively you also need, e.g.:
#   "${SDKMANAGER}" --sdk_root="${ANDROID_HOME}" "emulator" \
#     "system-images;android-30;default;x86_64"
# and hardware virtualization (a functional /dev/kvm). Without KVM the emulator
# falls back to slow software (TCG) emulation. A lightweight AOSP ("default")
# image is more stable under software emulation than a Google-APIs image.
