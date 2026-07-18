# Student area

Same feature-first structure as the admin and teacher areas.

```text
student/
|-- shell/       Shared student window, sidebar, and content host
|-- dashboard/   Enrolled courses, available quizzes, and recent scores
|-- quizzes/     Browse quizzes published to enrolled courses
|-- session/     Active quiz-taking: presenting questions, timing, submission
|-- results/     The student's own results and attempt history
`-- shared/      Reusable controls, dialogs, and helpers (student-only)
```

Suggested files inside a feature package:

```text
SessionController.java
SessionView.fxml
AttemptService.java
```

## How this connects to admin and teacher

Everything flows through `quizapplicationsystem.shared`:

- The **admin** creates the student account and enrolls the student in courses
  (`shared.model` + `shared.data`). The student signs in via `shared.auth`.
- Quizzes published by a **teacher** appear in `quizzes/` because both sides
  read the same `shared.data` store.
- A submitted attempt is saved as a `QuizResult` in `shared.data`, which is then
  visible to the teacher and admin. `results/` here only shows this student's
  own attempts.

Students never talk to the admin or teacher packages directly. The shared
model + data layer is the single connection point.
