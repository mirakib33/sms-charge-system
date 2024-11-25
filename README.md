# SMS CHARGE SYSTEM
### A system by MoMagic to retrieve inbox content, validate keywords, retrieve unlock codes, and handle charging operations efficiently.

## Application Dependencies

- **Java 21**: Required version of Java for optimal performance and compatibility.
- **Virtual Thread**: Enable preview feature if needed.
- **Spring Boot 3.3.6**: The Spring Boot framework for building and deploying the application.
- **MySQL (Current Version)**: MySQL is used as the database for storing and retrieving data.
  - Update the `inbox`, `charge_success_log`, and `charge_failure_log` tables to make the `id` field primary key and set it to auto-increment (if needed).

## Application Services

The application consists of two primary services:

### 1. Content Retrieval Service

- **Startup Behavior**:
  - The service starts by sending a request to the content provider when the application initializes.
- **Scheduled Operation**:
  - It sends requests to the content provider and saves the fetched content in the `inbox` table every 6 hours.
- **Data Storage**:
  - The fetched content is processed and stored in the `inbox` table for later use.
- **Thread Management**:
  - This service operates on a separate thread, leveraging Virtual Threads for concurrency.

### 2. SMS Charging Service

- **Startup Behavior**:
  - The service starts by sending a request to fetch data from the `inbox` table when the application initializes.
- **Data Retrieval**:
  - Inbox data is retrieved from the database in chunks of 2000 records at a time, using an offset and limit to ensure no duplicate processing.
- **Scheduled Operation**:
  - It sends requests and processes inbox data every 8 hours on a scheduled basis.
- **Processing Workflow**:
  1. Validates each inbox record against the keywords associated with it from the `keyword_details` table.
  2. Automatically adapts to new keywords added to the database while the application is running.
  3. Retrieves the unlock code for the content, if available.
  4. Performs a charging operation based on the content and unlock code.
- **Logging**:
  - **Success**: Logs successful charge information in the `charge_success_log` table.
  - **Failure**: Logs failed charge attempts in the `charge_failed_log` table.
- **Inbox Status Update**:
  - Updates the status of the processed content in the `inbox` table to reflect the success or failure of the charging operation.
- **Thread Management**:
  - This service also operates on a separate thread, leveraging Virtual Threads for concurrency.

## Additional

- **Unit Testing**: Added unit testing for Content Retrieval Service

## Key Features and Technologies

➤ **Self Request**: **Automatically sends requests at the time of application startup.**  
➤ **Scheduling**: **Supports scheduled operations for periodic tasks.**  
➤ **Chunked Data Processing**: **Handles and saves data in manageable chunks for efficiency.**  
➤ **Virtual Threads**: **Utilizes Virtual Threads for consistent and scalable concurrency.**  
➤ **Dynamic Keyword Adaptation**: **Automatically adapts to new keywords added to the database while the application is running.**  




