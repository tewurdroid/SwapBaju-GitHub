# Segmentation integration

The app now has a pluggable `SegmentationEngine`.

## Current fallback

`HeuristicSegmentationEngine` uses pose landmarks to create a conservative
torso mask. This is deliberately simple and deterministic.

## Production upgrade

Add a compatible local MediaPipe Image Segmenter `.task` model under:

`app/src/main/assets/`

Then instantiate:

`MediaPipeSegmentationEngine(context, "your_model.task")`

The exact category mapping must be documented by the model you choose.
Do not assume that a generic segmentation model has a "clothing" class.

## Recommended production masks

The final VTON pipeline should maintain separate masks for:

- person silhouette
- hair/head
- left arm
- right arm
- hands
- face/neck
- existing clothing
- new garment

The garment should be composited behind the occluding regions.

## Important

A generic person segmentation model is not automatically a clothing
segmentation model. For accurate clothing replacement, use a model whose
labels/capabilities explicitly support the required clothing classes.
