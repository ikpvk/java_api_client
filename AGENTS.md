# Development Guidelines for Java API Client

This document provides comprehensive guidelines for agentic coding agents working on the Java Swing API client project. Follow these patterns to maintain consistency, quality, and security.

## Build Commands

### Essential Maven Commands
```bash
# Clean build
mvn clean package

# Run tests
mvn test

# Run single test
mvn test -Dtest=ClassName#methodName

# Run specific test class
mvn test -Dtest=HttpRequestServiceTest

# Run application
mvn exec:java -Dexec.mainClass="com.apiclient.Main"

# Run from JAR
java -jar target/java-api-client-1.0.0-jar-with-dependencies.jar
```

### Quick Development Commands
```bash
# Build and run
mvn clean package && java -jar target/java-api-client-1.0.0-jar-with-dependencies.jar

# Test and build
mvn test && mvn package

# Skip tests during development (use sparingly)
mvn package -DskipTests
```

## Code Style Guidelines

### Java 11+ Compatibility
- Target Java 11 (configured in pom.xml:17-18)
- Use var for local variable type inference when type is obvious
- Leverage Java 11 HttpClient features (async API, proper timeout handling)
- Use modern collection methods (List.of(), Map.of() for immutables)

### Import Organization
```java
// Standard Java imports first, sorted alphabetically
import java.awt.*;
import java.net.http.HttpClient;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// Third-party imports next
import com.google.gson.Gson;

// Project imports last
import com.apiclient.model.RequestData;
import com.apiclient.ui.RequestPanel;
```

