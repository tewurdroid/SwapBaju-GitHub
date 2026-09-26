# Model modes — v1.3

The application now has three logical modes:

## FALLBACK

Uses pose landmarks and deterministic geometric processing.

Works without learned VTON models.

## AI_HUMAN_PARSING

Intended to use a local human-parsing model.

The model's tensor and class mapping must be configured before it is enabled
for inference.

## FULL_VTON

Reserved for a complete local model stack:

- human parsing
- dense correspondence
- image refinement/synthesis

The app checks whether the model assets exist before selecting this mode.

Important: presence of a `.tflite` file alone does not prove compatibility.
Model adapters must still validate tensor shapes and preprocessing.
