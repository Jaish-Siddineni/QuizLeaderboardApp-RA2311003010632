# Bajaj Finserv Health: Quiz Leaderboard System

This repository contains the solution for the Quiz Leaderboard backend integration assignment.

## Technical Approach: Zero-Dependency Pure Java

Instead of relying on external JSON parsing libraries like Jackson or Gson, this solution is built entirely using native Java libraries to ensure maximum portability and zero setup overhead.

### Key Components:

1. **API Client:** Uses the native `java.net.http.HttpClient` (introduced in Java 11) to handle GET and POST requests.
2. **JSON Parsing:** Uses native `java.util.regex` (Regex) to efficiently extract `roundId`, `participant`, and `score` directly from the raw string response.
3. **Data Deduplication:** Implements a `HashSet` that stores a combined string key (`roundId_participant`). This ensures that if the same participant's score for a specific round is delivered in multiple polls, it is only processed once.
4. **Aggregation:** Uses a `HashMap<String, Integer>` to maintain a running total of scores for each unique participant.
5. **Rate Limiting:** Implements a strict `Thread.sleep(5000)` to respect the mandatory 5-second interval between API polls.

## Prerequisites

- Java Development Kit (JDK) 11 or higher (Tested on JDK 17).

## How to Run

Since this is a zero-dependency project, no build tools (like Maven or Gradle) are required.

1. Clone the repository:
   ```bash
   git clone [https://github.com/your-username/your-repo.git](https://github.com/your-username/your-repo.git)
   ```
