#!/usr/bin/env bash

# build script for the resource pack generator

# build package
pip install .

# cd to build folder to avoid relative importing
cp launcher.py build/. && cd build

# build with PyInstaller
pyinstaller launcher.py --console --onefile -n FoxNapRPG \
  --collect-all foxnap_rpg

# clean up
pip uninstall .
