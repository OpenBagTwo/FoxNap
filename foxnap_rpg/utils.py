"""Assorted helper functions"""
import os
from collections import Counter
from typing import cast

import ffmpeg

from . import _start_at, bin


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


def validate_track_numbers(*nums: int | None, check_contiguous: bool = False) -> None:
    """Validate a given list of track numbers

    Parameters
    ----------
    *nums : int or None
        The set of track numbers to validate (None functions as a wild card)
    check_contiguous : bool, optional
        By default, this method only checks for conflicts. If this method is called with
        check_contiguous=True, then check also if the specified set of track numbers
        will result in gaps.

    Raises
    ------
    ValueError
        If any of the provided track numbers are invalid
    RuntimeError
        If any of the validations fail
    """
    _counter = Counter(nums)
    wildcard_count: int = _counter.pop(None, 0)
    counter = cast(dict[int, int], _counter)  # now that we've yeeted the None

    invalid_report: str = ""
    for num in sorted(counter.keys()):
        if num < 1 or int(num) != num:
            invalid_report += f"\n - {num} is not a valid track number"
    if invalid_report:
        raise ValueError(
            "The provided track numbers contain invalid values:" + invalid_report
        )

    dupes_report: str = ""
    for num, count in sorted(counter.items()):
        if count > 1:
            dupes_report += f"\n - {num} is included {count} times"
    if dupes_report:
        raise RuntimeError(
            "The provided track numbers contain duplicates:" + dupes_report
        )

    if not check_contiguous:
        return

    missing: tuple[int, ...] = tuple(
        num
        for num in range(_start_at, max(counter.keys() or (0,)))
        if num not in counter.keys()
    )
    if len(missing) > wildcard_count:
        if wildcard_count == 0:
            wildcard_line = ""
        elif wildcard_count == 1:
            wildcard_line = (
                f"\nwhereas only {wildcard_count} track was"
                " provided with an unspecified track numbers"
            )
        else:
            wildcard_line = (
                f"\nwhereas only {wildcard_count} tracks were"
                " provided with unspecified track numbers"
            )
        message = (
            "There are more missing track numbers"
            " than tracks that can fill those track numbers."
            f"\nNo tracks are specified with numbers {missing}"
        )
        message += wildcard_line + "."
        raise RuntimeError(message)
