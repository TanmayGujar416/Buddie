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

## Open decision
Exact species/shape (generic blob vs. a specific animal) isn't locked yet — treat this doc as the requirements the mascot must satisfy, and propose 2-3 concrete visual directions against it before committing to final art.
