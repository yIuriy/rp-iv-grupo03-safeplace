# Component contracts

[Design system in Figma](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=74-99).

Reuse the component family and its supported states. Names ending in `#...` are Figma property identifiers; the implementation may use ordinary names such as `label`, `state` and `value` while preserving their meaning.

## Core behavior

| Family | Implementation contract |
| --- | --- |
| Button | Native button, 44 px control height, 16 px horizontal padding, 8 px icon gap, Inter 600 at 14/20, 8 px radius. Primary confirms, Secondary complements, Quiet cancels, Danger names a destructive operation. Loading prevents repeated activation. |
| IconButton | 44 by 44 px target and 20 px glyph. Supply an accessible action name. |
| TextField | Persistent label, 44 px control, helper below. Empty, Filled, Focus, Error, Disabled and ReadOnly are different states. |
| TextArea | Multiline text with label and helper. Respect natural text wrapping and a usable minimum height. |
| Select | Implement accessible selection using native select where appropriate. The current Figma family shows closed states, not a complete interactive dropdown. |
| SearchInput | 44 px control, 12 px padding, 8 px icon gap and 20 px search icon. Text represents placeholder or value, depending on state. |
| Checkbox / Radio / Toggle | Preserve native semantics and keyboard operation. Radios in one group share a name. |
| Alert / Toast | Include a meaningful title or message and an appropriate live-region strategy when needed. Keep failures recoverable. |
| ConfirmDialog | Explicit operation, secondary cancellation, keyboard focus contained while open, focus restored when closed. |
| NavItem / Tab | Selection represents the current location. Apply appropriate navigation or tab semantics in the application. |
| AppHeader | Desktop at 1200 px and above, compact below. Height 64 px. Menu opens accessible navigation. |
| TableRow | Preserve clear identification and status; allow the table container to scroll horizontally if necessary. |
| EmptyState | Explain the absence of results and provide a relevant recovery action. |
| HistoryItem | Show date/time, action and description in reverse chronological order. |
| RiskAreaCard | Show area, location, textual risk level, hazards, linked PPE and a consultation action. Allow its height to grow with text. |

## Canonical family map

### Web/Button/Primary

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=27-186)

Supported variants: `State=Default`, `State=Hover`, `State=Focus`, `State=Disabled`, `State=Loading`.

Properties: `Label#27:0` (text), `Show icon#27:6` (boolean), `Icon#27:12` (instance_swap), `State` (variant).

### Web/Button/Secondary

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=29-161)

Supported variants: `State=Default`, `State=Hover`, `State=Focus`, `State=Disabled`, `State=Loading`.

Properties: `Label#29:0` (text), `Show icon#29:6` (boolean), `Icon#29:12` (instance_swap), `State` (variant).

### Web/Button/Quiet

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=29-212)

Supported variants: `State=Default`, `State=Hover`, `State=Focus`, `State=Disabled`, `State=Loading`.

Properties: `Label#29:18` (text), `Show icon#29:24` (boolean), `Icon#29:30` (instance_swap), `State` (variant).

### Web/Button/Danger

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=29-255)

Supported variants: `State=Default`, `State=Hover`, `State=Focus`, `State=Disabled`, `State=Loading`.

Properties: `Label#29:36` (text), `Show icon#29:42` (boolean), `Icon#29:48` (instance_swap), `State` (variant).

### Web/IconButton

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=30-146)

Supported variants: `State=Default`, `State=Hover`, `State=Focus`, `State=Disabled`.

Properties: `Icon#30:0` (instance_swap), `State` (variant).

### Web/TextField

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=31-196)

Supported variants: `State=Empty`, `State=Filled`, `State=Focus`, `State=Error`, `State=Disabled`, `State=ReadOnly`.

Properties: `Label#31:0` (text), `Value#31:7` (text), `Placeholder#31:14` (text), `Helper#31:21` (text), `State` (variant).

### Web/TextArea

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=32-165)

Supported variants: `State=Default`, `State=Error`.

Properties: `Label#32:0` (text), `Value#32:3` (text), `Helper#32:6` (text), `State` (variant).

### Web/Select

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=33-173)

Supported variants: `State=Empty`, `State=Filled`, `State=Disabled`.

Properties: `Label#33:0` (text), `Value#33:4` (text), `Placeholder#33:8` (text), `State` (variant).

### Web/Checkbox

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=34-177)

Supported variants: `State=Unchecked`, `State=Checked`, `State=Disabled`.

Properties: `Label#34:0` (text), `State` (variant).

### Web/Radio

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=35-169)

Supported variants: `State=Unchecked`, `State=Checked`, `State=Disabled`.

