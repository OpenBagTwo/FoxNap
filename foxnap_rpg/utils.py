"""Assorted helper functions"""
import os

import ffmpeg

from . import bin


def is_valid_music_track(file_path: str | os.PathLike) -> bool:
    """Probe a file to determine if it's convertible using ffmpeg

    Parameters
    ----------
    file_path : pathlike
        The path to the file to probe

    Returns
    -------
    bool
        True if the file is a music track that can be converted using ffmpeg, False
        if not
    """
    try:
        metadata = ffmpeg.probe(file_path, cmd=bin.ffprobe)
    except ffmpeg.Error:
        return False

    for stream in metadata["streams"]:
        if stream["codec_type"] == "audio":
            return True

    return False
