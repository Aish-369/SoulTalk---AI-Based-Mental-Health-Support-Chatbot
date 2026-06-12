# SoulTalk Backend Engine (FastAPI & PostgreSQL)

Welcome to the **SoulTalk Empathetic Core Engine** backend codebase. This service orchestrates secure user registration, multi-factor encrypted login, stateless JWT sessions, and persistent profile updates in real-time, working hand-in-hand with the **SoulTalk Jetpack Compose Android Client**. 

---

## 🛠️ Tech Stack & Architecture

- **Web Framework:** [FastAPI](https://fastapi.tiangolo.com) (Stateless Async ASGI Engine)
- **Database ORM:** [SQLAlchemy](https://www.sqlalchemy.org) (High-speed relational mapping layer)
- **Database Engine:** [PostgreSQL](https://www.postgresql.org) (with automated fallback SQLite engine for local sandbox evaluation)
- **Authentication:** OAuth Google validation & custom JWT tokens (Access token + Refresh token rotation)
- **Security Hashing:** [Bcrypt](https://en.wikipedia.org/wiki/Bcrypt) via Passlib (Cryptographic user password protection)

---

## 📁 Codebase Structure

```text
/backend
├── auth.py          # Cryptographic hashing & JWT token issuance/validation
├── database.py      # Resilient connection configurations to PostgreSQL / SQLite
├── main.py          # Main ASGI Controller endpoints and middleware
├── models.py        # SQLAlchemy relational table schemas ('users')
├── requirements.txt # Python dependency pack
└── schemas.py       # Pydantic strong-typing serialization/validation models
```

---

## ⚙️ Environment Configuration

The backend accesses environment configurations directly from the `.env` file at the root of the workspace. Key parameters include:

- `DATABASE_URL`: PostgreSQL connection URI format (`postgresql://[user]:[password]@[host]:[port]/[database]`).
- `BACKEND_BASE_URL`: Base address the Android Companion hits (`http://10.0.2.2:8000/` inside emulators).

---

## 🚀 Speedrun Start

### 1. Install Dependencies
Ensure you have Python 3.8+ installed, then move to the server directory and run:
```bash
pip install -r backend/requirements.txt
```

### 2. Launch the Development Server
Power up the live-refresh server using `uvicorn`:
```bash
uvicorn backend.main:app --host 0.0.0.0 --port 8000 --reload
```
Once launched, open your web browser to check the status:
* Main Page: `http://localhost:8000/`
* Swagger Interactive Docs: `http://localhost:8000/docs`

---

## 🌐 API Endpoints Catalog

### **Authentication Module**
* `POST /auth/register` — Standard email registration. Hashes passwords securely and issues JWT access and refresh token pairs.
* `POST /auth/login` — Verifies valid user credentials against stored bcrypt database blocks for session entrance.
* `POST /auth/google` — Secure sign-in utilizing verified Google ID tokens, creating accounts matching the user's name when unique.
* `POST /auth/refresh` — Standard stateless access token rotation to prolong active sessions gracefully.

### **Companion Module**
* `POST /companion/select` — Authenticated action allowing logged-in souls to customize or update their selected empathetic guardian shape (e.g. Cat, Owl, Rabbit).
