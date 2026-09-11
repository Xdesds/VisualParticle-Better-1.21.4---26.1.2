# VisualParticle Better

Client-side Fabric foundation for Minecraft Java Edition 1.21.11 with a custom
NanoVG/OpenGL HUD and ClickGUI renderer.

Press **Right Shift** in game to open ClickGUI.

## ClickGUI

The interface follows the supplied dark dashboard references:

- rounded full-window panel with a fixed category sidebar
- Combat, Movement, Visuals, Player, Misc, and Settings categories
- selectable module cards with animated state switches
- a dedicated settings panel for every module
- draggable sliders, boolean toggles, mode selectors, and keybind badges
- vector Inter text and Material Icons Round symbols
- responsive sizing based on Minecraft's logical window dimensions

At a native 1920x1080 GUI surface the panel is capped at 1600x900 and centered.
At other resolutions and Minecraft GUI scales, all spacing, radii, text, icons,
cards, and controls use one proportional scale factor.

## Requirements

- JDK 21
- A legally owned Minecraft Java Edition account for authenticated play

## Run

The repository is in a directory with Cyrillic characters, so use the included
launcher. It temporarily maps the project to an ASCII-only drive while Gradle
runs:

```powershell
powershell -ExecutionPolicy Bypass -File .\dev.ps1 runClient
```

Build the distributable mod:

```powershell
powershell -ExecutionPolicy Bypass -File .\dev.ps1 build
```

The JAR is written to `build/libs/visualparticle-better-0.0.8.jar`.

## Rendering API

Build a `UiDrawList` during HUD or screen preparation and submit it through
`ClientCore.getInstance().renderer().submit(graphics, drawList)`.

Available primitives include solid and gradient rectangles, antialiased rounded
rectangles and outlines, circles, lines, nested scissors, vector text, and icon
glyphs. NanoVG executes the command list in Minecraft's picture-in-picture GUI
draw phase, preserving the 1.21.11 extraction/draw ordering.

## Fonts and icons

- Inter is bundled under the SIL Open Font License 1.1.
- Material Icons Round is bundled under Apache License 2.0.
- The complete license texts are next to the font resources.

The current distributable bundles the Windows NanoVG native library.
