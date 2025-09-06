#!/bin/bash

# Koog AI Assistant v1.0.0 - Cloud Deployment Script
# Usage: ./deploy-cloud.sh [provider] [environment] [action]
# Example: ./deploy-cloud.sh aws production deploy

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
PROVIDER=${1:-aws}
ENVIRONMENT=${2:-production}
ACTION=${3:-deploy}

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
    
    case $PROVIDER in
        "aws")
            if ! command -v aws &> /dev/null; then
                error "AWS CLI is not installed. Please install AWS CLI first."
            fi
            if ! command -v kubectl &> /dev/null; then
                error "kubectl is not installed. Please install kubectl first."
            fi
            if ! command -v helm &> /dev/null; then
                error "Helm is not installed. Please install Helm first."
            fi
            if ! command -v terraform &> /dev/null; then
                error "Terraform is not installed. Please install Terraform first."
            fi
            ;;
        "gcp")
            if ! command -v gcloud &> /dev/null; then
                error "Google Cloud CLI is not installed. Please install gcloud first."
            fi
            if ! command -v kubectl &> /dev/null; then
                error "kubectl is not installed. Please install kubectl first."
            fi
            if ! command -v helm &> /dev/null; then
                error "Helm is not installed. Please install Helm first."
            fi
            ;;
        "azure")
            if ! command -v az &> /dev/null; then
                error "Azure CLI is not installed. Please install Azure CLI first."
            fi
            if ! command -v kubectl &> /dev/null; then
                error "kubectl is not installed. Please install kubectl first."
            fi
            if ! command -v helm &> /dev/null; then
                error "Helm is not installed. Please install Helm first."
            fi
            ;;
        *)
            error "Unknown provider: ${PROVIDER}. Supported providers: aws, gcp, azure"
            ;;
    esac
    
    success "Prerequisites check passed"
}

# Deploy infrastructure
deploy_infrastructure() {
    log "Deploying infrastructure on ${PROVIDER}..."
    
    case $PROVIDER in
        "aws")
            cd terraform/aws
            terraform init
            terraform plan -var="environment=${ENVIRONMENT}"
            terraform apply -auto-approve -var="environment=${ENVIRONMENT}"
            cd ../..
            ;;
        "gcp")
            log "GCP deployment not implemented yet"
            ;;
        "azure")
            log "Azure deployment not implemented yet"
            ;;
    esac
    
    success "Infrastructure deployed successfully"
}

# Deploy application
deploy_application() {
    log "Deploying Koog AI Assistant to Kubernetes..."
    
    # Create namespace
    kubectl create namespace koog-ai --dry-run=client -o yaml | kubectl apply -f -
    
    # Deploy using Helm
    helm upgrade --install koog-ai-assistant ./helm/koog-ai-assistant \
        --namespace koog-ai \
        --set koogAi.replicaCount=3 \
        --set ollama.enabled=true \
        --set ollama.resources.requests.memory=4Gi \
        --set ollama.resources.requests.cpu=2 \
        --set ollama.resources.limits.memory=16Gi \
        --set ollama.resources.limits.cpu=8 \
        --set ollama.persistence.enabled=true \
        --set ollama.persistence.size=100Gi \
        --set autoscaling.enabled=true \
        --set autoscaling.minReplicas=2 \
        --set autoscaling.maxReplicas=10 \
        --wait
    
    success "Application deployed successfully"
}

# Deploy monitoring
deploy_monitoring() {
    log "Deploying monitoring stack..."
    
    # Create monitoring namespace
    kubectl create namespace monitoring --dry-run=client -o yaml | kubectl apply -f -
    
    # Deploy Prometheus
    kubectl apply -f monitoring/prometheus-config.yaml
    
    # Deploy Grafana (simplified)
    kubectl create configmap grafana-dashboard --from-file=monitoring/grafana-dashboard.json -n monitoring --dry-run=client -o yaml | kubectl apply -f -
    
    success "Monitoring deployed successfully"
}

# Get application status
get_status() {
    log "Checking application status..."
    
    # Check pods
    kubectl get pods -n koog-ai
    
    # Check services
    kubectl get services -n koog-ai
    
    # Check ingress
    kubectl get ingress -n koog-ai 2>/dev/null || echo "No ingress found"
    
    # Check HPA
    kubectl get hpa -n koog-ai
    
    # Get external IP
    EXTERNAL_IP=$(kubectl get service koog-ai-loadbalancer -n koog-ai -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "Pending")
    if [ "$EXTERNAL_IP" != "Pending" ] && [ "$EXTERNAL_IP" != "" ]; then
        success "Application is accessible at: http://${EXTERNAL_IP}"
    else
        warning "External IP is not ready yet. Check with: kubectl get service koog-ai-loadbalancer -n koog-ai"
    fi
}

# Show logs
show_logs() {
    log "Showing application logs..."
    kubectl logs -f deployment/koog-ai-assistant -n koog-ai
}

# Scale application
scale_application() {
    log "Scaling application..."
    kubectl scale deployment koog-ai-assistant --replicas=5 -n koog-ai
    success "Application scaled to 5 replicas"
}

# Clean up
cleanup() {
    log "Cleaning up resources..."
    
    # Delete Helm release
    helm uninstall koog-ai-assistant -n koog-ai || true
    
    # Delete namespace
    kubectl delete namespace koog-ai || true
    
    # Delete infrastructure (if requested)
    if [ "$ENVIRONMENT" = "cleanup" ]; then
        case $PROVIDER in
            "aws")
                cd terraform/aws
                terraform destroy -auto-approve -var="environment=${ENVIRONMENT}"
                cd ../..
                ;;
        esac
    fi
    
    success "Cleanup completed"
}

# Main execution
main() {
    log "☁️  Koog AI Assistant v1.0.0 Cloud Deployment Script"
    log "Provider: ${PROVIDER}"
    log "Environment: ${ENVIRONMENT}"
    log "Action: ${ACTION}"
    
    case $ACTION in
        "deploy")
            check_prerequisites
            deploy_infrastructure
            deploy_application
            deploy_monitoring
            get_status
            ;;
        "infrastructure")
            check_prerequisites
            deploy_infrastructure
            ;;
        "application")
            deploy_application
            ;;
        "monitoring")
            deploy_monitoring
            ;;
        "status")
            get_status
            ;;
        "logs")
            show_logs
            ;;
        "scale")
            scale_application
            ;;
        "cleanup")
            cleanup
            ;;
        *)
            echo "Usage: $0 [provider] [environment] [action]"
            echo ""
            echo "Providers:"
            echo "  aws   - Amazon Web Services"
            echo "  gcp   - Google Cloud Platform"
            echo "  azure - Microsoft Azure"
            echo ""
            echo "Environments:"
            echo "  production - Production deployment"
            echo "  staging    - Staging deployment"
            echo "  development - Development deployment"
            echo ""
            echo "Actions:"
            echo "  deploy         - Deploy everything (infrastructure + application + monitoring)"
            echo "  infrastructure - Deploy only infrastructure"
            echo "  application    - Deploy only application"
            echo "  monitoring     - Deploy only monitoring"
            echo "  status         - Show application status"
            echo "  logs           - Show application logs"
            echo "  scale          - Scale application"
            echo "  cleanup        - Clean up resources"
            echo ""
            echo "Examples:"
            echo "  $0 aws production deploy"
            echo "  $0 gcp staging application"
            echo "  $0 azure production status"
            ;;
    esac
}

# Run main function
main "$@"
