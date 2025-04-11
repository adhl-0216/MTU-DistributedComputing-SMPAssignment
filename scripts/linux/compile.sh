#!/bin/bash

# Change to project root directory (two levels up from script location)
cd "$(dirname "$0")/../.."

# Create output directory
mkdir -p bin

# Compile protocol classes first
javac -d bin protocol/*.java

# Compile client and server, with protocol classes in classpath
javac -cp bin -d bin client/*.java
javac -cp bin -d bin server/*.java

echo "Compilation complete. Classes in bin/"