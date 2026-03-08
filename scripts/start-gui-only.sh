#!/bin/bash

# Script per avviare SOLO la GUI Dashboard
# Usa questo se il backend è già in esecuzione

# Resolve project root relative to script location
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

echo "🎨 AIHoneypot - GUI Dashboard Launcher"
echo "========================================"
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if backend is running
echo -e "${BLUE}🔍 Checking backend connection...${NC}"
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Backend is UP on http://localhost:8080${NC}"
else
    echo -e "${RED}❌ Backend not responding!${NC}"
    echo ""
    echo -e "${YELLOW}Please start the backend first:${NC}"
    echo "   cd honeypot"
    echo "   mvn spring-boot:run"
    echo ""
    echo "Or use the complete startup script:"
    echo "   $PROJECT_ROOT/scripts/start-complete.sh"
    echo ""
    read -p "Do you want to start the backend now? (y/n) " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}🚀 Starting backend...${NC}"
        cd honeypot
        nohup mvn spring-boot:run > /tmp/aihoneypot-backend.log 2>&1 &
        echo "   Backend started in background"
        echo "   Logs: tail -f /tmp/aihoneypot-backend.log"
        cd ..

        # Wait for backend
        echo -e "${BLUE}⏳ Waiting for backend (30s)...${NC}"
        for i in {1..30}; do
            if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
                echo -e "${GREEN}✅ Backend is ready!${NC}"
                break
            fi
            echo -n "."
            sleep 1
        done
        echo ""
    else
        exit 1
    fi
fi

# Start GUI
echo ""
echo -e "${BLUE}🎨 Launching GUI Dashboard...${NC}"
echo ""

cd gui
mvn javafx:run

# Cleanup message
echo ""
echo -e "${YELLOW}👋 GUI closed${NC}"

