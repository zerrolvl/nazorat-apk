# Nazorat — o'zini cheklash ilovasi (Honor, Android 8–16 / MagicOS)

O'z telefoningizdagi ilovalar vaqtini kuzatadi, limitdan oshsa Telegramga
xabar beradi va ilovalarni **faqat Telegram orqali** qulflaydi/ochadi.
Telefonning o'zida ochish tugmasi yo'q — zaif daqiqada o'zingiz ocholmaysiz.

> Dvigatel **Accessibility (Maxsus imkoniyatlar)** xizmati sifatida ishlaydi.
> Shuning uchun **doimiy bildirishnoma yo'q** va foreground-service cheklovlari
> (Android 14+) ta'sir qilmaydi. Ilova faqat *Sozlamalar → Maxsus
> imkoniyatlar* ro'yxatida ko'rinadi.

---

## 1. Telegram bot yaratish

1. Telegramda **@BotFather** ni oching → `/newbot` → nom va username bering.
2. Beradigan **token**ni nusxalang (masalan `123456789:AAF...`).
3. Botni keyinroq (sozlashdan so'ng) ochib `/start` yuborasiz — birinchi yozgan
   akkaunt **admin** bo'lib qoladi, boshqa hech kim boshqara olmaydi.

---

## 2. APK yig'ish (Android Studio)

1. Kompyuterga **Android Studio** o'rnating (bepul, Windows/Mac/Linux).
2. `File → Open` → shu `SelfControl` papkasini tanlang.
3. Gradle "sync" tugashini kuting (internet kerak — kutubxonalarni yuklaydi).
4. `Build → Build Bundle(s) / APK(s) → Build APK(s)`.
5. Tayyor fayl: `app/build/outputs/apk/debug/app-debug.apk`
6. Shu `.apk` ni telefonga o'tkazing (Telegram/kabel/USB) va oching.

---

## 3. O'rnatish va ruxsatlar (qadamma-qadam — tartib muhim!)

1. APK ni o'rnating (telefon "Noma'lum manbalar"ga ruxsat so'rasa — bering).
2. **"Nazorat"** ilovasini oching, **bot tokenini** kiriting → **Saqlash**.
3. Ruxsatlarni tugmalar orqali bering:
   - **① Usage access** → ro'yxatdan "Nazorat"ni yoqing (vaqt hisobi uchun).
   - **② Accessibility (Maxsus imkoniyatlar)** — eng muhimi:
     - **Android 13+ / MagicOS:** avval ilova sahifasida (App info) o'ng
       yuqoridagi **⋮** → **"Cheklangan sozlamalarga ruxsat"** (Allow
       restricted settings) ni bosing. Aks holda tizim Accessibility'ni
       yoqishga ruxsat bermaydi.
     - So'ng: Maxsus imkoniyatlar → **Nazorat** → yoqing.
   - **③ Ustidan chizish (overlay)** → yoqing.
   - **④ Batareya cheklovini olib tashlash** → ruxsat bering.
4. **MagicOS/EMUI batareya** (bu qilmasangiz tizim xizmatni o'ldiradi):
   - Sozlamalar → **Batareya → Ilovalarni ishga tushirish** (App launch) →
     **Nazorat** → **Qo'lda boshqarish** → "Avtomatik ishga tushirish",
     "Bilvosita", "Fonda ishlash" — hammasini **yoqing**.
5. Botga **`/start`** yuboring → tugmali menyu chiqadi, siz admin bo'lasiz.
6. (Ixtiyoriy) **"Ikonkani yashirish"** tugmasini bosing yoki `/hide`.

> Diqqat: avval Accessibility'ni yoqib, keyin `/start` yuboring. `/start` dan
> keyin "qo'riqchi" yoqiladi va ilova sahifasini himoya qila boshlaydi.

---

## 4. Bot menyusi (tugmalar)

Botga `/start` → menyu:

- **📊 Bugun** — eng ko'p ishlatilgan ilovalar + qurilma jami vaqti
- **📅 7 kunlik** — har kun bo'yicha vaqt va eng ko'p ilova
- **📱 Ilovalar** → ilovani bosing → uning **bugun/kecha** vaqti + **🔒/🔓** tugma
- **🔒 Hammasini qulflash** / **🔓 Hammasini ochish**
- **🛡 Qo'riqchi ON/OFF** — o'chirishdan himoya
- **👻 Yashirish** — ikonkani yashirish

🟢 = ochiq, 🔒 = qulflangan. (Telegram botlarida tugma rangini o'zgartirib
bo'lmaydi — holat emoji bilan ko'rsatiladi.)

**Faqat siz boshqarasiz:** birinchi `/start` yuborgan akkaunt admin. Boshqa
hamma (bot username'ini topib olsa ham) e'tiborsiz qoldiriladi.

**Internet:** yoqsangiz avtomatik ulanadi, o'chirsangiz jim to'xtaydi
(xatosiz), qayta yoqsangiz o'zi davom etadi.

**Telefon yonganda:** "📱 Telefon yoqildi" xabari keladi.

### Matnli buyruqlar (menyuga qo'shimcha)
`/stats`, `/lock [ilova]`, `/unlock [ilova]`, `/limit instagram 60`,
`/add com.paket 30`, `/remove instagram`, `/reset`, `/hide`, `/show`,
`/guard on|off`. Nomlar: `instagram`, `roblox` yoki to'liq paket nomi.

---

## 5. O'chirishdan himoya va zaxira eshiklar

Qo'riqchi yoqilgan bo'lsa (standart), ilovaning **App info** sahifasini
(Force stop / O'chirish joyi) ochsangiz telefon Home'ga chiqib ketadi.

**O'chirmoqchi bo'lsangiz (qasddan qiyin, lekin mumkin):**
1. Noutbukdan Telegramga **`/guard off`** → himoya o'chadi → o'chirasiz. *(asosiy)*
2. Yoki Sozlamalar → **Maxsus imkoniyatlar** → "Nazorat"ni o'chiring (qo'riqchi
   bu sahifada ishlamaydi) → so'ng o'chiring.
3. Yoki **Xavfsiz rejim (Safe Mode)** da yoqing — barcha tashqi accessibility
   xizmatlari o'chadi — va o'chiring.

---

## 6. Halol ogohlantirishlar

- **Sozlamalar → Ilovalar / Maxsus imkoniyatlar** ro'yxatidan root'siz
  butunlay yashirib bo'lmaydi. Ikonka va "recent"dan yashiramiz; App info
  sahifasini "qo'riqchi" himoya qiladi.
- Eng yangi **MagicOS**da ikonkani yashirish har doim ham ishlamasligi mumkin
  (tizim cheklovi) — bu holda `/hide` ta'sir qilmaydi, qolgani ishlaydi.
- **Telegram buyruqlari** telefon chuqur uyquda (ekran o'chiq, ishlatilmayotgan)
  bo'lsa biroz kechikishi mumkin — telefon uyg'onganda yetib boradi. Qulflash
  esa ekran yoniq paytda (kerak bo'lganda) ishlaydi.
- **"Telefon o'chmoqda"** xabari ishonchli emas (o'chish juda tez). "Yoqildi"
  ishlaydi.
- **Haqiqiy "o'chmaydigan"** nazorat faqat **root** yoki kompyuterdan **ADB
  (device owner)** bilan bo'ladi — xohlasangiz alohida ko'rsataman.
- Kod **telefonda sinalmagan** — Android Studio'da yig'ganda xato chiqsa,
  matnini menga yuboring, tuzataman.

---

## Paket nomini topish
Play Store'da ilova sahifasi URL'idagi `id=...` qismi:
`.../details?id=com.instagram.android` → `com.instagram.android`.
