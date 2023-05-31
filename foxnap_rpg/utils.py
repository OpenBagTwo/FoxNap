"""Assorted helper functions"""
import math
import os
from collections import Counter
from pathlib import Path
from typing import Iterable, TypeVar, cast

import ffmpeg

from . import bin

T = TypeVar("T")

BUILT_IN_DISC_COUNT = 7  # number of discs included with the mod


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


def extract_track_duration(track_path: os.PathLike | str) -> int:
    """Extract the duration of the track from metadata

    Parameters
    ----------
    track_path: pathlike
        path to the track

    Returns
    -------
    int
        The length of the track in seconds (rounded up).

    Raises
    ------
    ValueError
        If for some reason the track's duration cannot be parsed from the
        metadata/
    """
    metadata = ffmpeg.probe(os.fspath(track_path), cmd=bin.ffprobe)
    try:
        return math.ceil(float(metadata["format"]["duration"]))
    except (KeyError, TypeError, ValueError) as parse_fail:
        raise ValueError(
            "Could not extract duration from track metadata:" f"\n  {parse_fail}"
        )


def spec_matches_path(
    path_spec: os.PathLike | str | tuple[str, ...],
    file_path: os.PathLike | str | tuple[str, ...],
) -> bool:
    """Determine if a given path spec matches a provided file path

    Parameters
    ----------
    path_spec : pathlike
        The path specification to match against
    file_path : pathlike
        The path to evaluate

    Returns
    -------
    bool
        True if the file_path matches the path_spec, False otherwise

    Notes
    -----
    - The plan is for this to eventually support wildcards and regular expressions
    """
    if isinstance(path_spec, (str, os.PathLike)):
        path_spec = _part_out_path(path_spec)
    if isinstance(file_path, (str, os.PathLike)):
        file_path = _part_out_path(os.path.abspath(file_path))
    spec_ext, *spec_parts = path_spec
    file_ext, *file_parts = file_path
    if file_parts[: len(spec_parts)] != spec_parts:
        return False
    if file_ext == spec_ext or spec_ext == "":
        return True
    return False


def _part_out_path(path: os.PathLike | str) -> tuple[str, ...]:
    """Split a given path into a format that's easier to evaluate against

    Parameters
    ----------
    path : pathlike
        The path to split out

    Returns
    -------
    str
        The extension of the path
    *str
        The parts of the path (sans extension) in reverse order
    """
    path = Path(path)
    return path.suffix, *reversed(path.with_suffix("").parts)


def _reassemble_path(ext: str, *parts: str) -> str:
    """Undo the above operation to return the original path (as string)

    Parameters
    ----------
    ext : str
        The file extension
    *parts : str
        The parts of the path (sans extension) in reverse order

    Returns
    -------
    str
        The original path that was parted out
    """
    return os.path.join(*reversed(parts)) + ext


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
        for num in range(BUILT_IN_DISC_COUNT + 1, max(counter.keys() or (0,)))
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


def validate_track_file_specs(*path_specs: os.PathLike | str, strict=False) -> None:
    """Validate a given list of file name specs

    Parameters
    ----------
    *path_specs: pathlike
        The file paths of the tracks. These can either be the full (or terminating)
        paths, or the file names, with or without extension.
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
    counter: dict[str, int] = Counter((os.fspath(Path(spec)) for spec in path_specs))

    # TODO: check for invalid file specs. But what is an invalid filename on *nix?

    conflicts_report: str = _generate_dupes_report(counter)

    parted_specs: list[tuple[str, ...]] = sorted(
        _part_out_path(spec) for spec in counter.keys()
    )

    for spec in parted_specs:
        for reference_spec in parted_specs:
            if spec == reference_spec:
                continue
            if spec_matches_path(reference_spec, spec):
                spec_path = _reassemble_path(*spec)
                reference_path = _reassemble_path(*reference_spec)
                conflicts_report += (
                    f"\n - Files matching '{spec_path}'"
                    " would also match any files matching"
                    f" '{reference_path}'"
                )
            elif strict:
                if spec[0] == "":  # if spec has no extension
                    reference_ext, *reference_parts = reference_spec
                    if spec_matches_path(("", *reference_parts), spec):
                        spec_path = _reassemble_path(*spec)
                        reference_path = _reassemble_path(*reference_spec)
                        conflicts_report += (
                            f"\n - Files matching '{spec_path}'"
                            " may also match files matching"
                            f" '{reference_path}'"
                        )

                # regex will go here, and BOY WILL IT BE FUN
                # TO PROVE THOSE ARE MUTUALLY EXCLUSIVE

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
