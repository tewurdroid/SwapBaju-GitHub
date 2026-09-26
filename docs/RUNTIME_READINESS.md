# VTON Runtime Readiness — v2.4

Before running the full pipeline, the app can validate the active:
- Human Parsing model + configuration
- Dense Correspondence model + configuration
- Image Refinement model + configuration

The checker instantiates each adapter and validates its tensor contract. It then closes the interpreter immediately.

This is a compatibility check, not a quality benchmark. A model can be tensor-compatible while still being semantically unsuitable for the intended VTON task.
