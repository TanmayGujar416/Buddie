# Assignment reminder app — build doc

## Concept summary

A cute floating mascot character that lives on the screen (Android overlay bubble), draggable and dockable to a screen edge. Tapping it opens a small card with three sections — submissions, timetable, exams — pulled from a timetable the user sets up once. The mascot's mood reflects the closest deadline across all sections.

Two interchangeable card visual styles ship at launch, chosen in settings:
- **Speech bubble style** — mascot sits outside the card, card is a speech bubble coming from it, swipe left/right between sections, dot indicator shows position.
- **Companion header style** — mascot perched on top of the card as an avatar, vertical icon rail on the side switches sections, small urgency badge dot on any section with an item due soon.

---

## Data model

### Subject
| Field | Type | Notes |
|---|---|---|
| id | string | primary key |
| name | string | e.g. "COA" |
| color | string | optional, for UI tagging |

### TimetableSlot
| Field | Type | Notes |
|---|---|---|
| id | string | primary key |
| subjectId | string | FK → Subject |
| sessionType | enum | `lecture` \| `lab` \| `tutorial` |
| room | string | e.g. "Room 204", "Lab 3" |
| dayOfWeek | enum | Mon–Sun |
| startTime | time | |
| endTime | time | |

A subject can have multiple `TimetableSlot` rows — most will just have one `lecture` row; subjects with a practical component add a separate `lab` row with its own room.

### Submission (assignment)
| Field | Type | Notes |
|---|---|---|
| id | string | primary key |
| subjectId | string | FK → Subject |
| sessionType | enum \| null | `lecture` \| `lab` \| `null` (no location) — determines which room shows |
| title | string | e.g. "Assignment 3" |
| deadline | datetime | |
| status | enum | `not_started` \| `in_progress` \| `done` |

Room shown on the card = looked up from `TimetableSlot` via `subjectId` + `sessionType`. If `sessionType` is null, no room line is shown.

### Exam
| Field | Type | Notes |
|---|---|---|
| id | string | primary key |
| subjectId | string | FK → Subject |
| title | string | e.g. "MSE 1" |
| datetime | datetime | |
| durationMinutes | int | |
| hall | string | separate from regular classroom, often differs |
| seatNumber | string | optional |
| syllabusScope | text | optional notes on topics covered |
| allowedMaterials | text | optional, e.g. "calculator allowed" |

### UserSettings
| Field | Type | Notes |
|---|---|---|
| cardStyle | enum | `speech_bubble` \| `companion_header` |
| bubbleEdge | enum | `left` \| `right` |
| bubblePositionY | float | remembers last vertical dock position |

---

## Screen list — v1 (normal in-app screens, no overlay yet)

Per the pragmatic build order: get all of this working as a regular app first, add the floating overlay last.

1. **Onboarding — timetable setup**
   Grid of day × time slot. Tap a slot → pick/create subject → if subject has a lab, prompt once for a separate lab room.
2. **Home (Submissions)**
   List of upcoming assignments, most urgent first. Tap → mark done / snooze / edit.
3. **Timetable (read-only view)**
   Today's/weekly schedule, subject + room per slot.
4. **Exams**
   List of upcoming exams, more prominent countdown styling than submissions.
5. **Add/edit submission**
   Subject picker, session type toggle (lecture/lab/none), deadline picker.
6. **Add/edit exam**
   Subject, date/time, hall, seat number, syllabus notes, allowed materials.
7. **Settings**
   Card style picker (speech bubble / companion header preview thumbnails), notification timing preferences, edit timetable.

## v2 — the floating overlay

Only after v1's data model and screens are fully working:

1. `SYSTEM_ALERT_WINDOW` permission request flow (deep-link to system settings, since Android won't grant this via a normal dialog).
2. Foreground `Service` with a small persistent notification, keeping the bubble alive in the background.
3. `WindowManager`-attached bubble view (`TYPE_APPLICATION_OVERLAY`), manual touch handling for drag, edge-snap on release.
4. Expand/collapse animation showing the card (reusing the same UI components built in v1's Home/Timetable/Exams screens — the overlay is a shell around existing screens, not a rebuild).
5. Mascot mood state (calm/aware/urgent/overdue) computed from the single nearest deadline across submissions + exams.
6. Guide users to whitelist the app from battery optimization (OEM-specific, especially Xiaomi/OnePlus-type devices) so the background service isn't killed.

**Optional lighter-weight alternative to the full overlay**: a home-screen widget showing mascot mood + next deadline, shippable well before tackling `WindowManager` overlays — worth doing as an in-between milestone if the overlay proves heavier than expected.

---

## Component structure note

Keep mascot + card content as one shared component, with card style as a swappable layout wrapper around it:

```
MascotCardContainer
 ├─ content: SubmissionsView | TimetableView | ExamsView   (shared, style-independent)
 └─ shell: SpeechBubbleShell | CompanionHeaderShell         (swappable via UserSettings.cardStyle)
```

This keeps adding a third card style later a matter of writing one new shell, not touching the data or content logic.
