import os
import re

# Cari di semua folder res/values*
res_path = "workdir/res/"

def sodok_brutal():
    print("--- Memulai Deep Scan Sapphire ---")
    target_keys = {
        "is_aux_supported": "true",
        "is_hdr_supported": "true",
        "is_night_mode_supported": "true",
        "is_multiple_cameras_supported": "true",
        "back_camera_number": "3",
        "aux_camera_id": "2" # Kita coba ID 2 lagi karena di log lu dia Hardware Level 3
    }

    for root, dirs, files in os.walk(res_path):
        for file in files:
            if file.endswith(".xml"):
                file_full = os.path.join(root, file)
                with open(file_full, 'r') as f:
                    content = f.read()
                
                orig_content = content
                for key, val in target_keys.items():
                    # Ganti kalau ada false
                    pattern_bool = f'name="{key}">false'
                    if pattern_bool in content:
                        content = content.replace(pattern_bool, f'name="{key}">true')
                    
                    # Ganti kalau ada integer salah
                    if key == "back_camera_number" or key == "aux_camera_id":
                        pattern_int = re.compile(rf'name="{key}">\d+</integer>')
                        content = pattern_int.sub(f'name="{key}">{val}</integer>', content)

                if content != orig_content:
                    with open(file_full, 'w') as f:
                        f.write(content)
                    print(f"[!] Berhasil suntik: {file}")

# Eksekusi
if __name__ == "__main__":
    sodok_brutal()
