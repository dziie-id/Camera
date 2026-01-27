#!/bin/bash
keytool -genkey -v -keystore release.keystore -alias aperture -keyalg RSA -keysize 2048 -validity 10000   -dname "CN=Aperture, OU=Dev, O=Standalone, L=Jakarta, ST=Jakarta, C=ID"   -storepass password -keypass password
