# SwapBaju architecture — v0.7

```text
UI
 |
 +-- Camera / Gallery
 |
 V
Pose Detector
 |
 V
HumanParser ------------------+
 |                            |
 V                            |
Garment / Body masks          |
 |                            |
 +----------> Correspondence -+
                    |
                    V
              VtonEngine
                    |
                    V
              Bitmap Result
                    |
                    V
                 Save
```

The important design rule is that UI code must not depend on a particular
AI model. Model-specific tensor handling belongs in adapter classes.
