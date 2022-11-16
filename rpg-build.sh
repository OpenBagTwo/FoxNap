#!/usr/bin/env bash

# build script for the resourcepack generator

# pip install
pip install --user --upgrade --force-reinstall .

# build with PyInstaller
pyinstaller launcher.py --console --onefile -n FoxNapRPG \
  --collect-all foxnap_rpg
