# Bajaj Finserv Health: Quiz Leaderboard System

This repository contains the solution for the Quiz Leaderboard backend integration assignment for the SRM Internship.

## Problem Overview

The validator simulates a quiz show where multiple participants receive scores across rounds. The same API response data may appear across multiple polls. The goal is to correctly deduplicate, aggregate, sort, and submit a final leaderboard.

## Technical Approach: Zero-Dependency Pure Java

Instead of relying on external JSON parsing libraries like Jackson or Gson, this solution is built entirely using native Java libraries to ensure maximum portability and zero setup overhead.

### Key Components

1. **API Client:** Uses the native `java.net.http.HttpClient` (introduced in Java 11) to handle GET and POST requests.
2. **JSON Parsing:** Uses native `java.util.regex` (Regex) to efficiently extract `roundId`, `participant`, and `score` directly from the raw string response.
3. **Data Deduplication:** Implements a `HashSet` that stores a combined string key (`roundId_participant`). This ensures that if the same participant's score for a specific round is delivered in multiple polls, it is only processed once.
4. **Aggregation:** Uses a `HashMap<String, Integer>` to maintain a running total of scores for each unique participant.
5. **Sorting:** The final leaderboard is sorted in descending order by `totalScore` before submission.
6. **Rate Limiting:** Implements a strict `Thread.sleep(5000)` to respect the mandatory 5-second interval between API polls.

## How It Works

1. Poll `GET /quiz/messages?regNo=<regNo>&poll=0` through `poll=9` with a 5-second delay between each call.
2. For every event received, generate a deduplication key: `roundId + "_" + participant`.
3. If the key has not been seen before, add the score to that participant's running total.
4. After all 10 polls, sort participants by `totalScore` in descending order.
5. Submit the final leaderboard once via `POST /quiz/submit`.

## Deduplication Logic

In distributed systems, the same event can be delivered multiple times across polls. This solution handles that with a `HashSet<String>`:
```
Poll 1 → { roundId: "R1", participant: "Alice", score: 10 } → NEW       → Alice total: 10
Poll 3 → { roundId: "R1", participant: "Alice", score: 10 } → DUPLICATE → Ignored
Final  → Alice totalScore: 10 ✓
```
## Prerequisites

- Java Development Kit (JDK) 11 or higher (tested on JDK 17)
- No external libraries or build tools required

## How to Run

Since this is a zero-dependency project, no build tools (like Maven or Gradle) are required.

1. Clone the repository:
```bash
git clone https://github.com/your-username/your-repo.git
cd your-repo
```

2. Compile the source file:
```bash
javac QuizLeaderboardApp.java
```

3. Run the application:
```bash
java QuizLeaderboardApp
```

The program will poll the API 10 times (approximately 45 seconds total) and print the submission result on completion.

## Sample Output
```
Polling index: 0...
Polling index: 1...
...
Polling index: 9...
Submission Status: {"isCorrect":true,"isIdempotent":true,"submittedTotal":220,"expectedTotal":220,"message":"Correct!"}
```
## API Reference

**Base URL:** `https://devapigw.vidalhealthtpa.com/srm-quiz-task`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/quiz/messages?regNo=&poll=` | Fetch quiz events for a given poll index (0–9) |
| POST | `/quiz/submit` | Submit the final leaderboard |

## Submission Checklist

- [x] 10 polls executed with 5-second delay between each
- [x] Duplicate events deduplicated using `roundId + participant` key
- [x] Scores aggregated per participant
- [x] Leaderboard sorted by `totalScore` descending
- [x] Leaderboard submitted exactly once