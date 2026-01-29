import os

def sodok_shamim():
    # Daftar file yang bakal kita paksa aktif
    targets = [
        "workdir/res/values/bools.xml",
        "workdir/res/values/integers.xml",
        "workdir/res/values/strings.xml"
    ]

    for path in targets:
        if not os.path.exists(path): continue
        with open(path, 'r') as f:
            data = f.read()
        
        # Aktifin semua saklar dasar
        data = data.replace('name="is_hdr_supported">false', 'name="is_hdr_supported">true')
        data = data.replace('name="is_aux_supported">false', 'name="is_aux_supported">true')
        data = data.replace('name="is_wide_angle_supported">false', 'name="is_wide_angle_supported">true')
        data = data.replace('name="is_multiple_cameras_supported">false', 'name="is_multiple_cameras_supported">true')

        # Setting khusus ID Kamera sesuai log Note 13 lu
        if "integers.xml" in path:
            # Kita set back_camera ke 3 karena lu punya Main, Wide, Macro
            data = data.replace('name="back_camera_number">1', 'name="back_camera_number">3')
            # Paksa AUX ke ID 2 (Wide) atau ID 4 (Logical)
            data = data.replace('name="aux_camera_id">0', 'name="aux_camera_id">2')

        with open(path, 'w') as f:
            f.write(data)
        print(f"[+] Berhasil Tweak: {path}")

if __name__ == "__main__":
    sodok_shamim()
    
