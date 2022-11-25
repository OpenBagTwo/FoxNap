from setuptools import setup

import versioneer

setup(
    name="foxnap_rpg",
    python_requires=">=3.10",
    description="Resource Pack generator for the FoxNap mod",
    author='Gili "OpenBagTwo" Barlev',
    url="https://github.com/OpenBagTwo/FoxNap",
    packages=["foxnap_rpg", "foxnap_rpg.assets", "foxnap_rpg.bin"],
    entry_points={
        "console_scripts": [
            "FoxNapRPG = foxnap_rpg.cli:main",
        ]
    },
    license="GPL v3",
    include_package_data=True,
    version=versioneer.get_version(),
    cmdclass=versioneer.get_cmdclass(),
    install_requires=["pillow", "ffmpeg-python"],
)
