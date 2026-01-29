import os
import xml.etree.ElementTree as ET

# Lokasi file target
xml_path = "workdir/res/values/bools.xml"

def sodok_xml():
    if not os.path.exists(xml_path):
        print(f"[-] Error: {xml_path} gak ketemu bray!")
        return

    print("[+] Memulai ritual sodok XML...")
    
    # Daftar fitur yang mau kita paksa jadi TRUE
    target_fitur = [
        "is_hdr_supported",
        "is_night_mode_supported",
        "is_portrait_supported",
        "is_aux_supported",
        "is_multiple_cameras_supported",
        "is_wide_angle_supported",
        "is_google_lens_supported",
        "is_storage_saver_supported"
    ]

    with open(xml_path, 'r') as f:
        lines = f.readlines()

    new_lines = []
    for line in lines:
        updated = False
        for fitur in target_fitur:
            if f'name="{fitur}"' in line:
                # Ganti false jadi true
                new_line = line.replace("false", "true")
                new_lines.append(new_line)
                print(f"[!] Aktifin Fitur: {fitur}")
                updated = True
                break
        if not updated:
            new_lines.append(line)

    with open(xml_path, 'w') as f:
        f.writelines(new_lines)

    print("[+] Ritual XML selesai!")

if __name__ == "__main__":
    sodok_xml()
    
