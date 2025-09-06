#!/bin/bash

# Koog AI Assistant v1.0.0 - DigitalOcean Deployment Script
# Domain: mohamedfaridelsherbini.com
# IP: 164.90.186.11

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
DOMAIN="mohamedfaridelsherbini.com"
IP="164.90.186.11"
REGION="nyc3"
ENVIRONMENT="production"
ACTION=${1:-deploy}

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
    log "Checking prerequisites for DigitalOcean deployment..."
    
    if ! command -v doctl &> /dev/null; then
        error "DigitalOcean CLI (doctl) is not installed. Please install it first: https://docs.digitalocean.com/reference/doctl/how-to/install/"
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
    
    # Check if DO token is set
    if [ -z "$DO_TOKEN" ]; then
        error "DO_TOKEN environment variable is not set. Please set it with your DigitalOcean API token."
    fi
    
    success "Prerequisites check passed"
}

# Authenticate with DigitalOcean
authenticate_do() {
    log "Authenticating with DigitalOcean..."
    
    # Set the token
    doctl auth init --access-token $DO_TOKEN
    
    # Verify authentication
    if ! doctl account get &> /dev/null; then
        error "Failed to authenticate with DigitalOcean. Please check your API token."
    fi
    
    success "Successfully authenticated with DigitalOcean"
}

# Deploy infrastructure
deploy_infrastructure() {
    log "Deploying infrastructure on DigitalOcean..."
    
    cd terraform/digitalocean
    
    # Initialize Terraform
    terraform init
    
    # Create terraform.tfvars if it doesn't exist
    if [ ! -f "terraform.tfvars" ]; then
        log "Creating terraform.tfvars from example..."
        cp terraform.tfvars.example terraform.tfvars
        warning "Please edit terraform.tfvars with your DigitalOcean API token and other settings"
        warning "Then run this script again"
        exit 1
    fi
    
    # Plan and apply
    terraform plan -var="do_token=$DO_TOKEN" -var="domain_name=$DOMAIN"
    terraform apply -auto-approve -var="do_token=$DO_TOKEN" -var="domain_name=$DOMAIN"
    
    # Get kubeconfig
    log "Configuring kubectl..."
    doctl kubernetes cluster kubeconfig save koog-ai-cluster-$ENVIRONMENT
    
    cd ../..
    success "Infrastructure deployed successfully"
}

# Deploy application
deploy_application() {
    log "Deploying Koog AI Assistant to DigitalOcean Kubernetes..."
    
    # Create namespace
    kubectl create namespace koog-ai --dry-run=client -o yaml | kubectl apply -f -
    
    # Deploy cert-manager for SSL
    kubectl apply -f k8s/digitalocean/cert-manager.yaml
    
    # Wait for cert-manager to be ready
    log "Waiting for cert-manager to be ready..."
    kubectl wait --for=condition=available --timeout=300s deployment/cert-manager -n cert-manager
    
    # Deploy using Helm
    helm upgrade --install koog-ai-assistant ./helm/koog-ai-assistant \
        --namespace koog-ai \
        --set koogAi.replicaCount=3 \
        --set ollama.enabled=true \
        --set ollama.resources.requests.memory=4Gi \
        --set ollama.resources.requests.cpu=2 \
        --set ollama.resources.limits.memory=8Gi \
        --set ollama.resources.limits.cpu=4 \
        --set ollama.persistence.enabled=true \
        --set ollama.persistence.size=50Gi \
        --set autoscaling.enabled=true \
        --set autoscaling.minReplicas=2 \
        --set autoscaling.maxReplicas=5 \
        --wait
    
    # Deploy ingress
    kubectl apply -f k8s/digitalocean/ingress.yaml
    
    success "Application deployed successfully"
}

