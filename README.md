# DevBookmark — Backend

A full-stack social platform where developers save, share, and discover programming resources.

🔗 **Live Demo:** https://devbookmark.netlify.app  
📦 **Frontend Repo:** https://github.com/Siddhi-1711/devbookmark-frontend

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Database | PostgreSQL (Neon) |
| ORM | Spring Data JPA / Hibernate |
| Auth | Stateless JWT (HMAC-SHA256) |
| File Storage | Cloudinary |
| Deployment | Render (Docker) |

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT (React 18)                        │
│                    devbookmark.netlify.app                      │
└──────────────────────────────┬──────────────────────────────────┘
                               │ HTTPS
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                  SPRING BOOT 3.5 REST API                       │
│              devbookmark-backend.onrender.com                   │
│                                                                 │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────────┐  │
│  │  Security   │  │  Controllers │  │      Services          │  │
│  │  Filter     │→ │  (24 slices) │→ │  (Business Logic)      │  │
│  │  Chain      │  │              │  │                        │  │
│  │  JWT Auth   │  │ 112 Endpoints│  │  Feed / Trending /     │  │
│  │  Ban Check  │  │              │  │  Search / Recommend    │  │
│  │  RBAC       │  └──────────────┘  └────────────────────────┘  │
│  └─────────────┘                             │                  │
│                                              ▼                  │
│                                   ┌──────────────────┐          │
│                                   │   Repositories   │          │
│                                   │  Spring Data JPA │          │
│                                   └────────┬─────────┘          │
└────────────────────────────────────────────┼────────────────────┘
                                             │
               ┌─────────────────────────────┼──────────────────┐
               │                             │                  │
               ▼                             ▼                  ▼
  ┌────────────────────┐       ┌─────────────────────┐  ┌───────────────┐
  │   PostgreSQL       │       │     Cloudinary      │  │   Actuator    │
  │   (Neon)           │       │  (File Storage)     │  │   /health     │
  │  23 Tables         │       │  Images / Docs      │  │               │
  └────────────────────┘       └─────────────────────┘  └───────────────┘
```

---

## Security Pipeline

Every authenticated request passes through a 4-layer filter chain before reaching any controller:

```
Incoming Request
      │
      ▼
┌─────────────────────────────────┐
│  1. JWT Validation              │
│     Extract Bearer token        │
│     Verify HMAC-SHA256 signature│
│     Check expiry                │
│     Load UserDetails            │
└──────────────┬──────────────────┘
               │ valid token
               ▼
┌─────────────────────────────────┐
│  2. Ban Enforcement             │
│     Check user.banned flag      │
│     Reject if banned → 403      │
└──────────────┬──────────────────┘
               │ not banned
               ▼
┌─────────────────────────────────┐
│  3. Role-Based Access Control   │
│     ROLE_USER  → standard APIs  │
│     ROLE_ADMIN → /api/admin/**  │
└──────────────┬──────────────────┘
               │ authorized
               ▼
┌─────────────────────────────────┐
│  4. Content Visibility Rules    │
│     PUBLIC  → everyone          │
│     FOLLOWERS → followers only  │
│     PRIVATE → owner only        │
└──────────────┬──────────────────┘
               │
               ▼
          Controller
```




---


## File Upload Validation Pipeline

```
Incoming File Upload
        │
        ▼
┌───────────────────────────┐
│  Layer 1: Size Check      │
│  MAX 10MB enforced        │
│  Reject oversized files   │
└─────────────┬─────────────┘
              │ pass
              ▼
┌───────────────────────────┐
│  Layer 2: MIME Allowlist  │
│  PDF, DOC, DOCX, TXT,     │
│  PNG, JPG, JPEG, GIF      │
│  Reject unknown types     │
└─────────────┬─────────────┘
              │ pass
              ▼
┌───────────────────────────┐
│  Layer 3: Magic Bytes     │
│  Read file header bytes   │
│  Verify actual format     │
│  Blocks disguised .exe    │
│  renamed as .pdf          │
└─────────────┬─────────────┘
              │ pass
              ▼
     Upload to Cloudinary
```

---

## API Overview

| Domain | Endpoints | Auth Required |
|---|---|---|
| Auth | POST /api/auth/register, /login | No |
| Resources | GET/POST/PUT/DELETE /api/resources/** | GET: No, Write: Yes |
| Feed | GET /api/feed | Yes |
| Explore / Trending | GET /api/explore/trending | No |
| Search | GET /api/search | No |
| Collections | GET/POST/PUT/DELETE /api/collections/** | GET: No, Write: Yes |
| Series | GET/POST/PUT/DELETE /api/series/** | GET: No, Write: Yes |
| Tags | GET/POST /api/tags/** | GET: No, Follow: Yes |
| Social (likes/saves) | POST/DELETE /api/resources/{id}/like,save | Yes |
| Reposts | POST/DELETE /api/reposts/{id} | Yes |
| Comments | GET/POST/DELETE /api/resources/{id}/comments | GET: No, Write: Yes |
| Notifications | GET /api/notifications | Yes |
| Reading List | GET/POST/DELETE /api/reading-list | Yes |
| Recommendations | GET /api/recommendations | Yes |
| File Upload | POST /api/files/upload | Yes |
| Admin | /api/admin/** | ROLE_ADMIN |

---

## Local Setup

**Prerequisites:** Java 17, Maven, PostgreSQL

```bash
# Clone
git clone https://github.com/Siddhi-1711/devbookmark-backend
cd devbookmark-backend

# Create .env file
cp .env.example .env
# Fill in your values (see below)

# Run
./mvnw spring-boot:run
```

**Environment Variables:**

```env
DB_URL=jdbc:postgresql://localhost:5432/devbookmark
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=your_256_bit_secret
JWT_EXPIRATION_MINUTES=1440
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
FRONTEND_URL=http://localhost:5173
```

**Health check:** `http://localhost:8080/actuator/health`

---

## Deployment

Deployed on **Render** using Docker:

```dockerfile
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx400m", "-jar", "app.jar"]
```

Database hosted on **Neon** (serverless PostgreSQL).

---

