#!/bin/bash

# Script per avviare la GUI Dashboard di AIHoneypot

# Resolve project root relative to script location
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

echo "🛡️  AIHoneypot GUI Dashboard Launcher"
echo "======================================"
echo ""

# Check if backend is running
echo "🔍 Checking backend connection..."
if curl -s http://localhost:8080/api/dashboard/health > /dev/null 2>&1; then
    echo "✅ Backend is running on http://localhost:8080"
else
    echo "⚠️  Backend not detected on http://localhost:8080"
    echo "   Start it with: cd honeypot && mvn spring-boot:run"
    echo ""
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo ""
echo "🚀 Starting JavaFX GUI..."
echo ""

# Navigate to GUI directory
cd "$PROJECT_ROOT/gui"

# Run JavaFX application
mvn javafx:run

echo ""
echo "👋 GUI closed. Goodbye!"

