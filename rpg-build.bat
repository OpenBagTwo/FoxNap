# build script for the resourcepack generator

# build package
pip install .

# cd to build folder to avoid relative importing
copy launcher.py build\launcher.py
cd build

# build stand-alone executable with PyInstaller
pyinstaller launcher.py --console --onefile -n FoxNapRPG --collect-all foxnap_rpg
