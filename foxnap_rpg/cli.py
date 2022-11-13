"""Command-line interface"""
import argparse
import logging
import sys
from collections.abc import Generator, Iterable, Sequence
from pathlib import Path
from typing import Any

from . import __version__
from .builder import Spec, TrackBuilder
from .config import read_specs_from_config_file
from .pack_generator import LOGGER as PACKGEN_LOGGER
from .pack_generator import Track, generate_resourcepack
from .utils import is_valid_music_track

LOGGER = logging.getLogger(__name__)


def parse_args(
    argv: Sequence[str],
) -> tuple[Path, list[Path], Path | None, dict[str, Any]]:
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
    dict
        Settings for the TrackBuilder
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
        default=Path("FoxNapRP.zip"),
        type=Path,
        help=(
            "path and filename for saving the resource pack"
            "\n(default is FoxNapRP.zip in the current working directory)"
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

    parser.add_argument(
        "--ignore-missing",
        dest="default_required",
        action="store_false",
        help="ignore any missing files specified in the config unless they are"
        "\nexplicitly marked as required (default behavior is that all specified"
        "\nfiles are required unless explicitly marked otherwise)",
    )

    parser.add_argument(
        "--unspecified-file-handling",
        default="use-defaults",
        type=str,
        choices=("use-defaults", "warning", "error"),
        help="behavior when a file is found that does not match a provided spec."
        "Options are:"
        "\n - use-defaults : (default) create the track based on default handling"
        " settings"
        "\n - warning : create the track based on default handling settings, but"
        " emit a warning"
        "\n - error : raise an error",
    )

    parser.add_argument(
        "--allow-track-number-gaps",
        dest="enforce_contiguous",
        action="store_false",
        help="do not raise an error if the resulting resource pack would have gaps in"
        " track / record numbers."
        "\nYou only want to do this if your resource pack is being layered on top"
        "of an existing resource pack.",
    )

    parser.add_argument(
        "--silent",
        dest="verbosity",
        action="store_const",
        const=30,
        help="Only print messages to the console if there's a problem.",
    )

    parser.add_argument(
        "--verbose",
        dest="verbosity",
        action="store_const",
        const=10,
        help="Print debug messages to the console.",
    )

    args = parser.parse_args(argv[1:])
    builder_kwargs = {
        "verbosity": args.verbosity or 20,
        "required": args.default_required,
        "unspecified_file_handling": args.unspecified_file_handling,
        "enforce_contiguous_track_numbers": "error"
        if args.enforce_contiguous
        else "ignore",
    }

    inputs = args.inputs or [Path(".")]

    return args.output, inputs, args.config, builder_kwargs


def resolve_tracks(
    builder: TrackBuilder,
    *inputs: Path,
) -> Generator[Track, None, None]:
    """Given a list of input paths (and, optionally, a configuration file), generate
    the track specifications

    Parameters
    ----------
    builder : TrackBuilder
        A configured TrackBuilder responsible for creating the Tracks per user intent
    *inputs : Path
        The paths to grab inputs from. If no paths are provided, any files in the
        current working directory will be scanned

    Returns
    -------
    list-like of Tracks
        A generator that will loop through all the input paths and yield Track
        specifications
    """
    if len(inputs) == 0:
        inputs = (Path("."),)

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
            if is_valid_music_track(input_file):
                yield builder[input_file]
            else:
                continue


def main():
    console_logger = logging.StreamHandler()
    console_logger.setFormatter(
        logging.Formatter("%(asctime)s - %(name)s - %(levelname)s - %(message)s")
    )
    LOGGER.addHandler(console_logger)
    PACKGEN_LOGGER.addHandler(console_logger)

    output_path, inputs, config, builder_kwargs = parse_args(sys.argv)

    PACKGEN_LOGGER.setLevel(builder_kwargs.pop("verbosity"))

    if config:
        specs: Iterable[Spec] = read_specs_from_config_file(config)
    else:
        specs = ()
    with TrackBuilder(*specs, **builder_kwargs) as builder:
        tracks = resolve_tracks(builder, *inputs)
        generate_resourcepack(output_path, *tracks)
