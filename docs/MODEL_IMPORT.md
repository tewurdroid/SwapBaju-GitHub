# Model import — v1.5

Models can now be selected from Android storage using the system document
picker.

Supported logical roles:

1. Human Parsing
2. Dense Correspondence
3. Image Refinement

The importer validates that TensorFlow Lite can open the file and records:

- model name
- size
- input tensor shapes
- output tensor shapes
- model role
- source URI

The URI permission is persisted so the app can continue accessing the model.

## Important

Importing a model does not automatically make it compatible with the
corresponding VTON adapter.

Tensor layout, preprocessing, output semantics, class IDs, and post-processing
must still match the adapter contract.
