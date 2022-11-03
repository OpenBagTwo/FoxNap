# Resource Pack Generator for Fox Nap 🦊

_Banner Pending_

An app for quickly making custom music disc resource packs.

## Installation

This resource pack generator is entirely portable and comes bundled
with all dependencies that are needed to run it. No installation
necessary--simply download a release for your particular architecture
and operating system from [the release page](../../releases).

### Building from Source

Of course, if you really want to, you _can_ build this from source as well. Simply:

1. Clone this repo
1. Create and activate a virtual environment using python 3.10 or above
1. From the repo's root run `python -m pip install .`

## Usage

### Any Operating System

Place the generator executable in an empty folder, then move any music you
want to turn into records into that folder. **There is no limit** to the
number of tracks you can include, and they **do not** need to be pre-converted
to [Ogg](https://en.wikipedia.org/wiki/Ogg). **The only requirement** is that
the files have to be
[decodable by `ffmpeg`](https://www.ffmpeg.org/general.html#Supported-File-Formats_002c-Codecs-or-Features).

_**Pro Tip:** if your music files include [metadata](https://en.wikipedia.org/wiki/ID3), the title and artist
name will get automatically extracted, and any album art will be used to help generate the music disc texture._

When you're ready, simply double-click the executable, and you should soon have a new file in that folder
named `FoxNapRP.zip`, which you can then add as a resource pack to your game. You should also get a file
named `foxnap.yaml`. Take that and put it in your minecraft `config` folder, overwriting any existing
file.

### Windows Only

If you're a Windows user, you can _also_ take a folder containing all your music and _drag and drop it onto_ `FoxNapRPG.exe`,
which will create `FoxNapRP.zip` and `foxnap.yml` in the folder containing the exe.

### Advanced Users (any operating system)

You can also run the generator from the command-line, which will give you access to a bunch of additional customization options.

For details, run

```bash
$ ./FoxNapRPG --help
```

## Contributing

Please direct all bug reports, feature requests and potential contributions to the main [FoxNap repo](https://github.com/OpenBagTwo/FoxNap/).

## License

All code in this repository is licensed under
[GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html).

