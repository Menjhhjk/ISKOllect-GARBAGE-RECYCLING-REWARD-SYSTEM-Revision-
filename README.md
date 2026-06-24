# ISKOllect v2

ISKOllect is an administrator-operated JavaFX desktop application for recording student plastic-bottle recycling and managing reward points.

The revised system follows the June 23-24, 2026 analysis, ERD, schema, and JDBC design:

- Administrator login
- Student registration and record management
- Student registration with the first bottle drop-off
- Bottle submissions with a minimum of 5 bottles
- Automatic calculation at 0.5 points per bottle
- Rewards catalog management
- Atomic reward redemption
- Combined bottle and redemption transaction history
- Top-student recycling leaderboard
- PostgreSQL/Supabase persistence through JDBC

Students do not log in to the application. An administrator performs all system operations.

## Requirements

- JDK 21
- Maven 3.9+ or the included Maven wrapper
- PostgreSQL 14+ or a Supabase PostgreSQL project

## Database setup

Run [sql/iskollect_schema.sql](sql/iskollect_schema.sql) in an empty PostgreSQL database.

The script creates these six tables:

- `users`
- `students`
- `bottle_records`
- `points_ledger`
- `rewards_catalog`
- `redemptions`

It also seeds the initial reward catalog. No shared/default administrator password is inserted.

## Database configuration

Database credentials are not stored in the repository. Set these environment variables before launching:

```powershell
$env:ISKOLLECT_DB_URL = "jdbc:postgresql://HOST:5432/DATABASE?sslmode=require"
$env:ISKOLLECT_DB_USER = "postgres"
$env:ISKOLLECT_DB_PASSWORD = "your-password"
```

For local PostgreSQL:

```powershell
$env:ISKOLLECT_DB_URL = "jdbc:postgresql://localhost:5432/iskollect_db"
```

For local development, an ignored `.env` file may be created in the project root:

```text
ISKOLLECT_DB_URL=jdbc:postgresql://localhost:5432/iskollect_db
ISKOLLECT_DB_USER=postgres
ISKOLLECT_DB_PASSWORD=your-password
```

Launch using `run.bat` or:

```powershell
.\run-local.ps1
```

The local password file is excluded by `.gitignore`.

## First run

Start the application:

```powershell
.\mvnw.cmd clean javafx:run
```

On the login screen, choose **Create the first administrator**. This option works only while the `users` table is empty. The password is stored as a BCrypt hash.

## Build and test

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd -DskipTests package
```

## Architecture

```text
src/main/java/com/iskollect/
|-- Main.java
|-- IskollectApplication.java
|-- AppContext.java
|-- AppNavigator.java
|-- controller/
|   |-- LoginController.java
|   |-- DashboardController.java
|   |-- RewardsCatalogController.java
|   |-- TransactionController.java
|   `-- popup controllers
|-- dao/
|   `-- IskollectRepository.java
|-- model/
|   |-- DashboardStats.java
|   |-- Reward.java
|   |-- Student.java
|   `-- TransactionEntry.java
|-- service/
|   `-- IskollectService.java
`-- util/
    |-- ConnectionFactory.java
    |-- DBConnection.java
    `-- PasswordUtil.java
```

The service validates input and applies business rules. The repository owns JDBC queries and transactions. Bottle submissions and redemptions update their detail record, student balance, and points ledger atomically.

`Main.java` is a plain launcher so Maven-aware IDEs can run it without triggering Java's special JavaFX launcher behavior. `IskollectApplication.java` contains the JavaFX application lifecycle.

## FXML interface

The compatible FXML layouts and their referenced image assets from the supplied design archive are available under:

```text
src/main/resources/com/iskollect/fxml/
src/main/resources/com/iskollect/assets/
src/main/resources/com/iskollect/style.css
```

These are the active application screens. Their original visual layout is preserved while controllers, resource paths, database fields, and navigation are connected to the revised service and JDBC layers.

## Data behavior

- Deleting a student also removes that student's submissions, point ledger, and redemptions.
- A reward with redemption history cannot be deleted.
- Student and reward names are unique.
- The database rejects submissions below five bottles and negative balances.
- Transaction history is generated from bottle and redemption records.
