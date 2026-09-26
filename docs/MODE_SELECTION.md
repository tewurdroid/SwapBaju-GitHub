# Runtime Mode Selection — v2.5

Pipeline mode is now based on **actual adapter readiness**, not only on whether model files exist.

Rules:
- `FULL_VTON`: human parsing, correspondence, and refinement all validate successfully.
- `AI_HUMAN_PARSING`: human parsing validates, but the complete VTON stack does not.
- `FALLBACK`: human parsing is not ready.

This prevents the UI from claiming that a model is active merely because a file exists.
