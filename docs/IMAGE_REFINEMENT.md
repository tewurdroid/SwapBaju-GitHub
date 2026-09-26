# Image Refinement — v2.3

An imported refinement TFLite model can now be executed after VTON compositing.

Supported adapter contract:
- input: RGB `[1,H,W,3]`
- output: RGB `[1,H,W,3]` or `[1,3,H,W]`
- configurable input/output numeric ranges
- output alpha is preserved from the composited image

The model must match this contract. If it does not, the app falls back to `IdentityImageRefiner`.
