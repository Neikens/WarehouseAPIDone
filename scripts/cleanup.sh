#!/bin/bash

# Find and kill process using port 8082
pid=$(lsof -ti:8082)
if [ ! -z "$pid" ]; then
    echo "Killing process on port 8082"
    kill -9 $pid
fi

# Wait for port to be released
sleep 2