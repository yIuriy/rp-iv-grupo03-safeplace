# SafePlace design foundations

Version 1.0.0, exported on September 6, 2026.

[Open the Figma design system](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=74-99).

This package connects the SafePlace brand kit to the web interface. Figma contains the editable components and visual references. This folder contains their exact token values, text styles, source links and original SVG assets.

## Contents

- `tokens.json`: 67 Figma variables and 10 text styles. This is the documented SafePlace snapshot format, not DTCG.
- `tokens.css`: the same variables as CSS custom properties, preserving semantic aliases, plus text-style classes.
- `figma-map.json`: 44 component families, their properties, supported variants, reference frames and asset sources.
- `assets/logo-primary.svg` and `assets/symbol-primary.svg`: original Figma exports with outlined logo text.
- `assets/icons/`: 19 original icon exports from the Figma library.
- `component-contracts.md`: implementation rules and direct links to each family.

This folder retains the Figma foundation snapshot. The React implementation now lives in [`frontend/src/shared/components`](../src/shared/components/) and [`frontend/src/shared/layout`](../src/shared/layout/). See the [component guide](README.md) for the interactive catalog, usage and validation. Business operations and persistence remain the responsibility of feature code.

## Use the foundations

Load `tokens.css` before application styles. For example:

```css
@import "./tokens.css";

.application {
  color: var(--sp-text-primary);
  background: var(--sp-surface-page);
  font-family: var(--sp-font-body), sans-serif;
}

.panel {
  padding: var(--sp-space-24);
  border: var(--sp-stroke-default) solid var(--sp-border-default);
  border-radius: var(--sp-radius-8);
  background: var(--sp-surface-white);
}
```

Use classes such as `sp-type-heading-md`, `sp-type-body-md` and `sp-type-label-md` when their role matches the element. The typography classes intentionally do not set semantic HTML elements, color or margins.

### Fonts and assets

Load Manrope 600 and 700 and Inter 400, 500 and 600 before comparing screenshots. The React frontend now bundles these fonts through pinned Fontsource packages. This snapshot folder does not contain font binaries. A fallback sans-serif face changes line lengths and is not a fidelity reference.

Use the complete logo SVG at 160 px wide in the desktop header and 144 px in the compact header, preserving its aspect ratio. The SVG wordmark is outlined, so it does not depend on browser text rendering.

Icons use a 20 by 20 px box. Their geometry must remain proportional. The exported icon files preserve their original color; if the interface needs another semantic color, apply the SVG as a CSS mask with that color or export the matching Figma state. Provide an accessible name on icon-only controls and hide decorative icons from assistive technology.

## Layout contract

| Viewport width | Navigation | Content padding | Layout |
| --- | --- | --- | --- |
| 1200 px and above | 216 px sidebar | 32 px | Desktop columns as needed |
| 768 to 1199 px | Menu opens navigation | 24 px | Columns when content fits |
| Below 768 px | Menu opens navigation | 16 px | One column |

The header is 64 px high. Use 24 px between content columns. Inputs and buttons use a 44 px control height; longer labels can require a greater minimum height. Keep text readable instead of shrinking it to fit.

The Figma examples include 1344 px application frames, a 768 px tablet reference and a 360 px mobile reference. The outer documentation boards include titles and margins; compare the application frames listed in `figma-map.json`.

CSS custom properties cannot be used directly as ordinary media-query conditions. Keep literal breakpoints in media queries synchronized with `layout/tablet-breakpoint` and `layout/desktop-breakpoint` in the token file.

The compact Menu examples specify appearance. Implement its open state, focus handling and keyboard controls in the web application. A table may use a labeled region with horizontal scrolling on narrow screens.

## Accessibility and behavior

RNF11 in the project documentation targets WCAG 2.1 AA. The design system establishes visual and interaction rules; Figma inspection does not certify the future website.

- Use semantic buttons, inputs, labels, radio groups and checkboxes.
- Keep a visible keyboard focus. Buttons use a 2 px ring with a 4 px offset; the supplied input focus states use a 2 px border.
- Associate field help and errors using `aria-describedby`. Set `aria-invalid` when the input is invalid.
- Use status messages for operation results. Reserve assertive error announcements for urgent problems.
- A modal needs a title, an initial focus target, contained keyboard navigation, Escape handling and focus returned to its opener.
- Prevent repeat submission during loading while keeping the operation label available.
- Show status using meaningful text as well as color.
- Enforce permissions and validation in the application. Disabled styling is not an authorization mechanism.

References: [WCAG 2.1 text contrast](https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html) and [WAI-ARIA modal dialog pattern](https://www.w3.org/WAI/ARIA/apg/patterns/dialog-modal/).

### Measured color pairs

Ratios below are calculated from the exported sRGB values. Normal text requires at least 4.5:1; large text requires at least 3:1. This check covers these pairs only, not every future screen.

| Foreground | Background | Contrast |
| --- | --- | --- |
| `text/primary` | `surface/white` | 14.89:1 |
| `text/secondary` | `surface/page` | 5.85:1 |
| `text/inverse` | `brand/primary` | 9.93:1 |
| `feedback/info` | `feedback/info-bg` | 6.35:1 |
| `feedback/success` | `feedback/success-bg` | 6.03:1 |
| `feedback/warning` | `feedback/warning-bg` | 5.79:1 |
| `feedback/danger` | `feedback/danger-bg` | 5.99:1 |
| `control/border` | `surface/white` | 6.29:1 |

## Fidelity checklist for implementation

1. Load the exact font families and weights.
2. Use the original SVG assets and the semantic variables.
3. Compare the relevant Figma application frame at the same viewport width and browser zoom of 100%.
4. Check spacing, wrapping, control heights and icon alignment.
5. Check every supported state in `figma-map.json`, plus real validation and loading behavior.
6. Test keyboard operation, text zoom, content with long labels, and narrow viewports.
7. Check the current project requirements before adding workflow fields or actions.

The sample names, classifications and records in Figma are fictitious. They demonstrate composition and do not change the project's functional requirements.

## Maintaining the system

Update the canonical component in Figma and keep its instances connected. Export variables, text styles and component metadata again after changes. Preserve variable aliases in CSS. Update the JSON and CSS together, and keep the component map tied to actual node IDs.

This folder is a versioned snapshot. It does not synchronize automatically with Figma. The [component guide](README.md) maps the implemented React components to their Figma nodes. No Code Connect integration has been configured.
