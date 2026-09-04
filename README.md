# LiveWebWallpaper v2.0

Live Wallpaper اندرویدی برای تبدیل صفحه وب، Matrix و تایپ کد به پس‌زمینه زنده.

## امکانات
- URL دلخواه و WebView
- بزرگ‌نمایی/کوچک‌نمایی
- کم‌رنگ کردن محتوا
- انتخاب رنگ پس‌زمینه با RGB
- سرعت و فونت Matrix
- سرعت و فونت تایپ کد
- حالت تمام‌سطح Wallpaper
- حذف اسکرول‌بار افقی و عمودی
- توقف فریم‌ها هنگام مخفی بودن Wallpaper برای مصرف کمتر
- آیکون مدرن و ساده
- بخش حمایت: CoffeeBede و Reymit

## رفع خطای Duplicate Kotlin Classes
در `app/build.gradle` ماژول‌های قدیمی `kotlin-stdlib-jdk7` و `kotlin-stdlib-jdk8` حذف و `kotlin-stdlib:1.8.22` به‌صورت force انتخاب شده است.

## GitHub Actions
Workflow از Gradle 8.11.1 و JDK 17 استفاده می‌کند و APK را در بخش Artifacts قرار می‌دهد.

> نکته: در Live Wallpaper، «تمام‌صفحه» به معنی استفاده از کل سطحی است که سیستم به Wallpaper اختصاص می‌دهد؛ نمایش یا مخفی‌کردن نوارهای خود لانچر/سیستم در اختیار Wallpaper نیست.
