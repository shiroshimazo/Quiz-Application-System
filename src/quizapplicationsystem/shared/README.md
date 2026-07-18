# Shared area

Cross-cutting code reused by the `admin`, `teacher`, and `student` areas. This
is the connective tissue: none of the three role areas depend on each other
directly — they all depend on `shared`.

```text
shared/
|-- model/   Domain entities: User, Teacher, Student, Course, Category,
|            Quiz, Question, QuizResult. UI-agnostic, reused by every role.
|-- auth/    Authentication, roles (ADMIN, TEACHER, STUDENT), login session.
`-- data/    Repositories and the single shared data store (source of truth).
```

## Why this links the three sides

```text
        admin  ──┐
        teacher ─┼──►  shared.model + shared.data + shared.auth
        student ─┘
```

- **admin** writes accounts, courses, and categories into `shared.data`.
- **teacher** reads their assigned courses and writes quizzes/questions.
- **student** reads published quizzes and writes results (attempts).

Because all three read and write the same `shared.data` store and authenticate
through the same `shared.auth`, an action on one side (admin creates a teacher,
teacher publishes a quiz, student submits a result) is immediately visible to
the others. Keep role-specific UI out of this package.
