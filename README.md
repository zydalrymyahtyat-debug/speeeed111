# Internet Speed Meter — Android

نسخة Flutter Android تحتوي على:
- واجهة عربية RTL.
- عرض نوع الشبكة.
- عداد Download / Upload والسرعة الإجمالية داخل التطبيق.
- Foreground Service أصلي على Android.
- إشعار دائم يعرض سرعة التحميل والرفع، مع عرض السرعة الإجمالية كعنوان للإشعار ليبقى ظاهراً في الأعلى.
- أزرار تشغيل وإيقاف الخدمة، وتعمل تلقائياً عند فتح التطبيق.
- تم تحديث بنية مشروع أندرويد ودعم أحدث إصدارات Gradle و Flutter v2 embedding.
- يتضمن إعدادات GitHub Actions Workflow لبناء ملف APK عند الرفع.

## التشغيل
```bash
flutter pub get
flutter run
```

## البناء
```bash
cd android
./gradlew assembleRelease
# الملف الناتج سيكون في android/app/build/outputs/apk/release/app-release.apk
```

ملاحظة: قياس سرعة الشبكة في الخلفية يعتمد على قراءة عدادات حركة البيانات من Android، لذلك يعرض معدل النقل الفعلي للجهاز.
