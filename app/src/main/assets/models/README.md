# Local AI models

Place only models whose licenses permit use and redistribution.

Recommended separation:

- `pose_landmarker_full.task`
  - MediaPipe pose model.
- `human_parsing.tflite`
  - optional human-parsing / clothing-segmentation model.
- future VTON model
  - optional learned garment-transfer/refinement model.

Important:
A `.tflite` file cannot be assumed to work just because it loads.
`TfliteHumanParser` requires the selected model's input size, normalization,
output tensor shape, and class mapping.

For a clothing parser, the class mapping must identify at minimum:
- upper garment
- lower garment where applicable
- arms/hands
- skin
- hair/head

The app intentionally refuses to guess unknown class indices.