### Naming Conventions
- **Classes**: PascalCase (e.g., `HttpRequestService`, `ApiClientFrame`)
- **Methods**: camelCase with descriptive names (e.g., `executeRequestAsync`, `setSendingState`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_TIMEOUT_SECONDS`)
- **Packages**: lowercase with dots (e.g., `com.apiclient.http`)
- **Variables**: camelCase, meaningful names (e.g., `responseTimeMs`, `isSending`)

### Formatting Standards
- 4-space indentation (no tabs)
- Line length maximum 120 characters
- Opening braces on same line for methods/classes
- Space between method name and parentheses: `method(param)` not `method (param)`
- Consistent spacing around operators: `a + b`, not `a+b`

## Type System Guidelines

### Strong Typing
- Always specify generic types: `List<String>` not `List`
- Use specific collection interfaces: `Map<String, String>` not `Map`
- Avoid raw types completely
- Use `@NonNull` and `@Nullable` annotations where appropriate

### Exception Handling Patterns
```java
// Preferred: Specific exceptions with meaningful messages
public ResponseData executeRequest(RequestData requestData) throws IOException {
    try {
        // ... request logic
    } catch (ConnectException e) {
        throw new IOException("Failed to connect to " + requestData.getUrl(), e);
    } catch (TimeoutException e) {
        throw new IOException("Request timed out after " + timeout + "ms", e);
    }
}

// Async error handling
CompletableFuture<ResponseData> future = service.executeRequestAsync(request)
    .exceptionally(throwable -> {
        log.error("Request failed", throwable);
        return createErrorResponse(throwable);
    });
```

## Testing Patterns

### Test Structure (Arrange-Act-Assert)
```java
@Test
void testGetRequest_ValidUrl_ReturnsResponse() throws Exception {
    // Arrange
    String responseBody = "{\"message\": \"success\"}";
    stubFor(get(urlEqualTo("/test"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(responseBody)));
    
    RequestData requestData = new RequestData("GET", "http://localhost:8080/test");
    
    // Act
    ResponseData response = httpRequestService.executeRequest(requestData);
    
    // Assert
    assertThat(response.getStatusCode()).isEqualTo(200);
    assertThat(response.getBody()).isEqualTo(responseBody);
}
```

### Test Categories
- **Unit Tests**: Individual class/method testing with mocks
- **Integration Tests**: Multiple component interaction (use WireMock for HTTP)
- **UI Tests**: Swing component testing (headless mode required)
- **End-to-End Tests**: Complete user workflows

### Testing Best Practices
- Use descriptive test method names that explain behavior
- Test both happy path and edge cases
- Use AssertJ for fluent assertions
- Mock external dependencies (WireMock for HTTP services)
- Clean up resources in @AfterEach
- Test async operations with CompletableFuture assertions

## Java Swing Specific Guidelines

### Event Dispatch Thread (EDT) Rules
```java
// Always update UI on EDT
SwingUtilities.invokeLater(() -> {
    responsePanel.setResponseData(responseData);
    setSendingState(false);
});

// For long-running operations, show progress
setSendingState(true); // Disable UI, show progress
CompletableFuture<ResponseData> future = service.executeRequestAsync(request);
```

### Component Design Patterns
```java
// Initialize components in separate method
private void initializeComponents() {
    sendButton = new JButton("Send");
    sendButton.setBackground(new Color(76, 175, 80));
    sendButton.setFocusPainted(false);
}

// Use GridBagLayout for complex layouts
private void setupLayout() {
    setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0; gbc.gridy = 0;
    add(component1, gbc);
}
```

### Thread Safety Rules
- Never perform network operations on EDT
- Use SwingUtilities.invokeLater() for UI updates from background threads
- Keep UI state updates atomic
- Use proper synchronization for shared data structures

## HTTP Client Implementation Patterns

### Java 11+ HttpClient Usage
```java
// Proper client configuration
HttpClient client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .followRedirects(HttpClient.Redirect.NORMAL)
    .version(HttpClient.Version.HTTP_2)
    .build();

// Async request pattern
CompletableFuture<HttpResponse<String>> future = client.sendAsync(
    request, HttpResponse.BodyHandlers.ofString());
```

### Request Building Pattern
```java
HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
    .uri(URI.create(requestData.getUrl()))
    .timeout(Duration.ofSeconds(30));

// Set method and body
String method = requestData.getMethod().toUpperCase();
HttpRequest.BodyPublisher bodyPublisher = requestData.hasBody() 
    ? BodyPublishers.ofString(requestData.getBody()) 
    : BodyPublishers.noBody();
requestBuilder.method(method, bodyPublisher);

// Add headers
requestData.getHeaders().forEach(requestBuilder::header);
```

### Error Handling
- Handle all network exceptions gracefully
- Provide meaningful error messages to users
- Implement request timeouts (default 30 seconds)
- Retry logic for transient failures (with exponential backoff)

## Security Considerations

### Input Validation
```java
// URL validation
if (requestData.getUrl() == null || !isValidUrl(requestData.getUrl())) {
    throw new IllegalArgumentException("Invalid URL: " + requestData.getUrl());
}

// Header validation for security
requestData.getHeaders().forEach((key, value) -> {
    if (key.toLowerCase().contains("authorization")) {
        // Log warnings for sensitive headers
        log.warn("Authorization header present in request");
    }
});
```

### Security Best Practices
- Never log sensitive data (passwords, tokens, API keys)
- Validate all user inputs before processing
- Use HTTPS by default when possible
- Implement proper SSL certificate validation
- Consider rate limiting for API requests
- Sanitize error messages to avoid information disclosure

## Performance Guidelines

### Memory Management
```java
// Process large responses in chunks
if (response.body().length() > LARGE_RESPONSE_THRESHOLD) {
    // Stream processing for large responses
    processLargeResponse(response.body().reader());
}

// Clean up resources properly
try (InputStream is = response.body().byteStream()) {
    // Process stream
}
```

### Performance Optimization
- Use connection pooling for HTTP client
- Implement response caching where appropriate
- Lazy initialization for UI components
- Background processing for long operations
- Monitor and limit response sizes
- Use efficient data structures (ArrayList vs LinkedList)

## Documentation Requirements

### Code Documentation
```java
/**
 * Execute an HTTP request asynchronously.
 * 
 * @param requestData the request configuration containing URL, method, headers, and body
 * @return CompletableFuture containing the response data or error response
 * @throws IllegalArgumentException if request data is invalid
 * @throws IOException if network operation fails
 */
public CompletableFuture<ResponseData> executeRequestAsync(RequestData requestData) {
    // Implementation
}
```

### Documentation Standards
- Document all public methods with proper JavaDoc
- Include parameter and return value descriptions
- Document exceptions that can be thrown
- Use `@since` for version information
- Provide usage examples for complex methods
- Document thread safety guarantees

## Project Structure Guidelines

### Package Organization
```
com.apiclient/
├── Main.java                    # Application entry point
├── model/                       # Data models
│   ├── RequestData.java
│   └── ResponseData.java
├── http/                        # HTTP service layer
│   └── HttpRequestService.java
└── ui/                          # Swing UI components
    ├── ApiClientFrame.java      # Main window
    ├── RequestPanel.java        # Request configuration
    └── ResponsePanel.java      # Response display
```

### Development Workflow
1. **Feature Development**: Create feature branch from main
2. **Testing**: Write tests before implementation (TDD preferred)
3. **Code Review**: Ensure all guidelines are followed
4. **Integration**: Test with existing functionality
5. **Documentation**: Update relevant documentation

## Environment Setup

### Required Dependencies
- Java 11+ (configured in pom.xml:17-18)
- Maven 3.6+ for build management
- JUnit 5 for testing
- Mockito for mocking
- WireMock for HTTP service testing
- AssertJ for fluent assertions

### IDE Configuration
- Set Java 11 as project SDK
- Enable annotation processing
- Configure code style to match project standards
- Set up automatic code formatting on save

## Quality Assurance

### Code Quality Checks
- Run `mvn test` before committing
- Ensure test coverage above 80%
- Check for PMD/Checkstyle violations if configured
- Verify memory usage in profiling tools

### Performance Testing
- Test with large response payloads (>1MB)
- Verify UI responsiveness during requests
- Test concurrent request handling
- Monitor memory usage patterns

This document serves as the definitive guide for maintaining consistency and quality in the Java API Client project. All contributors should follow these guidelines to ensure a maintainable and robust codebase.