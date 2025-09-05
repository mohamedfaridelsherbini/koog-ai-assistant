#!/bin/bash

# Koog AI Assistant v1.0.0 - Production Deployment Script
# Usage: ./deploy.sh [environment] [action]
# Example: ./deploy.sh production start

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="koog-ai-assistant"
VERSION="v1.0.0"
DOCKER_IMAGE="ghcr.io/mohamedfaridelsherbini/koog-ai-assistant"
ENVIRONMENT=${1:-production}
ACTION=${2:-start}

# Functions
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

success() {
    echo -e "${GREEN}✅ $1${NC}"
}

warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

error() {
    echo -e "${RED}❌ $1${NC}"
    exit 1
}

# Check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."
    
    if ! command -v docker &> /dev/null; then
        error "Docker is not installed. Please install Docker first."
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose is not installed. Please install Docker Compose first."
    fi
    
    success "Prerequisites check passed"
}

# Build application
build_app() {
    log "Building Koog AI Assistant..."
    
    # Build Gradle project
    ./gradlew clean build
    
    # Build Docker image
    docker build -t ${DOCKER_IMAGE}:${VERSION} .
    docker tag ${DOCKER_IMAGE}:${VERSION} ${DOCKER_IMAGE}:latest
    
    success "Application built successfully"
}

# Deploy application
deploy_app() {
    log "Deploying Koog AI Assistant to ${ENVIRONMENT}..."
    
    case $ENVIRONMENT in
        "production")
            docker-compose -f docker-compose.yml up -d
            ;;
        "development")
            docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
            ;;
        "staging")
            docker-compose -f docker-compose.yml -f docker-compose.staging.yml up -d
            ;;
        *)
            error "Unknown environment: ${ENVIRONMENT}"
            ;;
    esac
    
    success "Application deployed successfully"
}

# Start application
start_app() {
    log "Starting Koog AI Assistant..."
    docker-compose up -d
    success "Application started"
}

# Stop application
stop_app() {
    log "Stopping Koog AI Assistant..."
    docker-compose down
    success "Application stopped"
}

# Restart application
restart_app() {
    log "Restarting Koog AI Assistant..."
    docker-compose restart
    success "Application restarted"
}

# Show status
show_status() {
    log "Checking application status..."
    docker-compose ps
    
    # Check health
    if curl -f http://localhost:8080/health &> /dev/null; then
        success "Application is healthy"
    else
        warning "Application health check failed"
    fi
}

# Show logs
show_logs() {
    log "Showing application logs..."
    docker-compose logs -f
}

# Clean up
cleanup() {
    log "Cleaning up..."
    docker-compose down -v
    docker system prune -f
    success "Cleanup completed"
}

# Main execution
main() {
    log "🚀 Koog AI Assistant v1.0.0 Deployment Script"
    log "Environment: ${ENVIRONMENT}"
    log "Action: ${ACTION}"
    
    case $ACTION in
        "build")
            check_prerequisites
            build_app
            ;;
        "deploy")
            check_prerequisites
            build_app
            deploy_app
            ;;
        "start")
            check_prerequisites
            start_app
            ;;
        "stop")
            stop_app
            ;;
        "restart")
            restart_app
            ;;
        "status")
            show_status
            ;;
        "logs")
            show_logs
            ;;
        "cleanup")
            cleanup
            ;;
        *)
            echo "Usage: $0 [environment] [action]"
            echo ""
            echo "Environments:"
            echo "  production  - Production deployment"
            echo "  development - Development deployment"
            echo "  staging     - Staging deployment"
            echo ""
            echo "Actions:"
            echo "  build   - Build application and Docker image"
            echo "  deploy  - Build and deploy application"
            echo "  start   - Start application"
            echo "  stop    - Stop application"
            echo "  restart - Restart application"
            echo "  status  - Show application status"
            echo "  logs    - Show application logs"
            echo "  cleanup - Clean up containers and volumes"
            echo ""
            echo "Examples:"
            echo "  $0 production deploy"
            echo "  $0 development start"
            echo "  $0 production status"
            ;;
    esac
}

# Run main function
main "$@"
