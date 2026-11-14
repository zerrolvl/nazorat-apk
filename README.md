# Nazorat — shaxsiy o'zini cheklash ilovasi

**Honor · Android 16 / MagicOS uchun**

O'z telefoningizdagi ilovalar (Instagram, Roblox va h.k.) vaqtini kuzatadi,
limitdan oshsa Telegramga xabar beradi va ilovalarni **faqat Telegram bot
orqali** qulflaydi/ochadi. Telefonda ochish tugmasi yo'q — zaif daqiqada
o'zingiz ocholmaysiz, faqat botdan ochasiz.

- Bot token va Telegram ID **kodga joylangan** — ilovani ochib sozlash shart emas.
- **Bosh ekran/menyuda ikonka YO'Q** (launcher kategoriyasi olib tashlangan).
- Dvigatel **Accessibility (Maxsus imkoniyatlar)** xizmati — doimiy
  bildirishnoma yo'q, Android 14+ foreground-service cheklovlari ta'sir qilmaydi.
- Faqat **sizning Telegram ID'ingiz** (8581234999) botni boshqaradi.

---

## 1. Ilova telefonda qanday ko'rinadi?

- **Bosh ekranda / ilovalar menyusida** — umuman yo'q (ikonka yo'q).
- Faqat **Sozlamalar → Ilovalar** va **Sozlamalar → Maxsus imkoniyatlar**
  ro'yxatlarida **"Tizim xizmati"** nomi bilan turadi (oddiy tishli ikon).
- Doimiy bildirishnoma yo'q.

> Shuning uchun quyidagi sozlashlar ilovani ochmasdan, to'g'ridan-to'g'ri
> **Sozlamalar** orqali qilinadi. Ilovani "Tizim xizmati" nomi bilan qidiring.

---

## 2. O'rnatish

