import os
from importlib.resources import files

ffmpeg: str = os.fspath(
    files("foxnap_rpg.bin") / "ffmpeg"  # type: ignore[call-overload]
)
ffprobe: str = os.fspath(
    files("foxnap_rpg.bin") / "ffprobe"  # type: ignore[call-overload]
)

__all__ = ["ffmpeg", "ffprobe"]
