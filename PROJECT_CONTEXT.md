# Java API Client - Project Context & Development Guide

## Project Overview
A simple Postman-like API client built with Java Swing and Java 11+ HttpClient. This is a desktop application for testing HTTP requests and viewing responses.

## Technical Stack
- **Language**: Java 11+
- **Build Tool**: Maven 3.6+
- **UI Framework**: Java Swing
- **HTTP Client**: Java 11+ HttpClient (built-in)
- **JSON Processing**: Google Gson (for pretty printing)
- **Architecture**: MVC pattern with clean separation

## Project Structure
```
java-api-client/
├── pom.xml                          # Maven configuration
├── README.md                        # User documentation
├── PROJECT_CONTEXT.md                # This file - development context
├── run.sh                          # Startup script
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── apiclient/
│       │           ├── Main.java                 # Entry point
│       │           ├── ui/
│       │           │   ├── ApiClientFrame.java   # Main window (Controller/View)
│       │           │   ├── RequestPanel.java     # Request configuration UI
│       │           │   └── ResponsePanel.java   # Response display UI
│       │           ├── http/
│       │           │   └── HttpRequestService.java # HTTP request handling
│       │           └── model/
│       │               ├── RequestData.java      # Request data model
│       │               └── ResponseData.java    # Response data model
│       └── resources/                     # Resources (icons, etc.)
└── target/                              # Build output
```

## Component Responsibilities

### **Main.java**
- Application entry point
- Sets up Look and Feel
- Launches GUI on Event Dispatch Thread
- Handles startup configuration

### **ApiClientFrame** (Main Window)
- Top-level application frame
- Coordinates between RequestPanel and ResponsePanel
- Handles toolbar with Send button and progress bar
- Manages status bar
- Implements request/response flow with proper threading using SwingUtilities.invokeLater

### **RequestPanel** (Request Configuration)
- HTTP method selector (GET, POST, PUT, DELETE)
- URL input field
- Headers table (add/remove functionality)
- Request body textarea (enabled/disabled based on method)
- Form validation and data collection

### **ResponsePanel** (Response Display)
- Status display with color coding (green=success, red=error)
- Headers display area
- Body display with tabs:
  - "Raw" tab: Original response content
  - "Pretty" tab: Formatted JSON (basic implementation)
- Response time tracking

### **HttpRequestService** (HTTP Layer)
- Wrapper around Java 11 HttpClient
- Async request processing
- Error handling and timeout management
- Response data conversion
- Connection configuration (30s timeout, follow redirects)

### **RequestData** (Model)
- Immutable request representation
- Method, URL, headers, body
- Convenience methods for validation

### **ResponseData** (Model)
- Response representation
- Status code, status text, headers, body
- Response time tracking
- Success status checking

## Key Implementation Details

### **Threading Model**
- All HTTP requests run on background threads using CompletableFuture
- UI updates occur on Event Dispatch Thread (EDT) via SwingUtilities.invokeLater()
- Progress indicators during request processing
- UI components disabled during requests to prevent conflicts

### **Error Handling**
- Network exceptions caught and converted to user-friendly messages
- Response data with status code -1 for errors
- Status bar updates with error information
- Graceful degradation for connectivity issues

### **UI Design Patterns**
- GridBagLayout for flexible, professional layouts
- JSplitPane for adjustable request/response areas
- Tabbed panes for multiple response views
- Proper component sizing and borders

### **HTTP Client Configuration**
- 30-second request timeout
- Automatic redirect following
- Default User-Agent header
- Support for all major HTTP methods
- Body handling for POST/PUT methods

## Current Features
✅ Basic HTTP methods (GET, POST, PUT, DELETE)
✅ Custom headers management
✅ Request body editor
✅ Response status, headers, and body display
✅ JSON pretty printing (basic)
✅ Response time tracking
✅ Progress indication
✅ Error handling
✅ Responsive UI with proper threading

