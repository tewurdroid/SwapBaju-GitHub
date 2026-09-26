#!/usr/bin/env bash
set -e
test -f app/src/main/assets/pose_landmarker_full.task || {
  echo "ERROR: missing app/src/main/assets/pose_landmarker_full.task"
  exit 1
}
echo "MediaPipe model present."
