import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuizLeaderboardApp {

    private static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    private static final String REG_NO = "RA2311003010632";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) {
        try {
            Set<String> processedEvents = new HashSet<>();
            Map<String, Integer> leaderboardMap = new HashMap<>();

            // 1. Poll the API 10 times
            for (int i = 0; i <= 9; i++) {
                System.out.println("Polling index: " + i + "...");
                fetchAndProcess(i, processedEvents, leaderboardMap);
                
                if (i < 9) Thread.sleep(5000); 
            }

            // 2. Prepare the final JSON payload manually
            List<Map<String, Object>> leaderboard = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : leaderboardMap.entrySet()) {
                Map<String, Object> map = new HashMap<>();
                map.put("participant", entry.getKey());
                map.put("totalScore", entry.getValue());
                leaderboard.add(map);
            }

            // 3. Submit the result
            submitLeaderboard(leaderboard);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void fetchAndProcess(int pollIndex, Set<String> processedEvents, Map<String, Integer> leaderboardMap) throws Exception {
        String url = String.format("%s/quiz/messages?regNo=%s&poll=%d", BASE_URL, REG_NO, pollIndex);
        
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String body = response.body();
            Matcher objMatcher = Pattern.compile("\\{[^{}]+\\}").matcher(body);

            while (objMatcher.find()) {
                String eventStr = objMatcher.group();
                if (!eventStr.contains("\"roundId\"") || !eventStr.contains("\"participant\"")) {
                    continue;
                }

                String roundId = extractString(eventStr, "roundId");
                String participant = extractString(eventStr, "participant");
                int score = extractInt(eventStr, "score");

                // Deduplication logic: Unique key = roundId + participant 
                String uniqueKey = roundId + "_" + participant;

                if (!processedEvents.contains(uniqueKey)) {
                    processedEvents.add(uniqueKey);
                    leaderboardMap.put(participant, leaderboardMap.getOrDefault(participant, 0) + score);
                }
            }
        }
    }

    private static void submitLeaderboard(List<Map<String, Object>> leaderboard) throws Exception {
        StringBuilder jsonPayloadBuilder = new StringBuilder();
        jsonPayloadBuilder.append("{\"regNo\":\"").append(REG_NO).append("\",\"leaderboard\":[");
        for (int i = 0; i < leaderboard.size(); i++) {
            Map<String, Object> entry = leaderboard.get(i);
            jsonPayloadBuilder.append("{")
                    .append("\"participant\":\"").append(entry.get("participant")).append("\",")
                    .append("\"totalScore\":").append(entry.get("totalScore"))
                    .append("}");
            if (i < leaderboard.size() - 1) {
                jsonPayloadBuilder.append(",");
            }
        }
        jsonPayloadBuilder.append("]}");
        String jsonPayload = jsonPayloadBuilder.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/quiz/submit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Submission Status: " + response.body());
    }

    private static String extractString(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        if (m.find()) return m.group(1);
        return "";
    }

    private static int extractInt(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        if (m.find()) return Integer.parseInt(m.group(1));
        return 0;
    }
}