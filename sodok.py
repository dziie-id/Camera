import os

# Lokasi folder smali
path = "workdir/smali/"

def ganti_kode(label, keyword, lama, baru):
    count = 0
    for root, dirs, files in os.walk(path):
        for file in files:
            if file.endswith(".smali"):
                f_path = os.path.join(root, file)
                try:
                    with open(f_path, 'r') as f:
                        data = f.read()
                    
                    if keyword in data and lama in data:
                        data = data.replace(lama, baru)
                        with open(f_path, 'w') as f:
                            f.write(data)
                        count += 1
                except Exception:
                    continue
    if count > 0:
        print(f"[+] {label}: Berhasil sodok {count} titik.")

# --- EKSEKUSI MODIFIKASI SULTAN ---

print("--- Memulai Ritual Penodokan Note 13 ---")

# 1. Aktifin Fitur Inti (HDR, Night Mode, Portrait)
ganti_kode("HDR+", "isHdrSupported", "const/4 v0, 0x0", "const/4 v0, 0x1")
ganti_kode("NIGHT MODE", "isNightModeSupported", "const/4 v0, 0x0", "const/4 v0, 0x1")
ganti_kode("PORTRAIT", "isPortraitSupported", "const/4 v0, 0x0", "const/4 v0, 0x1")

# 2. Aktifin Lensa Tambahan (AUX/Wide/Macro)
ganti_kode("AUX CARDS", "isAuxiliaryCardsSupported", "const/4 v0, 0x0", "const/4 v0, 0x1")
ganti_kode("MULTI CAM", "isMultipleCamerasSupported", "const/4 v0, 0x0", "const/4 v0, 0x1")
ganti_kode("WIDE ANGLE", "isWideAngleSupported", "const/4 v0, 0x0", "const/4 v0, 0x1")

# 3. Manipulasi Device Profile (Bypass Biar Gak FC di Xiaomi)
# Kita paksa sistem ngerasa ini Pixel 4a biar library HDR+ nya mau jalan
ganti_kode("DEVICE SPOOF", "getDeviceModel", "const-string v0, ", "const-string v0, \"Pixel 4a\" #")

# 4. Aktifin Google Lens & Storage Saver
ganti_kode("G-LENS", "isGoogleLensSupported", "const/4 v0, 0x0", "const/4 v0, 0x1")
ganti_kode("STORAGE SAVER", "isStorageSaverSupported", "const/4 v0, 0x0", "const/4 v0, 0x1")

print("--- Ritual Selesai! Gas Rakit Bray! ---")
