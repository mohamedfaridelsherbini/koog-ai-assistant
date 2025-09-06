# Koog AI Assistant - DigitalOcean Terraform Configuration
terraform {
  required_version = ">= 1.0"
  required_providers {
    digitalocean = {
      source  = "digitalocean/digitalocean"
      version = "~> 2.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.23"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.11"
    }
  }
}

# Configure DigitalOcean Provider
provider "digitalocean" {
  token = var.do_token
}

# Data sources
data "digitalocean_kubernetes_versions" "stable" {
  version_prefix = "1.28."
}

data "digitalocean_kubernetes_cluster" "koog_ai_cluster" {
  name = digitalocean_kubernetes_cluster.koog_ai_cluster.name
  depends_on = [digitalocean_kubernetes_cluster.koog_ai_cluster]
}

# VPC Configuration
resource "digitalocean_vpc" "koog_ai_vpc" {
  name     = "koog-ai-vpc-${var.environment}"
  region   = var.region
  ip_range = "10.10.0.0/16"
}

# Kubernetes Cluster
resource "digitalocean_kubernetes_cluster" "koog_ai_cluster" {
  name    = "koog-ai-cluster-${var.environment}"
  region  = var.region
  version = data.digitalocean_kubernetes_versions.stable.latest_version
  vpc_uuid = digitalocean_vpc.koog_ai_vpc.id

  node_pool {
    name       = "koog-ai-nodes"
    size       = var.node_size
    node_count = var.node_count
    auto_scale = true
    min_nodes  = var.min_nodes
    max_nodes  = var.max_nodes
    tags       = ["koog-ai", "production"]
  }

  tags = ["koog-ai", "production", var.environment]
}

# Load Balancer
resource "digitalocean_loadbalancer" "koog_ai_lb" {
  name   = "koog-ai-lb-${var.environment}"
  region = var.region
  vpc_uuid = digitalocean_vpc.koog_ai_vpc.id

  forwarding_rule {
    entry_protocol  = "http"
    entry_port      = 80
    target_protocol = "http"
    target_port     = 30080
  }

  forwarding_rule {
    entry_protocol  = "https"
    entry_port      = 443
    target_protocol = "http"
    target_port     = 30080
    tls_passthrough = true
  }

  healthcheck {
    protocol               = "http"
    port                   = 30080
    path                   = "/health"
    check_interval_seconds = 10
    response_timeout_seconds = 5
    unhealthy_threshold    = 3
    healthy_threshold      = 2
  }

  droplet_ids = digitalocean_kubernetes_cluster.koog_ai_cluster.node_pool[0].nodes[*].id
}

# Domain Configuration
resource "digitalocean_domain" "koog_ai_domain" {
  name = var.domain_name
}

# Domain Records
resource "digitalocean_record" "koog_ai_www" {
  domain = digitalocean_domain.koog_ai_domain.name
  type   = "A"
  name   = "www"
  value  = digitalocean_loadbalancer.koog_ai_lb.ip
  ttl    = 300
}

resource "digitalocean_record" "koog_ai_root" {
  domain = digitalocean_domain.koog_ai_domain.name
  type   = "A"
  name   = "@"
  value  = digitalocean_loadbalancer.koog_ai_lb.ip
  ttl    = 300
}

# Spaces Bucket for static assets
resource "digitalocean_spaces_bucket" "koog_ai_assets" {
  name   = "koog-ai-assets-${var.environment}"
  region = var.region
}

# Spaces Bucket CORS
resource "digitalocean_spaces_bucket_cors" "koog_ai_assets_cors" {
  bucket = digitalocean_spaces_bucket.koog_ai_assets.name
  region = var.region

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "HEAD"]
    allowed_origins = ["https://${var.domain_name}", "https://www.${var.domain_name}"]
    max_age_seconds = 3000
  }
}

# Firewall
resource "digitalocean_firewall" "koog_ai_firewall" {
  name = "koog-ai-firewall-${var.environment}"

  droplet_ids = digitalocean_kubernetes_cluster.koog_ai_cluster.node_pool[0].nodes[*].id

  inbound_rule {
    protocol         = "tcp"
    port_range       = "22"
    source_addresses = ["0.0.0.0/0", "::/0"]
  }

  inbound_rule {
    protocol         = "tcp"
    port_range       = "80"
    source_addresses = ["0.0.0.0/0", "::/0"]
  }

  inbound_rule {
    protocol         = "tcp"
    port_range       = "443"
    source_addresses = ["0.0.0.0/0", "::/0"]
  }

  inbound_rule {
    protocol         = "tcp"
    port_range       = "30000-32767"
    source_addresses = ["0.0.0.0/0", "::/0"]
  }

  outbound_rule {
    protocol              = "tcp"
    port_range            = "1-65535"
    destination_addresses = ["0.0.0.0/0", "::/0"]
  }

  outbound_rule {
    protocol              = "udp"
    port_range            = "1-65535"
    destination_addresses = ["0.0.0.0/0", "::/0"]
  }
}

# Container Registry
resource "digitalocean_container_registry" "koog_ai_registry" {
  name                   = "koog-ai-registry"
  subscription_tier_slug = "starter"
  region                 = var.region
}

# Outputs
output "cluster_id" {
  description = "The ID of the Kubernetes cluster"
  value       = digitalocean_kubernetes_cluster.koog_ai_cluster.id
}

output "cluster_endpoint" {
  description = "The endpoint of the Kubernetes cluster"
  value       = digitalocean_kubernetes_cluster.koog_ai_cluster.endpoint
}

output "cluster_kubeconfig" {
  description = "The kubeconfig for the Kubernetes cluster"
  value       = digitalocean_kubernetes_cluster.koog_ai_cluster.kube_config[0].raw_config
  sensitive   = true
}

output "load_balancer_ip" {
  description = "The IP address of the load balancer"
  value       = digitalocean_loadbalancer.koog_ai_lb.ip
}

output "domain_name" {
  description = "The domain name"
  value       = digitalocean_domain.koog_ai_domain.name
}

output "spaces_bucket_name" {
  description = "The name of the Spaces bucket"
  value       = digitalocean_spaces_bucket.koog_ai_assets.name
}

output "spaces_bucket_url" {
  description = "The URL of the Spaces bucket"
  value       = digitalocean_spaces_bucket.koog_ai_assets.bucket_domain_name
}

output "container_registry_endpoint" {
  description = "The endpoint of the container registry"
  value       = digitalocean_container_registry.koog_ai_registry.server_url
}
