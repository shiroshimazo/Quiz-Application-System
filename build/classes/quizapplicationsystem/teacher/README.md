# Teacher area

Same feature-first structure as the admin area. Keep each feature's JavaFX
controller, FXML view, and feature-specific service together until it grows
large enough to split.

```text
teacher/
|-- shell/       Shared teacher window, sidebar, and content host
|-- dashboard/   Assigned courses, recent activity, and summaries
|-- courses/     Courses the admin assigned, and their enrolled students
|-- quizzes/     Create, edit, publish, and unpublish quizzes
|-- questions/   Author and organize the question bank
|-- results/     Review and export results for the teacher's quizzes
`-- shared/      Reusable controls, dialogs, and helpers (teacher-only)
```

Suggested files inside a feature package:

```text
QuizzesController.java
QuizzesView.fxml
QuizService.java
```

## How this connects to admin and student

Everything flows through `quizapplicationsystem.shared`:

- The **admin** creates teacher accounts and assigns courses. Those accounts
  and courses live in `shared.model` + `shared.data`, so the teacher signs in
  (via `shared.auth`) and sees exactly what the admin set up.
- Quizzes a teacher publishes are written to `shared.data`, so **students**
  enrolled in that course can see and take them.
- Results students submit are read back here in `results/` and roll up into the
  admin's global `results/` view.

Teachers never talk to the admin or student packages directly. The shared
model + data layer is the single connection point.
