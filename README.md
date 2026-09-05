# LiveWebWallpaper v5.0

رفع خطای `E cannot be converted to Context` و `self-reference in initializer`.
تمام دسترسی‌های Prefs داخل Wallpaper Engine از `context()` استفاده می‌کنند تا `this` مربوط به Engine اشتباهاً به عنوان Context ارسال نشود.


## Built-in HTML backgrounds
Matrix mode loads `app/src/main/assets/Matrix4.html` and Code mode loads `app/src/main/assets/Code9.html`. These are the supplied default pages and work offline.
