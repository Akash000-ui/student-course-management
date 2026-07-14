# SendGrid Setup Guide for StudieHub

## Why SendGrid?
Render blocks SMTP ports (587 and 465) on free tier. SendGrid uses HTTP API which works perfectly on all platforms.

## Step 1: Create SendGrid Account (FREE)

1. Go to https://signup.sendgrid.com/
2. Sign up for a **FREE account** (100 emails/day forever)
3. Verify your email address
4. Complete the onboarding process

## Step 2: Get SendGrid API Key

1. Login to SendGrid dashboard
2. Go to **Settings** → **API Keys**
3. Click **Create API Key**
4. Name it: `StudieHub Production`
5. Choose **Full Access** (or at least **Mail Send** permission)
6. Click **Create & View**
7. **COPY the API key immediately** (you won't see it again!)
   - Format: `SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

## Step 3: Verify Sender Email (IMPORTANT!)

SendGrid requires sender verification to prevent spam:

1. Go to **Settings** → **Sender Authentication**
2. Click **Verify a Single Sender**
3. Fill in the form:
   - **From Name**: StudieHub
   - **From Email Address**: taryareddy123@gmail.com
   - **Reply To**: taryareddy123@gmail.com
   - **Company**: StudieHub
   - Fill other required fields
4. Click **Create**
5. **Check your email** (taryareddy123@gmail.com)
6. **Click the verification link** in the SendGrid email
7. Wait for "Verified" status in dashboard

## Step 4: Update Render Environment Variables

Go to your Render dashboard → Your service → Environment:

### **Remove Old SMTP Variables:**
- ❌ Delete `SPRING_MAIL_HOST`
- ❌ Delete `SPRING_MAIL_PORT`
- ❌ Delete `SPRING_MAIL_USERNAME`
- ❌ Delete `SPRING_MAIL_PASSWORD`
- ❌ Delete all `SPRING_MAIL_PROPERTIES_*` variables

### **Add New SendGrid Variables:**

```
SENDGRID_API_KEY=SG.your_actual_api_key_here
SENDGRID_FROM_EMAIL=taryareddy123@gmail.com
SENDGRID_FROM_NAME=StudieHub
```

**⚠️ CRITICAL:** Replace `SG.your_actual_api_key_here` with your actual API key from Step 2!

## Step 5: Deploy Updated Code

### Option 1: Rebuild and Push Docker Image

```powershell
cd d:\stdudent_website\Student-Course-Management
docker build -t akash757/studiehub-backend:latest .
docker push akash757/studiehub-backend:latest
```

Then redeploy on Render.

### Option 2: Render Auto-Deploy (if connected to GitHub)

Just push your code to GitHub and Render will auto-deploy.

## Step 6: Test Email Functionality

1. Go to your deployed frontend
2. Try Sign In with OTP
3. Enter an email address
4. Click "Send OTP"
5. ✅ You should receive the OTP email within seconds!

## Troubleshooting

### "403 Forbidden" Error
- **Cause**: Sender email not verified
- **Fix**: Complete Step 3 above

### "401 Unauthorized" Error
- **Cause**: Invalid API key
- **Fix**: Check that `SENDGRID_API_KEY` in Render matches exactly from SendGrid

### "Rate Limit Exceeded"
- **Cause**: Sent more than 100 emails/day on free plan
- **Fix**: Upgrade to paid plan or wait 24 hours

### Still Not Working?
- Check Render logs for detailed error messages
- Verify all environment variables are set correctly
- Ensure sender email is verified in SendGrid

## Benefits of SendGrid

✅ Works on ALL cloud platforms (Render, Heroku, Vercel, etc.)  
✅ 100 free emails per day (forever)  
✅ Reliable delivery (99.9% uptime)  
✅ Email tracking and analytics  
✅ No SMTP port blocking issues  
✅ Faster than SMTP (uses HTTP API)  

## Free Tier Limits

- **100 emails/day** forever
- Full API access
- Email validation
- Bounce handling
- Analytics dashboard

Need more? Paid plans start at $15/month for 40,000 emails.
