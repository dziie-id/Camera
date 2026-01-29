import os
import re

def sodok_shamim():
    # Daftar folder values (v31, v33, dll) biar kena semua
    res_path = "workdir/res/"
    
    target_keys = {
        "is_hdr_supported": "true",
        "is_night_mode_supported": "true",
        "is_aux_supported": "true",
        "is_multiple_cameras_supported": "true",
        "is_wide_angle_supported": "true",
        "back_camera_number": "3",
        "aux_camera_id": "2" # Lensa Wide Note 13 lu
    }

    print("--- Memulai Tweak Base Shamim ---")
    
    for root, dirs, files in os.walk(res_path):
        for file in files:
            if file in ["bools.xml", "integers.xml"]:
                file_path = os.path.join(root, file)
                with open(file_path, 'r') as f:
                    content = f.read()
                
                orig = content
                for key, val in target_keys.items():
                    # Sodok Boolean
                    content = content.replace(f'name="{key}">false', f'name="{key}">true')
                    # Sodok Integer
                    if "camera_id" in key or "camera_number" in key:
                        content = re.sub(rf'name="{key}">\d+</integer>', f'name="{key}">{val}</integer>', content)
                
                if content != orig:
                    with open(file_path, 'w') as f:
                        f.write(content)
                    print(f"[+] Modified: {file_path}")

if __name__ == "__main__":
    sodok_shamim()
