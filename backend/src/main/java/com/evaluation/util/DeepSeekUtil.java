package com.evaluation.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeepSeekUtil {

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Call the DeepSeek chat API to generate a learning evaluation comment
     * for the given student based on their scores.
     *
     * @param studentName name of the student
     * @param scores      map of score dimension names to their values
     * @return AI-generated evaluation comment, or a fallback message on failure
     */
    public String generateComment(String studentName, Map<String, Object> scores) {
        try {
            String url = baseUrl + "/v1/chat/completions";

            // Build the prompt
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("请根据以下学生的各项成绩，生成一段综合学习评价评语。\n");
            promptBuilder.append("学生姓名：").append(studentName).append("\n");
            promptBuilder.append("各项成绩：\n");
            for (Map.Entry<String, Object> entry : scores.entrySet()) {
                promptBuilder.append("  - ").append(entry.getKey()).append("：").append(entry.getValue()).append("\n");
            }
            promptBuilder.append("请用中文生成一段客观、具有建设性的评价评语，包含优点和改进建议。");

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一位经验丰富的教育评价专家，擅长根据学生成绩数据生成专业的学习评价评语。");
            messages.add(systemMessage);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", promptBuilder.toString());
            messages.add(userMessage);

            requestBody.put("messages", messages);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Call the API
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

            if (response != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        return (String) message.get("content");
                    }
                }
            }

            return "AI评语生成失败，请稍后重试。";
        } catch (Exception e) {
            return "AI评语生成失败：" + e.getMessage() + "。请检查网络连接或API配置后重试。";
        }
    }
}
