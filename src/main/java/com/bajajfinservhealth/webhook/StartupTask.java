package com.bajajfinservhealth.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class StartupTask {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() throws Exception {
        // 1. Generate webhook
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "Tanguturi Venkata Thanuj");
        requestBody.put("regNo", "22bce20003");
        requestBody.put("email", "venkata.22bce20003@vitapstudent.ac.in");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            System.err.println("Failed to generate webhook: " + response.getStatusCode());
            return;
        }
        JsonNode json = objectMapper.readTree(response.getBody());
        String webhookUrl = json.get("webhook").asText();
        String accessToken = json.get("accessToken").asText();

        // 2. Prepare SQL query for the new question (highest salaried employee per department, excluding payments on 1st day)
        String finalQuery = "SELECT d.DEPARTMENT_NAME, p.AMOUNT AS SALARY, CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS EMPLOYEE_NAME, "
            + "FLOOR(DATEDIFF(CURDATE(), e.DOB) / 365.25) AS AGE\n"
            + "FROM PAYMENTS p\n"
            + "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID\n"
            + "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID\n"
            + "WHERE DAY(p.PAYMENT_TIME) <> 1\n"
            + "AND p.AMOUNT = (\n"
            + "    SELECT MAX(p2.AMOUNT)\n"
            + "    FROM PAYMENTS p2\n"
            + "    WHERE p2.EMP_ID = p.EMP_ID\n"
            + "      AND DAY(p2.PAYMENT_TIME) <> 1\n"
            + ")\n"
            + "ORDER BY d.DEPARTMENT_NAME, SALARY DESC;";

        // 3. Submit the solution
        Map<String, String> answerBody = new HashMap<>();
        answerBody.put("finalQuery", finalQuery);
        HttpHeaders answerHeaders = new HttpHeaders();
        answerHeaders.setContentType(MediaType.APPLICATION_JSON);
        answerHeaders.setBearerAuth(accessToken);
        HttpEntity<Map<String, String>> answerEntity = new HttpEntity<>(answerBody, answerHeaders);

        ResponseEntity<String> answerResponse = restTemplate.postForEntity(webhookUrl, answerEntity, String.class);
        if (answerResponse.getStatusCode() == HttpStatus.OK) {
            System.out.println("Solution submitted successfully!");
        } else {
            System.err.println("Failed to submit solution: " + answerResponse.getStatusCode());
        }
    }
}
