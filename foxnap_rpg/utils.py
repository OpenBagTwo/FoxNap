"""Assorted helper functions"""
import os
from collections import Counter
from pathlib import Path
from typing import Iterable, TypeVar, cast

import ffmpeg

from . import _start_at, bin

T = TypeVar("T")


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
        check_contiguous=True, then it will also check if the specified set of track
        numbers will result in gaps.

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

    if dupes_report := _generate_dupes_report(counter):
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


def validate_track_file_specs(*file_specs: str, strict=False) -> None:
    """Validate a given list of file name specs

    Parameters
    ----------
    *file_specs: str
        The file names of the tracks. These can either be the full (or terminating)
        path, or the file name, with or without extension.
    strict: bool, optional
        Whether to raise a validation error for *possible* conflicts in addition to
        definite ones. Default is False.

    Raises
    ------
    ValueError
        If any of the file specs are invalid
    RuntimeError
        If any of the validations fail
    """
    counter: dict[str, int] = Counter(file_specs)

    # TODO: check for invalid file specs. But what is an invalid filename on *nix?

    conflicts_report: str = _generate_dupes_report(counter)

    specs_with_ext = []
    no_ext_specs = []
    for file_spec in sorted(counter.keys()):
        if Path(file_spec).suffix == "":
            no_ext_specs.append(file_spec)
        else:
            specs_with_ext.append(file_spec)

    for file_spec in sorted(counter.keys()):
        for reference_spec in specs_with_ext:
            if file_spec == reference_spec:
                continue
            if file_spec.endswith(reference_spec):
                conflicts_report += (
                    f"\n - Files matching '{file_spec}' would also match"
                    f" any files matching '{reference_spec}'"
                )
        for reference_spec in no_ext_specs:
            if file_spec == reference_spec:
                continue
            if os.fspath(Path(file_spec).with_suffix("")).endswith(
                os.fspath(Path(reference_spec))
            ):
                conflicts_report += (
                    f"\n - Files matching '{file_spec}' would also match"
                    f" any files matching '{reference_spec}'"
                )
            if strict and os.fspath(Path(reference_spec)).endswith(
                os.fspath(Path(file_spec).with_suffix(""))
            ):
                conflicts_report += (
                    f"\n - Files matching '{file_spec}' may also match"
                    f" files matching '{reference_spec}'"
                )

    # regex will go here, and BOY WILL IT BE FUN TO PROVE THOSE ARE MUTUALLY EXCLUSIVE

    if conflicts_report:
        raise RuntimeError(
            "The provided file specifications contain the following conflicts:"
            + conflicts_report
        )


def _generate_dupes_report(counts: dict[T, int]) -> str:
    dupes_report: str = ""
    try:
        items: Iterable[tuple[T, int]] = sorted(counts.items())
    except TypeError:
        items = counts.items()
    for value, count in items:
        if count > 1:
            dupes_report += f"\n - {repr(value)} is included {count} times"
    return dupes_report