Properties: `Label#35:0` (text), `State` (variant).

### Web/Toggle

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=35-182)

Supported variants: `State=Off`, `State=On`, `State=Disabled`.

Properties: `Label#35:4` (text), `State` (variant).

### Web/NavItem

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=40-196)

Supported variants: `State=Default`, `State=Active`, `State=Hover`.

Properties: `Label#40:0` (text), `Icon#40:4` (instance_swap), `State` (variant).

### Web/Tab

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=41-197)

Supported variants: `State=Default`, `State=Active`.

Properties: `Label#41:0` (text), `State` (variant).

### Web/Alert/info

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=37-154)

Default size: 960 by 100 px.

Properties: `Title#37:0` (text), `Body#37:1` (text), `Dismissible#37:2` (boolean).

### Web/Alert/success

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=37-172)

Default size: 960 by 100 px.

Properties: `Title#37:3` (text), `Body#37:4` (text), `Dismissible#37:5` (boolean).

### Web/Alert/warning

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=37-182)

Default size: 960 by 100 px.

Properties: `Title#37:6` (text), `Body#37:7` (text), `Dismissible#37:8` (boolean).

### Web/Alert/danger

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=37-192)

Default size: 960 by 100 px.

Properties: `Title#37:9` (text), `Body#37:10` (text), `Dismissible#37:11` (boolean).

### Web/ConfirmDialog

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=38-166)

Default size: 624 by 256 px.

Properties: `Title#38:0` (text), `Body#38:1` (text).

### Web/Toast

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=39-168)

Default size: 624 by 64 px.

Properties: `Message#39:0` (text).

### Web/Icon/Plus

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-342)

Default size: 20 by 20 px.

Properties: none.

### Web/Icon/Check

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-347)

Default size: 20 by 20 px.

Properties: none.

### Web/Icon/X

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-352)

Default size: 20 by 20 px.

Properties: none.

### Web/Icon/Info

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-357)

Default size: 20 by 20 px.

Properties: none.

### Web/Icon/Alert triangle

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-362)

Default size: 20 by 20 px.

Properties: none.

### Web/Icon/Search

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-367)

Default size: 20 by 20 px.

Properties: none.

### Web/Icon/Chevron down

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-373)

Default size: 20 by 20 px.

Properties: none.

### Web/Icon/Chevron right

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-378)

Default size: 20 by 20 px.

Properties: none.

### Web/Icon/Arrow left

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-383)

Default size: 20 by 20 px.

Properties: none.

### Web/Icon/Calendar

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-388)

Default size: 20 by 20 px.

Properties: none.

### Web/Icon/Paperclip

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-393)

Default size: 20 by 20 px.

Properties: none.

### Web/Icon/Clipboard

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-398)

Default size: 20 by 20 px.

Properties: none.

### Web/Icon/Package

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-404)

Default size: 20 by 20 px.

Properties: none.

### Web/Icon/Map pin

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-409)

Default size: 20 by 20 px.

Properties: none.

### Web/Icon/Users

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-414)

Default size: 20 by 20 px.

Properties: none.

### Web/Icon/More horizontal

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-419)

Default size: 20 by 20 px.

Properties: none.

### Web/Icon/Filter

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-424)

Default size: 20 by 20 px.

Properties: none.

### Web/Icon/Loader

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-429)

Default size: 20 by 20 px.

Properties: none.

### Web/Icon/Check circle

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=26-435)

Default size: 20 by 20 px.

Properties: none.

### Web/TableRow

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=46-286)

Default size: 1064 by 72 px.

Properties: `Name#46:0` (text), `Identifier#46:1` (text).

### Web/EmptyState

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=48-335)

Default size: 1344 by 176 px.

Properties: `Title#48:0` (text), `Body#48:1` (text).

### Web/HistoryItem

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=56-370)

Default size: 648 by 112 px.

Properties: `Meta#56:0` (text), `Title#56:1` (text), `Description#56:2` (text), `Icon#56:3` (instance_swap).

### Web/RiskAreaCard

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=61-504)

Default size: 520 by 336 px.

Properties: `Title#61:0` (text), `Location#61:1` (text), `Hazards#61:2` (text), `PPE#61:3` (text), `Risk badge#61:4` (instance_swap).

### Web/SearchInput

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=79-129)

Supported variants: `State=Empty`, `State=Filled`, `State=Focus`, `State=Disabled`.

Properties: `Text#79:4` (text), `State` (variant).

### Web/AppHeader

[Open component](https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=79-597)

Supported variants: `Viewport=Desktop`, `Viewport=Compact`.

Properties: `Role#79:7` (text), `Viewport` (variant).
