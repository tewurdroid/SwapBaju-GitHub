# Pose Model Import — v2.7

The app can now import `pose_landmarker_full.task` through Android's document picker.

Priority:
1. Imported private model.
2. Bundled `app/src/main/assets/pose_landmarker_full.task`.
3. Unavailable.

The imported file is copied into the app's private `files/models` directory. No external storage path is required after import.

The app still does not generate or fabricate a MediaPipe model binary. A genuine compatible `.task` file is required.
