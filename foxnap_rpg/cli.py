"""Command-line interface"""
import argparse
import sys
from pathlib import Path
from typing import Iterable, Sequence

from . import __version__
from .pack_generator import Track, generate_resourcepack


def parse_args(argv: Sequence[str]) -> tuple[Path, Iterable[Track]]:
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
    list-like of Tracks
        The tracks to be included
    """
    parser = argparse.ArgumentParser(
        prog="FoxNapRPG",
        description=(f"Resourcepack generator for the FoxNap mod\nv{__version__}"),
        formatter_class=argparse.RawTextHelpFormatter,
    )
    parser.add_argument(
        "-v", "--version", action="version", version=f"%(prog)s v{__version__}"
    )

    parser.parse_args(argv)
    raise NotImplementedError


def main():
    output_path, tracks = parse_args(sys.argv)
    generate_resourcepack(output_path, *tracks)
