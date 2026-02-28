# FloatyMo

Aplikasi Android yang menampilkan animasi GIF sebagai overlay mengambang di atas layar. GIF dapat dicari langsung dari Giphy atau dipilih dari galeri perangkat, lalu ditampilkan sebagai floating overlay yang bisa diatur posisi, ukuran, dan transparansinya.

## Fitur

- Menampilkan satu atau beberapa GIF sebagai overlay di atas aplikasi lain (maksimal 5 overlay aktif secara bersamaan)
- Pencarian GIF melalui Giphy API dengan debounce dan pagination
- Import GIF dari galeri perangkat
- Pengaturan ukuran overlay (Small, Medium, Large, Extra Large) dan opacity per-overlay
- Drag overlay untuk mengubah posisi secara langsung
- Halaman Position Editor untuk mengatur posisi overlay saat service sedang berjalan
- Foreground service agar overlay tetap aktif di background
- Data disimpan secara lokal menggunakan SharedPreferences

## Setup Development

### Prasyarat

- Android Studio terbaru (disarankan Ladybug atau lebih baru)
- JDK 11
- Android SDK dengan API level 36

### Langkah-langkah

1. Clone repository:

```bash
git clone https://github.com/bagusindrayana/floatymo.git
cd floatymo
```

2. Buat akun developer di [Giphy Developers](https://developers.giphy.com/) dan buat API key.

3. Buka file `local.properties` di root project, lalu tambahkan baris berikut:

```properties
GIPHY_API_KEY=api_key_kamu
```

4. Buka project di Android Studio dan tunggu Gradle sync selesai.

5. Jalankan aplikasi di emulator atau perangkat fisik dengan minimal Android 8.0 (API 26).

### Permission yang Dibutuhkan

Aplikasi memerlukan beberapa permission yang akan diminta saat runtime:

| Permission | Kegunaan |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Menampilkan overlay di atas aplikasi lain |
| `INTERNET` | Mengakses Giphy API dan mengunduh GIF |
| `FOREGROUND_SERVICE` | Menjalankan service overlay di background |
| `POST_NOTIFICATIONS` | Menampilkan notifikasi foreground service (Android 13+) |
| `READ_MEDIA_IMAGES` | Mengakses galeri untuk import GIF |

## Build

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## Lisensi

Konten GIF yang diakses melalui aplikasi ini disediakan oleh [Giphy](https://giphy.com/) dan tunduk pada [Giphy Terms of Service](https://support.giphy.com/hc/en-us/articles/360020027752).
