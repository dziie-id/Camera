import os

def tweak_shamim():
    # File config Shamim biasanya beda tempat
    paths = [
        "workdir/res/values/bools.xml",
        "workdir/res/values/integers.xml",
        "workdir/res/xml/preferences_camera.xml" # Shamim sering naruh di sini
    ]

    for path in paths:
        if not os.path.exists(path): continue
        with open(path, 'r') as f:
            data = f.read()
        
        # Aktifin fitur yang mungkin masih off
        data = data.replace('name="is_hdr_supported">false', 'name="is_hdr_supported">true')
        data = data.replace('name="is_aux_supported">false', 'name="is_aux_supported">true')
        
        # Paksa ID 2 atau 4 (sesuai log lu) langsung di config
        if "integers.xml" in path:
            data = data.replace('name="aux_camera_id">0', 'name="aux_camera_id">2')
            data = data.replace('name="back_camera_number">1', 'name="back_camera_number">3')

        with open(path, 'w') as f:
            f.write(data)
        print(f"[+] Shamim Tweaked: {path}")

if __name__ == "__main__":
    tweak_shamim()
    
