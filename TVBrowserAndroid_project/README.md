# TV Browser — Android TV / Google TV starter project

A project starter for a remote-friendly, colorful tile-based WebView browser.

## Features included
- Leanback launcher entry for Android TV / Google TV
- Colorful home tiles and bookmark tiles
- URL/search field and home navigation
- Basic domain-based ad request blocking (small built-in list)
- Popup/new-window blocking by default
- WebView HTML5 video fullscreen handling and best-effort fullscreen prompt
- Conservative WebView settings for a modest-memory device

## Build
1. Install Android Studio (current stable) and Android SDK Platform 35.
2. Open this folder as a project.
3. Let Gradle sync.
4. Connect a Google TV device or select an Android TV emulator.
5. Build > Build APK(s), then install the APK on your device.

## Important limitations
- This is starter source code, not a prebuilt or device-tested APK.
- The built-in blocklist is intentionally small and is not equivalent to a maintained filter list. Some ads will remain; some sites may break.
- WebView does not expose a universal API to cap video resolution. The website/player determines available resolution. The browser can offer fullscreen, but cannot force every site to provide 720p/1080p.
- The video fullscreen prompt is best-effort and depends on the site's DOM and video player.
- Bookmarks are held in memory in this starter and reset when the app process is killed. Persistent bookmark storage should be added before daily use.
- Some sites require cookies, DRM, Widevine support, or their own app and may not work as expected.
- Test with the actual 2 GB RAM TV. Avoid opening many tabs; this starter intentionally uses one WebView.
