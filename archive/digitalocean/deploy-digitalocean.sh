#!/bin/bash

# DigitalOcean Deployment Script for Koog AI Assistant
# Usage: ./scripts/deploy-digitalocean.sh [deploy|status|logs|stop|cleanup]

set -e

# Configuration
PROJECT_NAME="koog-ai-assistant"
DROPLET_NAME="koog-ai-production"
DROPLET_SIZE="s-2vcpu-4gb"  # 2 vCPU, 4GB RAM
REGION="fra1"
IMAGE="ubuntu-22-04-x64"
SSH_KEY_NAME="Personal Mac"
DOMAIN="www.mohamedfaridelsherbini.com"
IP_ADDRESS="159.89.5.103"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

check_requirements() {
    log_info "Checking requirements..."
    
    # Check if doctl is installed
    if ! command -v doctl &> /dev/null; then
        log_error "doctl is not installed. Please install it first:"
        echo "  brew install doctl"
        echo "  doctl auth init"
        exit 1
    fi
    
    # Check if DO_TOKEN is set
    if [ -z "$DO_TOKEN" ]; then
        log_error "DO_TOKEN environment variable is not set"
        echo "  export DO_TOKEN=\"your_digitalocean_token\""
        exit 1
    fi
    
    # Check if Docker is running
    if ! docker info &> /dev/null; then
        log_error "Docker is not running. Please start Docker first."
        exit 1
    fi
    
    log_success "All requirements met"
}

create_ssh_key() {
    log_info "Setting up SSH key..."
    
    # Check if SSH key already exists
    if doctl compute ssh-key list | grep -q "$SSH_KEY_NAME"; then
        log_info "SSH key '$SSH_KEY_NAME' already exists"
        return
    fi
    
    log_error "SSH key '$SSH_KEY_NAME' not found in DigitalOcean account"
    echo "Available SSH keys:"
    doctl compute ssh-key list
    echo ""
    echo "Please update the SSH_KEY_NAME in the script to use an existing key, or create a new one manually."
    exit 1
}

create_droplet() {
    log_info "Creating DigitalOcean droplet..."
    
    # Check if droplet already exists
    if doctl compute droplet list | grep -q "$DROPLET_NAME"; then
        log_warning "Droplet '$DROPLET_NAME' already exists"
        return
    fi
    
    # Get SSH key ID
    SSH_KEY_ID=$(doctl compute ssh-key list --format ID,Name --no-header | grep "$SSH_KEY_NAME" | awk '{print $1}')
    
    if [ -z "$SSH_KEY_ID" ]; then
        log_error "SSH key not found"
        exit 1
    fi
    
    # Create droplet
    doctl compute droplet create "$DROPLET_NAME" \
        --size "$DROPLET_SIZE" \
        --image "$IMAGE" \
        --region "$REGION" \
        --ssh-keys "$SSH_KEY_ID" \
        --wait
    
    log_success "Droplet created successfully"
}

get_droplet_ip() {
    doctl compute droplet list --format Name,PublicIPv4 --no-header | grep "$DROPLET_NAME" | awk '{print $2}'
}

setup_droplet() {
    local ip=$(get_droplet_ip)
    
    if [ -z "$ip" ]; then
        log_error "Could not get droplet IP address"
        exit 1
    fi
    
    log_info "Setting up droplet at $ip..."
    
    # Wait for droplet to be ready
    log_info "Waiting for droplet to be ready..."
    sleep 30
    
    # Update system and install Docker
    ssh -i ~/.ssh/id_ed25519_digital -o StrictHostKeyChecking=no root@$ip << 'EOF'
        apt-get update
        apt-get install -y apt-transport-https ca-certificates curl gnupg lsb-release
        
        # Install Docker
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
        echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
        apt-get update
        apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
        
        # Install Docker Compose
        curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
        chmod +x /usr/local/bin/docker-compose
        
        # Install Java 17
        apt-get install -y openjdk-17-jdk
        
        # Install Git
        apt-get install -y git
        
        # Install Nginx
        apt-get install -y nginx
        
        # Install Certbot for SSL
        apt-get install -y certbot python3-certbot-nginx
        
        # Create application directory
        mkdir -p /opt/koog-ai
        chown -R root:root /opt/koog-ai
EOF
    
    log_success "Droplet setup completed"
}

deploy_application() {
    local ip=$(get_droplet_ip)
    
    log_info "Deploying application to $ip..."
    
    # Build the application locally
    log_info "Building application..."
    ./gradlew clean build -x test
    
    # Create deployment package
    log_info "Creating deployment package..."
    tar -czf koog-ai-deployment.tar.gz \
        app/build/libs/app.jar \
        docker-compose.yml \
        nginx.conf \
        scripts/
    
    # Copy files to droplet
    log_info "Copying files to droplet..."
    scp -i ~/.ssh/id_ed25519_digital -o StrictHostKeyChecking=no koog-ai-deployment.tar.gz root@$ip:/opt/koog-ai/
    scp -i ~/.ssh/id_ed25519_digital -o StrictHostKeyChecking=no nginx/digitalocean.conf root@$ip:/etc/nginx/sites-available/koog-ai
    
    # Extract and setup on droplet
    ssh -i ~/.ssh/id_ed25519_digital -o StrictHostKeyChecking=no root@$ip << EOF
        cd /opt/koog-ai
        tar -xzf koog-ai-deployment.tar.gz
        rm koog-ai-deployment.tar.gz
        
        # Setup Nginx
        ln -sf /etc/nginx/sites-available/koog-ai /etc/nginx/sites-enabled/
        rm -f /etc/nginx/sites-enabled/default
        nginx -t
        systemctl reload nginx
        
        # Start Docker services
        docker-compose up -d
        
        # Start the application
        nohup java -jar app.jar --web > app.log 2>&1 &
        
        # Setup systemd service
        cat > /etc/systemd/system/koog-ai.service << 'SERVICE_EOF'
[Unit]
Description=Koog AI Assistant
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/koog-ai
ExecStart=/usr/bin/java -jar app.jar --web
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
SERVICE_EOF
        
        systemctl daemon-reload
        systemctl enable koog-ai
        systemctl restart koog-ai
EOF
    
    # Cleanup local files
    rm -f koog-ai-deployment.tar.gz
    
    log_success "Application deployed successfully"
}

