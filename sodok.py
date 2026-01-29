import os

path = "workdir/smali/"

def ganti_kode(keyword, lama, baru):
    for root, dirs, files in os.walk(path):
        for file in files:
            if file.endswith(".smali"):
                f_path = os.path.join(root, file)
                with open(f_path, 'r') as f:
                    data = f.read()
                
                # Cek apakah keyword ada DAN lama ada di file yang sama
                if keyword in data and lama in data:
                    print(f"Sodok aman di: {file}")
                    data = data.replace(lama, baru)
                    with open(f_path, 'w') as f:
                        f.write(data)

# --- MODIFIKASI VERSI STABIL (ANTI-FC) ---

# Aktifin HDR (Biasanya paling aman)
ganti_kode("isHdrSupported", "const/4 v0, 0x0", "const/4 v0, 0x1")

# Aktifin Night Mode
ganti_kode("isNightModeSupported", "const/4 v0, 0x0", "const/4 v0, 0x1")

# Aktifin AUX (Pake keyword yang lebih spesifik buat GCam Go 3.8)
ganti_kode("isAuxiliaryCardsSupported", "const/4 v0, 0x0", "const/4 v0, 0x1")

print("Modifikasi Halus Selesai!")
