#!/usr/bin/env bash

# build script for the resourcepack generator

# build package
python setup.py build

cp launcher.py build/. && cd build

# build with PyInstaller
pyinstaller launcher.py --console --onefile -n FoxNapRPG \
  --collect-all foxnap_rpg
