# Mascot style guide

## Purpose
This doc defines the mascot's requirements so generated art stays consistent across mood states, both card styles, and future screens — rather than being redesigned ad hoc each time it's drawn.

## Character brief — FINALIZED
Minimalist kawaii "mochi" character named Buddie.

- **Shape**: gumdrop/bell shape, NOT a uniform oval or mochi shape — tapers inward near the top (flatter sides, narrower "head" area), with a distinctly FLAT bottom edge giving it a solid base to rest on (not a continuous sweeping curve all the way around). Perfectly symmetrical along the vertical axis. No limbs, ears, fur, or accessories.
- **Body color**: solid, bright white — reads clearly against the app's dark charcoal (#121212) background.
- **Linework**: warm dark-brown outline, eyes, and mouth (not black). The stroke should be BOLD and noticeably thick — not a delicate thin line.
- **Eyes**: wide-set, dark brown, perfectly circular, with white catchlight highlights (one large + one small) for a shiny, glossy look in the default/calm state.
- **Cheeks**: soft pastel-pink blush circles that slightly OVERLAP the lower portion of the eyes, creating a subtle layered/translucent effect — not spaced entirely apart from the eyes.
- **Proportions**: facial features clustered low and tight on the body, leaving a large blank "forehead" — classic high head-to-face-ratio kawaii geometry (infant/puppy-like, triggers a "cute" response).
- **Shading/shadow**: a distinct floating elliptical drop-shadow cast on the background beneath the character (not an internal gradient along the body's bottom edge).
- Should stay crisp and readable as small as a ~40px docked bubble and as large as an ~80px card avatar — the design's simplicity (4 features: outline, eyes, cheeks, mouth) is exactly what makes this achievable.

## Mood states
Mood is driven by the single closest deadline across submissions + exams. Each maps to one of the character's natural kawaii expressions — a distinct feature change, not just a recolor.

| App state | Trigger | Expression |
|---|---|---|
| Calm | Nothing due for several days | **Content/Happy (default)**: shiny reflective eyes with catchlights, soft round blush, simple "U"-shaped smile |
| Aware | Something due in ~2–3 days | **Curious/Surprised**: eyes shrink to small dots, mouth forms a small round "o", blush fades — reads as "noticing something coming" |
| Urgent | Something due within ~24 hours | **Anxious/Pouting**: small furrowed-brow lines above the eyes, mouth as an inverted "V", cheeks a deeper/bolder pink — visibly stressed without being distressing |
| Overdue | Deadline passed, not marked done | **Sad/Tearful**: eyes droop slightly, tiny blue tear-drops at the corners, mouth flips into a wobbly frown, blush fades away entirely — comically sad rather than guilt-inducing |

This mapping reuses the character's own natural expression range rather than inventing new poses, which keeps every state feeling like the same character throughout.

## Persistent visibility (v1 requirement, not just empty states)
The mascot's mood must be visible regardless of whether a screen's list is empty — that's the entire point of an at-a-glance companion. In v1 (before the floating overlay exists), this means Home and Exams need a small, always-present mascot mood indicator (not just the empty-state illustration), so the current mood is checkable at any time.

## Presentation per card style
- **Speech bubble style**: mascot renders as a standalone circular avatar outside the card, with the card as a speech bubble extending from it. Full body/expression visible at all times.
- **Companion header style**: mascot renders as a smaller circular avatar perched on top of the card, partially overlapping it. Since it's smaller here, expressions need to stay legible at a reduced size.

## Motion notes (for later animation pass)
- Idle micro-animation for the Calm state (e.g. gentle breathing/bob) so the bubble doesn't look static/dead on screen.
- A short one-off animation for completing a task (celebration) and for entering the Overdue state (transition into dramatic pose) — both should be brief, not looping.

## Companion system (multi-character)
The app supports multiple selectable companion characters, chosen once in Settings (CompanionType, stored alongside cardStyle in UserSettings). Every companion expresses the same four app-driven mood states (CALM / AWARE / URGENT / OVERDUE per the same deadline-proximity logic already built), but through its own natural expression vocabulary — not a shared rig recolored per character. Each companion below is its own fully distinct design.

---

## Companion 1 — "Buddie" (default)
[See character brief and mood table above — unchanged.]

---

## Companion 2 — Cat ("Momo")
Sleek, playful/mischievous archetype — the visual opposite of Buddie's soft innocence.

**Form**: sleek, slightly elongated oval body (less round than Buddie), two triangular pointed ears on top, a curled tail extending from the base, two or three thin whisker lines on each cheek. No visible legs/paws — same minimalist "body + face" economy as Buddie.

**Color**: soft lavender-purple body, deep plum/dark-purple outline (not brown — this is a deliberate departure to help it read as a different character at a glance), soft pink cheek blush.

**Mood expressions**:
| State | Expression |
|---|---|
| CALM | Half-lidded, content eyes; closed-mouth small smirk (not a full smile — carries the mischievous personality even at rest); tail curled loosely at the side |
| AWARE | Ears perk fully upright, eyes widen slightly, tail straightens/flicks — a "noticed something" cat posture |
| URGENT | Eyes wide with small shrunk pupils, ears flatten back slightly, tail puffs up — classic startled-cat cues |
| OVERDUE | Eyes droop half-closed, tail droops down and drags, slumped posture — comically "given up," not sad-crying like Buddie's tears |

---

## Companion 3 — Cloud/Ghost ("Wisp")
Soft, dreamy/chill archetype — floats rather than sits.

**Form**: amorphous soft cloud/ghost blob, NO flat bottom (unlike Buddie) — trails off into a soft wavy, wispy tail at the base, like a classic gentle ghost silhouette. Reads as floating, not grounded.

**Color**: pale sky-blue to soft lavender gradient body, a light soft grey-blue outline (noticeably lighter/softer than Buddie's warm brown or Momo's deep plum — reinforces the dreamy, airy feel), no cheek blush (keeps it ethereal rather than cutesy-warm).

**Mood expressions**:
| State | Expression |
|---|---|
| CALM | Eyes closed in gentle downward dreamy arcs, small sparkle motifs drifting near it, wispy tail swaying slowly |
| AWARE | One eye partially opens, slight head tilt as if listening |
| URGENT | Wispy trailing tail becomes jagged/quivering instead of smooth, eyes open wide and round |
| OVERDUE | Body sinks/deflates lower and smaller, eyes become soft teardrop shapes, wispy tail flattens against the ground instead of floating |
