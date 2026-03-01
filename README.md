# Student Evaluation System

> Student Learning Process Evaluation System — A multi-role academic evaluation management platform built with Spring Boot, Vue 2, Element UI and ECharts

## Features

- **Three Roles** — Admin / Teacher / Student, each with an independent dark-theme interface
- **Multi-dimensional Evaluation** — Attendance, homework, experiments, quizzes, and class participation with configurable weights
- **Weighted Total Score** — Automatically calculates overall grades based on per-course dimension weights
- **AI Comments** — Integrates the DeepSeek large model to generate personalized student comments with one click
- **Visualization** — ECharts radar charts, bar charts, and trend analysis
- **Score Import** — Batch import scores from Excel (EasyExcel)
- **Early Warning System** — Automatic alerts for low scores, missing homework, and delayed evaluations
- **Appeals & Feedback** — Students submit appeals, teachers respond online
- **System Configuration** — Custom logo upload, system name, evaluation periods, and other parameters

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Backend | Spring Boot + Spring Security + JWT | 2.7.18 |
| ORM | MyBatis Plus | 3.5.3.1 |
| Database | MySQL / MariaDB | 5.7+ / 10.3+ |
| Frontend | Vue 2 + Element UI + ECharts | 2.6 / 2.15 / 5.4 |
| AI | DeepSeek API | - |

## Quick Start

### Prerequisites

- Java 8 (JDK 1.8)
- Maven 3.6+
- Node.js 16+ & npm 8+
- MySQL 5.7+ or MariaDB 10.3+

### 1. Clone & Init Database

```bash
git clone https://github.com/snozzz/stu-evaluation.git
cd stu-evaluation

# Init database
mysql -u root -p < database.sql
```

### 2. Configure Backend

Edit `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    username: root           # your db username
    password: your_password  # your db password


deepseek:
  api-key: sk-your-key
```

### 3. Run

```bash
# Terminal 1 — Backend (port 8088)
cd backend
mvn spring-boot:run

# Terminal 2 — Frontend (port 9090)
cd frontend
npm install
npm run serve
```

Open http://localhost:9090

### Demo Accounts

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Teacher | `teacher1` | `123456` |
| Teacher | `teacher2` | `123456` |
| Student | `stu001` | `123456` |
| Student | `stu002` | `123456` |
| Student | `stu003` | `123456` |
| Student | `stu004` | `123456` |

## Project Structure

```
├── backend/                          # Spring Boot
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/evaluation/
│       │   ├── config/               # Security, CORS, JWT, MyBatis
│       │   ├── controller/           # REST controllers
│       │   ├── entity/               # JPA entities (17 tables)
│       │   ├── mapper/               # MyBatis Plus mappers
│       │   ├── service/              # Business logic
│       │   └── util/                 # JWT, Result, DeepSeek client
│       └── resources/
│           └── application.yml
│
├── frontend/                         # Vue 2
│   ├── vue.config.js                 # Dev server & proxy config
│   └── src/
│       ├── api/index.js              # Axios API layer
│       ├── router/index.js           # Route definitions
│       ├── store/index.js            # Vuex state
│       ├── components/               # Layout components
│       └── views/
│           ├── admin/                # 12 admin pages
│           ├── teacher/              # 7 teacher pages
│           └── student/              # 5 student pages
│
├── database.sql                      # One-click DB init script
└── score_import_template.xlsx        # Score import Excel template
```

## Admin Modules

| Module | Path | Description |
|--------|------|-------------|
| Dashboard | `/admin/dashboard` | Student/teacher/course stats |
| User Management | `/admin/users` | CRUD users with role filter |
| Semester | `/admin/semesters` | Manage semesters, set current |
| Courses | `/admin/courses` | Course CRUD, link to semester |
| Classes | `/admin/classes` | Class CRUD (college/major/grade) |
| Eval Dimensions | `/admin/dimensions` | 5 evaluation dimensions |
| Weight Config | `/admin/weights` | Per-course dimension weights |
| Permission Assign | `/admin/bindings` | Assign teacher-course-class bindings |
| Announcements | `/admin/announcements` | Publish/pin announcements |
| Carousels | `/admin/carousels` | Homepage banner management |
| Alert Rules | `/admin/alerts` | Score/homework/eval delay alerts |
| System Config | `/admin/configs` | System name, logo upload, params |

## Teacher Modules

| Module | Path | Description |
|--------|------|-------------|
| Dashboard | `/teacher/dashboard` | Teaching overview |
| Course Binding | `/teacher/bindCourse` | View bound courses & classes |
| Score Entry | `/teacher/scores` | Grade students by dimension, batch entry, Excel import |
| Comments | `/teacher/comments` | Write/AI-generate/publish student comments |
| Appeals | `/teacher/appeals` | Handle student appeals |
| Reports | `/teacher/report` | ECharts visualization reports |

## Student Modules

| Module | Path | Description |
|--------|------|-------------|
| Dashboard | `/student/dashboard` | Radar chart, announcements, alerts |
| My Scores | `/student/scores` | View scores, weighted total, teacher comments |
| Self Eval | `/student/selfEval` | Self-evaluation by dimension |
| Appeals | `/student/appeals` | Submit & track appeals |

## Troubleshooting

**"Access denied for user"** — Update db credentials in `application.yml`

**Frontend blank / 404** — Ensure backend is running on port 8088, check proxy in `vue.config.js`

**npm install slow** — `npm config set registry https://registry.npmmirror.com`

**Maven download slow** — Add Alibaba mirror to `~/.m2/settings.xml`

**AI comments not working** — Set a valid DeepSeek API key in `application.yml`

**Login "password error"** — Passwords are BCrypt hashed. Regenerate with:
```bash
python3 -c "import bcrypt; print(bcrypt.hashpw(b'admin123', bcrypt.gensalt(10)).decode())"
```
