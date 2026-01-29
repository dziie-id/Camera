import os
import re

def sodok_shamim():
    res_path = "workdir/res/"
    # Settingan sesuai log Camera ID lu (ID 2 = Wide)
    target_keys = {
        "is_hdr_supported": "true",
        "is_night_mode_supported": "true",
        "is_aux_supported": "true",
        "is_multiple_cameras_supported": "true",
        "is_wide_angle_supported": "true",
        "back_camera_number": "3",
        "aux_camera_id": "2"
    }

    for root, dirs, files in os.walk(res_path):
        for file in files:
            if file in ["bools.xml", "integers.xml"]:
                file_path = os.path.join(root, file)
                with open(file_path, 'r') as f:
                    content = f.read()
                
                orig = content
                for key, val in target_keys.items():
                    content = content.replace(f'name="{key}">false', f'name="{key}">true')
                    if "camera" in key:
                        content = re.sub(rf'name="{key}">\d+</integer>', f'name="{key}">{val}</integer>', content)
                
                if content != orig:
                    with open(file_path, 'w') as f:
                        f.write(content)

if __name__ == "__main__":
    sodok_shamim()
    
