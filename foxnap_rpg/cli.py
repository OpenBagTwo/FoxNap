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
from .pack_generator import Track, generate_resource_pack
from .utils import BUILT_IN_DISC_COUNT, is_valid_music_track

LOGGER = logging.getLogger(__name__)


def _get_cwd() -> Path:
    """Get the folder that should be considered the current working directory,
    which will be different depending on whether this is being run from inside
    a PyInstaller bundle"""
    if getattr(sys, "frozen", False):
        if hasattr(sys, "_MEIPASS"):
            # then we're inside a PyInstaller bundle
            return Path(sys.argv[0]).parent
    return Path(".")


def parse_args(
    argv: Sequence[str],
) -> tuple[Path, Path, list[Path], Path | None, dict[str, Any]]:
    """Parse the provided command-line options to identify the parameters to use
    when generating the resource pack

    Parameters
    ----------
    argv : list-like of str (sys.argv)
        The options passed into the command line
    Returns
    -------
    Path
        The output path for the resource pack zip
    Path
        The output path for the mod config file
    list of Paths
        The paths of music files or folders to include
    Path or None
        The path of a configuration file to load (or None if one is not specified)
    dict
        Settings for the TrackBuilder
    """
    parser = argparse.ArgumentParser(
        prog="FoxNapRPG",
        description=f"Resource pack generator for the FoxNap mod\nv{__version__}",
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
        action="store",
        default=_get_cwd() / "FoxNapRP.zip",
        type=Path,
        help=(
            "the path and filename for saving the resource pack"
            "\n(default is FoxNapRP.zip in the current working directory)"
        ),
    )

    parser.add_argument(
        "-n",
        "--start-at",
        action="store",
        default=1,
        type=int,
        help=(
            "The minumum track number to auto-assign. Default is 1, which will overwrite"
            f" the tracks included with the mod. Set to {BUILT_IN_DISC_COUNT}"
            " if you want to keep the music bundled with the mod or to a different"
            " number to avoid conflicting with another FoxNap resource pack (in which"
            " case you'll also want to use -g."
        ),
    )

    parser.add_argument(
        "-c",
        "--config-dir",
        action="store",
        default=_get_cwd(),
        type=Path,
        help=(
            "the folder to write the FoxNap mod configuration file (foxnap.yaml) into."
            "\nBy default this will be the current working directory, but you could,"
            "\nfor example, specify your minecraft mod config folder."
        ),
    )

    parser.add_argument(
        "-s",
        "--specs",
        dest="spec_file",
        action="store",
        type=Path,
        help="a configuration file to load for fine-grained record track specification",
    )

    parser.add_argument(
        "-m",
        "--ignore-missing",
        dest="default_required",
        action="store_false",
        help="ignore any missing files specified in the config unless they are"
        "\nexplicitly marked as required (default behavior is that all specified"
        "\nfiles are required unless explicitly marked otherwise)",
    )

    parser.add_argument(
        "-u",
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
        "-g",
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
        "start_at": args.start_at,
        "verbosity": args.verbosity or 20,
        "required": args.default_required,
        "unspecified_file_handling": args.unspecified_file_handling,
        "enforce_contiguous_track_numbers": (
            "error" if args.enforce_contiguous else "ignore"
        ),
    }

    inputs = args.inputs or [_get_cwd()]

    config_path = args.config_dir / "foxnap.yaml"

    return args.output, config_path, inputs, args.spec_file, builder_kwargs


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
    for input_path in sorted(inputs):
        LOGGER.debug(f"Searching {input_path}")
        if input_path.is_file():
            input_files: Iterable[Path] = (input_path,)
        elif input_path.is_dir():
            input_files = sorted(input_path.rglob("*"))
        else:
            LOGGER.warning(f"{input_path} is not a valid path")
            continue
        for input_file in input_files:
            if input_file.is_dir():
                continue
            if is_valid_music_track(input_file):
                LOGGER.debug(f"Found music file {input_file}")
                try:
                    yield builder[input_file]
                except ValueError as oh_no:
                    LOGGER.warning(
                        f"Could not parse {input_file}:" f"\n  {oh_no}" "\n\nSkipping."
                    )
            else:
                continue


def main():
    console_logger = logging.StreamHandler()

    console_logger.setFormatter(
        logging.Formatter("%(asctime)s - %(name)s - %(levelname)s - %(message)s")
    )
    LOGGER.addHandler(console_logger)
    PACKGEN_LOGGER.addHandler(console_logger)

    output_path, config_path, inputs, config, builder_kwargs = parse_args(sys.argv)

    log_level = builder_kwargs.pop("verbosity")
    LOGGER.setLevel(log_level)
    PACKGEN_LOGGER.setLevel(log_level)

    if config:
        specs: Iterable[Spec] = read_specs_from_config_file(config)
    else:
        specs = ()
    with TrackBuilder(*specs, **builder_kwargs) as builder:
        tracks = resolve_tracks(builder, *inputs)
        track_durations = generate_resource_pack(output_path, *tracks)
    LOGGER.info(f"Writing config file to {config_path}")
    with config_path.open("w") as config_file:
        # TODO: make pyyaml a requirement and actually use YAML dump
        config_file.write(f"# auto-generated by FoxNapRPG v{__version__}\n")
        config_file.write(f"n_discs: {builder.n_discs}\n")
        if builder.n_discs > 64:
            config_file.write(
                f"max_discs: {builder.n_discs}"
                "  # WARNING! This may cause issues during multiplayer!"
                "\n"
            )
        config_file.write("track_lengths:\n")
        for num, length in track_durations.items():
            config_file.write(f"  {num}: {length}\n")
