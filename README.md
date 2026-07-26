# 🌙 SoulTalk — AI-Based Mental Wellness Companion

<div align="center">

<img src="https://img.shields.io/badge/Platform-Android-green?style=for-the-badge&logo=android" />
<img src="https://img.shields.io/badge/Language-Kotlin-purple?style=for-the-badge&logo=kotlin" />
<img src="https://img.shields.io/badge/UI-Jetpack%20Compose-blue?style=for-the-badge&logo=jetpackcompose" />
<img src="https://img.shields.io/badge/Backend-FastAPI-009688?style=for-the-badge&logo=fastapi" />
<img src="https://img.shields.io/badge/Database-PostgreSQL-336791?style=for-the-badge&logo=postgresql" />
<img src="https://img.shields.io/badge/AI-Gemini%20API-4285F4?style=for-the-badge&logo=google" />

<br><br>

### **A private, personalized space to express, reflect, and grow.**

SoulTalk is an AI-powered mental wellness companion that combines conversational AI, emotion detection, mood tracking, personalized memory, mindfulness activities, and safety-focused interaction design in one Android application.

</div>

---

## 📌 Overview

Mental wellness support is not always immediately accessible. Many people hesitate to express their emotions because of fear of judgment, loneliness, or the lack of someone available to listen.

**SoulTalk** is designed as a supportive digital companion that gives users a calm and private space to:

* Express their thoughts through conversation
* Understand and track their emotional patterns
* Receive personalized AI responses
* Practice breathing and mindfulness activities
* Build a long-term relationship with a customizable virtual companion
* Access safety resources when conversations indicate serious distress

The goal is not to replace professional mental-health care.

The goal is to create a supportive first layer where a user can pause, express what they are feeling, and receive a thoughtful response.

---

## 🎯 Problem Statement

Many people experience stress, loneliness, anxiety, and emotional difficulties but may not always have immediate access to a trusted person or professional support.

Existing chatbot applications often provide generic responses without understanding:

* The user's current emotional state
* Previous conversations
* Personal preferences
* Long-term emotional patterns
* The need for safety-aware responses

SoulTalk addresses this problem by combining:

> **Conversation + Emotion Awareness + Personalization + Memory + Mood Tracking + Safety Detection**

into a single mobile experience.

---

## ✨ Core Features

### 🤖 AI Emotional Companion

* Natural conversational interaction
* Emotion-aware response generation
* Personalized conversation context
* Multiple companion personality modes
* Supportive, non-judgmental interaction style
* English and Marathi language preference support

---

### 🧠 Emotion Detection

SoulTalk analyzes user messages to identify emotional states such as:

* Happy
* Sad
* Angry
* Anxious
* Lonely
* Stressed
* Excited
* Neutral

The emotion detection engine uses weighted keyword analysis with:

* High-confidence keywords
* Medium-confidence keywords
* Low-confidence keywords
* Context-based adjustments
* Confidence scoring
* Recent emotional history

Example pipeline:

```text
User Message
     ↓
Text Analysis
     ↓
Emotion Detection
     ↓
Confidence Score
     ↓
Context Analysis
     ↓
Personalized AI Response
```

---

### 🧩 Personalized Memory System

SoulTalk maintains relevant conversation context to make interactions more personalized.

The system can use:

* Recent conversation history
* User preferences
* Companion personality
* Emotional history
* Important memories
* Dominant emotional patterns

The context is then used to construct a personalized prompt for the AI system.

```text
User
 ↓
Current Message
 ↓
Emotion Detection
 ↓
Recent Conversation
 ↓
Important Memories
 ↓
Emotional History
 ↓
User Preferences
 ↓
Context Injection
 ↓
AI Response
```

---

### 🛡️ Safety-Aware Interaction

Because SoulTalk operates in the mental-wellness domain, the application includes a dedicated safety layer.

The safety system can identify different levels of emotional distress:

```text
NONE
  ↓
LOW
  ↓
MEDIUM
  ↓
HIGH
  ↓
SEVERE
```

The safety layer includes:

* Crisis keyword detection
* Severity classification
* Confidence scoring
* Safety protocol triggers
* Crisis-oriented response handling
* Emergency and professional-support resources

The application is designed to provide supportive guidance and encourage appropriate professional or emergency help when necessary.

---

### 😊 Mood Tracking

Users can:

