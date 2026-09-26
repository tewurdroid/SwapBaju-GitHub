# Device hardening — v1.2

## Camera orientation

Camera capture now declares a target display rotation.

Imported images are also corrected using EXIF orientation before processing.

## Memory

Large input images are capped at a 1536-pixel longest side before VTON
processing. This reduces the risk of Android `OutOfMemoryError`.

## Processing guard

The UI prevents starting multiple VTON jobs simultaneously.

## Lifecycle

MainActivity releases its large bitmap references when destroyed.

## Model validation

Use `ModelResources.require()` before enabling a production model.

The app should not silently fall back from a missing required model if the
user has explicitly selected an AI mode.
