# SafePlace component library

The React implementation of the [SafePlace Figma design system](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=74-99). Use these shared components when building feature screens so the team uses the same fonts, assets, spacing and interaction states.

## Run the catalog

From `frontend/`, using Node.js 22.13 or newer:

```sh
npm ci
npm run dev
```

- `/design-system`: interactive component catalog. The root `/` also opens it while the application is being built.
- `/design-system/examples/risk-areas`: composed reference screen with fictional data, filters and consultation dialogs.
- The reference navigation links lead to related catalog examples. Registration opens an explanation of the demonstration; it does not create a record.

The catalog does not call the backend. Its form simulates a short wait, validates two required fields, and keeps selected files on the current page. Reloading clears demonstration state. Feature permissions, domain validation, persistence and offline synchronization belong to the application.

## Import the shared components

`src/main.tsx` imports `src/index.css` once. That stylesheet loads the fonts, the existing token snapshot and the shared component styles. Feature screens should not import the global stylesheet again.

```tsx
import { Button, TextField } from '../../shared/components'

export function SectorForm() {
  return <form onSubmit={event => event.preventDefault()}>
    <TextField label="Setor" name="sector" helper="Informe o setor onde ocorreu o fato." />
    <Button type="submit" icon="check">Salvar registro</Button>
  </form>
}
```

Import paths are relative to the feature file. Components live in [`src/shared/components`](../src/shared/components/); application layout lives in [`src/shared/layout`](../src/shared/layout/). Complete usage examples are in [`src/features/design-system`](../src/features/design-system/).

## Component API and Figma map

| Component | Main properties | Figma reference |
| --- | --- | --- |
| `Button` | `variant`: `primary`, `secondary`, `quiet`, `danger`; `icon`, `loading`, native button props | [Primary](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=27-186), [Secondary](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=29-161), [Quiet](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=29-212), [Danger](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=29-255) |
| `IconButton` | Required `icon` and accessible `label`; button props | [30:146](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=30-146) |
| `TextField` | `label`, `helper`, `error`, native input props including `ref` | [31:196](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=31-196) |
| `TextArea` | `label`, `helper`, `error`, native textarea props | [32:165](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=32-165) |
| `Select` | `label`, `helper`, `error`, native select props and `option` children | [33:173](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=33-173) |
| `SearchInput` | Required `label`, `hiddenLabel` (default `true`), native input props | [79:129](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=79-129) |
| `Checkbox`, `Radio`, `Toggle` | `label`, native input props; `name` shared by related radios | [34:177](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=34-177), [35:169](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=35-169), [35:182](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=35-182) |
| `FileUpload` | `label`, `helper`, `error`, native file input props | Extension for the attachment examples; native file chooser |
| `Alert` | `tone`, `title`, children, `live`: `off` (default), `polite`, `assertive` | [37:154](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=37-154) and tone variants |
| `Badge` | `tone`: `info`, `success`, `warning`, `danger`; text children | [12:78](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=12-78) |
| `Toast` | `message`, `tone`, `onDismiss`; empty message hides the visual notification | [39:168](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=39-168) |
| `Dialog` | `open`, `title`, `description`, `onClose`, children | Native modal behavior shared by confirmation and mobile navigation |
| `ConfirmDialog` | `open`, `title`, `description`, `confirmLabel`, `cancelLabel`, `loading`, `onConfirm`, `onClose` | [38:166](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=38-166) |
| `Tabs` | `label`, controlled `value`, `onValueChange`, `items` (`id`, `label`, `content`) | [41:197](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=41-197) |
| `Table`, `TableRow` | Required table `caption`, semantic `thead`/`tbody`/`th`/`td` children | [46:286](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=46-286) |
| `EmptyState` | `title`, `description`, optional `actionLabel` and `onAction` | [48:335](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=48-335) |
| `HistoryItem` | `dateTime` (ISO), `dateLabel`, `title`, children; place in an ordered list | [56:370](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=56-370) |
| `RiskAreaCard` | `title`, `location`, `hazards`, `ppe`, `riskLabel`, `riskTone`, `onConsult` | [61:504](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=61-504) |
| `Icon` | `name` (one of 19 exported glyphs), optional accessible `label` | [Icon inventory](figma-map.json) |
| `NavItem` | `href`, `icon`, `active`, link text, native anchor props | [40:196](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=40-196) |
| `AppHeader`, `AppShell` | Shell: `roleLabel`, `navigation`, children; manages compact menu | [79:597](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=79-597) |