* Record their daily mood
* View mood history
* Review emotional patterns
* View mood calendars
* Track emotional progress over time
* View emotional insights

Mood data can contribute to a more personalized companion experience.

---

### 🌦️ Emotional Weather

SoulTalk transforms emotional patterns into a simple visual representation of the user's emotional state.

Examples include:

* Sunny Mind
* Flourishing
* Recovery Mode
* Emotional Rain

This feature is designed to make emotional reflection more approachable and understandable.

---

### 🧘 Mindfulness & Breathing

The application includes features designed to support relaxation and self-care:

* Breathing sessions
* Mindfulness activities
* Relaxation support
* Positive affirmations
* Motivational guidance

---

### 🎙️ Voice Companion

SoulTalk includes voice-based interaction support with:

* Voice conversation sessions
* Voice processing flow
* Voice response handling
* Voice conversation history

This allows users to interact with their companion beyond traditional text messaging.

---

### 🐾 Customizable AI Companion

Users can personalize their companion through:

* Companion name
* Companion type
* Personality style
* Companion customization
* Interaction preferences

Available personality modes include:

* Gentle Friend
* Calm Listener
* Motivational Coach

---

### 📈 Personal Growth & Timeline

SoulTalk helps users reflect on their journey through:

* Life timeline events
* Emotional milestones
* Growth summaries
* Companion progress
* Achievements

The goal is to help users look back at their journey rather than focusing only on the current moment.

---

### 🔐 User Data Management

The application provides user-focused data management features such as:

* Profile management
* Preference management
* Memory reset
* Data export
* Data deletion
* User data reset functionality

---

# 🏗️ System Architecture

```text
┌──────────────────────────────────────┐
│          Android Application         │
│                                      │
│       Kotlin + Jetpack Compose       │
│                                      │
│  UI Screens • ViewModels • Repos     │
└──────────────────┬───────────────────┘
                   │
                   │ REST API
                   │ Retrofit + OkHttp
                   ▼
┌──────────────────────────────────────┐
│              FastAPI Backend         │
│                                      │
│  Authentication                      │
│  Chat Processing                     │
│  Emotion Detection                   │
│  Safety Layer                        │
│  Memory System                       │
│  Context Injection                   │
│  Mood & Profile Management           │
└──────────────┬───────────────┬───────┘
               │               │
               ▼               ▼
      ┌────────────────┐  ┌───────────────┐
      │  PostgreSQL    │  │   Gemini API  │
      │    Database    │  │  AI Response  │
      └────────────────┘  └───────────────┘
```

---

# 🧠 AI Conversation Pipeline

The core conversation flow is:

```text
1. User sends a message
              ↓
2. Content moderation
              ↓
3. Crisis detection
              ↓
4. Emotion detection
              ↓
5. Retrieve user context
              ↓
6. Retrieve conversation history
              ↓
7. Retrieve important memories
              ↓
8. Build personalized AI context
              ↓
9. Generate AI response
              ↓
10. Store conversation and emotional data
```

This architecture allows the system to respond based on more than just the latest message.

---

# 🧱 Technology Stack

## Android Application

| Technology        | Purpose                         |
| ----------------- | ------------------------------- |
| Kotlin            | Application development         |
| Jetpack Compose   | Declarative UI                  |
| Material 3        | UI components and design system |
| Retrofit          | REST API communication          |
| OkHttp            | HTTP networking                 |
| Moshi             | JSON serialization              |
| Room              | Local data persistence          |
| Kotlin Coroutines | Asynchronous programming        |
| ViewModel         | UI state management             |

---

## Backend

| Technology    | Purpose                         |
| ------------- | ------------------------------- |
| Python        | Backend development             |
| FastAPI       | REST API framework              |
| SQLAlchemy    | ORM and database interaction    |
| PostgreSQL    | Persistent database             |
| Pydantic      | Request and response validation |
| JWT           | Authentication                  |
| bcrypt        | Password hashing                |
| Uvicorn       | ASGI server                     |
| python-dotenv | Environment configuration       |

---

## AI & Intelligent Systems

| Component                | Purpose                               |
| ------------------------ | ------------------------------------- |
| Gemini API               | AI-generated conversational responses |
| Emotion Detection Engine | Emotion classification                |
| Context Injection        | Personalized AI prompts               |
| Memory System            | Long-term personalization             |
| Safety Layer             | Crisis and distress detection         |
| Content Moderator        | Basic content safety checks           |

