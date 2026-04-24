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