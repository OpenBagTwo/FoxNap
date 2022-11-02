from . import _version
from .pack_generator import generate_resourcepack

__version__ = _version.get_versions()["version"]

__all__ = ["generate_resourcepack", "__version__"]
