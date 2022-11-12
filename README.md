# Music Discs for Fox Nap 🦊

_Banner Pending_

<img src="https://i.imgur.com/Ol1Tcf8.png" alt="Requires Fabric" width="150"/>


A simple mod that adds custom music discs to be played in my Survival world's outdoor concert venue.
Or, I guess,
anywhere jukeboxes can be found.

## Setup and Use

_TODO_

## Resource Pack Generator

This repo also contains a stand-alone resource pack generator to simplify the process of adding
your own music discs.

### Installation

The resource pack generator is entirely portable and comes bundled
with all dependencies that are needed to run it. No installation
necessary--simply download the executable for your particular computer
and operating system from [the release page](../../releases) that
matches your version of the FoxNap mod.

#### Building from Source

You can also build the generator from source.

1. Clone this repo
1. [Download](https://ffmpeg.org/download.html) or install a version of `ffmpeg` that can decode
   files from your music library and that has support for encoding using `libvorbis`. Put the
   executable (or symbolic links to the executable) in the `foxnap_rpg/bin` folder.
1. Create and activate a virtual environment using python 3.10 or above
1. From the repo's root run `python -m pip install .`

### Usage

#### Any Operating System

Place the generator executable in an empty folder, then move any music you
want to turn into records into that folder. **There is no limit** to the
number of tracks you can include, and they **do not** need to be pre-converted
to [Ogg](https://en.wikipedia.org/wiki/Ogg). **The only requirement** is that
the files have to be
[decodable by `ffmpeg`](https://www.ffmpeg.org/general.html#Supported-File-Formats_002c-Codecs-or-Features)
.

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

#### Bonus for Windows Only

If you're a Windows user, you can _also_ take a folder containing all your music and _drag and drop
it onto_ `FoxNapRPG.exe`,
which will create `FoxNapRP.zip` in the folder containing the exe.

#### Advanced Users (any operating system)

You can also run the generator from the command-line, which will give you access to a bunch of
additional customization options.

For details, run

```bash
$ ./FoxNapRPG --help
```

from the folder where you saved the generator executable.

## Contributing

Find a bug? Have a suggestion or a question? Want to contribute a new feature or enhancement?
[Open an issue](https://github.com/OpenBagTwo/FoxNap/issues/new)!

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
[his tutorial for adding a custom villager profession](https://gist.github.com/Kaupenjoe/237846a971fdd254c7da9639c85e65c1)
.

Also shouting out [@Siphalor](https://github.com/Siphalor) and Reddit's
[jSdCool](https://www.reddit.com/user/jSdCool/) for
[this conversation](https://www.reddit.com/r/fabricmc/comments/mkumx8/comment/gticqn2/) on adding
non-mod external libraries to a Fabric mod. It should not have been this hard to add the SnakeYAML
library to a mod.