`Button` defaults to `type="button"`. Use `type="submit"` explicitly for form submission. Pass `loading={saving}` while awaiting a real operation; it disables repeated activation and keeps the visible action label. Hover and focus are real browser states, not a `state` prop to set manually.

Inputs support both uncontrolled usage (`defaultValue`) and controlled usage (`value` plus `onChange`). Do not mix these on the same input. In React 19, the native `ref` prop passes through the field component. Labels and helper/error IDs are generated with `useId`; an explicit `id` is supported when needed. An error replaces the helper text and sets `aria-invalid`.

```tsx
<Select label="Setor" name="sector" defaultValue="">
  <option value="">Selecione o setor</option>
  <option value="loading">Área de carga</option>
</Select>
<IconButton icon="more-horizontal" label="Mais opções do EPI" onClick={openOptions} />
```

## Layout and fidelity

| Viewport | Header and navigation | Main padding | Reference cards |
| --- | --- | --- | --- |
| At least 1200 px | 64 px header, 216 px sidebar | 32 px | Two columns, 24 px gap |
| 768 to 1199 px | 64 px compact header, modal menu | 24 px | Two columns, 24 px gap |
| Below 768 px | Compact header and modal menu | 16 px | One column |

The desktop reference at 1344 px has 520 px cards. The tablet reference at 768 px has 348 px cards. At 360 px, cards are 328 px wide. A card starts at 336 px tall and grows with wrapped text. Inputs and buttons use a 44 px standard control size; long button labels may increase height to avoid clipping. Tables keep their columns and scroll inside a labeled, keyboard-focusable region on small screens.

Fonts are self-hosted through pinned Fontsource packages: Manrope 600/700 and Inter 400/500/600, Latin subset (including Portuguese accents). The Vite build includes the font files; it makes no runtime request to a font CDN. Font licenses are distributed in [`public/licenses`](../public/licenses/). The outlined logo and all 19 SVGs are the exact existing Figma exports. CSS masks recolor icons without redrawing their paths.

Use the `--sp-*` semantic tokens from [`tokens.css`](tokens.css). `docs/design-system/` retains the design snapshot; `frontend/design-system/` is the runtime copy. They are not automatically synchronized. Update the corresponding snapshots together when the design changes. Do not create a second button or copy colors into each feature.

## Interaction and accessibility

- Native buttons, form fields, radio groups and switches preserve keyboard behavior.
- `Dialog` uses native `showModal()`, a named heading, contained focus, Escape handling and focus restoration. `ConfirmDialog` initially focuses the safer cancellation action. While loading, confirmation cannot be repeated or closed with Escape.
- Tabs support Left/Right, Home and End. Only the selected tab is in the tab order; inactive panels are hidden.
- A skip link leads to the main content. The compact menu closes on selection, Escape, its close button or switching to the desktop breakpoint.
- Static alert samples do not announce themselves. Choose `live="polite"` for routine dynamic results or `live="assertive"` for an urgent error. Keep a `Toast` mounted and update its message for brief results; errors that require action should remain beside the relevant content.
- Status labels always contain text. Interface tones do not establish a technical risk classification.
- Reduced motion disables spinner animation. Test the completed feature with keyboard and zoom as well as visual comparison.

## Verify changes

```sh
npm run build
npm run lint
npx playwright install chromium
npm test
```

On Linux environments missing browser system libraries, Playwright reports the necessary installation steps. Tests run in Chromium against the catalog and composed screen. They check real input behavior, validation and focus, loading, dialog focus restoration, tabs, table search/selection, risk filters, asset loading and the three reference widths. `test-results/` contains generated screenshots. `npm run test:ui` opens the interactive test runner.

Compare the reference route with Figma node `60:463` at the same 1344 px width and 100% browser zoom. The demonstration footer identifies fictional data and is intentionally additional to the Figma application frame. Native select popup menus and file pickers follow the browser/operating system. The closed controls follow the design system.

## Team workflow

1. Build each feature using the existing shared components and shell.
2. Keep feature state and API calls inside the feature. The component receives data, callbacks and loading/error props.
3. If a shared component needs to change, update it in one place and add a catalog example for the new behavior.
4. Run the checks above and compare the affected screen with Figma at desktop and mobile widths.
5. Review the shared change with the group before replacing established visual conventions.

The library provides a common implementation; fidelity still depends on using it consistently when composing each screen. The complete application and its business workflows are separate work. This delivery does not publish a package or configure Figma Code Connect.
