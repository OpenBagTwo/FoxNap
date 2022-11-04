"""Command-line interface"""
import argparse
import logging
import sys
from collections.abc import Generator, Iterable, Sequence
from pathlib import Path

from . import __version__, _start_at
from .pack_generator import Track, generate_resourcepack
from .utils import is_valid_music_track

LOGGER = logging.getLogger(__name__)


def parse_args(argv: Sequence[str]) -> tuple[Path, list[Path], Path | None]:
    """Parse the provided command-line options to identify the parameters to use
    when generating the resource pack

    Parameters
    ----------
    argv : list-like of str (sys.argv)
        The options passed into the command line
    Returns
    -------
    Path
        The output path (name of the resource pack to be generated)
    list of Paths
        The paths of music files or folders to include
    Path or None
        The path of a configuration file to load (or None if one is not specified)
    """
    parser = argparse.ArgumentParser(
        prog="FoxNapRPG",
        description=f"Resourcepack generator for the FoxNap mod\nv{__version__}",
        formatter_class=argparse.RawTextHelpFormatter,
    )
    parser.add_argument(
        "-v", "--version", action="version", version=f"%(prog)s v{__version__}"
    )

    parser.add_argument(
        "-i",
        "--input",
        dest="inputs",
        action="append",
        type=Path,
        help=(
            "a music file or folder to add to the resource pack"
            "\n(if none are specified, the current working directory will be scanned"
            " for music files"
        ),
    )

    parser.add_argument(
        "-o",
        "--output",
        dest="output",
        action="store",
        default=Path("FoxNapRPG.zip"),
        type=Path,
        help=(
            "path and filename for saving the resource pack"
            "\n(default is FoxNapRP.zip in the current working directory.)"
        ),
    )

    parser.add_argument(
        "-c",
        "--config",
        dest="config",
        action="store",
        type=Path,
        help="a configuration file to load (for fine-grained resource pack control)",
    )

    args = parser.parse_args(argv[1:])
    return args.output, args.inputs, args.config


def resolve_tracks(
    *inputs: Path, config: Path | None = None
) -> Generator[Track, None, None]:
    """Given a list of input paths (and, optionally, a configuration file), generate
    the track specifications

    Parameters
    ----------
    *inputs : Path
        The paths to grab inputs from. If no paths are provided, any files in the
        current working directory will be scanned
    config : Path, optional
        Optionally, a config file to read

    Returns
    -------
    list-like of Tracks
        A generator that will loop through all the input paths and yield Track
        specifications
    """
    if config is not None:
        raise NotImplementedError("Config files are not yet implemented")
    if len(inputs) == 0:
        inputs = (Path("."),)

    next_track_number = _start_at
    for input_path in inputs:
        if input_path.is_file():
            input_files: Iterable[Path] = (input_path,)
        elif input_path.is_dir():
            input_files = input_path.rglob("*")
        else:
            LOGGER.warning(f"{input_path} is not a valid path")
            continue
        for input_file in input_files:
            if input_file.is_dir():
                continue
            # yield config[input_file] or
            if is_valid_music_track(input_file):
                yield Track(next_track_number, input_file, True)
            else:
                continue
            while True:
                next_track_number += 1
                # if next_track_number not in config.track_numbers:
                break


def main():
    console_logger = logging.StreamHandler()
    console_logger.setLevel(logging.WARNING)
    console_logger.setFormatter(
        logging.Formatter("%(asctime)s - %(name)s - %(levelname)s - %(message)s")
    )
    LOGGER.addHandler(console_logger)

    output_path, inputs, config = parse_args(sys.argv)
    tracks = resolve_tracks(*inputs, config=config)
    generate_resourcepack(output_path, *tracks)
