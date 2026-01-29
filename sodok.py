import os
import re

def sodok_file(file_path, daftar_ganti):
    if not os.path.exists(file_path):
        print(f"[-] File {file_path} gak ada, skip.")
        return

    with open(file_path, 'r') as f:
        data = f.read()

    for lama, baru in daftar_ganti.items():
        if lama in data:
            data = data.replace(lama, baru)
            print(f"[+] Berhasil ganti di {os.path.basename(file_path)}")

    with open(file_path, 'w') as f:
        f.write(data)

# --- KONFIGURASI ---

# 1. Bools & Integers (Tetap perlu)
bools_path = "workdir/res/values/bools.xml"
ganti_bools = {
    'name="is_hdr_supported">false': 'name="is_hdr_supported">true',
    'name="is_night_mode_supported">false': 'name="is_night_mode_supported">true',
    'name="is_aux_supported">false': 'name="is_aux_supported">true',
    'name="is_multiple_cameras_supported">false': 'name="is_multiple_cameras_supported">true',
    'name="is_wide_angle_supported">false': 'name="is_wide_angle_supported">true'
}

integers_path = "workdir/res/values/integers.xml"
ganti_integers = {
    'name="back_camera_number">1': 'name="back_camera_number">3',
    'name="aux_camera_id">0': 'name="aux_camera_id">4' # Pakai Logical ID 4 dulu
}

# 2. Sodok Arrays (Daftar ID Kamera)
arrays_path = "workdir/res/values/arrays.xml"

def sodok_arrays():
    if not os.path.exists(arrays_path): return
    with open(arrays_path, 'r') as f:
        data = f.read()
    
    # Tambah ID 2 (Wide) dan 4 (Logical) ke dalam daftar kamera yang dikenali
    if '<array name="camera_ids">' in data:
        # Kita replace isi array-nya biar ada ID 0, 2, 4
        pola = r'<array name="camera_ids">.*?</array>'
        baru = '<array name="camera_ids">\n        <item>0</item>\n        <item>2</item>\n        <item>4</item>\n    </array>'
        data = re.sub(pola, baru, data, flags=re.DOTALL)
        print("[+] Berhasil suntik ID Kamera ke Arrays!")
        
    with open(arrays_path, 'w') as f:
        f.write(data)

# Eksekusi
print("--- Ritual Penetrasi Sapphire ---")
sodok_file(bools_path, ganti_bools)
sodok_file(integers_path, ganti_integers)
sodok_arrays()
print("--- Selesai bray! ---")
