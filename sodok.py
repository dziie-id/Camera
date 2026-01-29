import os

# Folder tempat smali
path = "workdir/smali/"

def ganti_kode(keyword, lama, baru):
    for root, dirs, files in os.walk(path):
        for file in files:
            if file.endswith(".smali"):
                f_path = os.path.join(root, file)
                with open(f_path, 'r') as f:
                    data = f.read()
                
                if keyword in data:
                    print(f"Sodok file: {file}")
                    # Pake replace biar lebih agresif
                    data = data.replace(lama, baru)
                    with open(f_path, 'w') as f:
                        f.write(data)

# --- MULAI MODIFIKASI SULTAN ---

# 1. Aktifin Fitur Dasar
ganti_kode("isHdrSupported", "const/4 v0, 0x0", "const/4 v0, 0x1")
ganti_kode("isNightModeSupported", "const/4 v0, 0x0", "const/4 v0, 0x1")
ganti_kode("isPortraitSupported", "const/4 v0, 0x0", "const/4 v0, 0x1")

# 2. AKTIFIN AUX (Lensa Wide/Macro)
# Kita cari method yang ngecek jumlah kamera dan kita paksa jadi 'true'
ganti_kode("isAuxiliaryCardsSupported", "const/4 v0, 0x0", "const/4 v0, 0x1")
ganti_kode("isMultipleCamerasSupported", "const/4 v0, 0x0", "const/4 v0, 0x1")

# 3. Bypass Camera ID (Biar dia gak cuma baca kamera utama)
ganti_kode("isWideAngleSupported", "const/4 v0, 0x0", "const/4 v0, 0x1")

print("Modifikasi AUX & Fitur Selesai bray!")