---

# 📁 Project Structure

```text
SoulTalk/
│
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/example/
│       │   │       ├── data/
│       │   │       │   ├── api/
│       │   │       │   ├── database/
│       │   │       │   └── repository/
│       │   │       │
│       │   │       ├── security/
│       │   │       │
│       │   │       ├── ui/
│       │   │       │   └── screens/
│       │   │       │
│       │   │       └── MainActivity.kt
│       │   │
│       │   └── res/
│       │
│       └── test/
│
├── backend/
│   ├── main.py
│   ├── database.py
│   ├── models.py
│   ├── schemas.py
│   ├── auth.py
│   ├── security.py
│   ├── emotion_engine.py
│   ├── safety_layer.py
│   ├── memory_system.py
│   ├── context_injection.py
│   │
│   └── dataset/
│       ├── conversations.json
│       ├── conversation_chains.json
│       ├── quote.json
│       └── emergency_resources.json
│
├── backend/requirements.txt
├── app/build.gradle.kts
└── README.md
```

---

# 🔐 Authentication Flow

```text
User Registration
        ↓
Password Hashing
        ↓
PostgreSQL User Record
        ↓
Login
        ↓
JWT Access Token
        +
JWT Refresh Token
        ↓
Authenticated API Requests
```

The backend validates authenticated requests before accessing protected user data.

---

# 📡 Backend API Modules

The backend is organized around multiple functional API areas.

### Authentication

```text
POST /auth/register
POST /auth/login
POST /auth/google
POST /auth/refresh
```

### Companion

```text
POST /companion/select
GET  /companion/status
PUT  /companion/update
GET  /companion/memories
POST /companion/memories
GET  /companion/achievements
PUT  /companion/customize
```

### Chat

```text
POST /chat/send
GET  /chat/history
GET  /chat/context
```

### Mood & Insights

```text
POST /mood/log
GET  /mood/history
GET  /mood/calendar
GET  /weather/history
GET  /insights
```

### Voice

```text
POST /voice/start
POST /voice/process
POST /voice/response
GET  /voice/history
```

### Timeline & Growth

```text
GET  /timeline
POST /timeline
POST /timeline/generate
PUT  /timeline/event/{event_id}
GET  /timeline/growth-summary
```

### Profile & Settings

```text
GET  /profile
PUT  /profile/update
GET  /profile/insights
POST /profile/reset-data
GET  /profile/export
GET  /settings
PUT  /settings/update
POST /settings/reset-memory
DELETE /user/delete-all-data
```

---

# ⚙️ Local Setup

## Prerequisites

Install the following:

* Android Studio
* JDK 11 or compatible Java environment
* Android SDK
* Python 3.10+
* PostgreSQL database
* Gemini API key

---

## 1. Clone the Repository

```bash
git clone https://github.com/Aish-369/SoulTalk---AI-Based-Mental-Health-Support-Chatbot.git

cd SoulTalk---AI-Based-Mental-Health-Support-Chatbot
```

---

## 2. Configure the Backend

Navigate to the backend directory:

```bash
cd backend
```

Create a virtual environment:

```bash
python -m venv venv
```

Activate it.

### Windows

```bash
venv\Scripts\activate
```

### macOS / Linux

```bash
source venv/bin/activate
```

Install dependencies:

```bash
pip install -r requirements.txt
```

---

## 3. Configure Environment Variables

Create a `.env` file inside the `backend` directory.

Example:

```env
DATABASE_URL=postgresql://username:password@host:5432/database_name

SECRET_KEY=your_secure_secret_key

ALGORITHM=HS256

ACCESS_TOKEN_EXPIRE_MINUTES=30

REFRESH_TOKEN_EXPIRE_DAYS=30

GEMINI_API_KEY=your_gemini_api_key
```

> Never commit real API keys, database credentials, or secret keys to GitHub.

---

## 4. Start the Backend

From the project root:

```bash
uvicorn backend.main:app --reload
```

The backend will start locally.

The FastAPI interactive API documentation can be accessed through the local server's `/docs` endpoint.

---

## 5. Configure the Android Application

Open the project in Android Studio.

Update the API base URL in the Android configuration according to your local environment.

For Android Emulator, the local host is commonly accessed through:

```text
10.0.2.2
```

