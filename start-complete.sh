#!/bin/bash

# Script completo per avviare backend e GUI di AIHoneypot

echo "AIHoneypot - Complete Startup Script"
echo "=========================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check if backend is running
check_backend() {
    curl -s http://localhost:8080/api/dashboard/health > /dev/null 2>&1
    return $?
}

# Function to wait for backend
wait_for_backend() {
    echo -e "${BLUE}⏳ Waiting for backend to start...${NC}"
    for i in {1..60}; do
        if check_backend; then
            echo -e "${GREEN}✅ Backend is UP!${NC}"
            return 0
        fi
        echo -n "."
        sleep 1
    done
    echo -e "${RED}❌ Backend failed to start in 60 seconds${NC}"
    return 1
}

# Step 1: Check if backend is already running
echo -e "${BLUE}🔍 Checking backend status...${NC}"
if check_backend; then
    echo -e "${GREEN}✅ Backend already running on http://localhost:8080${NC}"
else
    echo -e "${YELLOW}⚠️  Backend not running${NC}"

    # Step 2: Build project if needed
    echo ""
    echo -e "${BLUE}📦 Checking if project is built...${NC}"
    if [ ! -f "honeypot/target/honeypot-1.0.0-SNAPSHOT.jar" ]; then
        echo -e "${YELLOW}Building project (this may take a minute)...${NC}"
        mvn clean install -DskipTests -q
        if [ $? -ne 0 ]; then
            echo -e "${RED}❌ Build failed${NC}"
            exit 1
        fi
        echo -e "${GREEN}✅ Build complete${NC}"
    else
        echo -e "${GREEN}✅ Project already built${NC}"
    fi

    # Step 3: Start backend
    echo ""
    echo -e "${BLUE}🚀 Starting backend...${NC}"
    cd honeypot
    nohup mvn spring-boot:run > /tmp/aihoneypot-backend.log 2>&1 &
    BACKEND_PID=$!
    echo "   Backend PID: $BACKEND_PID"
    echo "   Logs: tail -f /tmp/aihoneypot-backend.log"
    cd ..

    # Step 4: Wait for backend
    if ! wait_for_backend; then
        echo -e "${RED}Failed to start backend. Check logs:${NC}"
        echo "   tail -20 /tmp/aihoneypot-backend.log"
        exit 1
    fi
fi

# Step 5: Start GUI
echo ""
echo -e "${BLUE}🎨 Starting GUI Dashboard...${NC}"
echo ""

cd gui
mvn javafx:run

# Cleanup message
echo ""
echo -e "${YELLOW}👋 GUI closed${NC}"
echo ""
echo -e "${BLUE}Note: Backend is still running in background${NC}"
echo "   To stop it: pkill -f 'spring-boot:run'"
echo "   To view logs: tail -f /tmp/aihoneypot-backend.log"

