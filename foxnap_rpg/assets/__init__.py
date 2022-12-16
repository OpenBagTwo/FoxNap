from importlib.resources import files
from typing import Any

PACK_ICON = files("foxnap_rpg.assets") / "pack.png"
RECORD_TEMPLATE = files("foxnap_rpg.assets") / "template_black.png"
COLORED_VINYL_TEMPLATE = files("foxnap_rpg.assets") / "template_colored_vinyl.png"

MCMETA: str = r"""{
  "pack": {
    "pack_format": 12,
    "description": [
      {
        "text": "%%title%%",
        "color": "%%title_color%%"
      },
      {
        "text": "\n%%license%%",
        "color": "%%license_color%%"
      }
    ]
  }
}
"""

__all__ = ["PACK_ICON", "RECORD_TEMPLATE", "COLORED_VINYL_TEMPLATE", "MCMETA"]