For a physical Android device, use the local machine's network IP address.

---

## 6. Build and Run

1. Open the project in Android Studio
2. Sync Gradle
3. Start the backend
4. Configure the API base URL
5. Run the Android application on an emulator or physical device

---

# 🧪 Testing

The project includes testing support for:

* Unit testing
* Android instrumentation testing
* Robolectric-based testing
* Compose UI testing
* Screenshot testing

Important areas for testing include:

* Authentication
* Emotion detection
* Crisis detection
* API communication
* Database operations
* User data management
* UI state handling

Example emotion detection scenarios:

```text
"I feel happy today"
        ↓
Emotion: HAPPY

"I feel very anxious"
        ↓
Emotion: ANXIOUS

"I feel completely alone"
        ↓
Emotion: LONELY
```

---

# 🛡️ Privacy & Safety

SoulTalk handles emotionally sensitive information, so privacy and safety are important design considerations.

The application includes:

* Token-based authentication
* Password hashing
* Authenticated API access
* User data management
* Memory reset functionality
* Data export functionality
* Data deletion functionality
* Safety-aware conversation handling

However, SoulTalk should be treated as a software project and not as a substitute for professional clinical care.

---

# ⚠️ Important Disclaimer

**SoulTalk is not a medical device, therapist, psychologist, psychiatrist, or emergency service.**

The application is designed for:

* Emotional reflection
* General wellness support
* Conversational companionship
* Self-care activities
* Mood awareness

It must not be used as a replacement for qualified mental-health professionals or emergency services.

If someone is in immediate danger or experiencing a mental-health crisis, they should contact local emergency services or a qualified crisis-support professional.

---

# 🚧 Current Limitations

The current system has several limitations:

* Emotion detection is based on a rule-based weighted keyword approach and may not understand every context correctly.
* Crisis detection should not be considered a complete clinical risk-assessment system.
* AI-generated responses may occasionally be inaccurate or inappropriate.
* Internet connectivity is required for backend communication and AI-powered functionality.
* The system is not a replacement for professional psychological or medical support.

These limitations are important because responsible AI systems should clearly communicate what they can and cannot do.

---

# 🔮 Future Scope

Potential future improvements include:

* Transformer-based emotion classification
* Multilingual emotion detection
* Improved contextual sentiment analysis
* More advanced crisis-risk classification
* Speech-to-text and text-to-speech improvements
* On-device AI capabilities
* Better offline functionality
* Advanced emotional analytics
* Improved personalization algorithms
* Professional support integration
* Explainable AI insights
* More advanced privacy-preserving architecture

---

# 🎓 Learning Outcomes

Through this project, the following concepts were explored:

* Android application development using Kotlin
* Modern UI development using Jetpack Compose
* REST API design
* FastAPI backend development
* PostgreSQL database integration
* SQLAlchemy ORM
* JWT-based authentication
* Password hashing
* API integration
* AI-powered application development
* Emotion detection
* Context-aware response generation
* Memory-based personalization
* Safety-aware AI interaction
* User data management
* Software architecture and system integration

---

# 💡 Why SoulTalk?

Most chatbot applications focus only on:

```text
User Message → AI Response
```

SoulTalk attempts to build a richer interaction loop:

```text
User Message
      ↓
Emotion Detection
      ↓
Safety Evaluation
      ↓
Conversation Context
      ↓
Emotional History
      ↓
Personal Memories
      ↓
User Preferences
      ↓
Personalized AI Response
      ↓
Mood & Memory Update
```

This makes SoulTalk more than a simple question-and-answer chatbot.

It is designed as a personalized emotional wellness companion.

---

# 👩‍💻 Developer

## Aishwarya Pawar

**AI & Android Developer**

Interested in:

* Artificial Intelligence
* Generative AI
* Android Development
* Backend Engineering
* UI/UX Design
* Human-Centered Technology

SoulTalk is an exploration of how technology can become more emotionally aware, personalized, and supportive.

> **Building technology that listens to hearts, not just commands.** 💙

---

# ⭐ Support the Project

If you find SoulTalk interesting, consider giving the repository a ⭐ on GitHub.

Every star, suggestion, and contribution helps improve the project.

---

<div align="center">

### 🌙 SoulTalk

**Listen. Reflect. Grow.**

Made with 💙 by **Aishwarya Pawar**

</div>
