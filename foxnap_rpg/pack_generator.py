"""Functionality for converting a selection of audio tracks into a resource pack"""

import io
import json
import logging
import os
import random
import shutil
from enum import IntEnum, auto
from pathlib import Path
from tempfile import NamedTemporaryFile, TemporaryDirectory
from typing import Any, NamedTuple

import ffmpeg
from PIL import Image

from . import assets, bin

LOGGER = logging.getLogger(__name__)


class License(IntEnum):
    """More properly, "permissions" for usage of the music track and, by extension
    the pack"""

    UNRESTRICTED = auto()
    """e.g. Public Domain, CC0 -- use anywhere, no attribution required"""

    ATTRIBUTION = auto()
    """e.g. CC-BY, CC-BY-SA, GPL -- use anywhere, but credit the artist"""

    RESTRICTED = auto()
    """e.g. CC-BY-NC, YouTube Audio Library License, most royalty-free music services:
    these are free to use in specific places, by specific people or under specific
    conditions
    """

    PERSONAL = auto()
    """most things in your private music library -- for personal use only"""


class Track(NamedTuple):
    """The specification of a track to be converted into a record

    Attributes
    ----------
    num : int
        the number of the track (need not be sequential, can overwrite  one of the
        default tracks)
    path : pathlike
        the path to the music track
    hue : bool or float, optional
        specification of the record texture template:
          - False: use the regular record template
          - True: use the colored vinyl template, with a random hue (default)
          - numeric value: use the specified hue shift, in degrees
    description: str, optional
        The name to give the track (in the language file). If None is specified, the
        name will be extracted from the track metadata
    use_album_art : bool, optional
        If True, the generator will attempt to extract album art from the track to use
        for the inlay of the record texture. If False, the track will always use a
        random inlay. Default is True.
    license : License, optional
        The permission level for use of the specified track. If None is specified,
        it will be assumed that the track is for PERSONAL use only.
    """

    num: int
    path: os.PathLike | str
    hue: bool | float = True
    description: str | None = None
    use_album_art: bool = True
    license: License = License.PERSONAL

    def __str__(self):
        return repr(self.description or os.fspath(self.path))


