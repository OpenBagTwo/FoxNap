from importlib.resources import files
from typing import Any

PACK_ICON = files("foxnap_rpg.assets") / "pack.png"
RECORD_TEMPLATE = files("foxnap_rpg.assets") / "template_black.png"
COLORED_VINYL_TEMPLATE = files("foxnap_rpg.assets") / "template_colored_vinyl.png"

RP_MCMETA: str = r"""{
    "pack": {
        "pack_format": 31,
        "supported_formats": {"min_inclusive": 15, "max_inclusive": 999},
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

DP_MCMETA: str = r""" {
    "pack": {
        "pack_format": 45,
        "supported_formats": {"min_inclusive": 45, "max_inclusive": 999},
        "description": "%%title%%"
    }
}
"""

__all__ = ["PACK_ICON", "RECORD_TEMPLATE", "COLORED_VINYL_TEMPLATE", "MCMETA"]
