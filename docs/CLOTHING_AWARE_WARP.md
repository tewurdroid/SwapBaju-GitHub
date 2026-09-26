# Clothing-aware warp — v0.9

The garment warp now accepts a clothing segmentation mask.

Pipeline:

1. Garment foreground extraction
2. Garment segmentation mask
3. Landmark-based geometric warp
4. Mask resize
5. Edge feathering
6. Body/occlusion mask multiplication
7. Alpha rendering

This prevents transparent/background pixels from being treated as garment
content and gives the renderer a clean visibility boundary.

## Current limitation

The warp itself is still geometric. It does not infer realistic folds,
sleeve deformation, lighting, shadows, or hidden body geometry.

Those require a learned dense-correspondence and image-synthesis model.
