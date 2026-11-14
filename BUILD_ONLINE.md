# APK ni bepul ONLINE yig'ish (kompyutersiz) — GitHub Actions

Kompyuter shart emas. GitHub bulutda APK'ni o'zi yasab beradi, bepul.
Loyihada allaqachon kerakli fayllar bor: `gradlew` (wrapper) va
`.github/workflows/build.yml`.

## 1. GitHub akkaunt
`github.com` da bepul ro'yxatdan o'ting (email + parol).

## 2. Repozitoriy (repo) yarating
- O'ng yuqori **+ → New repository**
- Nom: masalan `nazorat`
- **Private** (shaxsiy) qilib qo'ying
- **Create repository** bosing

## 3. Fayllarni yuklang
Eng oson — kompyuterda (bir martagina, bir necha daqiqa):
- `SelfControl.zip` ni papkaga chiqaring (extract)
- Repo sahifasida **Add file → Upload files**
- `SelfControl` papkasi **ichidagi** hamma narsani (jumladan `app`,
  `.github`, `gradle`, `gradlew`, `settings.gradle.kts` ...) **sudrab**
  (drag-and-drop) tashlang — papka tuzilishi saqlanadi
- Pastda **Commit changes** bosing

> ⚠️ `.github` papkasi ham albatta yuklansin — build shunga bog'liq.
> (Yashirin papka, lekin yuklash kerak.)

## 4. Build avtomatik boshlanadi
- Yuqoridagi **Actions** bo'limiga o'ting
- "Build APK" ishga tushganini ko'rasiz (sariq = ketyapti, yashil = tayyor)
- Birinchi marta ~3–6 daqiqa

## 5. APK ni yuklab oling
- Yashil ✓ bo'lgan build'ni oching
- Pastda **Artifacts → app-debug-apk** ni bosing → `.zip` yuklanadi
- Ichida `app-debug.apk` bor — telefonga o'rnating

## Keyingi safar o'zgartirsangiz
Fayl o'zgarsa yoki **Actions → Build APK → Run workflow** bossangiz —
yangi APK yasaladi. Buni **telefon brauzeridan** ham qila olasiz, va
artifact'ni telefonga yuklab olasiz. Ya'ni bir marta repo tayyorlasangiz,
keyin kompyuter kerak emas.

## Agar build qizil (xato) bo'lsa
Build'ni ochib, qizil qadamni bosing, xato matnini nusxalab menga yuboring —
tuzatib beraman.

---

### Muqobil: Codemagic
`codemagic.io` ham bepul (oyiga cheklangan daqiqa) — GitHub repongizni
ulab, APK yasatasiz va hatto emailga yuborishi mumkin. Lekin GitHub Actions
yetarli.
