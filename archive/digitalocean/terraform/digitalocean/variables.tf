# Koog AI Assistant - DigitalOcean Variables

variable "do_token" {
  description = "DigitalOcean API token"
  type        = string
  sensitive   = true
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "production"
}

variable "region" {
  description = "DigitalOcean region"
  type        = string
  default     = "nyc3"
}

variable "domain_name" {
  description = "Domain name for the application"
  type        = string
  default     = "mohamedfaridelsherbini.com"
}

variable "node_size" {
  description = "Size of the Kubernetes nodes"
  type        = string
  default     = "s-2vcpu-4gb"
}

variable "node_count" {
  description = "Number of nodes in the cluster"
  type        = number
  default     = 2
}

variable "min_nodes" {
  description = "Minimum number of nodes"
  type        = number
  default     = 1
}

variable "max_nodes" {
  description = "Maximum number of nodes"
  type        = number
  default     = 5
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = list(string)
  default = [
    "koog-ai",
    "production",
    "ai-assistant",
    "ollama"
  ]
}
