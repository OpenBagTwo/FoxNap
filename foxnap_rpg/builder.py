"""Logic for translating user intent into Track definitions"""
import logging
import os
from contextlib import AbstractContextManager
from pathlib import Path
from typing import Any, NamedTuple

from . import _start_at, utils
from .pack_generator import License, Track

LOGGER = logging.getLogger(__name__)


class Spec(NamedTuple):
    """Settings specifying the intent for handling a track

    Attributes
    ----------
    path_spec : Path
        The path of the input file this should match
    distinct : bool, optional
        Whether this spec is allowed to match more than one file. Right now only True
        (default) is supported.
    license_type : License, optional
        The permission level for use of the specified track. If None is specified,
        use the handler's default.
    required : bool, optional
        Whether to ensure that this track is found and included in the resource pack.
        If None is specified, use the handler's default.
    description: str, optional
        The name to give the track. If None is specified, the name will be extracted
        from the track metadata.
    num : int, optional
        The number to assign to the track. If None is specified, allow the handler
        to automatically assign a track number
    hue : bool or float, optional
        specification of the record texture template:
          - False: use the regular record template
          - True: use the colored vinyl template, with a random hue (default)
          - numeric value: use the specified hue shift, in degrees
        If None is specified, let this be set by the handler.
    use_album_art : bool, optional
        If True, the generator will attempt to extract album art from the track to use
        for the inlay of the record texture. If False, the track will always use a
        random inlay. If None is specified, let this be set by the handler.

    Notes
    -----
    - The plan is to extend this Spec to support matching multiple files
      (distinct=False). This would involve
        - file_spec supporting wild cards or regular expressions
        - description allowing for a format-style specification
        - num providing for more dynamic track number generation
    - The hue and use_album_art attributes are planned to be deprecated or replaced
      in favor of functionality that would allow a user to provide their own templates
      and complete item textures.
    """

    path_spec: Path
    distinct: bool | None = True
    license_type: License | None = None
    required: bool | None = None
    description: str | None = None
    num: int | None = None
    hue: bool | float | None = None
    use_album_art: bool | None = None


