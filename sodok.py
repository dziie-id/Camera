import os
import re

def sodok_file(file_path, daftar_ganti):
    if not os.path.exists(file_path): return
    with open(file_path, 'r') as f:
        data = f.read()
    for lama, baru in daftar_ganti.items():
        data = re.sub(lama, baru, data)
    with open(file_path, 'w') as f:
        f.write(data)

# --- CONFIG KHUSUS SAPPHIRE ---
# Kita coba cara paling halus tapi mematikan

bools_path = "workdir/res/values/bools.xml"
ganti_bools = {
    r'name="is_hdr_supported">.*?<': 'name="is_hdr_supported">true<',
    r'name="is_night_mode_supported">.*?<': 'name="is_night_mode_supported">true<',
    r'name="is_aux_supported">.*?<': 'name="is_aux_supported">true<',
    r'name="is_wide_angle_supported">.*?<': 'name="is_wide_angle_supported">true<'
}

integers_path = "workdir/res/values/integers.xml"
ganti_integers = {
    # Paksa sistem anggap ada 2 kamera belakang yang setara
    r'name="back_camera_number">.*?<': 'name="back_camera_number">2<',
    # Coba pake ID 2 (Wide lu) sebagai AUX utama
    r'name="aux_camera_id">.*?<': 'name="aux_camera_id">2<'
}

# Kadang di Android 14+ perlu pancingan di strings
strings_path = "workdir/res/values/strings.xml"
ganti_strings = {
    # Paksa string key buat aux jadi 'true' atau berisi ID
    r'name="pref_camera_aux_key">.*?<': 'name="pref_camera_aux_key">true<'
}

print("--- Menjalankan Operasi Penyamaran ---")
sodok_file(bools_path, ganti_bools)
sodok_file(integers_path, ganti_integers)
sodok_file(strings_path, ganti_strings)
print("--- Selesai! Cobain bray! ---")
