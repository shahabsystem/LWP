# LiveWebWallpaper v4.0

این نسخه Matrix و Code را مستقیماً با Canvas اندروید اجرا می‌کند؛ بنابراین به WebView/JavaScript برای انیمیشن‌های داخلی وابسته نیست و مشکل «ثابت بودن» نسخه‌های قبلی حذف شده است.

منطق Matrix و Code بر اساس کدهای HTML آپلودشده در گفتگو (ساختار `matrixFrame` و `typeCode`) بازطراحی شده است.

## بهبود Web
- WebView با Hardware Layer
- `setOffscreenPreRaster(true)` برای رندر روان‌تر
- invalidate و redraw پیوسته تا صفحات دارای انیمیشن CSS/JS بهتر به‌روز شوند
- viewport و اندازه WebView دقیقاً با Surface هماهنگ می‌شود
- ScrollBar قابل خاموش کردن است
- Zoom قابل تنظیم است
- WebView فقط در حالت Web ساخته می‌شود تا Matrix/Code سبک بمانند

## Build
`.github/workflows/build.yml` با JDK 17 و Gradle 8.11.1 روی GitHub APK تولید می‌کند.
