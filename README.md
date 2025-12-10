# ISP Automation System (ISPAS)

Simple Java CLI demo for Internet Service Provider automation: customer registration, usage logging (hotspot/device name, date/time, MB used), tickets, billing and plan selection.

Tech stack:
- Java 17
- Maven
- SQLite (embedded via JDBC)

Build and run:

1. Build the fat JAR:

```bash
mvn -q clean package
```

2. Run the CLI demo:

```bash
java -jar target/ispas-0.1.0-jar-with-dependencies.jar
```

Data is stored under the `data/` folder as `ispas.db`.

Notes:
- This is a minimal, extensible prototype. You can add a REST API, a web UI, or background billing jobs later.
- If you'd like, I can commit these changes and push to the repository for you — tell me to proceed.
# ISPAS
An ISP Management System that automates customer registration, plan selection, data usage tracking, and monthly billing. The system provides secure login, real-time usage monitoring, payment integration, and an admin dashboard for managing users, plans, and service activities efficiently.
