# Fox Nap 🦊
![server + client mod](https://img.shields.io/badge/Server\/Client-both-purple)
![mod loader: fabric](https://img.shields.io/badge/Mod_Loader-fabric-dbd0b4)
[![lint status](https://github.com/OpenBagTwo/FoxNap/actions/workflows/lint.yml/badge.svg)](https://github.com/OpenBagTwo/FoxNap/actions/workflows/lint.yml)
[![mod build status](https://github.com/OpenBagTwo/FoxNap/actions/workflows/build_mod.yml/badge.svg)](https://github.com/OpenBagTwo/FoxNap/actions/workflows/build_mod.yml)
[![RPG build status](https://github.com/OpenBagTwo/FoxNap/actions/workflows/build_rpg.yml/badge.svg)](https://github.com/OpenBagTwo/FoxNap/actions/workflows/build_rpg.yml)
![supported versions](https://img.shields.io/badge/Supported_Versions-1.19,1.19.2,1.19.3-blue)



_**A Survival-, Multiplayer- and Copyright-friendly mod for adding custom music to Minecraft**_

_Banner Pending_

<img src="https://i.imgur.com/Ol1Tcf8.png" alt="Requires Fabric" width="150"/>

<!-- TOC -->
* [Fox Nap 🦊](#fox-nap-)
  * [What is This?](#what-is-this)
  * [Setup and Customization](#setup-and-customization)
    * [Manual Resource Pack Creation](#manual-resource-pack-creation)
    * [Resource Pack Generator](#resource-pack-generator)
      * [Installation](#installation)
      * [Generating Resource Packs](#generating-resource-packs)
      * [Advanced Options](#advanced-options)
    * [Obtaining Records _and More!_](#obtaining-records-and-more)
  * [Contributing](#contributing)
    * [Building the Mod from Source](#building-the-mod-from-source)
    * [Building the Resource Pack Generator from Source](#building-the-resource-pack-generator-from-source)
  * [License and Acknowledgements](#license-and-acknowledgements)
<!-- TOC -->

## What is This?

FoxNap is a simple "Vanilla Plus" mod I wrote to enable putting on
["live music" concerts](https://www.google.com/search?q=lip+syncing+concert) in my
single-player survival world. In the process of putting together my voxel-island playlist,
I realized there was a core problem: **including the music I wanted within the mod's assets
would violate copyright**. I wasn't even looking at music I'd ~~downloaded from Kazaa in college~~
ripped from my personal music collection, it was tracks from Youtube's "Free"
[audio library](youtube.com/audiolibrary), where the license terms explicitly state:

> You may not make available, distribute or perform the music files from this library separately
> from videos and other content into which you have incorporated these music files
> (e.g., **standalone distribution of these files is not permitted**).

As I saw it, there were two options for getting around this limitation (besides abandoning the
project or not sharing the mod with anyone)

1. Limit the music included with the mod to permissively-licensed (public domain, attribution or
   [copyleft](https://www.gnu.org/licenses/copyleft.en.html)) audio
1. Allow for mod users to provide their own music via a config or a resource pack

In the end, I decided, [why not both?](https://www.youtube.com/watch?v=vqgSO8_cRio&t=5s)

## Setup and Customization

This mod comes pre-bundled with seven new music discs:

1. ["Colors," by Tobu](https://www.youtube.com/watch?v=eyLml-zzXzw)
2. [Camille Saint-Saëns: "Danse Macabre," performed by Kevin MacLeod](https://freemusicarchive.org/music/Kevin_MacLeod/Classical_Sampler)
* Four tracks performed by [PM Music](https://pmmusic.pro)
  from [Lud and Schlatts Musical Emporium](https://www.youtube.com/channel/UCFbtXFIaAJ0fOtgyeDs8Jog/)

  3. [Richard Strauss: Theme from _Also Sprach Zarathustra_](https://www.youtube.com/watch?v=9K3GQdD30F0)
  4. [Peter Ilyich Tchaikovsky: Love Theme from _Romeo & Juliet_](https://www.youtube.com/watch?v=unvW5g_YWEk)
  5. [Antonio Vivaldi: "Winter" from _The Four Seasons_](https://www.youtube.com/watch?v=VBSP75pr2bg)
  6. [Richard Wagner: Flight of the Valkyries](https://www.youtube.com/watch?v=uNkRW_9pHRQ)
 
7. [Nikokai Rimsky-Korsakov: "Flight of the Bumblebee" from _Tsar Saltan_, performed by The US Army Band](https://commons.wikimedia.org/wiki/File:Rimsky-Korsakov_-_flight_of_the_bumblebee.oga)

all of which are permissively licensed under the terms specified
[here](src/main/resources/assets/foxnap/sounds/records/LICENSES.md), allowing me to redistribute
them with this mod under the [Creative Commons Attribution-ShareAlike 4.0 License](https://creativecommons.org/licenses/by-sa/4.0/).

If this built-in playlist sounds like your jam, and you have no desire to add anything else, then
congrats! This is easy! Just download the mod to your instance's mods folder, start the game, and
[go find a village](#obtaining-records-and-more).

But if you're interested in some customization, read on:

### Manual Resource Pack Creation

FoxNap's item and sound registration structure was designed to make it as easy as possible for you
to replace or add to the built-in tracks via a resource pack similar to what 
[you'd make if you were replacing one of the vanilla discs](https://www.planetminecraft.com/blog/how-to-add-costume-music-the-easy-way-1-12/),
with the advantage that the number of discs provided by the mod is _completely dynamic_ and can be
set or changed simply by going into your instance's mod `config` folder, opening `foxnap.yaml` in
notepad, TextEditor, vim or any plaintext editor, and changing the number set in `n_discs`.

From there, if you're used to vanilla disc replacement resourcepacks, the differences will be:
- instead of `assets/minecraft`, all your files should be in `assets/foxnap`
- the ids of the sound files you'll be replacing (in `assets/foxnap/sounds.json`) will be
  `foxnap:track_1`, `foxnap:track_2`, etc. all the way up to the number `n_discs`
  you set in the `foxnap.yaml` config file stored in your instance's mod `config` folder.
- to set or replace the record textures, you'll need to create files named `track_1.json`,
  `track_2.json`, etc. within `assets/foxnap/models/item`
- when changing the names of the tracks to display, you'll need to edit
  `assets/foxnap/lang/en_us.json` and refer to the language entries as `item.foxnap.track_1` /
  `item.foxnap.track_1.desc`, `item.foxnap.track_2` / `item.foxnap.track_2.desc`, etc.

### Resource Pack Generator

If manually converting mp3s and hand-editing JSON isn't your idea of a fun time, **this project
provides an alternative** in the form of a stand-alone and portable (read: no installation or
setup required) resource pack generator.

#### Installation

1. Download the executable from [the release page](../../releases) that
   matches your operating system and your version of the mod.
1. Depending on your operating system and security settings, you may need
   to explicitly make the resource pack generator executable (on \*nix systems,
   you can do this from a terminal by running `chmod u+x /path/to/FoxNapRPG`

You can also [build the generator from source](#building-the-resource-pack-generator-from-source).

#### Generating Resource Packs

Place the generator executable in an empty folder, then move any music you
want to turn into records into that folder. **There is no limit** to the
number of tracks you can include, and they **do not** need to be pre-converted
to [Ogg](https://en.wikipedia.org/wiki/Ogg). The only requirement is that
the files have to be
[decodable by `ffmpeg`](https://www.ffmpeg.org/general.html#Supported-File-Formats_002c-Codecs-or-Features).

_**Pro Tip:** if your music files include [metadata](https://en.wikipedia.org/wiki/ID3), the title
and artist
name will get automatically extracted, and any album art will be used to help generate the music
disc texture._

When you're ready, simply double-click the `FoxNapRPG` executable. A terminal window should pop
up showing progress of the resource pack creation, and before you know it you should soon have a
new file in your folder named `FoxNapRP.zip`, which you can then add as a resource pack to your
game. You will then need to go into your minecraft `config` folder and edit `foxnap.yaml` to raise
the number of `n_discs` to include the ones added by the resource pack (this number needs to be
the number of built-in tracks plus the number of custom tracks you added).

#### Advanced Options

You can also run the generator from the command-line, which will give you access to a bunch of
additional customization options.

For details, run

```bash
$ ./FoxNapRPG --help
```

from the folder where you saved the generator executable.

### Obtaining Records _and More!_

## Contributing

Find a bug? Have a suggestion or a question? Want to contribute a new feature or enhancement?
[Open an issue](https://github.com/OpenBagTwo/FoxNap/issues/new)!

### Building the Mod from Source

### Building the Resource Pack Generator from Source

You can also build the generator from source.

1. Clone this repo
1. [Download](https://ffmpeg.org/download.html) or install a version of `ffmpeg` that can decode
   files from your music library and that has support for encoding using `libvorbis`. Put the
   executable (or symbolic links to the executable) in the `foxnap_rpg/bin` folder.
1. Create and activate a virtual environment using python 3.10 or above
    1. If you have a [`conda`-based](https://docs.conda.io/en/latest/) environment and package manager installed on your
       system, such as [mambaforge](https://github.com/conda-forge/miniforge#mambaforge), you can use the project's
       dedicated dev/build environment, creatable from the repo root via
       `mamba env create -f environment.yml` (substitute `conda` for `mamba` as needed)
1. From the repo's root, with your virtual environment activated, run `python -m pip install .`
1. At this point, you have two options:
    1. Use FoxNap as a python package, with `$ FoxNapRPG` available from the command line
    1. Create a stand-alone executable using [`pyinstaller`](https://pyinstaller.org/en/stable/) (included in the `conda`
       environment). The scripts `./rpg-build.sh` for \*nix or `.\rpg-build.bat` for Windows are available for reference.

## License and Acknowledgements

All code in this repository is licensed under
[GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html).

All assets in this repository are distributed under the
[Creative Commons Attribution-ShareAlike 4.0 License](https://creativecommons.org/licenses/by-sa/4.0/)
unless
otherwise stated.

Instrument icons are taken from the mod [mxTune](https://github.com/AeronicaMC/mxTune)
by [@AeronicaMC](https://github.com/AeronicaMC).

Instrument sounds are courtesy of [Philharmonia](https://philharmonia.co.uk)'s
[sound sample library](https://philharmonia.co.uk/resources/sound-samples/).

Many thanks to [@FoundationGames](https://github.com/FoundationGames) for making the code of
his awesome [Sandwichable](https://github.com/FoundationGames/Sandwichable) mod so easy to
understand and learn from, and similarly to
[Modding by Kaupenjoe](https://www.youtube.com/c/TKaupenjoe) for his awesome and detailed
tutorials on Minecraft modding, in this case
[his tutorial for adding a custom villager profession](https://gist.github.com/Kaupenjoe/237846a971fdd254c7da9639c85e65c1).

Also shouting out [@Siphalor](https://github.com/Siphalor) and Reddit's
[jSdCool](https://www.reddit.com/user/jSdCool/) for
[this conversation](https://www.reddit.com/r/fabricmc/comments/mkumx8/comment/gticqn2/) on adding
non-mod external libraries to a Fabric mod. It should not have been this hard to add the SnakeYAML
library to a mod.
