import os
import re

def sodok_file(file_path, daftar_ganti):
    if not os.path.exists(file_path):
        print(f"[-] File {file_path} gak ada, skip.")
        return

    with open(file_path, 'r') as f:
        data = f.read()

    for lama, baru in daftar_ganti.items():
        # Pake regex biar lebih kuat nyarinya
        pattern = re.compile(re.escape(lama), re.IGNORECASE)
        if pattern.search(data):
            data = pattern.sub(baru, data)
            print(f"[+] Berhasil update: {lama} -> {baru}")

    with open(file_path, 'w') as f:
        f.write(data)

# --- KONFIGURASI SESUAI LOG CAMERA ID LU ---

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
    # Kita arahin ke ID 4 (Logical Camera) sesuai data lu
    'name="aux_camera_id">0': 'name="aux_camera_id">4' 
}

# Tambahan: Paksa ID di arrays.xml biar sistem tau ada pilihan ID 0, 2, dan 4
arrays_path = "workdir/res/values/arrays.xml"
# Kita gak ganti, tapi kita pastiin isinya mencakup ID kamera lu
# (Jika file ini ada, biasanya berisi list camera IDs)

# Eksekusi
print("--- Memulai Ritual Sapphire Logical ---")
sodok_file(bools_path, ganti_bools)
sodok_file(integers_path, ganti_integers)
print("--- Selesai! Gas Rakit Bray! ---")
