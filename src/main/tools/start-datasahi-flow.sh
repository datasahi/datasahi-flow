#!/bin/bash

# Set environment variables
export DATASAHI_PORT=${DATASAHI_PORT:-8080}  # Default to 8080 if not set
export DATASAHI_CONFIG_PATHS=${DATASAHI_CONFIG_PATHS:-"config"}  # Default to "config" if not set
export DATASAHI_WORK_DIR=${DATASAHI_WORK_DIR:-"work"}  # Default to "work" if not set

# Create logs directory if it doesn't exist
LOGS_DIR="$DATASAHI_WORK_DIR/logs"
mkdir -p "$LOGS_DIR"

# Set Java memory and GC logging options
export JAVA_OPTS="-Xms512m -Xmx2048m \
-Xlog:gc*=info:file=$LOGS_DIR/datasahi-flow.gc.log:time,uptime,level,tags:filecount=5,filesize=100m \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=$LOGS_DIR"

# Function to check Java version
check_java_version() {
    if ! command -v java >/dev/null 2>&1; then
        echo "Error: Java is not installed or not in PATH"
        exit 1
    fi

    # Get Java version
    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F. '{print $1}')

    if [ -z "$java_version" ]; then
        echo "Error: Could not determine Java version"
        exit 1
    fi

    if [ "$java_version" -lt 17 ]; then
        echo "Error: Java version must be 17 or higher. Current version: $java_version"
        exit 1
    fi

    echo "Java version $java_version detected"
}

# Function to start the server
start_server() {
    local jar_file="datasahi-flow-0.1.0-all.jar"
    local log_file="$LOGS_DIR/datasahi-flow.log"

    if [ ! -f "$jar_file" ]; then
        echo "Error: $jar_file not found in current directory"
        exit 1
    fi

    echo "Starting Datasahi Flow server..."
    echo "Using JAVA_OPTS: $JAVA_OPTS"
    echo "Port: $DATASAHI_PORT"
    echo "Config Paths: $DATASAHI_CONFIG_PATHS"
    echo "Work Directory: $DATASAHI_WORK_DIR"
    echo "Log file: $log_file"
    echo "GC log file: $LOGS_DIR/datasahi-flow.gc.log"

    # Start the server and redirect output to log file
    java $JAVA_OPTS -jar "$jar_file" > "$log_file" 2>&1 &

    # Save the PID
    echo $! > "$DATASAHI_WORK_DIR/datasahi-flow.pid"

    echo "Server started with PID $(cat "$DATASAHI_WORK_DIR/datasahi-flow.pid")"
    echo "Log file: $log_file"
    echo "To follow logs: tail -f $log_file"
    echo "Health endpoint: http://localhost:$DATASAHI_PORT/health"
    echo "Health check to see all servers are reachable: http://localhost:$DATASAHI_PORT/health/check"
    echo "Verify a sample sync run: http://localhost:$DATASAHI_PORT/sync/verify/<flow.id>/100/30 - to sync maximum of 100 records or run for 30 seconds maximum"
    echo "Start sync: http://localhost:$DATASAHI_PORT/sync/start"
    echo "Stop sync: http://localhost:$DATASAHI_PORT/sync/stop"
}

# Main execution
echo "Initializing Datasahi Flow..."

# Check Java version
check_java_version

# Start the server
start_server