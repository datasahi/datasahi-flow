#!/bin/bash

# Set environment variables if not already set
export DATASAHI_WORK_DIR=${DATASAHI_WORK_DIR:-"work"}
export DATASAHI_PORT=${DATASAHI_PORT:-8080}

# PID file location
PID_FILE="$DATASAHI_WORK_DIR/datasahi-flow.pid"

# Function to check if process is running
check_process() {
    local pid=$1
    if ps -p "$pid" > /dev/null; then
        return 0 # Process is running
    else
        return 1 # Process is not running
    fi
}

# Function to make API call for graceful shutdown
call_stop_api() {
    echo "Calling shutdown API endpoint..."

    # Make API call with timeout and proper error handling
    local api_response
    local api_status

    # Try the API call with a 30-second timeout
    api_response=$(curl -s -w "%{http_code}" --max-time 30 "http://localhost:${DATASAHI_PORT}/sync/stop" 2>/dev/null)
    api_status=$?

    # Check curl exit status
    if [ $api_status -ne 0 ]; then
        case $api_status in
            7)  echo "Error: Failed to connect to the server. Connection refused." ;;
            28) echo "Error: Request timed out" ;;
            *)  echo "Error: API call failed with curl exit code: $api_status" ;;
        esac
        return 1
    fi

    # Get HTTP status code (last 3 characters of response)
    local http_code=${api_response: -3}
    api_response=${api_response%???}  # Remove last 3 characters (status code)

    if [ "$http_code" = "200" ]; then
        echo "Shutdown API call successful"
        echo "API Response: $api_response"
        return 0
    else
        echo "Error: API call failed with HTTP status $http_code"
        echo "API Response: $api_response"
        return 1
    fi
}

# Function to shutdown the server
shutdown_server() {
    # Check if PID file exists
    if [ ! -f "$PID_FILE" ]; then
        echo "Error: PID file not found at $PID_FILE"
        echo "Server might not be running or PID file was deleted"
        exit 1
    fi

    # Read PID from file
    PID=$(cat "$PID_FILE")

    # Validate PID
    if ! [[ "$PID" =~ ^[0-9]+$ ]]; then
        echo "Error: Invalid PID found in $PID_FILE"
        rm -f "$PID_FILE"
        exit 1
    fi

    # Check if process is running
    if ! check_process "$PID"; then
        echo "Process with PID $PID is not running"
        rm -f "$PID_FILE"
        exit 1
    fi

    echo "Initiating graceful shutdown sequence..."

    # Try API shutdown
    if call_stop_api; then
        echo "API shutdown signal sent successfully, terminating process..."

        # Immediately send SIGTERM after successful API call
        kill -15 "$PID"

        # Wait for up to 10 seconds for the process to end
        TIMEOUT=10
        while [ $TIMEOUT -gt 0 ] && check_process "$PID"; do
            echo "Waiting for process to end... ($TIMEOUT seconds remaining)"
            sleep 1
            TIMEOUT=$((TIMEOUT - 1))
        done

        # If process is still running after SIGTERM, use SIGKILL
        if check_process "$PID"; then
            echo "Process did not terminate after SIGTERM. Using SIGKILL..."
            kill -9 "$PID"
            sleep 1
        fi

        # Final check
        if check_process "$PID"; then
            echo "Error: Failed to terminate process"
            exit 1
        else
            echo "Server shutdown successfully"
            rm -f "$PID_FILE"
            exit 0
        fi
    else
        echo "API shutdown failed, attempting direct process termination..."

        # Try graceful shutdown with SIGTERM
        kill -15 "$PID"

        # Wait for up to 10 seconds
        TIMEOUT=10
        while [ $TIMEOUT -gt 0 ] && check_process "$PID"; do
            echo "Waiting for server to shutdown... ($TIMEOUT seconds remaining)"
            sleep 1
            TIMEOUT=$((TIMEOUT - 1))
        done

        # If process is still running, force kill
        if check_process "$PID"; then
            echo "Server did not shutdown gracefully. Forcing shutdown..."
            kill -9 "$PID"
            sleep 1
        fi

        # Final check
        if check_process "$PID"; then
            echo "Error: Failed to shutdown server"
            exit 1
        else
            echo "Server shutdown successfully"
            rm -f "$PID_FILE"
        fi
    fi
}

# Main execution
echo "Initiating Datasahi Flow shutdown..."
echo "Using port: $DATASAHI_PORT"
shutdown_server