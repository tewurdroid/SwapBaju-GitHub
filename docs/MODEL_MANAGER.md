# Model Manager — v1.4

The app now includes a diagnostics screen for local AI models.

It checks:

- whether the model file exists
- whether it is empty
- whether TensorFlow Lite can load it
- input tensor shapes
- output tensor shapes
- approximate model file size

Accessible from:

`CEK MODEL AI`

## Why this matters

A model filename alone does not establish compatibility. The diagnostics
screen provides the first layer of runtime verification before enabling a
model adapter.

The model-specific class mapping and preprocessing contract still need to
match the selected model's documentation.
