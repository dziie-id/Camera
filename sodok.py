import os
import re

# Lokasi folder smali hasil decompile
smali_dir = "workdir/smali/"

def sodok_fitur():
    print("--- Memulai Proses Penodokan Smali ---")
    
    for root, dirs, files in os.walk(smali_dir):
        for file in files:
            if file.endswith(".smali"):
                file_path = os.path.join(root, file)
                
                with open(file_path, "r") as f:
                    content = f.read()

                # CONTOH MODIFIKASI: 
                # 1. Aktifin HDR (Mencari method isHdrSupported dan maksa return true)
                if "isHdrSupported" in content:
                    print(f"[+] Menodok HDR di: {file}")
                    content = re.sub(
                        r'const/4 v0, 0x0', 
                        r'const/4 v0, 0x1', 
                        content
                    )

                # 2. Aktifin Night Mode (Mencari method isNightModeSupported)
                if "isNightModeSupported" in content:
                    print(f"[+] Menodok Night Mode di: {file}")
                    content = re.sub(
                        r'const/4 v0, 0x0', 
                        r'const/4 v0, 0x1', 
                        content
                    )

                # Simpan perubahan
                with open(file_path, "w") as f:
                    f.write(content)

    print("--- Proses Penodokan Selesai! ---")

if __name__ == "__main__":
    sodok_fitur()
