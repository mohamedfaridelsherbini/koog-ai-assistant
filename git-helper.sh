#!/bin/bash

# Git Workflow Helper Script
# Usage: ./git-helper.sh [command] [options]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper functions
print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if we're in a git repository
check_git_repo() {
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        print_error "Not in a git repository!"
        exit 1
    fi
}

# Show current status
show_status() {
    print_header "Git Status"
    echo "Current branch: $(git branch --show-current)"
    echo "Last commit: $(git log -1 --oneline)"
    echo ""
    git status --short
}

# Start new feature
start_feature() {
    if [ -z "$1" ]; then
        print_error "Please provide feature name: ./git-helper.sh feature my-feature-name"
        exit 1
    fi
    
    local feature_name="$1"
    local branch_name="feature/$feature_name"
    
    print_header "Starting New Feature: $feature_name"
    
    # Switch to develop and pull latest
    git checkout develop
    git pull origin develop
    
    # Create feature branch
    git checkout -b "$branch_name"
    
    print_success "Created feature branch: $branch_name"
    print_warning "Remember to commit your changes and merge back to develop when ready"
}

# Finish feature
finish_feature() {
    local current_branch=$(git branch --show-current)
    
    if [[ ! "$current_branch" =~ ^feature/ ]]; then
        print_error "Not on a feature branch!"
        exit 1
    fi
    
    print_header "Finishing Feature: $current_branch"
    
    # Check for uncommitted changes
    if ! git diff --quiet; then
        print_warning "You have uncommitted changes. Please commit or stash them first."
        git status --short
        exit 1
    fi
    
    # Switch to develop and merge
    git checkout develop
    git pull origin develop
    git merge "$current_branch"
    
    print_success "Merged $current_branch into develop"
    
    # Ask if user wants to delete feature branch
    read -p "Delete feature branch $current_branch? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        git branch -d "$current_branch"
        print_success "Deleted feature branch: $current_branch"
    fi
    
    print_warning "Don't forget to push develop: git push origin develop"
}

# Create release
create_release() {
    if [ -z "$1" ]; then
        print_error "Please provide version number: ./git-helper.sh release 1.1.0"
        exit 1
    fi
    
    local version="$1"
    local release_branch="release/v$version"
    
    print_header "Creating Release: v$version"
    
    # Switch to develop and pull latest
    git checkout develop
    git pull origin develop
    
    # Create release branch
    git checkout -b "$release_branch"
    git push origin "$release_branch"
    
    print_success "Created release branch: $release_branch"
    print_warning "Make final preparations, then run: ./git-helper.sh finish-release $version"
}

# Finish release
finish_release() {
    if [ -z "$1" ]; then
        print_error "Please provide version number: ./git-helper.sh finish-release 1.1.0"
        exit 1
    fi
    
    local version="$1"
    local release_branch="release/v$version"
    local tag_name="v$version"
    
    print_header "Finishing Release: v$version"
    
    # Check if we're on release branch
    local current_branch=$(git branch --show-current)
    if [ "$current_branch" != "$release_branch" ]; then
        print_error "Not on release branch $release_branch!"
        exit 1
    fi
    
    # Merge to main
    git checkout main
    git pull origin main
    git merge "$release_branch"
    
    # Create tag
    git tag -a "$tag_name" -m "Release $tag_name"
    
    # Push main and tag
    git push origin main
    git push origin "$tag_name"
    
    # Merge back to develop
    git checkout develop
    git merge "$release_branch"
    git push origin develop
    
    # Clean up
    git branch -d "$release_branch"
    
    print_success "Release v$version completed!"
    print_success "Merged to main and develop"
    print_success "Created tag: $tag_name"
}

# Create hotfix
create_hotfix() {
    if [ -z "$1" ]; then
        print_error "Please provide hotfix name: ./git-helper.sh hotfix critical-bug-fix"
        exit 1
    fi
    
    local hotfix_name="$1"
    local branch_name="hotfix/$hotfix_name"
    
    print_header "Creating Hotfix: $hotfix_name"
    
    # Switch to main and pull latest
    git checkout main
    git pull origin main
    
    # Create hotfix branch
    git checkout -b "$branch_name"
    
    print_success "Created hotfix branch: $branch_name"
    print_warning "Fix the issue, then run: ./git-helper.sh finish-hotfix $hotfix_name"
}

# Finish hotfix
finish_hotfix() {
    if [ -z "$1" ]; then
        print_error "Please provide hotfix name: ./git-helper.sh finish-hotfix critical-bug-fix"
        exit 1
    fi
    
    local hotfix_name="$1"
    local branch_name="hotfix/$hotfix_name"
    
    print_header "Finishing Hotfix: $hotfix_name"
    
    # Check if we're on hotfix branch
    local current_branch=$(git branch --show-current)
    if [ "$current_branch" != "$branch_name" ]; then
        print_error "Not on hotfix branch $branch_name!"
        exit 1
    fi
    
    # Merge to main
    git checkout main
    git pull origin main
    git merge "$branch_name"
    
    # Create patch tag (assuming current version is v1.0.0)
    local current_tag=$(git describe --tags --abbrev=0)
    local patch_version=$(echo "$current_tag" | sed 's/v\([0-9]*\)\.\([0-9]*\)\.\([0-9]*\)/\1.\2.\3/' | awk -F. '{print $1"."$2"."($3+1)}')
    local tag_name="v$patch_version"
    
    git tag -a "$tag_name" -m "Hotfix $tag_name: $hotfix_name"
    
    # Push main and tag
    git push origin main
    git push origin "$tag_name"
    
    # Merge back to develop
    git checkout develop
    git merge "$branch_name"
    git push origin develop
    
    # Clean up
    git branch -d "$branch_name"
    
    print_success "Hotfix completed!"
    print_success "Created tag: $tag_name"
}

# Show help
show_help() {
    print_header "Git Workflow Helper"
    echo "Usage: ./git-helper.sh [command] [options]"
    echo ""
    echo "Commands:"
    echo "  status                    - Show current git status"
    echo "  feature <name>            - Start new feature branch"
    echo "  finish-feature           - Finish current feature"
    echo "  release <version>        - Create release branch"
    echo "  finish-release <version> - Finish release"
    echo "  hotfix <name>            - Create hotfix branch"
    echo "  finish-hotfix <name>     - Finish hotfix"
    echo "  help                     - Show this help"
    echo ""
    echo "Examples:"
    echo "  ./git-helper.sh feature user-auth"
    echo "  ./git-helper.sh release 1.1.0"
    echo "  ./git-helper.sh hotfix critical-bug"
}

# Main script logic
check_git_repo

case "${1:-help}" in
    "status")
        show_status
        ;;
    "feature")
        start_feature "$2"
        ;;
    "finish-feature")
        finish_feature
        ;;
    "release")
        create_release "$2"
        ;;
    "finish-release")
        finish_release "$2"
        ;;
    "hotfix")
        create_hotfix "$2"
        ;;
    "finish-hotfix")
        finish_hotfix "$2"
        ;;
    "help"|*)
        show_help
        ;;
esac
