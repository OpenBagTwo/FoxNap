"""Functionality for converting a selection of audio tracks into a resource pack"""

import io
import json
import os
import random
import shutil
from pathlib import Path
from tempfile import NamedTemporaryFile, TemporaryDirectory
from typing import NamedTuple

import ffmpeg
from PIL import Image

from . import assets


class Track(NamedTuple):
    """The specification of a track to be converted into a record

    Attributes
    ----------
    num : int
        the number of the track (need not be sequential, can overwrite  one of the
        default tracks)
    path : pathlike
         the path to the music track
    colored : bool
        if True, make the item texture a colored vinyl (if False, use the black
        record template)
    """

    num: int
    path: os.PathLike | str
    colored: bool = True


def generate_resourcepack(output_path: os.PathLike | str, *tracks: Track) -> None:
    """Generate a FoxNap resource pack!

    Parameters
    ----------
    output_path : pathlike
        The filename of the resourcepack
    *tracks : Tracks
        The tracks to generate

    Returns
    -------
    None
    """
    colored_vinyl_template = Image.open(assets.COLORED_VINYL_TEMPLATE)
    record_template = Image.open(assets.RECORD_TEMPLATE)

    with TemporaryDirectory() as tmpdir:
        root = Path(tmpdir)

        with (root / "pack.mcmeta").open("w") as f:
            json.dump(assets.MCMETA, f)
        shutil.copy(assets.PACK_ICON, root / "icon.png")  # type: ignore[call-overload]

        foxnap_root = root / "assets" / "foxnap"
        foxnap_root.mkdir(parents=True, exist_ok=True)

        sounds = foxnap_root / "sounds"
        sounds.mkdir(exist_ok=True)
        for track in tracks:
            ffmpeg.input(os.fspath(track.path)).audio.output(
                os.fspath(sounds / f"track_{track.num}.ogg"), acodec="libvorbis", ac=1
            ).overwrite_output().run()

        with (foxnap_root / "sounds.json").open("w") as f:
            json.dump(generate_sound_registry(*(track[0] for track in tracks)), f)

        models = foxnap_root / "models" / "item"
        models.mkdir(parents=True, exist_ok=True)
        for track in tracks:
            with (models / f"track_{track.num}.json").open("w") as f:
                json.dump(generate_model(track.num), f)

        textures = foxnap_root / "textures"
        textures.mkdir(exist_ok=True)
        for track in tracks:
            inlay = extract_album_art(track.path)
            if inlay is None:
                inlay = generate_random_inlay()
            else:
                inlay = inlay.resize((5, 3), resample=0)
            if track.colored:
                template = create_colored_vinyl(colored_vinyl_template)
            else:
                template = record_template
            record = Image.new("RGBA", (16, 16))
            record.paste(inlay, (5, 6))
            record.paste(template, (0, 0), mask=template)
            with (textures / f"record_{track.num}.png").open("wb") as f:
                record.save(f, format="png")

        lang = foxnap_root / "lang"
        lang.mkdir(exist_ok=True)
        with (lang / "en_us.json").open("w") as f:
            json.dump(generate_lang_file(*tracks), f)

        output_path_as_str = str(output_path)
        if output_path_as_str.endswith(".zip"):
            output_path_as_str = output_path_as_str[:-4]
        shutil.make_archive(output_path_as_str, "zip", root)


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
        The RGBA image for a colored vinyl template
    """
    if template is None:
        template = Image.open(assets.COLORED_VINYL_TEMPLATE)
    if hue_shift is None:
        hue_shift = 256.0 * random.random()

    hsv = template.convert("HSV")

    accesser = hsv.load()

    for i in range(hsv.size[0]):
        for j in range(hsv.size[1]):
            h, s, v = accesser[i, j]
            accesser[i, j] = (int(h + hue_shift) % 256, s, v)

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
        the album art embedded in the audio track, or None if the
        track didn't have any album art embedded
    """
    track_path = os.fspath(track)
    metadata = ffmpeg.probe(track_path)
    if "video" not in (stream["codec_type"] for stream in metadata["streams"]):
        return None
    with NamedTemporaryFile(mode="w+b", suffix=".png") as cover:
        ffmpeg.input(track_path).video.output(
            os.fspath(cover.name)
        ).overwrite_output().run()

        # move it to in-memory buffer so tempfile can be deleted
        album_art = Image.open(cover.name)
        buffer = io.BytesIO()
        album_art.save(buffer, format="png")

        return Image.open(buffer)


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
        sounds[f"track_{track}"] = {"category": "record", "sounds": [f"record_{track}"]}
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
        "textures": {"layer0": f"record_{track_number}"},
    }


def generate_lang_file(*tracks: Track) -> dict[str, str]:
    """Generate the language file for all new tracks

    Parameters
    ----------
    *tracks : Tracks
        The tracks to generate entries for.

    Returns
    -------
    dict
        The language file, all set to be written as json
    """
    lang: dict[str, str] = {}
    for track in tracks:
        lang[f"item.foxnap.track_{track.num}"] = "Music Disc"
        lang[f"item.foxnap.track_{track.num}.desc"] = extract_track_description(
            track.path
        )
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
    metadata = ffmpeg.probe(os.fspath(track_path))
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
