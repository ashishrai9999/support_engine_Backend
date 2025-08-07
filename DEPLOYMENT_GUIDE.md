# Deployment Guide for MCP Vert.x Backend

This guide will help you deploy your Vert.x backend to free platforms.

## Prerequisites

1. **GitHub Account** - Your code should be in a GitHub repository
2. **Environment Variables** - You'll need to set up your API keys
3. **MongoDB** - You'll need a MongoDB instance

## Option 1: Railway (Recommended)

### Step 1: Prepare Your Repository
1. Push your code to GitHub
2. Make sure your `railway.json` file is in the root directory

### Step 2: Deploy to Railway
1. Go to [Railway.app](https://railway.app)
2. Sign up with your GitHub account
3. Click "New Project" → "Deploy from GitHub repo"
4. Select your repository
5. Railway will automatically detect your Dockerfile and deploy

### Step 3: Set Environment Variables
1. In your Railway project dashboard, go to "Variables"
2. Add these environment variables:
   ```
   GEMINI_API_KEY=your_gemini_api_key_here
   MONGODB_URI=your_mongodb_connection_string
   JAVA_OPTS=-Xmx1g -Xms512m
   ```

### Step 4: Add MongoDB
1. In Railway dashboard, click "New" → "Database" → "MongoDB"
2. This will automatically create a MongoDB instance
3. Copy the connection string and update your `MONGODB_URI` variable

### Step 5: Get Your URL
- Railway will provide you with a URL like: `https://your-app-name.railway.app`
- Your API endpoints will be available at:
  - `https://your-app-name.railway.app/stream/chat`
  - `https://your-app-name.railway.app/stream/login`
  - `https://your-app-name.railway.app/stream/saas`
  - etc.

## Option 2: Render

### Step 1: Deploy to Render
1. Go to [Render.com](https://render.com)
2. Sign up with your GitHub account
3. Click "New" → "Web Service"
4. Connect your GitHub repository
5. Render will automatically detect your `render.yaml` file

### Step 2: Configure Environment Variables
1. In your Render dashboard, go to "Environment"
2. Add the same environment variables as Railway

### Step 3: Add MongoDB
1. In Render dashboard, click "New" → "PostgreSQL" (or use external MongoDB)
2. For free tier, you might need to use MongoDB Atlas (free tier available)

## Option 3: Fly.io

### Step 1: Install Fly CLI
```bash
# macOS
brew install flyctl

# Linux
curl -L https://fly.io/install.sh | sh
```

### Step 2: Login and Deploy
```bash
fly auth login
fly launch
```

### Step 3: Set Secrets
```bash
fly secrets set GEMINI_API_KEY=your_api_key_here
fly secrets set MONGODB_URI=your_mongodb_uri
```

## MongoDB Options

### Option A: MongoDB Atlas (Free Tier)
1. Go to [MongoDB Atlas](https://www.mongodb.com/atlas)
2. Create a free account
3. Create a new cluster (free tier)
4. Get your connection string
5. Use it in your environment variables

### Option B: Railway/Render MongoDB
- Both Railway and Render offer MongoDB as a service
- Use their built-in MongoDB instances for easier setup

## Environment Variables Required

Make sure to set these in your chosen platform:

```bash
GEMINI_API_KEY=your_gemini_api_key_here
MONGODB_URI=mongodb://username:password@host:port/database
JAVA_OPTS=-Xmx1g -Xms512m
```

## Testing Your Deployment

Once deployed, test your endpoints:

```bash
# Test health check
curl https://your-app-url/mcp/sse

# Test chat endpoint
curl -X POST https://your-app-url/stream/chat \
  -H "Content-Type: application/json" \
  -d '{
    "params": {
      "query": "Hello",
      "module": "test"
    }
  }'
```

## Troubleshooting

### Common Issues:

1. **Build Failures**
   - Check if all dependencies are in `build.gradle`
   - Ensure Dockerfile is correct

2. **Runtime Errors**
   - Check environment variables are set correctly
   - Verify MongoDB connection string

3. **Memory Issues**
   - Adjust `JAVA_OPTS` for your platform's memory limits
   - Railway: `-Xmx1g -Xms512m`
   - Render: `-Xmx1g -Xms512m`
   - Fly.io: `-Xmx1g -Xms512m`

### Logs
- Railway: Check "Deployments" tab for logs
- Render: Check "Logs" tab
- Fly.io: `fly logs`

## Cost Considerations

### Free Tier Limits:
- **Railway**: $5/month free credit, then pay-as-you-go
- **Render**: Free tier with sleep after inactivity
- **Fly.io**: 3 shared-cpu-1x 256mb VMs, 3GB persistent volume storage

### Recommendations:
1. **Start with Railway** - Easiest setup, good free tier
2. **Use MongoDB Atlas** - Free tier with 512MB storage
3. **Monitor usage** - Set up alerts to avoid unexpected charges

## Next Steps

1. Deploy to your chosen platform
2. Set up environment variables
3. Test all endpoints
4. Set up monitoring and alerts
5. Configure custom domain (optional)

Your backend will be live and accessible via HTTPS! 