# Setup SSL certificate
setup_ssl() {
    log "Setting up SSL certificate for $DOMAIN..."
    
    # Install certbot if not installed
    if ! command -v certbot &> /dev/null; then
        log "Installing certbot..."
        if [[ "$OSTYPE" == "darwin"* ]]; then
            brew install certbot
        else
            sudo apt-get update
            sudo apt-get install -y certbot python3-certbot-nginx
        fi
    fi
    
    # Get the load balancer IP
    LB_IP=$(kubectl get service koog-ai-loadbalancer -n koog-ai -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "")
    
    if [ -z "$LB_IP" ]; then
        warning "Load balancer IP not found. SSL setup will be handled by cert-manager in Kubernetes."
        return
    fi
    
    log "Load balancer IP: $LB_IP"
    log "Please ensure your domain $DOMAIN points to $LB_IP"
    log "You can check this with: dig $DOMAIN"
    
    success "SSL setup instructions provided"
}

# Get application status
get_status() {
    log "Checking application status..."
    
    # Check pods
    kubectl get pods -n koog-ai
    
    # Check services
    kubectl get services -n koog-ai
    
    # Check ingress
    kubectl get ingress -n koog-ai
    
    # Check HPA
    kubectl get hpa -n koog-ai
    
    # Get external IP
    EXTERNAL_IP=$(kubectl get service koog-ai-loadbalancer -n koog-ai -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "Pending")
    if [ "$EXTERNAL_IP" != "Pending" ] && [ "$EXTERNAL_IP" != "" ]; then
        success "Application is accessible at:"
        success "  HTTP:  http://$DOMAIN"
        success "  HTTPS: https://$DOMAIN"
        success "  IP:    $EXTERNAL_IP"
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

# Update application
update_application() {
    log "Updating application..."
    
    # Build and push new image
    log "Building new Docker image..."
    docker build -t ghcr.io/mohamedfaridelsherbini/koog-ai-assistant:latest .
    docker push ghcr.io/mohamedfaridelsherbini/koog-ai-assistant:latest
    
    # Update Helm deployment
    helm upgrade koog-ai-assistant ./helm/koog-ai-assistant \
        --namespace koog-ai \
        --set koogAi.image.tag=latest \
        --wait
    
    success "Application updated successfully"
}

# Clean up
cleanup() {
    log "Cleaning up DigitalOcean resources..."
    
    # Delete Helm release
    helm uninstall koog-ai-assistant -n koog-ai || true
    
    # Delete namespace
    kubectl delete namespace koog-ai || true
    
    # Delete infrastructure
    cd terraform/digitalocean
    terraform destroy -auto-approve -var="do_token=$DO_TOKEN" -var="domain_name=$DOMAIN"
    cd ../..
    
    success "Cleanup completed"
}

# Main execution
main() {
    log "🌊 Koog AI Assistant v1.0.0 - DigitalOcean Deployment"
    log "Domain: $DOMAIN"
    log "IP: $IP"
    log "Region: $REGION"
    log "Action: $ACTION"
    
    case $ACTION in
        "deploy")
            check_prerequisites
            authenticate_do
            deploy_infrastructure
            deploy_application
            setup_ssl
            get_status
            ;;
        "infrastructure")
            check_prerequisites
            authenticate_do
            deploy_infrastructure
            ;;
        "application")
            deploy_application
            ;;
        "ssl")
            setup_ssl
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
        "update")
            update_application
            ;;
        "cleanup")
            cleanup
            ;;
        *)
            echo "Usage: $0 [action]"
            echo ""
            echo "Actions:"
            echo "  deploy         - Deploy everything (infrastructure + application + SSL)"
            echo "  infrastructure - Deploy only infrastructure"
            echo "  application    - Deploy only application"
            echo "  ssl            - Setup SSL certificate"
            echo "  status         - Show application status"
            echo "  logs           - Show application logs"
            echo "  scale          - Scale application"
            echo "  update         - Update application"
            echo "  cleanup        - Clean up all resources"
            echo ""
            echo "Environment Variables:"
            echo "  DO_TOKEN       - DigitalOcean API token (required)"
            echo ""
            echo "Example:"
            echo "  export DO_TOKEN=your_digitalocean_api_token"
            echo "  $0 deploy"
            ;;
    esac
}

# Run main function
main "$@"
