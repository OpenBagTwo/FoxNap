"""Functionality for creating the datapack to accompany the generated resourcepack"""

import json
import logging
import os
import shutil
from pathlib import Path
from tempfile import TemporaryDirectory
from typing import Iterable

from . import assets

LOGGER = logging.getLogger(__name__)


def generate_datapack(
    output_path: os.PathLike | str,
    jukebox_songs: Iterable[tuple[str, float, int]],
    title: str = "Custom Fox Nap Records",
) -> None:
    """Given a list of track numbers, durations and redstone strengths,
    generate an accompanying datapack

    Parameters
    ----------
    output_path : pathlike
        The filename of the datapack
    jukebox_songs : list-like of (str, float, int) tuples
        The specifications of the jukebox songs, where the values are:
            1. The song name (should match the sound event and item name)
            2. The duration of the track in seconds
            3. The redstone signal strength of the comparator output that should
               be emitted from a jukebox playing that track
    title : str, optional
        The title for the datapack. Default is "Custom Fox Nap Records"
    """
    with TemporaryDirectory() as tmpdir:
        song_directory = Path(tmpdir) / "foxnap" / "jukebox_song"
        song_directory.mkdir(parents=True)
        for song_spec in jukebox_songs:
            LOGGER.debug("Generating %s", f"{song_spec[0]}.json")
            (song_directory / f"{song_spec[0]}.json").write_text(
                generate_jukebox_song(*song_spec)
            )
        output_path_as_str = str(output_path)
        if output_path_as_str.endswith(".zip"):
            output_path_as_str = output_path_as_str[:-4]
        LOGGER.info(
            "Compressing archive and saving as %s",
            Path(output_path_as_str).with_suffix(".zip").absolute(),
        )
        (Path(tmpdir) / "pack.mcmeta").write_text(generate_mcmeta(title))
        shutil.make_archive(output_path_as_str, "zip", tmpdir)


def generate_jukebox_song(
    song_name: str, length_in_seconds: float, comparator_output: int
) -> str:
    """Generate a jukebox song spec JSON given the provided track specification

    Parameters
    ----------
    song_name : str
        The name of the jukebox song
    length_in_seconds : float
        The duration of the song in seconds
    comparator_output : int
        The redstone signal strength of the comparator output that should be emitted
        from a jukebox playing that track
    """
    return json.dumps(
        {
            "comparator_output": comparator_output,
            "description": {"translate": f"item.foxnap.{song_name}.desc"},
            "length_in_seconds": length_in_seconds,
            "sound_event": f"foxnap:{song_name}",
        },
        sort_keys=True,
        indent=4,
    )


def generate_mcmeta(title: str) -> str:
    """Fill out the pack.mcmeta template

    Parameters
    ----------
    title : str
        The datapack title

    Returns
    -------
    str
        The filled-out mcmeta, ready to be written to file
    """
    return assets.DP_MCMETA.replace("%%title%%", title)
