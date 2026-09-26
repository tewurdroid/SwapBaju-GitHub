# SwapBaju VTON Pipeline — v0.4

Current flow:

1. Person photo / CameraX
2. MediaPipe Pose Landmarker
3. Garment background preprocessing
4. Landmark-guided mesh warp
5. Torso garment mask
6. Arm/forearm occlusion corridors
7. Feathered alpha compositing
8. Save to `Pictures/SwapBaju`

## What this improves

The garment no longer has to be treated as a full rectangular image. A
simple background-removal pass attempts to preserve the garment foreground,
and the arm corridors are removed from the garment mask.

## What remains for production quality

The current background removal is a heuristic and should be replaced by a
dedicated garment segmentation model.

The current mesh uses pose landmarks only. A production VTON system should
use dense correspondence or a trained VTON model to account for:

- body contour
- sleeve deformation
- garment folds
- perspective
- pose-dependent shape
- hands/arms
- hair and neck occlusion
- shadows and lighting

Do not treat this prototype as photorealistic VTON yet.
