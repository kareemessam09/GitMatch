# GitMatch: Project Summary & Roadmap

This project combines the addictive nature of swiping interfaces with the professional need to stay updated in the fast-paced tech world. Below is the full breakdown of features and the technical architecture for your **KMP + Spring Boot** application.

---

## 📱 Core Mobile Features (KMP & Compose)

### 1. The Discovery Deck

* **Hybrid Tabbed Approach:** A unified "For You" feed mixing repos and news, with swipeable tabs at the top for "🧑‍💻 Repos" and "📰 News" to filter when needed.
* **Swiping UI:** A Tinder-style card-based interface for discovery.
* **Swipe Right (Like):** Automatically "Stars" the repo on GitHub or saves an article to your "Read Later" list.
* **Swipe Left (Ignore):** Dismisses the item and moves to the next.
* **Swipe Up (More Info):** Expands the card to show detailed information, full AI summaries, or the README.

* **Card Content - Repositories:** Displays the repo name, language tags, a code snippet, star count, a 1-sentence AI summary, and highlights if it has **"Good First Issues"** to help developers get into Open Source.
* **Card Content - News & Releases:** Displays articles, 3-bullet AI TL;DRs, and crucial **Release Notes** from major tech tools (e.g., React, Spring Boot, KMP updates) so developers never miss a big update.

### 2. Tailored Tech Stack

* **Onboarding:** Users select their interests (e.g., Android, Backend, AI, Linux) during the first launch.
* **Smart Filtering:** The feed prioritizes content based on these preferences to ensure every swipe is relevant.

### 3. The "Vault" (Saved Matches)

* **Organized Archive:** A central location for all "Right Swipes."
* **Direct Action:** Quick buttons to open the repository in the GitHub app or view the full article in an in-app browser.
* **Searchable History:** Filter through your saved items by tags or keywords.

---

## ⚙️ Backend & AI Features (Spring Boot)

### 1. AI Summarization Service

* **The Engine:** Integration with an LLM (like **Gemini API**) to process long `README.md` files or news articles into a concise, scannable format.
* **Focus Points:** The summary highlights *What it is*, *Who it's for*, and *Why it's trending*.

### 2. Data Aggregator (The "Harvester")

* **GitHub Integration:** A scheduled task that fetches the daily and weekly trending repositories, extracting code snippets and checking for **"good first issues"** tags.
* **Content Scraping:** Pulls latest updates from major tech news sources (Dev.to, Hacker News, etc.) and scours large frameworks for new **Release Notes**.
* **Local Caching:** All data is stored in a **PostgreSQL** database to bypass API rate limits and ensure fast loading for the mobile app.

### 3. User & Sync System

* **Secure Auth:** Support for GitHub Social Login.
* **Cross-Device Sync:** User preferences and saved "Matches" are stored in the cloud, allowing for a seamless experience across devices.

---

## 🛠️ Technical Stack Overview

| Layer | Technology |
| --- | --- |
| **Frontend** | **Kotlin Multiplatform (KMP)** & Compose Multiplatform |
| **Backend** | **Java Spring Boot** (Data JPA, Security) |
| **Database** | **PostgreSQL** (Dockerized) |
| **Networking** | **Ktor Client** (Mobile) & **REST** (Backend) |
| **AI** | **Gemini API** for automated summaries |
| **Serialization** | **Kotlinx.serialization** |

---

## 🚀 Recommended Initial Roadmap

1. **Phase 1 (Backend Foundation):** Set up a Spring Boot project on your Linux environment. Create a simple REST endpoint that returns a list of mock repository data in JSON format.
2. **Phase 2 (Mobile Connectivity):** Initialize the KMP project. Use **Ktor** in the `commonMain` module to fetch the mock data from your local backend.
3. **Phase 3 (The Swiping UI):** Implement the card deck in **Compose Multiplatform**. Focus on the animations and gesture detection for the "Aha!" user experience.
4. **Phase 4 (AI Integration):** Connect your Spring Boot service to the Gemini API to start generating real-time summaries for new repositories.

---
