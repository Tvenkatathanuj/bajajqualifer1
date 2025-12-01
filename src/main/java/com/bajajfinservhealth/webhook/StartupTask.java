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
    public void onStartup() {
        try {
            System.out.println("=== Starting Webhook Process ===");
            
            // 1. Generate webhook
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "TANGUTURI VENKATA THANUJ");
        requestBody.put("regNo", "22BCE20003");
        requestBody.put("email", "venkata.22bce20003@vitapstudent.ac.in");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        System.out.println("Sending POST to generate webhook...");
        System.out.println("Request: " + requestBody);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        
        if (response.getStatusCode() != HttpStatus.OK) {
            System.err.println("Failed to generate webhook: " + response.getStatusCode());
            return;
        }
        JsonNode json = objectMapper.readTree(response.getBody());
        String webhookUrl = json.get("webhook").asText();
        String accessToken = json.get("accessToken").asText();

        System.out.println("Webhook URL: " + webhookUrl);
        System.out.println("Access Token received: " + (accessToken != null ? "Yes" : "No"));

        // 2. Prepare SQL query for Question 1 (highest salaried employee per department, excluding payments on 1st day)
        String finalQuery = "SELECT d.DEPARTMENT_NAME, t.SALARY, t.EMPLOYEE_NAME, t.AGE "
            + "FROM (SELECT e.DEPARTMENT, SUM(p.AMOUNT) AS SALARY, "
            + "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS EMPLOYEE_NAME, "
            + "FLOOR(DATEDIFF('2025-12-01', e.DOB) / 365.25) AS AGE "
            + "FROM EMPLOYEE e JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID "
            + "WHERE DAY(p.PAYMENT_TIME) <> 1 "
            + "GROUP BY e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, e.DOB, e.DEPARTMENT) t "
            + "JOIN DEPARTMENT d ON t.DEPARTMENT = d.DEPARTMENT_ID "
            + "WHERE t.SALARY = (SELECT MAX(t2.SALARY) FROM (SELECT e2.DEPARTMENT, "
            + "SUM(p2.AMOUNT) AS SALARY FROM EMPLOYEE e2 JOIN PAYMENTS p2 ON e2.EMP_ID = p2.EMP_ID "
            + "WHERE DAY(p2.PAYMENT_TIME) <> 1 GROUP BY e2.EMP_ID, e2.DEPARTMENT) t2 "
            + "WHERE t2.DEPARTMENT = t.DEPARTMENT)";

        // 3. Submit the solution
        Map<String, String> answerBody = new HashMap<>();
        answerBody.put("finalQuery", finalQuery);
        HttpHeaders answerHeaders = new HttpHeaders();
        answerHeaders.setContentType(MediaType.APPLICATION_JSON);
        answerHeaders.setBearerAuth(accessToken);
        HttpEntity<Map<String, String>> answerEntity = new HttpEntity<>(answerBody, answerHeaders);

        System.out.println("Submitting solution to webhook...");
        ResponseEntity<String> answerResponse = restTemplate.postForEntity(webhookUrl, answerEntity, String.class);
        
        System.out.println("Solution Response Status: " + answerResponse.getStatusCode());
        System.out.println("Solution Response Body: " + answerResponse.getBody());
        
        if (answerResponse.getStatusCode() == HttpStatus.OK) {
            System.out.println("=== Solution submitted successfully! ===");
        } else {
            System.err.println("Failed to submit solution: " + answerResponse.getStatusCode());
        }
        
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
