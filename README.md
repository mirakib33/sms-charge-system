# sms-charge-system
 A system by MoMagic to process SMS content, validate keywords, retrieve unlock codes, and handle charging operations efficiently.

Key features of the application:
- **Content Retrieval**: At the time of application startup, the service automatically sends a request to the content provider to retrieve necessary contents.
- **Scheduled Functionality**: The application performs periodic tasks based on a scheduled configuration, allowing flexibility in timing for each functionality.
- **Virtual Threads**: The application uses virtual threads to run tasks concurrently in separate threads, ensuring better resource management and responsiveness.
- **Database Adaptability**: The application can dynamically adapt to changes in the database, including new keywords added during runtime, ensuring continuous operation without manual intervention.
- **Logging**: Exception logging is implemented to capture errors, while minimizing the logging for general activities to enhance performance.
- **Unit Testing**: The project includes unit tests using JUnit to ensure functionality and reliability.

## Application Services

The application consists of two primary services:

### 1. Content Retrieval Service

This service is responsible for fetching content from the content provider service. Upon retrieving the content, it saves the data in the `inbox` table in the database.

- The service starts by sending a request to the content provider on application startup.
- It processes and stores the fetched content in the `inbox` table for later use.

### 2. Content Processing and Charging Service

This service fetches content in chunks from the database (5000 records at a time) to avoid duplication and performance issues. It processes the content by validating the keywords, retrieving the unlock code, and performing the charging operation.

Key steps in the service:
- **Chunked Data Fetching**: The service retrieves content from the database in chunks of 5000 records at a time, using an offset to ensure no duplicates.
- **Keyword Validation**: It validates the keywords associated with each inbox record from the `keyword_details` table.
- **Unlock Code Retrieval**: The service retrieves the unlock code for the content, if available.
- **Charging Operation**: The service performs a charging operation based on the content and unlock code.
- **Logging**:
    - **Success**: Successful charge information is logged in the `charge_success_log` table.
    - **Failure**: Failed charge attempts are logged in the `charge_failed_log` table.
- **Inbox Status Update**: After processing, the status of the content in the `inbox` table is updated to reflect the success or failure of the charging operation.

## Application Dependencies

- **Java 21**: Required version of Java for optimal performance and compatibility.
- **Spring Boot 3.3.6**: The Spring Boot framework for building and deploying the application.
- **MySQL (Current Version)**: MySQL is used as the database for storing and retrieving data.



