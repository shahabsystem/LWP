# LiveWebWallpaper v3.0

نسخه جدید Live Wallpaper با موتور HTML/CSS/JavaScript برای افکت‌های Matrix و تایپ کد. منطق Matrix و موتور تایپ کد از محتوای HTML آپلودشده در گفتگو استخراج و برای WebView اندروید بازطراحی و برای WebView اندروید سبک‌سازی شده است.

## حالت‌ها
- Web: باز کردن URL دلخواه به‌عنوان Live Wallpaper
- Matrix: بارش واقعی کاراکترها با Canvas و requestAnimationFrame
- Code: پنجره شبیه ادیتور، شماره خطوط، syntax highlighting و تایپ حلقه‌ای

## تنظیمات
- Zoom وب
- کم‌رنگی
- رنگ پس‌زمینه
- رنگ Matrix/Code
- سرعت Matrix
- فونت Matrix
- سرعت Code
- فونت Code
- مخفی کردن ScrollBar
- حالت تمام‌سطح Wallpaper

## مصرف باتری
وقتی Wallpaper از دید سیستم خارج شود، JavaScript با `setWallpaperActive(false)` متوقف می‌شود. در حالت Web نیز انیمیشن داخلی صفحه توسط Wallpaper کنترل نمی‌شود؛ صفحات وب خارجی ممکن است خودشان JavaScript فعال داشته باشند.

## Build
Workflow موجود در `.github/workflows/build.yml` با JDK 17 و Gradle 8.11.1 اجرا می‌شود و APK را در Artifacts منتشر می‌کند.

## رفع خطاهای قبلی
این نسخه هیچ dependency از Kotlin stdlib/jdk7/jdk8 ندارد؛ بنابراین زنجیره خطای Duplicate Kotlin Classes قبلی وارد پروژه نمی‌شود.