def generate_resource_pack(
    output_path: os.PathLike | str,
    *tracks: Track,
    title: str = "Custom FoxNap Records",
    license_summary: License | str | None = None,
    license_file: os.PathLike | str | None = None,
    title_color: str = "gold",
    license_color: str | None = None,
) -> None:
    """Generate a FoxNap resource pack!

    Parameters
    ----------
    output_path : pathlike
        The filename of the resource pack
    *tracks : Tracks
        The tracks to generate
    title : str, optional
        The title for the pack to be displayed on the resource pack loading screen.
        Default is "Custom FoxNap Records"
    license_summary : License or str, optional
        The usage summary to display on the resource pack loading screen. If using a
        License level enum, the level must be at least as restrictive as the most
        restrictively-licensed track. See notes.  If None is provided, the license
        level will be set to the most permissive level appropriate for the included
        tracks.
    license_file : pathlike, optional
        The path to a license or credits file to include with the resource pack (for
        compliance with the terms of attribution-style or restricted use licenses)
    title_color : str, optional
        The color code to use for the title text on the resource pack loading screen.
        Default is "gold"
    license_color : str, optional
        The color code to use for the usage summary on the resource pack loading screen.
        If None is provided, one will be selected automatically.

    Returns
    -------
    None

    Raises
    ------
    RuntimeError
      - If license_summary is set to License.ATTRIBUTION or License.RESTRICTED
        and no license_file is provided
      - If the license level specified is less restrictive than the license level
        for any of the provided tracks (this is not checked when license_summary
        is provided via a custom string)

    Notes
    -----
    - If the license summary is not explicitly provided and all provided tracks are
      permissively licensed (License.UNRESTRICTED), the license summary will state that
      the pack is licensed under CC0: https://creativecommons.org/publicdomain/zero/1.0/
    - If the license summary is not explicitly provided, and the minimum license level
      from the provided tracks is determined to be License.ATTRIBUTION or
      License.RESTRICTED, the license summary will *still* be set to LICENSE.PERSONAL
      if no license file is provided.
    """
    colored_vinyl_template = Image.open(assets.COLORED_VINYL_TEMPLATE)
    record_template = Image.open(assets.RECORD_TEMPLATE)
    json_opts: dict[str, Any] = {"indent": 2, "sort_keys": True}

    with TemporaryDirectory() as tmpdir:
        root = Path(tmpdir)

        if license_file:
            LOGGER.info(f"Copying in license file {repr(os.fspath(license_file))}")
            shutil.copy(license_file, root / Path(license_file).name)
        else:
            LOGGER.info("Skipping license file--none specified.")

        if isinstance(license_summary, License) or license_summary is None:
            license_level = license_summary or License.UNRESTRICTED
            non_compliance_report = ""
            for track in tracks:
                if track.license > license_level:
                    if license_summary is None:
                        license_level = track.license
                    else:
                        if track.license == License.ATTRIBUTION:
                            compliance_str = "requires an attribution license"
                        elif track.license == License.RESTRICTED:
                            compliance_str = "requires a restricted license"
                        elif track.license == License.PERSONAL:
                            compliance_str = "is for personal use only"
                        else:
                            raise NotImplementedError(
                                f"Unrecognized license type {track.license}"
                            )
                        non_compliance_report += f"\n - {track} {compliance_str}"

            if non_compliance_report:
                raise RuntimeError(
                    f"The selected license level ({license_summary})"
                    " is too permissive for the following tracks:"
                    f"{non_compliance_report}"
                )

            if license_file is None and license_level in (
                License.ATTRIBUTION,
                License.RESTRICTED,
            ):
                message = (
                    f"Cannot use {license_level} due to lack of a license file."
                    "\nEither provide a license file or select a different license."
                )
                if license_summary is None:
                    LOGGER.warning(message, RuntimeWarning)
                else:
                    raise RuntimeError(message)
                license_level = License.PERSONAL

            # this should do nothing if license_summary is not None
            LOGGER.info(f"Setting license level to {license_level}")
            license_summary = license_level

        LOGGER.info("Writing pack.mcmeta")
        with (root / "pack.mcmeta").open("w") as f:
            f.write(generate_mcmeta(title, license_summary, title_color, license_color))
        LOGGER.info("Copying pack icon")
        shutil.copy(assets.PACK_ICON, root / "pack.png")  # type: ignore[call-overload]

        foxnap_root = root / "assets" / "foxnap"
        foxnap_root.mkdir(parents=True, exist_ok=True)

        sounds = foxnap_root / "sounds"
        sounds.mkdir(exist_ok=True)
        LOGGER.info("Beginning music track conversion")
        for track in tracks:
            LOGGER.info(f"Converting {track}")
            convert_music_to_ogg(track.path, sounds / f"track_{track.num}.ogg")
        LOGGER.info("Music track conversion complete")

        LOGGER.info("Writing sound registry")
        with (foxnap_root / "sounds.json").open("w") as f:
            json.dump(
                generate_sound_registry(*(track.num for track in tracks)),
                f,
                **json_opts,
            )

        models = foxnap_root / "models" / "item"
        models.mkdir(parents=True, exist_ok=True)
        LOGGER.info("Writing record item model jsons")
        for track in tracks:
            with (models / f"track_{track.num}.json").open("w") as f:
                json.dump(generate_model(track.num), f, **json_opts)

        item_textures = foxnap_root / "textures" / "item"
        item_textures.mkdir(exist_ok=True, parents=True)
        LOGGER.info("Beginning record item texture generation")
        for track in tracks:
            LOGGER.info(f"Creating texture for {track}")
            inlay: Image.Image | None = None
            if track.use_album_art:
                LOGGER.info(f"Attempting to extract inlay from album art for {track}")
                inlay = extract_album_art(track.path)
                if inlay is None:
                    LOGGER.warning(f"Failed to extract album art for {track}")
            if inlay is None:
                LOGGER.info("Generating random inlay")
                inlay = generate_random_inlay()

            if track.hue is False:
                template = record_template
            else:
                hue_shift = None if track.hue is True else track.hue
                template = create_colored_vinyl(
                    colored_vinyl_template, hue_shift=hue_shift
                )

            record_texture = composite_record_texture(template, inlay)
            with (item_textures / f"track_{track.num}.png").open("wb") as f:
                record_texture.save(f, format="png")

        lang = foxnap_root / "lang"
        lang.mkdir(exist_ok=True)
        LOGGER.info("Writing language file")
        with (lang / "en_us.json").open("w") as f:
            json.dump(generate_lang_file(*tracks), f, **json_opts)

        output_path_as_str = str(output_path)
        if output_path_as_str.endswith(".zip"):
            output_path_as_str = output_path_as_str[:-4]
        LOGGER.info(
            "Compressing archive and saving as"
            f" {Path(output_path_as_str).with_suffix('.zip').absolute()}"
        )
        shutil.make_archive(output_path_as_str, "zip", root)


