from . import _version
from .builder import Spec, TrackBuilder
from .config import read_specs_from_config_file
from .pack_generator import Track, generate_resourcepack

__version__ = _version.get_versions()["version"]

_start_at = 7

__all__ = [
    "generate_resourcepack",
    "read_specs_from_config_file",
    "Spec",
    "Track",
    "TrackBuilder",
    "__version__",
]
