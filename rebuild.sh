#!/bin/bash

# Rebuild and restart Docker containers

echo ""
echo "========================================"
echo "   Rebuilding Invoizo Containers"
echo "========================================"
echo ""

echo "🛑 Stopping existing containers..."
docker-compose down

echo ""
echo "🔨 Rebuilding containers..."
docker-compose build --no-cache

echo ""
echo "🚀 Starting containers..."
docker-compose up -d

echo ""
echo "========================================"
echo "   ✅ Rebuild Complete!"
echo "========================================"
echo ""
echo "🌐 Frontend:  http://localhost:5173"
echo "🔧 Backend:   http://localhost:8080"
echo "💾 MongoDB:   localhost:27017"
echo ""
echo "📝 View logs: docker-compose logs -f"
echo ""