def generate_mcmeta(
    title: str,
    license_summary: License | str,
    title_color: str,
    license_color: str | None,
) -> str:
    """Fill out the pack.mcmeta template

    Parameters
    ----------
    title : str
        The resource pack title
    license_summary : License or str
        The resource pack license
    title_color : str
        The color for the title line
    license_color : str or None
        The color for the license line. If None is provided, this will be chosen to
        match the license.

    Returns
    -------
    str
        The filled-out mcmeta, ready to be written to file
    """
    if isinstance(license_summary, License):
        if license_summary == License.PERSONAL:
            license_summary = "For personal use only"
            license_color = license_color or "red"
        elif license_summary == License.RESTRICTED:
            license_summary = "For limited public use. See pack for details."
            license_color = license_color or "dark_purple"
        elif license_summary == License.ATTRIBUTION:
            license_summary = "For public use. See pack for terms and attribution."
            license_color = license_color or "aqua"
        elif license_summary == License.UNRESTRICTED:
            license_summary = "For public use. Licensed under CC0."
            license_color = license_color or "green"
        else:
            raise NotImplementedError("This license type is not supported")
    else:
        license_color = license_color or "white"
    return (
        assets.MCMETA.replace("%%title%%", title)
        .replace("%%title_color%%", title_color)
        .replace("%%license%%", license_summary)
        .replace("%%license_color%%", license_color)
    )


def convert_music_to_ogg(
    input_path: os.PathLike | str, output_path: os.PathLike | str
) -> None:
    converter = (
        ffmpeg.input(os.fspath(input_path))
        .audio.output(os.fspath(os.fspath(output_path)), acodec="libvorbis", ac=1)
        .overwrite_output()
    )
    LOGGER.debug(
        f"Converting using the following command: {' '.join(converter.compile())}"
    )
    converter.run(cmd=bin.ffmpeg, quiet=True)


def generate_sound_registry(*track_numbers: int) -> dict:
    """Generate the sound registry for all new tracks

    Parameters
    ----------
    *track_numbers : ints
        The numbers of the tracks to generate. Need not
        be sequential if, for example, we're overwriting
        one of the default tracks

    Returns
    -------
    dict
        The sound registry, all set to be written as json
    """
    sounds = {}
    for track in track_numbers:
        sounds[f"track_{track}"] = {
            "category": "record",
            "replace": True,
            "sounds": [f"foxnap:track_{track}"],
        }
    return sounds


def generate_model(track_number: int) -> dict:
    """Generate a model JSON for a new record

    Parameters
    ----------
    track_number : int
        The number of the track to generate

    Returns
    -------
    dict
        The model info, all set to be written as JSON
    """
    return {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": f"foxnap:item/track_{track_number}"},
    }


