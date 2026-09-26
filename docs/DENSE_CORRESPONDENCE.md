# Dense Correspondence — v2.2

Dense correspondence is now connected to the warp stage.

When a compatible correspondence model returns an X/Y/confidence field:
1. The field is sampled at target-person resolution.
2. X/Y coordinates are used for backward sampling from the garment.
3. Confidence modulates output alpha.
4. Low-confidence samples remain transparent.
5. If no valid dense field is available, the existing pose/geometric warp remains the fallback.

This is a genuine dense-field warp, but its visual quality still depends on the exact correspondence model contract and training domain.
