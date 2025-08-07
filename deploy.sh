#!/bin/bash

# MCP Vert.x Backend Deployment Script
# This script helps you prepare and deploy your backend

echo "🚀 MCP Vert.x Backend Deployment Helper"
echo "========================================"

# Check if git is initialized
if [ ! -d ".git" ]; then
    echo "❌ Git repository not found. Please initialize git first:"
    echo "   git init"
    echo "   git add ."
    echo "   git commit -m 'Initial commit'"
    echo "   git remote add origin <your-github-repo-url>"
    echo "   git push -u origin main"
    exit 1
fi

# Check if all required files exist
echo "📋 Checking required files..."

required_files=("Dockerfile" "build.gradle" "railway.json" "render.yaml" "fly.toml")
missing_files=()

for file in "${required_files[@]}"; do
    if [ ! -f "$file" ]; then
        missing_files+=("$file")
    fi
done

if [ ${#missing_files[@]} -gt 0 ]; then
    echo "❌ Missing required files: ${missing_files[*]}"
    exit 1
fi

echo "✅ All required files found!"

# Check environment variables
echo ""
echo "🔧 Environment Variables Check"
echo "=============================="

if [ -z "$GEMINI_API_KEY" ]; then
    echo "⚠️  GEMINI_API_KEY not set"
    echo "   You'll need to set this in your deployment platform"
else
    echo "✅ GEMINI_API_KEY is set"
fi

if [ -z "$MONGODB_URI" ]; then
    echo "⚠️  MONGODB_URI not set"
    echo "   You'll need to set this in your deployment platform"
else
    echo "✅ MONGODB_URI is set"
fi

# Build the project locally to check for issues
echo ""
echo "🔨 Building project locally..."
if ./gradlew build; then
    echo "✅ Build successful!"
else
    echo "❌ Build failed! Please fix the issues before deploying."
    exit 1
fi

# Show deployment options
echo ""
echo "🌐 Deployment Options"
echo "===================="
echo "1. Railway (Recommended) - https://railway.app"
echo "2. Render - https://render.com"
echo "3. Fly.io - https://fly.io"
echo ""
echo "📖 See DEPLOYMENT_GUIDE.md for detailed instructions"
echo ""

# Check if code is pushed to GitHub
if git remote -v | grep -q "origin"; then
    echo "✅ GitHub remote configured"
    echo "🔗 Your repository URL: $(git remote get-url origin)"
else
    echo "⚠️  No GitHub remote configured"
    echo "   Please add your GitHub repository:"
    echo "   git remote add origin <your-github-repo-url>"
fi

echo ""
echo "🎯 Next Steps:"
echo "1. Push your code to GitHub: git push origin main"
echo "2. Choose a deployment platform from the options above"
echo "3. Follow the instructions in DEPLOYMENT_GUIDE.md"
echo "4. Set up your environment variables in the platform dashboard"
echo "5. Test your deployed endpoints"
echo ""
echo "Good luck with your deployment! 🚀" 