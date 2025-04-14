# watchlist-app
# 🎬 Film- und Serien-Watchlist

Dieses Projekt ist eine Web-Anwendung, mit der Nutzer ihre persönliche Watchlist für Filme und Serien verwalten können. Ziel ist es, eine moderne Anwendung mit Spring Boot (Backend), Vue.js (Frontend) und PostgreSQL (Datenbank) umzusetzen.

## 🛠️ Tech-Stack

- **Frontend:** Vue.js
- **Backend:** Spring Boot (Java)
- **Datenbank:** PostgreSQL
- **Deployment:** Render
- **CI/CD:** GitHub Actions

## ✅ Aktueller Stand (Meilenstein 1)

- Projektidee: Watchlist für Filme und Serien
- Entity-Klasse `MediaItem` überlegt
- Erste Spring Boot App aufgesetzt
- Dummy-GET-Route implementiert (`/api/media`)
- Beispielhafte Medienobjekte werden als JSON ausgeliefert

## 💡 Beispiel-Response der GET-Route

```json
[
  {
    "id": 1,
    "title": "Inception",
    "type": "Film",
    "genre": "Sci-Fi",
    "watched": true,
    "rating": 5
  },
  {
    "id": 2,
    "title": "Breaking Bad",
    "type": "Serie",
    "genre": "Drama",
    "watched": false,
    "rating": 0
  }
]