## Known Limitations
- JSON pretty printing is basic (no syntax highlighting)
- No request history/collection feature
- No authentication support
- No environment variables
- Basic headers table (no bulk import)
- Response body size not limited
- No proxy configuration
- No SSL certificate handling options

## Future Enhancement Ideas
1. **Request Management**: History, collections, save/load requests
2. **Authentication**: Basic, Bearer token, API key support
3. **Environment Variables**: Support for different environments (dev/staging/prod)
4. **Advanced UI**: Syntax highlighting, better JSON/XML formatting
5. **Network Settings**: Proxy configuration, SSL settings
6. **Performance**: Response size limits, streaming for large responses
7. **Advanced Features**: Batch requests, WebSocket support
8. **Import/Export**: Postman collection import/export
9. **Testing Mode**: Mock responses, automated testing
10. **Themes**: Dark mode, customizable appearance

## Build & Development Commands

### **Building**
```bash
# Clean and compile
mvn clean compile

# Package with dependencies
mvn clean package

# Run directly
mvn exec:java -Dexec.mainClass="com.apiclient.Main"
```

### **Running**
```bash
# Using startup script
./run.sh

# Direct Java execution
java -jar target/java-api-client-1.0.0-jar-with-dependencies.jar
```

### **Testing URLs for Development**
- GET: `https://jsonplaceholder.typicode.com/posts/1`
- POST: `https://jsonplaceholder.typicode.com/posts`
- Echo: `https://httpbin.org/anything` (shows full request details)
- Status codes: `https://httpbin.org/status/200`, `https://httpbin.org/status/404`

## Development Notes
- **Java Version**: Targeting Java 11+ compatibility
- **UI Framework**: Using Swing (not JavaFX) for broader compatibility
- **Dependencies**: Minimal external dependencies (only Gson for JSON)
- **Threading**: Critical to keep network operations off EDT
- **Error States**: Always provide feedback to user for any operation
- **Memory**: Consider large response handling in future iterations

## Common Development Tasks

### **Adding New HTTP Methods**
1. Add method to dropdown in RequestPanel.java
2. Update HttpRequestService to handle method
3. Adjust body enabling logic in RequestPanel.updateBodyState()

### **Enhancing Response Display**
1. Modify ResponsePanel.java UI components
2. Add new tabs to tabbedPane
3. Update setResponseData() method
4. Consider adding new processing methods for different content types

### **Adding Authentication**
1. Extend RequestData model to include auth fields
2. Add UI components to RequestPanel
3. Update HttpRequestService to apply auth headers
4. Handle different auth types (Basic, Bearer, etc.)

### **Improving JSON Processing**
1. Enhance prettyPrintJson() method in ResponsePanel
2. Add syntax highlighting using JTextPane with styles
3. Handle JSON validation and error reporting
4. Consider integrating a more advanced JSON library

## File Locations for Common Changes
- **UI Layout**: `src/main/java/com/apiclient/ui/ApiClientFrame.java`
- **Request Handling**: `src/main/java/com/apiclient/ui/RequestPanel.java`
- **Response Display**: `src/main/java/com/apiclient/ui/ResponsePanel.java`
- **HTTP Logic**: `src/main/java/com/apiclient/http/HttpRequestService.java`
- **Data Models**: `src/main/java/com/apiclient/model/`
- **Build Config**: `pom.xml`

## Debugging Tips
- Use SwingUtilities.isEventDispatchThread() to check thread context
- Log HTTP details before sending in HttpRequestService
- Test with https://httpbin.org/anything to see full request details
- Check response status codes and headers for debugging API issues
- Use Java debugger with breakpoints in async callbacks

## Performance Considerations
- Large responses can cause memory issues - consider pagination or streaming
- UI updates should be minimal during request processing
- HTTP client reuse (single instance) is more efficient
- Consider connection pooling for high-frequency usage
- Monitor response times for performance bottlenecks