def create_colored_vinyl(
    template: Image.Image | None = None, hue_shift: float | None = None
) -> Image.Image:
    """Create a colored vinyl template (record texture with transparency for the
    center)

    Parameters
    ----------
    template : Image, optional
        The starting template image (RGBA format).
        If None is provided, the template will be loaded from file.
    hue_shift : float, optional
        The degrees to shift the hue. If None is provided,
        the hue will be shifted by a random value.

    Returns
    -------
    Image
        The 16x16 RGBA image for a colored vinyl template
    """
    if template is None:
        template = Image.open(assets.COLORED_VINYL_TEMPLATE)
    if hue_shift is None:
        hue_shift = 360.0 * random.random()

    dh = int(256 * hue_shift // 360)

    hsv = template.convert("HSV")

    accesser = hsv.load()

    for i in range(hsv.size[0]):
        for j in range(hsv.size[1]):
            h, s, v = accesser[i, j]
            accesser[i, j] = ((h + dh) % 256, s, v)

    new_template = hsv.convert("RGB")
    new_template.putalpha(template.getchannel("A"))
    return new_template


def extract_album_art(track: os.PathLike | str) -> Image.Image | None:
    """Extract the album art from an audio track, if the track has album art encoded.

    Parameters
    ----------
    track: pathlike
        path to the track

    Returns
    -------
    Image or None
        the album art embedded in the audio track, downscaled to 5x3, or None if the
        track didn't have any album art embedded
    """
    track_path = os.fspath(track)
    metadata = ffmpeg.probe(track_path, cmd=bin.ffprobe)
    if "video" not in (stream["codec_type"] for stream in metadata["streams"]):
        return None
    with NamedTemporaryFile(mode="w+b", suffix=".png") as cover:
        ffmpeg.input(track_path).video.output(
            os.fspath(cover.name)
        ).overwrite_output().run(bin.ffmpeg, quiet=True)

        # move it to in-memory buffer so tempfile can be deleted
        album_art = Image.open(cover.name)
        buffer = io.BytesIO()
        album_art.save(buffer, format="png")

    return Image.open(buffer).resize((5, 3), resample=0)


def generate_random_inlay() -> Image.Image:
    """Generate a random 5x3 image

    Returns
    -------
    Image
        A random 5x3 image that can
        be used as an inlay
    """
    inlay = Image.new("HSV", (5, 3))
    accesser = inlay.load()

    for i in range(inlay.size[0]):
        for j in range(inlay.size[1]):
            # max saturation and brightness, random hue
            accesser[i, j] = (random.randint(0, 255), 255, 255)

    return inlay.convert("RGB")


def composite_record_texture(template: Image.Image, inlay: Image.Image) -> Image.Image:
    """Combine a texture template and an inlay into a final record texture

    Parameters
    ----------
    template: Image
        16x16 image (with transparency) to use as the template
    inlay: Image
        5x3 image to use as the inlay

    Returns
    -------
    Image
        The 16x16 RGBA image to use as the final record texture
    """
    record = Image.new("RGBA", (16, 16))
    record.paste(inlay, (5, 6))
    record.paste(template, (0, 0), mask=template)
    return record


def generate_lang_file(*tracks: Track) -> dict[str, str]:
    """Generate the language file for all new tracks

    Parameters
    ----------
    *tracks : Tracks
        The tracks that we will be including in the resource pack (and thus need
        language file entries)

    Returns
    -------
    dict
        The language file, all set to be written as json
    """
    lang: dict[str, str] = {}
    for track in tracks:
        lang[f"item.foxnap.track_{track.num}"] = "Music Disc"
        description = track.description or extract_track_description(track.path)
        lang[f"item.foxnap.track_{track.num}.desc"] = description
    return lang


def extract_track_description(track_path: os.PathLike | str) -> str:
    """Extract a description from an audio track, if the track
    has metadata encoded

    Parameters
    ----------
    track_path: pathlike
        path to the track

    Returns
    -------
    str
        A description of the track (comprising title, artist, composer, etc.)
        if such information was encoded, or just the filename otherwise.
    """
    metadata = ffmpeg.probe(os.fspath(track_path), cmd=bin.ffprobe)
    track_info = metadata.get("format", {}).get("tags", {})
    title = track_info.get("title")
    artist = track_info.get("artist")
    composer = track_info.get("composer")

    if title is None:
        return Path(track_path).name

    if artist is None and composer is None:
        return title

    if artist == composer or composer is None:
        return f"{artist} - {title}"

    if artist is None:
        return f"{composer} - {title}"

    return f"{artist} - {title} ({composer})"