class TrackBuilder(AbstractContextManager):
    """A handler that creates a Track specification based on intent specified by the
    user and/or laid out in the program's configuration. TrackBuilders are intended
    to be used as context managers so as to properly validate that the tracks that
    get created in one session are self-consistent.

    Parameters
    ----------
    *specs : foxnap_rpg.builder.Spec
        The track specifications read in from user input or config file
    **defaults
        Overrides of either the default track settings or the default handler settings

    Examples
    --------
    >>> specs = [
    ...     Spec(Path("hello.mp3"), use_album_art=False),
    ...     Spec(Path("world.mp3")),
    ... ]
    >>> tracks = []
    >>> with TrackBuilder(*specs, hue=False) as track_builder:
    ...     for file in Path("music for foxnap").glob("*.mp3"):
    ...         tracks.append(track_builder[file])
    """

    _DEFAULTS: tuple[tuple[str, Any], ...] = (
        # track settings
        ("required", True),
        ("hue", True),
        ("use_album_art", True),
        ("license", License.PERSONAL),
        # handler settings
        ("unspecified_file_handling", "use-defaults"),
        ("enforce_contiguous_track_numbers", "error"),
        ("strict_file_checking", False),
    )

    def __init__(self, *specs: Spec, **defaults):
        self.defaults = dict(TrackBuilder._DEFAULTS)
        self.defaults.update(defaults)
        self.validate_specs(*specs)
        self._specs = list(specs)

    def validate_specs(self, *specs: Spec, check_contiguous=False) -> None:
        """Validate a set of specs

        Parameters
        ----------
        *specs : Spec
            The set of specs to validate
        check_contiguous : bool, optional
            By default, this method only checks track numbers for conflicts.
            If this method is called with check_contiguous=True, then it will also check
            if the specified set of track numbers will result in gaps.

        Raises
        ------
        ValueError
            If any of the specs are invalid
        RuntimeError
            If any of the specs conflict with any other specs
        NotImplementedError
            If any of the specs specify a planned feature that is not yet implemented
        """
        utils.validate_track_numbers(
            *(spec.num for spec in specs), check_contiguous=check_contiguous
        )
        utils.validate_track_file_specs(
            *(spec.path_spec for spec in specs),
            strict=self.defaults["strict_file_checking"],
        )
        if not all((spec.distinct for spec in specs)):
            raise NotImplementedError("Multitrack specs are not currently supported")

    def add_spec(self, spec: Spec):
        """Add a Spec to the builder

        Parameters
        ----------
        spec : Spec
            The track specification to add

        Raises
        ------
        ValueError
            If the spec is invalid
        RuntimeError
            If the spec conflicts with existing specs
        """
        self.validate_specs(*self._specs, spec)
        self._specs.append(spec)

    def _generate_track_from_spec(
        self,
        track_file: os.PathLike | str,
        spec: Spec,
    ) -> Track:
        return Track(
            spec.num or self._next_track_num(),
            track_file,
            hue=spec.hue if spec.hue is not None else self.defaults["hue"],
            description=spec.description,
            use_album_art=spec.use_album_art
            if spec.use_album_art is not None
            else self.defaults["use_album_art"],
            license=spec.license_type or self.defaults["license"],
        )

    def __enter__(self):
        self.validate_specs(*self._specs, check_contiguous=True)
        self._unused: list[Spec] = [spec for spec in self._specs]
        self._assigned_track_numbers: list[int] = []
        return self

    def __exit__(self, *exc):
        try:
            unused_message = ""
            error = False
            for spec in self._unused:
                unused_message += (
                    f"\n- {os.fspath(spec.path_spec)}"
                    f" {'(required)' if spec.required else ''}"
                )
                if spec.required:
                    error = True
            if unused_message:
                unused_message = (
                    "The following required specs could not be matched to any files:"
                    + unused_message
                )
                if error:
                    raise RuntimeError(unused_message)
                else:
                    LOGGER.debug(unused_message)

            missing_track_numbers = tuple(
                i
                for i in range(_start_at, max(self._assigned_track_numbers or (-1,)))
                if i not in self._assigned_track_numbers
            )

            if missing_track_numbers:
                missing_message = (
                    "The following track numbers were never assigned:"
                    f" {missing_track_numbers}"
                )
                # then we've got a discontinuity
                if self.defaults["enforce_contiguous_track_numbers"] == "ignore":
                    LOGGER.debug(missing_message)
                elif self.defaults["enforce_contiguous_track_numbers"] in (
                    "warn",
                    "warning",
                ):
                    LOGGER.warning(missing_message)
                elif self.defaults["enforce_contiguous_track_numbers"] == "error":
                    raise RuntimeError(missing_message)
                else:
                    raise NotImplementedError(
                        "The specified method for enforcing track number contiguousness"
                        " is invalid or not currently implemented:"
                        f" '{self.defaults['enforce_contiguous_track_numbers']}"
                    )
        finally:
            del self._unused
            del self._assigned_track_numbers
        return False

    def _next_track_num(self) -> int:
        next_track_num = _start_at
        while (
            next_track_num in (spec.num for spec in self._unused)
            or next_track_num in self._assigned_track_numbers
        ):
            next_track_num += 1
        return next_track_num

    def __getitem__(self, track_file: os.PathLike | str) -> Track:
        if not hasattr(self, "_unused"):
            raise ValueError(
                "Improper use of a TrackBuilder: when building tracks, you must use"
                " the TrackBuilder as a context manager, i.e.,"
                "\n>>> with TrackBuilder(specs) as track_builder:"
                "\n...     track_one = track_builder[file_one]"
                "\n...     track_two = track_builder[file_two]"
            )
        for spec in self._unused:
            if utils.spec_matches_path(spec.path_spec, track_file):
                track = self._generate_track_from_spec(track_file, spec)
                self._unused.remove(spec)
                self._assigned_track_numbers.append(track.num)
                return track

        # not found? search all specs
        for spec in self._specs:
            # if spec not in self._unused:  # then it would have been caught above
            if utils.spec_matches_path(spec.path_spec, track_file):
                if spec.distinct:
                    raise RuntimeError(
                        f"The spec: '{os.fspath(spec.path_spec)}'"
                        f" matching the track '{os.fspath(track_file)}"
                        " was already used."
                    )
                elif not spec.distinct:
                    raise NotImplementedError(
                        "Multitrack specs are not currently supported"
                    )

        # still not found? then it's unSPECified
        if self.defaults["unspecified_file_handling"] == "use-defaults":
            LOGGER.debug(f"Using default spec for '{os.fspath(track_file)}'")
            return self._generate_track_from_spec(track_file, Spec(Path()))
        if self.defaults["unspecified_file_handling"] in ("warn", "warning"):
            LOGGER.warning(
                f"Could not find matching spec for '{os.fspath(track_file)}'."
                "Using default spec instead."
            )
            return self._generate_track_from_spec(track_file, Spec(Path()))
        if self.defaults["unspecified_file_handling"] == "error":
            raise KeyError(
                f"Could not find matching spec for '{os.fspath(track_file)}'"
            )
        raise NotImplementedError(
            "Unspecified file handling method"
            f" '{self.defaults['unspecified_file_handling']}' is invalid"
            " or is not yet implemented."
        )