setup_ssl() {
    local ip=$(get_droplet_ip)
    
    log_info "Setting up SSL certificate..."
    
    ssh -i ~/.ssh/id_ed25519_digital -o StrictHostKeyChecking=no root@$ip << EOF
        # Stop nginx temporarily
        systemctl stop nginx
        
        # Get SSL certificate
        certbot certonly --standalone -d $DOMAIN --non-interactive --agree-tos --email admin@$DOMAIN
        
        # Update nginx config for SSL
        sed -i 's/listen 80;/listen 443 ssl;/' /etc/nginx/sites-available/koog-ai
        sed -i '/server_name/a\\n    ssl_certificate /etc/letsencrypt/live/'$DOMAIN'/fullchain.pem;\n    ssl_certificate_key /etc/letsencrypt/live/'$DOMAIN'/privkey.pem;' /etc/nginx/sites-available/koog-ai
        
        # Add HTTP to HTTPS redirect
        cat >> /etc/nginx/sites-available/koog-ai << 'NGINX_EOF'

server {
    listen 80;
    server_name $DOMAIN;
    return 301 https://\$server_name\$request_uri;
}
NGINX_EOF
        
        # Test and reload nginx
        nginx -t
        systemctl start nginx
        systemctl reload nginx
        
        # Setup auto-renewal
        echo "0 12 * * * /usr/bin/certbot renew --quiet" | crontab -
EOF
    
    log_success "SSL certificate setup completed"
}

show_status() {
    local ip=$(get_droplet_ip)
    
    if [ -z "$ip" ]; then
        log_error "Droplet not found"
        return 1
    fi
    
    log_info "Droplet Status:"
    doctl compute droplet list --format Name,Status,PublicIPv4,Memory,Disk --no-header | grep "$DROPLET_NAME"
    
    log_info "Application Status:"
    ssh -i ~/.ssh/id_ed25519_digital -o StrictHostKeyChecking=no root@$ip << 'EOF'
        echo "Docker Services:"
        docker-compose ps
        
        echo -e "\nApplication Service:"
        systemctl status koog-ai --no-pager
        
        echo -e "\nNginx Status:"
        systemctl status nginx --no-pager
        
        echo -e "\nApplication Logs (last 20 lines):"
        tail -20 /opt/koog-ai/app.log
EOF
}

show_logs() {
    local ip=$(get_droplet_ip)
    
    if [ -z "$ip" ]; then
        log_error "Droplet not found"
        return 1
    fi
    
    log_info "Showing application logs..."
    ssh -i ~/.ssh/id_ed25519_digital -o StrictHostKeyChecking=no root@$ip "tail -f /opt/koog-ai/app.log"
}

stop_application() {
    local ip=$(get_droplet_ip)
    
    if [ -z "$ip" ]; then
        log_error "Droplet not found"
        return 1
    fi
    
    log_info "Stopping application..."
    ssh -i ~/.ssh/id_ed25519_digital -o StrictHostKeyChecking=no root@$ip << 'EOF'
        systemctl stop koog-ai
        docker-compose down
        systemctl stop nginx
EOF
    
    log_success "Application stopped"
}

cleanup() {
    log_info "Cleaning up DigitalOcean resources..."
    
    # Delete droplet
    if doctl compute droplet list | grep -q "$DROPLET_NAME"; then
        doctl compute droplet delete "$DROPLET_NAME" --force
        log_success "Droplet deleted"
    fi
    
    # Delete SSH key
    if doctl compute ssh-key list | grep -q "$SSH_KEY_NAME"; then
        SSH_KEY_ID=$(doctl compute ssh-key list --format ID,Name --no-header | grep "$SSH_KEY_NAME" | awk '{print $1}')
        doctl compute ssh-key delete "$SSH_KEY_ID" --force
        log_success "SSH key deleted"
    fi
    
    log_success "Cleanup completed"
}

# Main script
case "${1:-deploy}" in
    "deploy")
        log_info "Starting DigitalOcean deployment..."
        check_requirements
        create_ssh_key
        create_droplet
        setup_droplet
        deploy_application
        setup_ssl
        
        ip=$(get_droplet_ip)
        log_success "Deployment completed!"
        echo ""
        echo "🌐 Application URL: https://$DOMAIN"
        echo "🔧 Droplet IP: $ip"
        echo "📊 Check status: ./scripts/deploy-digitalocean.sh status"
        echo "📝 View logs: ./scripts/deploy-digitalocean.sh logs"
        ;;
    "status")
        show_status
        ;;
    "logs")
        show_logs
        ;;
    "stop")
        stop_application
        ;;
    "cleanup")
        cleanup
        ;;
    *)
        echo "Usage: $0 [deploy|status|logs|stop|cleanup]"
        echo ""
        echo "Commands:"
        echo "  deploy   - Deploy the application to DigitalOcean"
        echo "  status   - Show deployment status"
        echo "  logs     - Show application logs"
        echo "  stop     - Stop the application"
        echo "  cleanup  - Remove all DigitalOcean resources"
        exit 1
        ;;
esac