import os

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

# --- KONFIGURASI SODOK ---

# 1. Sodok Bools (Fitur ON/OFF)
bools_path = "workdir/res/values/bools.xml"
ganti_bools = {
    '<bool name="is_hdr_supported">false</bool>': '<bool name="is_hdr_supported">true</bool>',
    '<bool name="is_night_mode_supported">false</bool>': '<bool name="is_night_mode_supported">true</bool>',
    '<bool name="is_aux_supported">false</bool>': '<bool name="is_aux_supported">true</bool>',
    '<bool name="is_multiple_cameras_supported">false</bool>': '<bool name="is_multiple_cameras_supported">true</bool>',
    '<bool name="is_wide_angle_supported">false</bool>': '<bool name="is_wide_angle_supported">true</bool>'
}

# 2. Sodok Integers (Jumlah Kamera & ID)
# Ini kuncinya biar tombol AUX nongol
integers_path = "workdir/res/values/integers.xml"
ganti_integers = {
    '<integer name="back_camera_number">1</integer>': '<integer name="back_camera_number">3</integer>',
    '<integer name="aux_camera_id">0</integer>': '<integer name="aux_camera_id">2</integer>'
}

# Eksekusi
print("--- Memulai Ritual AUX Booster ---")
sodok_file(bools_path, ganti_bools)
sodok_file(integers_path, ganti_integers)
print("--- Selesai bray! ---")
