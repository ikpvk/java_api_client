#!/bin/bash

# Java API Client Startup Script

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 11 or higher"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 11 ]; then
    echo "Error: Java 11 or higher is required"
    echo "Current version: $(java -version 2>&1 | head -n 1)"
    exit 1
fi

# Find the JAR file
JAR_FILE="target/java-api-client-1.0.0-jar-with-dependencies.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    echo "Please build the project first using: mvn clean package"
    exit 1
fi

echo "Starting Java API Client..."
echo "Java version: $(java -version 2>&1 | head -n 1)"
echo "JAR file: $JAR_FILE"
echo ""

# Run the application
java -jar "$JAR_FILE"