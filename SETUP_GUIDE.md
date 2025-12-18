# Setup Guide - Dansk til Luis

## Prerequisites

1. **Android Studio** (Arctic Fox or newer)
   - Download from: https://developer.android.com/studio

2. **Android SDK**
   - Minimum SDK: 24 (Android 7.0)
   - Target SDK: 34 (Android 14)

## Installation Steps

### 1. Open the Project

1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to `danish texts/androidApp`
4. Click "OK"

### 2. Sync Gradle

Android Studio will automatically prompt you to sync Gradle files. If not:
1. Click "File" > "Sync Project with Gradle Files"
2. Wait for the sync to complete

### 3. Create local.properties (if needed)

If you get an error about SDK location:
1. Create a file called `local.properties` in the `androidApp` folder
2. Add this line (adjust path for your system):
   ```
   sdk.dir=C\:\\Users\\YOUR_USERNAME\\AppData\\Local\\Android\\Sdk
   ```
   Or on Mac/Linux:
   ```
   sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
   ```

### 4. Run the App

#### On an Emulator:
1. Click "Tools" > "Device Manager"
2. Create a new virtual device (recommended: Pixel 5 with API 34)
3. Click the green "Run" button
4. Select your emulator

#### On a Physical Device:
1. Enable Developer Options on your Android phone:
   - Go to Settings > About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings > Developer Options
   - Enable "USB Debugging"
2. Connect your phone via USB
3. Click the green "Run" button
4. Select your device

## Project Structure

```
androidApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/luisdecunto/dansktilluis/  # Kotlin source code
│   │   ├── res/                                  # Resources (layouts, strings)
│   │   └── AndroidManifest.xml                   # App configuration
│   └── build.gradle                              # App-level dependencies
├── build.gradle                                  # Project-level build config
├── settings.gradle                               # Gradle settings
└── README.md                                     # Main documentation
```

## Troubleshooting

### "SDK location not found"
- Create `local.properties` with your SDK path (see step 3 above)

### "Failed to resolve dependencies"
- Ensure you have an internet connection
- Try "File" > "Invalidate Caches / Restart"

### "Kotlin not configured"
- The project uses Kotlin - it should be configured automatically
- If prompted, click "Configure" to set up Kotlin

### Build errors
- Try "Build" > "Clean Project"
- Then "Build" > "Rebuild Project"

## What's Included

The skeleton app includes:

1. **Three Exercise Types**:
   - Multiple Choice
   - Fill in the Blank
   - Match Pairs

2. **Sample Exercises**:
   - Basic Greetings
   - Common Phrases
   - Numbers 1-10

3. **Progress Tracking**:
   - Automatically saves your progress
   - Shows completion percentage
   - Persists across app restarts

## Next Steps

1. Run the app and test the sample exercises
2. Read [ADDING_EXERCISES.md](ADDING_EXERCISES.md) to learn how to add your own
3. Customize the exercises based on your Danish learning materials
4. Enjoy learning Danish!

## Need Help?

- Check the [README.md](README.md) for project overview
- See [ADDING_EXERCISES.md](ADDING_EXERCISES.md) for adding content
- Review the code comments in Kotlin files
