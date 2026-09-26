# Pose Runtime — v2.6

The app now exposes an explicit runtime check for `pose_landmarker_full.task`.

Important:
- The file must be bundled at `app/src/main/assets/pose_landmarker_full.task`.
- A missing or zero-byte asset is reported as unavailable.
- This status is surfaced in diagnostics/readiness instead of being hidden behind a generic fallback message.
- The repository intentionally does not invent or embed a model binary.
