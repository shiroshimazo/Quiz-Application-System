# Admin area

The admin interface uses a feature-first structure. Keep each feature's JavaFX
controller, FXML view, and feature-specific service in the same folder until it
becomes large enough to split further.

```text
admin/
|-- shell/       Shared admin window, sidebar, and content host
|-- dashboard/   Dashboard metrics and summary cards
|-- teachers/    Create, update, search, and deactivate teachers
|-- students/    Create, update, search, and deactivate students
|-- courses/     Manage subjects and courses
|-- categories/  Manage quiz categories
|-- results/     Browse and filter all quiz results
|-- reports/     Generate and export reports
`-- shared/      Reusable controls, dialogs, styles, and utilities
```

Suggested files inside a feature package:

```text
TeachersController.java
TeachersView.fxml
TeacherService.java
```

Shared domain objects such as `Teacher`, `Student`, `Course`, and `QuizResult`
should eventually live outside the admin UI package so other parts of the
application can reuse them.
