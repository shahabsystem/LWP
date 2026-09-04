# LiveWebWallpaper v5.0

رفع خطای `E cannot be converted to Context` و `self-reference in initializer`.
تمام دسترسی‌های Prefs داخل Wallpaper Engine از `context()` استفاده می‌کنند تا `this` مربوط به Engine اشتباهاً به عنوان Context ارسال نشود.
