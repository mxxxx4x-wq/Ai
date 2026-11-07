# StreamVault XT

Original Android IPTV starter app for authorized Xtream Codes-compatible accounts. It is not affiliated with IPTV Smarters, Xtream Codes, or IMDb.

## Included
- Xtream login via `player_api.php`
- Live TV, movie, and series catalog retrieval
- Search and poster grid
- Media3/ExoPlayer playback for live and movie streams
- Android phone/tablet/TV launcher support
- IMDb metadata proxy skeleton using the licensed IMDb GraphQL API on AWS Data Exchange

## Important limitations of this MVP
- Series browsing is loaded, but episode selection needs `action=get_series_info&series_id=...` before playback.
- EPG, favorites, catch-up, parental PIN, multi-profile, downloads, subtitles/audio track UI, and Picture-in-Picture are not yet implemented.
- Xtream APIs vary by provider. Test against your authorized service.
- Cleartext HTTP is enabled for compatibility. For production, require HTTPS and narrow the network security policy.

## Build
1. Open this folder in Android Studio.
2. Let Gradle sync.
3. Run on an emulator/device using Android 7.0+ (API 24+).
4. For a physical device, change `METADATA_BASE_URL` in `app/build.gradle.kts` from `10.0.2.2` to your HTTPS backend URL.

## IMDb setup
Do not embed AWS or IMDb secrets in the APK.
1. Obtain licensed IMDb API access through AWS Data Exchange.
2. In `backend/`, run `npm install`.
3. Set `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`, `IMDB_DATASET_ID`, `IMDB_REVISION_ID`, `IMDB_ASSET_ID`, `IMDB_API_KEY`, and `IMDB_SEARCH_QUERY`.
4. Run `npm start`.
5. Adapt the response mapping in `server.mjs` to the schema included with your IMDb subscription.

For a consumer release, use a server-side cache keyed by normalized title, year, and type, and return only the fields used by the app.

## Build the APK from an Android tablet using GitHub Actions
1. Create a GitHub repository and upload all project files, including the hidden `.github` folder.
2. Open the repository's Actions tab and select **Build Android APK**.
3. Tap **Run workflow**.
4. When the build finishes, open the completed run and download **StreamVaultXT-debug-apk** from Artifacts.
5. Extract the downloaded ZIP on the tablet, then install `app-debug.apk`. Enable installation from the browser or file manager only when Android prompts you.
