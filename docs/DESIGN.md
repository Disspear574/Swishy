# Design system

The photo is the only bright object on screen; everything else stays near black or near white.

## Color

| Token | Light | Dark | Use |
| --- | --- | --- | --- |
| `ground` | `#EFEFF2` | `#0C0E13` | screen background |
| `surface` | `#FFFFFF` | `#161A22` | rows, sheets |
| `ink` | `#14161C` | `#F2F3F6` | text and numbers |
| `keep` | `#1F6FEB` | `#4D95FF` | swipe right |
| `trash` | `#D8412F` | `#F2604C` | swipe left |
| `move` | `#B86E00` | `#F0A52C` | swipe up, into an album |

Blue and red rather than green and red: the red–green pair is the most common form of color
blindness. With no color vision at all the icon and the word carry the direction.

## Type

Onest for text and Swishy Mono, a renamed subset of IBM Plex Mono, for numbers; both cover
Cyrillic. Onest weights are static instances cut from the variable font. Both are subset to
Latin and Cyrillic; licenses are in [licenses/fonts](../licenses/fonts).

## Swipe feedback

Offset is normalised by the threshold. The screen and the photo take on the direction color,
the icon and the word grow with the offset. Before the threshold the ring around the icon is
empty; past it the ring fills, so it is clear the release will count.

The card follows the finger through a spring with no bounce, leaves along an arc, and the next
card grows about half way with the gesture and settles with a spring after the swipe.

## Components

`SwipeDeck`, `SwipeResolution`, `ProgressTrack`, `HintBar`, `MonthRow`, `StatLine`, `HeroStat`,
`SelectableThumb`, `AlbumPickerSheet`, `ConfirmDialog`, `SwishyTextField`, `SwishyButton`,
`SwishyIconButton`, `SwishySurface`, `SwishyText`, `SectionLabel`, `EmptyState`, `VerdictIcons`.
