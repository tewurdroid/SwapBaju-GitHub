# SwapBaju Offline

Android/Kotlin prototype for an offline clothing-swap workflow.

## Build locally
1. Open in Android Studio.
2. Sync Gradle.
3. Add the required MediaPipe pose model as:
   `app/src/main/assets/pose_landmarker_full.task`
4. Build with:
   `./gradlew assembleDebug`

## Build on GitHub
Push the repository to GitHub. The workflow in `.github/workflows/android.yml`
builds the debug APK and uploads it as a workflow artifact.

## Important
This repository is a functional prototype. The current cloth stage uses
landmark-based scaling/placement and is NOT a photorealistic virtual try-on
model. A production VTON pipeline still needs clothing segmentation,
person segmentation/occlusion handling, mesh/TPS warping, and refinement.

## Current upgrade

The prototype now uses landmark-guided mesh warping and a conservative torso mask. It is still not a photorealistic learned VTON system; the next major upgrade is learned person/clothing segmentation and stronger occlusion handling.


## AI model layer

v0.6 includes a TensorFlow Lite runtime and a model-agnostic human parsing adapter. Add only license-compatible local models under `app/src/main/assets/models/` and configure their documented tensor/class mapping before enabling them.


## v0.8
Human parsing now uses an explicit configurable model contract and tensor validation. The template configuration intentionally leaves clothing class IDs empty until a compatible model is selected.

## v1.7

Active imported human-parsing TFLite models are now connected to the runtime when their tensor contract and explicit class mapping are configured. See `docs/ACTIVE_MODELS.md`.
