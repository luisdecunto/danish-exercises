# GitHub Setup Steps - Do This on Your Phone

## ✅ Already Done (Locally)
- Git repository initialized
- All files committed
- Data structure created with example exercises

---

## 📱 Next Steps (Do on Your Phone)

### Step 1: Create GitHub Repository

1. **Open GitHub** on your phone (browser or app)
   - Go to: https://github.com
   - Sign in to your account

2. **Create New Repository**
   - Tap the "+" icon → "New repository"
   - Repository name: **`danish-exercises`**
   - Description: "Exercise data for Dansk til Luis Android app"
   - **Make it PUBLIC** (so app can read without authentication)
   - **Don't** initialize with README (we already have one)
   - Click "Create repository"

3. **Copy the repository URL**
   - Should be: `https://github.com/YOUR_USERNAME/danish-exercises.git`
   - Keep this handy!

---

### Step 2: Push from Computer (One Time)

Since the repo is on your computer now, you need to push it once:

**On this computer, run:**
```bash
cd "c:\Users\luisd\Documents\Luis\danish texts\androidApp"

# Add GitHub as remote (replace YOUR_USERNAME)
git remote add origin https://github.com/YOUR_USERNAME/danish-exercises.git

# Push everything
git branch -M main
git push -u origin main
```

**Enter your GitHub credentials when prompted**

---

### Step 3: Clone on Your Phone (Using Claude Code)

1. **Open Claude Code on your phone**
2. **Clone the repository:**
   - Open command palette
   - Search for "Git: Clone"
   - Enter: `https://github.com/YOUR_USERNAME/danish-exercises`
   - Choose folder location
   - Open the cloned repository

---

### Step 4: Test Workflow on Phone

**In Claude Code on your phone:**

1. **Create a new exercise file:**
   ```
   data/exercises/batch_002.json
   ```

2. **Add this content:**
   ```json
   {
     "batch_id": "batch_002",
     "created_at": "2025-12-18",
     "version": 1,
     "exercises": [
       {
         "id": "ex_test_001",
         "type": "fill_in_blank",
         "question": "Test exercise: What is 'water' in Danish?",
         "text_id": null,
         "difficulty": "beginner",
         "data": {
           "correctAnswer": "vand"
         }
       }
     ]
   }
   ```

3. **Commit and push:**
   - Open Source Control panel
   - Stage changes (+ icon)
   - Write commit message: "Test: Add batch 002"
   - Commit (✓ icon)
   - Push (sync icon)

4. **Verify on GitHub:**
   - Open https://github.com/YOUR_USERNAME/danish-exercises
   - Should see `data/exercises/batch_002.json`

---

## ✅ Success Checklist

- [ ] GitHub repository created (public)
- [ ] Initial push from computer successful
- [ ] Repository cloned on phone in Claude Code
- [ ] Test commit from phone works
- [ ] Can see changes on GitHub website

---

## 🚨 Common Issues

### Issue: "Authentication failed"
**Solution:** Use a Personal Access Token instead of password:
1. Go to: GitHub Settings → Developer Settings → Personal Access Tokens → Tokens (classic)
2. Generate new token with `repo` scope
3. Use token as password when git asks

### Issue: "Repository not found"
**Solution:** Check the URL is exactly:
```
https://github.com/YOUR_ACTUAL_USERNAME/danish-exercises.git
```

### Issue: Can't push from phone
**Solution:** Make sure you have write access (you're the owner)

---

## Next: Update the Android App

After GitHub is set up, you need to configure the app to sync from YOUR repository.

See: `GITHUB_SYNC_IMPLEMENTATION.md` (coming next!)

---

## Your Complete Workflow (Once Setup)

```
Phone (Claude Code) → Edit exercises → Commit → Push
                           ↓
                     GitHub stores them
                           ↓
Phone (Dansk til Luis app) → Tap refresh → Downloads → Practice!
```

**Everything from your phone! No computer needed!** 📱✨
