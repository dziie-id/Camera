#!/bin/bash

# Warna-warni biar keren
HIJAU='\033[0;32m'
MERAH='\033[0;31m'
NORMAL='\033[0m'

echo -e "${HIJAU}Mulai Proses Modding...${NORMAL}"

# 1. Bongkar
java -jar apktool.jar d base.apk -o workdir

# 2. Sodok pake Python
python3 sodok.py

# 3. Rakit (Pake aapt2)
java -jar apktool.jar b workdir --use-aapt2 -o unsigned.apk

# 4. Sign
if [ -f "unsigned.apk" ]; then
    apksigner sign --ks debug.keystore --ks-pass pass:android --out hasil_mod_sultan.apk unsigned.apk
    echo -e "${HIJAU}BERHASIL! File: hasil_mod_sultan.apk${NORMAL}"
else
    echo -e "${MERAH}GAGAL RAKIT BRAY!${NORMAL}"
fi
