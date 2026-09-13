# v1 acceptance checklist

One line per screen defining what "done" means. Use this to check the agent's output before moving to the next screen — don't accept a screen as finished until its line is true.

1. **Onboarding — timetable setup**
   User can assign a subject (with lecture room, and optionally a separate lab room) to every day/time slot they use, and the timetable persists after closing and reopening the app.

2. **Home (Submissions)**
   All non-done submissions appear sorted by nearest deadline first; marking one done removes it from this view and it's reflected as done wherever else it's referenced.

3. **Timetable (read-only view)**
   Shows the correct subject and room for every slot, matching exactly what was entered in onboarding, including the lecture/lab room split.

4. **Exams**
   Exams list separately from submissions, each showing hall, seat number (if set), and countdown; countdown styling is visibly distinct from the submissions view.

5. **Add/edit submission**
   Subject picker only shows subjects that exist in the timetable; if a subject has both lecture and lab rooms, the session-type toggle is shown and changes which room will display; if "no location" is chosen, no room line appears on the resulting card.

6. **Add/edit exam**
   All exam fields (hall, seat number, syllabus scope, allowed materials) save and reload correctly, including when left blank.

7. **Settings**
   Card style picker shows a live preview of both styles with the current mascot art; switching styles updates the preview immediately without restarting the app.

## Cross-cutting check
Before calling v1 complete: close and fully reopen the app, and confirm every subject, timetable slot, submission, and exam entered earlier is still there exactly as entered — no data model bugs hiding behind a working UI.
