#!/usr/bin/env bash

# build script for the resourcepack generator

# build package
python setup.py build

# cd to build folder to avoid relative importing
cp launcher.py build/. && cd build

# build with PyInstaller
pyinstaller launcher.py --console --onefile -n FoxNapRPG \
  --collect-all foxnap_rpg
