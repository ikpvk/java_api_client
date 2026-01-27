# Java API Client

A simple Postman-like API client built with Java Swing and the Java 11+ HttpClient.

## Features

- **HTTP Methods**: Support for GET, POST, PUT, DELETE
- **Request Configuration**: 
  - URL input with validation
  - Custom headers management
  - Request body editor (for POST/PUT)
- **Response Display**:
  - Status code and status text with color coding
  - Response headers
  - Response body with raw and pretty-formatted views
  - Response time tracking
- **User Interface**:
  - Clean Swing-based GUI
  - Split-pane layout for request/response
  - Progress indication during requests
  - Responsive design with proper threading

## Requirements

- Java 11 or higher
- Maven 3.6+ (for building)

## Building the Application

1. Clone or download the project
2. Navigate to the project directory
3. Build using Maven:

```bash
mvn clean package
```

This will create two JAR files in the `target/` directory:
- `java-api-client-1.0.0.jar` - Main application JAR
- `java-api-client-1.0.0-jar-with-dependencies.jar` - Self-contained JAR with all dependencies

## Running the Application

### Option 1: Using the self-contained JAR (Recommended)

```bash
java -jar target/java-api-client-1.0.0-jar-with-dependencies.jar
```

### Option 2: Using Maven

```bash
mvn exec:java -Dexec.mainClass="com.apiclient.Main"
```

## Usage

### Making Requests

1. **Select HTTP Method**: Choose from GET, POST, PUT, DELETE
2. **Enter URL**: Type the target URL (e.g., `https://jsonplaceholder.typicode.com/posts/1`)
3. **Configure Headers**: 
   - Use the default "Content-Type: application/json" header or modify it
   - Add new headers using the "Add Header" button
   - Remove selected headers with "Remove Selected" button
4. **Set Request Body** (for POST/PUT):
   - Enter JSON or other content in the body textarea
   - Body is automatically disabled for GET and DELETE requests
5. **Send Request**: Click the green "Send" button

### Viewing Responses

The response panel displays:
- **Status**: HTTP status code with color coding (green for 2xx, red for 4xx/5xx)
- **Headers**: All response headers
- **Body**: 
   - "Raw" tab: Original response content
   - "Pretty" tab: Formatted JSON (for JSON responses)
- **Response Time**: Shown in the status bar

### Example Requests

#### GET Request
- Method: GET
- URL: `https://jsonplaceholder.typicode.com/posts/1`
- Headers: (default or empty)
- Body: (disabled for GET)

#### POST Request
- Method: POST
- URL: `https://jsonplaceholder.typicode.com/posts`
- Headers: `Content-Type: application/json`
- Body: 
```json
{
  "title": "foo",
  "body": "bar",
  "userId": 1
}
```

## Project Structure

```
src/main/java/com/apiclient/
├── Main.java                 # Application entry point
├── ui/
│   ├── ApiClientFrame.java   # Main application window
│   ├── RequestPanel.java     # Request configuration panel
│   └── ResponsePanel.java   # Response display panel
├── http/
│   └── HttpRequestService.java # HTTP request handling
└── model/
    ├── RequestData.java      # Request data model
    └── ResponseData.java    # Response data model
```

## Architecture

The application follows a clean architecture with clear separation of concerns:

- **UI Layer**: Swing components for user interaction
- **Service Layer**: HTTP request handling using Java 11 HttpClient
- **Model Layer**: Data structures for requests and responses
- **Threading**: Async request processing with proper EDT handling

## Technical Implementation

- **HTTP Client**: Java 11+ HttpClient with modern async API
- **UI Framework**: Java Swing with GridBagLayout for flexible layouts
- **JSON Processing**: Basic pretty printing for JSON responses
- **Thread Management**: Proper separation of network operations from EDT
- **Error Handling**: Comprehensive error handling with user-friendly messages

## Future Enhancements

Potential features to add:
- Request history and collections
- Environment variables
- Authentication support (Basic, Bearer Token)
- SSL certificate handling
- Proxy configuration
- Import/Export requests
- Syntax highlighting for code editors
- Response saving
- Batch requests
- WebSocket support

## Troubleshooting

### Application won't start
- Ensure you have Java 11 or higher installed
- Check that the JAR file exists and is executable
- Verify display system compatibility for Swing applications

### Request fails
- Check URL format and accessibility
- Verify network connectivity
- Check firewall/proxy settings
- Review error messages in the response panel

### Performance issues
- Large responses may take time to display
- Consider adding response size limits
- Memory usage scales with response content size

## License

This project is provided as-is for educational and development purposes.