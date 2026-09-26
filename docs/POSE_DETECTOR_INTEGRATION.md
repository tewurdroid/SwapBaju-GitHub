# Pose Detector Integration — v2.8

PoseDetector prioritizes an imported private pose_landmarker_full.task, then falls back to the bundled asset. Imported bytes are passed through MediaPipe BaseOptions.setModelAssetBuffer(). A genuine compatible .task model remains required.