1. `Nazorat.apk` (yoki `app-debug.apk`) ni telefonga o'tkazing.
2. O'rnatishda "Noma'lum manbalardan o'rnatish"ga ruxsat bering.
3. O'rnatgach, ilovada **"Open/Ochish" tugmasi bo'lmaydi** — bu normal
   (ikonka yo'q). To'g'ridan Sozlamalarga o'tasiz (3-bo'lim).

> Eski versiya ustiga o'rnatsangiz — yangilanadi, sozlamalar saqlanadi.

---

## 3. Ruxsatlarni berish (eng muhim qism — tartib bilan)

Hammasini **Sozlamalar**dan qilasiz. Ilovani "Tizim xizmati" deb qidiring.

### 3.1. Cheklangan sozlamalarga ruxsat (Android 13+ / MagicOS)
Sideload qilingan ilovaga Accessibility berishdan oldin shart:
- **Sozlamalar → Ilovalar → Tizim xizmati**
- O'ng yuqori **⋮ (uch nuqta) → "Cheklangan sozlamalarga ruxsat berish"**
  (Allow restricted settings)

### 3.2. Accessibility — Maxsus imkoniyatlar (DVIGATEL)
- **Sozlamalar → Maxsus imkoniyatlar (Accessibility)**
- (MagicOS'da ba'zan: **Sozlamalar → Qulayliklar → Maxsus imkoniyatlar**)
- **"Tizim xizmati"** ni toping → **yoqing** → chiqqan oynada "Ruxsat berish".

Bu yoqilishi bilan dvigatel ishga tushadi va bot ulanadi.

### 3.3. Usage access (foydalanish statistikasi)
- **Sozlamalar → Ilovalar → Maxsus ruxsatlar → Foydalanishga kirish
  (Usage access)**
- **"Tizim xizmati"** → yoqing.

### 3.4. Ustidan chizish (overlay)
- **Sozlamalar → Ilovalar → Maxsus ruxsatlar → Boshqa ilovalar ustidan
  chizish** → **"Tizim xizmati"** → yoqing.

### 3.5. Batareyada o'lib qolmasligi uchun (MagicOS — SHART)
- **Sozlamalar → Batareya → Ilovalarni ishga tushirish** (App launch)
- **"Tizim xizmati"** → **Qo'lda boshqarish** → "Avtomatik ishga tushirish",
  "Bilvosita ishga tushirish", "Fonda ishlash" — **hammasini yoqing**.

---

## 4. Ishga tushirish

Ruxsatlar berilgach, Telegram botingizni oching va **`/start`** yuboring.
Token kodda bo'lgani uchun bot darrov javob beradi va tugmali menyu chiqadi.
Faqat sizning ID'ingiz (8581234999) boshqaradi — boshqa hech kim emas.

---

## 5. Telegram menyu (tugmalar)

`/start` → menyu:

| Tugma | Vazifasi |
|---|---|
| 📊 **Bugun** | Bugungi eng ko'p ishlatilgan ilovalar + qurilma jami vaqti |
| 📅 **7 kunlik** | Oxirgi 7 kun bo'yicha kunlik vaqt va eng ko'p ilova |
| 📱 **Ilovalar** | Ilovani bosing → uning **bugun/kecha** vaqti + **🔒/🔓** tugma |
| 🔒 **Hammasini qulflash** | Barcha kuzatilgan ilovani qulflash |
| 🔓 **Hammasini ochish** | Hammasini ochish |
| 🛡 **Qo'riqchi ON/OFF** | O'chirishdan himoyani yoqish/o'chirish |

🟢 = ochiq, 🔒 = qulflangan.

### Avtomatik
- Biror ilova **kunlik limitidan oshsa** — avtomatik qulflanadi va Telegramga
  ⛔ xabar keladi.
- Har kuni **23:59** da kunlik hisobot keladi, hisob nollanadi.
- **Telefon yonganda** "📱 Telefon yoqildi" xabari keladi.
- Qulflangan ilova ochilsa — **~1 soniyada** bosh ekranga chiqarib yuboradi.

### Matnli buyruqlar (menyuga qo'shimcha)
| Buyruq | Vazifasi |
|---|---|
| `/stats` | Bugungi statistika |
| `/lock [ilova]` | Qulflash (bo'sh = hammasi) |
| `/unlock [ilova]` | Ochish (bo'sh = hammasi) |
| `/limit instagram 60` | Kunlik limit (daqiqa) |
| `/add com.paket.nomi 30` | Yangi ilova qo'shish |
| `/remove instagram` | Kuzatuvdan olib tashlash |
| `/reset` | Bugungi hisobni nollash |
| `/guard on` yoki `/guard off` | O'chirishdan himoya |

Qisqa nomlar: `instagram`, `roblox`. Boshqasi uchun to'liq paket nomi
(masalan `com.whatsapp`). Paket nomini Play Store havolasidagi `id=...` dan
olasiz.

---

## 6. O'chirishdan himoya (Qo'riqchi) va zaxira eshiklar

**Qo'riqchi** (standart yoqilgan): "Tizim xizmati"ning **App info** sahifasini
(Force stop / O'chirish joyi) ochsangiz — telefon avtomatik bosh ekranga
chiqib ketadi. Shu sabab zaif daqiqada o'chira olmaysiz.

**Chindan o'chirmoqchi bo'lsangiz (qasddan qiyin, lekin mumkin):**
1. Botga **`/guard off`** → himoya o'chadi → Sozlamalardan o'chirasiz. *(asosiy)*
2. Yoki **Maxsus imkoniyatlar** → "Tizim xizmati"ni o'chiring (qo'riqchi bu
   sahifada ishlamaydi) → so'ng o'chirasiz.
3. Yoki telefonni **Xavfsiz rejim (Safe Mode)** da yoqib o'chiring.

---

## 7. Halol chegaralar (root'siz Android 16)

- ✅ **Bosh ekran/menyudan ikonka butunlay yo'q.**
- ⚠️ Ilova baribir **Sozlamalar → Ilovalar / Maxsus imkoniyatlar** ro'yxatida
  "Tizim xizmati" nomi bilan **ko'rinadi** — buni faqat **root** yashiradi.
  Qo'riqchi App info sahifasini himoya qiladi.
- ⚠️ **To'liq o'chmaydigan qilish** faqat root yoki kompyuterdan ADB
  (device-owner) bilan bo'ladi. Honor bootloader'i qulflangani uchun root
  odatda imkonsiz.
- ⚠️ Telefon **chuqur uyquda** bo'lsa Telegram buyruqlari biroz kechikishi
  mumkin (uyg'onganda yetadi). Qulflash ekran yoniq paytda ishlaydi.
- ⚠️ "Telefon o'chmoqda" xabari ishonchsiz (o'chish juda tez). "Yoqildi" ishlaydi.

---

## 8. Kodni o'zgartirish / qayta yig'ish

Loyiha GitHub'da: `github.com/zerrolvl/nazorat-apk`. APK GitHub Actions'da
avtomatik yig'iladi.

- Yangi APK: **Actions → Build APK → Run workflow** (telefon brauzeridan ham).
- Tayyor APK: **Releases → latest → `app-debug.apk`**, yoki `apk-dist`
  branchidagi `dist/app-debug.apk`.
- Tokenni o'zgartirmoqchi bo'lsangiz: `app/.../Core.kt` dagi `DEFAULT_TOKEN`
  va `DEFAULT_ADMIN` ni o'zgartiring (yoki menga ayting).

Kompyuterda yig'ish: Android Studio'da papkani oching → Build → Build APK.
(Batafsil: `BUILD_ONLINE.md`.)

---

## 9. Texnik tuzilish (qisqacha)

- `AppBlockerService` — Accessibility: vaqt hisobi, qulflash, Telegram poll,
  qo'riqchi, kunlik alarm, "yoqildi" xabari (bosh dvigatel).
- `Bot` — Telegram menyu/buyruqlar.
- `Stats` — statistika (UsageStatsManager: bugun/kecha/7 kun).
- `Menu` — inline tugmalar.
- `Core` — holat, saqlash, Telegram API, kodga joylangan token/ID.
- `Receivers` — yoqilish/o'chish/kunlik hodisalar.
- Minimal kutubxona, qo'shimcha dependency'siz (faqat AndroidX core/appcompat).
