# Active Models — v1.7

v1.7 connects the active imported human-parsing TFLite model to the runtime.

Flow:
1. Import a TFLite model as **Human Parsing**.
2. The model is validated and registered as active.
3. The active file is resolved into app-private storage.
4. `VtonPipelineFactory` creates `FileTfliteHumanParser`.
5. The parser executes the active model during VTON processing.

The app does not guess segmentation class IDs. `models/human_parsing_config.json` must match the selected model's documented tensor/class contract. If garment class mappings are empty, the runtime deliberately does not claim a learned garment mask.
