#!/bin/bash

# macOS Database Setup Script
# Ensures Colima is running and starts PostgreSQL container

set -e

echo "🚀 Starting Car Builder VIN Database Setup..."

# Function to check if colima is running
check_colima_status() {
    if colima status &>/dev/null; then
        echo "✅ Colima is running"
        return 0
    else
        echo "❌ Colima is not running"
        return 1
    fi
}

# Check if colima is installed
if ! command -v colima &> /dev/null; then
    echo "❌ Colima is not installed. Please install it first:"
    echo "   brew install colima"
    exit 1
fi

# Check if Docker and Docker Compose are available
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker Desktop or Docker CLI"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose is not installed. Please install docker-compose"
    exit 1
fi

# Check and start Colima if needed
if ! check_colima_status; then
    echo "🔄 Starting Colima..."
    colima start
    
    # Wait a moment for Colima to fully start
    echo "⏳ Waiting for Colima to initialize..."
    sleep 10
    
    # Verify Colima started successfully
    if ! check_colima_status; then
        echo "❌ Failed to start Colima"
        exit 1
    fi
fi

# Check if Docker daemon is responding
echo "🔍 Checking Docker daemon..."
if ! docker info &>/dev/null; then
    echo "❌ Docker daemon is not responding. Please check your Docker setup."
    exit 1
fi

echo "✅ Docker daemon is running"

# Stop any existing containers to avoid conflicts
echo "🧹 Cleaning up any existing containers..."
docker-compose down --remove-orphans 2>/dev/null || true

# Start PostgreSQL container
echo "🐘 Starting PostgreSQL container..."
docker-compose up -d postgres

# Wait for PostgreSQL to be ready
echo "⏳ Waiting for PostgreSQL to be ready..."
timeout=60
counter=0
while [ $counter -lt $timeout ]; do
    if docker-compose exec postgres pg_isready -U myuser -d mydatabase &>/dev/null; then
        echo "✅ PostgreSQL is ready!"
        break
    fi
    
    echo -n "."
    sleep 2
    counter=$((counter + 2))
done

if [ $counter -ge $timeout ]; then
    echo ""
    echo "❌ PostgreSQL failed to start within ${timeout} seconds"
    echo "📋 Container logs:"
    docker-compose logs postgres
    exit 1
fi

echo ""
echo "🎉 Database setup complete!"
echo ""
echo "📊 Database Connection Details:"
echo "   Host: localhost"
echo "   Port: 5432"
echo "   Database: mydatabase"
echo "   Username: myuser"
echo "   Password: secret"
echo ""
echo "🔧 Next steps:"
echo "   1. Run './mvnw spring-boot:run' to start the application"
echo "   2. Flyway migrations will run automatically"
echo "   3. Check logs for any migration issues"
echo ""
echo "🛑 To stop the database:"
echo "   docker-compose down"