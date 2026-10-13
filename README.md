# Radha Movies

Created, Designed and Developed by Diwash Bhatta

Website: https://radhamovies.netlify.app/

## Downloads

[Download the latest preview release](https://github.com/DiwashBhatta17/radha-movies/releases/tag/v1.0.2-preview): Android APK and Windows x64 portable ZIP. These are preview builds; platform-specific verification limits are listed below.

## Android

Transfer `Radha-Movies-Android.apk` to an Android 8 or newer phone and open it to install. Android may ask you to allow installation from the file-opening app. This is a debug-signed preview APK, not a Play Store release.

The app loads the live website and rejects popup windows, external top-level navigation, and player downloads. It requires an internet connection.

In fullscreen, swipe near the left edge to adjust brightness or near the right edge to adjust media volume. Double-tap left/right to request a 10-second seek. Seeking uses the video already playing and preserves fullscreen. Android System WebView must support document-start scripts; keep it updated. Fullscreen automatically opens in landscape; press Back or the fullscreen control to exit. Gestures have compiled but have not been verified on a physical Android device.

## Windows

Extract `Radha-Movies-Windows.zip` completely, then run `Radha Movies.exe` inside the extracted folder. Keep all files together. This portable Windows x64 app is free to run and needs no paid installer. It is unsigned; Windows may show an unfamiliar-app notice. It blocks popup windows and external top-level navigation. It has been packaged and its window/navigation policies were tested, but real video playback in the Windows app has not been verified.

## Website features

- Typo correction for common titles and titles already loaded in your library, with an exact-search override. This is not a universal spelling dictionary.
- You're watching row with saved progress and season/episode values supplied by the player.
- Recommendations related to up to three recently watched titles, excluding titles in your history.
- Automatic next episode through the provider's documented option.
- Manual 30/60/90-second skip inside the apps without reloading; automatic intro boundaries are unavailable. In the website, use the player’s own seek controls.
- Audio and subtitle selections remain in the external player's menu. Dubbed tracks and subtitles depend on the stream.
- Small footer signature.

History and watchlists are local to each browser/app and do not sync between devices. Clearing app/browser storage removes them. Recommendation requests send recent title IDs to the site's backend, which obtains related-title information from TMDB.

## Known limits

Volume boost above the device maximum is NOT implemented: the embedded player does not expose its audio pipeline. The website itself cannot suppress third-party popup tabs; the native app wrappers deny new windows. In-player ads can remain, and providers may refuse playback when their popup attempts are blocked. We cannot guarantee every title, stream, language, or subtitle. The provider controls its own branding. No Netflix logo exists in Radha Movies' page source.

The catalogue depends on public TMDB page markup and can need maintenance if that markup changes. Video progress depends on the provider continuing to emit its documented events.

## Build from source

Website: `dist/` is the published directory; Netlify functions are in `netlify/functions/`. No third-party API secrets are bundled. Run `node tests.cjs` for search, event handling, and catalogue parser checks.

Android: install JDK 17 and Android SDK 35 with Build Tools 35.0.0. Create `android/local.properties` containing your own `sdk.dir=...` path, then run `gradlew.bat assembleDebug` from `android/`. The wrapper uses Gradle 8.11.1 and Android Gradle Plugin 8.9.1. The first build downloads dependencies. APK output is under `android/app/build/outputs/apk/debug/`.

Windows: from `windows/`, run `npm ci`, `node policy-test.cjs`, then `npm run package`. Node and dependency versions are recorded in package-lock.json. The existing ZIP is a portable x64 Windows build, not a signed installer.

Verification: Android compilation, APK signature/package metadata, search/progress/parser unit tests, Windows popup/navigation policy tests, live Netlify responses, live recommendation results, and browser playback progress were checked. Reopening a title was verified to pass the stored resume time to the provider. Provider-driven end-of-episode transitions, physical-phone gestures, and Windows video playback were not end-to-end verified.

## 1.0.1 fullscreen and seeking fixes

Windows now permits fullscreen for the trusted Radha Movies and Vidy origins, with F11 and Escape support. Android removes fullscreen padding, hides system bars, opens in landscape, and restores the prior orientation on exit. Native 10-second seek controls change the existing video position instead of rebuilding the iframe. Popup blocking remains enabled.

Tests: `node windows/prepare.cjs`, `node windows/policy-test.cjs`, `node native/player-controls.test.cjs`, and Android `gradlew.bat testDebugUnitTest`. Actual provider playback still needs verification on a physical phone and Windows desktop.

## 1.0.2 matching branding and cleaner fullscreen

Removed the Android Rotate/Exit overlay buttons and initial gesture banner. Back and the player controls still exit fullscreen. Website, Android, and Windows share a red/black theme and original Radha play monogram. Versioned assets refresh the styling without clearing watch history. Android gestures still show brief volume/brightness feedback.
