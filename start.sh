#!/bin/bash

# Quick Start Script for Invoizo (Linux/Mac)

echo ""
echo "========================================"
echo "   Invoizo - Docker Quick Start"
echo "========================================"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running!"
    echo "Please start Docker and try again."
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Check if .env file exists
if [ ! -f .env ]; then
    echo "⚠️  .env file not found"
    echo "Creating .env from .env.example..."
    cp .env.example .env
    echo ""
    echo "⚠️  Please edit .env file with your configuration"
    echo "Then run this script again."
    exit 1
fi

echo "✅ Environment file found"
echo ""

# Start services
echo "🚀 Starting all services..."
echo ""
docker-compose up -d

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ Failed to start services"
    echo "Check the error messages above"
    exit 1
fi

echo ""
echo "========================================"
echo "   ✅ Services Started Successfully!"
echo "========================================"
echo ""
echo "🌐 Frontend:  http://localhost:5173"
echo "🔧 Backend:   http://localhost:8080"
echo "💾 MongoDB:   localhost:27017"
echo ""
echo "📝 View logs:     docker-compose logs -f"
echo "🛑 Stop services: docker-compose down"
echo ""
echo "Opening frontend in browser..."
sleep 2

# Open browser (works on most systems)
if command -v xdg-open > /dev/null; then
    xdg-open http://localhost:5173
elif command -v open > /dev/null; then
    open http://localhost:5173
fi

echo ""
echo "Press Ctrl+C to stop viewing logs"
echo ""
sleep 2
docker-compose logs -f
