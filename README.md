# nfc

The nwHacks Android NFC app, used to manage hackathon participants via NFC-enabled nametags.

## Getting Started
For a technical overview (nwHacks 2018), see [this](https://medium.com/nwplusubc/nfc-nametags-7960c45aa7fd) blog post.

### Development
To setup this project for local development, follow these steps (assuming that you're using [Android Studio](https://developer.android.com/studio/)):
1. Clone this repo.
2. Download the configuration file (**google-services.json**) by following [these](https://support.google.com/firebase/answer/7015592?hl=en) instructions (for the **developer** Google Cloud project). Place this configuration file in the [/app](/app) folder.
3. Open this project in Android Studio, and follow [this](https://medium.com/pen-bold-kiln-press/sha-1-android-studio-ec02fb893e72) tutorial to get a SHA1 certificate fingerprint. Copy this SHA1 fingerprint and add it to the Firebase through the project settings page (the same page from where you downloaded the configuration file in step 2).

### Generating a new release
1. Switch your google-services.json file to the one downloaded from the prod database.
2. Build your APK using these instructions: https://developer.android.com/studio/publish/app-signing#sign_release
3. Get your **release certificate fingerprint** using these instructions: https://developers.google.com/android/guides/client-auth
4. Paste the SHA-1 from step 4 into the SHA certificate fingerprints for your NFC app on the prod Firebase console.
5. Upload your release to https://github.com/nwplus/nfc/releases

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

