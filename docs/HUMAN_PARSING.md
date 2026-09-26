# Human Parsing — v2.0

The runtime supports two common per-pixel segmentation output layouts:

- `[1,H,W,C]`
- `[1,C,H,W]`

The configured `outputClasses`, input dimensions, normalization, and semantic class IDs must still match the selected model.

The app does not infer semantic class IDs from tensor shape.
