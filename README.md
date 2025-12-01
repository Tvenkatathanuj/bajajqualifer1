# Bajaj Finserv Health Qualifier 1 - Java Solution

## Project Overview

This Spring Boot application automatically executes the Bajaj Finserv Health hiring challenge workflow:

1. On startup, sends a POST request to generate a webhook
2. Solves SQL Problem 1 (for odd regNo ending)
3. Submits the SQL solution to the webhook URL using JWT authentication

## Student Details

- **Name**: Tanguturi Venkata Thanuj
- **Registration Number**: 22bce20003
- **Email**: venkata.22bce20003@vitapstudent.ac.in

## SQL Solution (Question 1)

The application solves the following problem:

**Problem Statement:**
Find the highest salaried employee, per department, but do not include payments that were made on the 1st day of the month.

**Output Format:**
- DEPARTMENT_NAME: The name of the department
- SALARY: The total highest salary of an employee not including the payment received on the 1st day of the month
- EMPLOYEE_NAME: Combined FIRST_NAME and LAST_NAME (format: "First Last")
- AGE: The age of the employee who received that salary

**SQL Query:**
```sql
SELECT d.DEPARTMENT_NAME, 
       p.AMOUNT AS SALARY, 
       CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS EMPLOYEE_NAME, 
       FLOOR(DATEDIFF(CURDATE(), e.DOB) / 365.25) AS AGE
FROM PAYMENTS p
JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID
JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID
WHERE DAY(p.PAYMENT_TIME) <> 1
AND p.AMOUNT = (
    SELECT MAX(p2.AMOUNT)
    FROM PAYMENTS p2
    WHERE p2.EMP_ID = p.EMP_ID
      AND DAY(p2.PAYMENT_TIME) <> 1
)
ORDER BY d.DEPARTMENT_NAME, SALARY DESC;
```

## How to Run

**Using Maven (if installed):**
```bash
mvn clean package
java -jar target/webhook-sql-solution-1.0.0.jar
```

**Using Gradle (if installed):**
```bash
./gradlew clean build
java -jar build/libs/webhook-sql-solution-1.0.0.jar
```

**Using pre-built JAR:**
```bash
java -jar webhook-sql-solution-1.0.0.jar
```

## Project Structure

- `src/main/java/com/bajajfinservhealth/webhook/` - Spring Boot application source code
  - `WebhookSqlSolutionApplication.java` - Main Spring Boot application
  - `StartupTask.java` - Component that executes on startup
- `src/main/resources/application.properties` - Application configuration
- `pom.xml` - Maven build configuration
- `webhook-sql-solution-1.0.0.jar` - Executable JAR file

## Requirements Met

- ✅ Uses RestTemplate with Spring Boot
- ✅ No controller/endpoint triggers the flow (uses @EventListener on ApplicationReadyEvent)
- ✅ Uses JWT in Authorization header
- ✅ Automatic execution on startup
- ✅ Submits correct SQL solution for Question 1 (odd regNo: 22bce20003)
- ✅ Solves: Find highest salaried employee per department (excluding 1st day payments)

## Submission Details

- **GitHub Repository**: https://github.com/Tvenkatathanuj/bajaj_qualifier.git
- **JAR Download Link**: https://github.com/Tvenkatathanuj/bajaj_qualifier/raw/main/webhook-sql-solution-1.0.0.jar

## API Endpoints Used

1. **Generate Webhook:**
   - POST `https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA`
   - Returns: webhook URL and accessToken

2. **Submit Solution:**
   - POST `https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA`
   - Headers: Authorization (JWT token)
   - Body: `{"finalQuery": "YOUR_SQL_QUERY"}`
