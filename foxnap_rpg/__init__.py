from . import _version
from .pack_generator import Track, generate_resource_pack

__version__ = _version.get_versions()["version"]

_start_at = 8

__all__ = [
    "generate_resource_pack",
    "Track",
    "__version__",
]
