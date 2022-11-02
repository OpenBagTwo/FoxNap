from importlib.resources import files
from typing import Any

PACK_ICON = files("foxnap_rpg.assets") / "icon.png"
RECORD_TEMPLATE = files("foxnap_rpg.assets") / "template_black.png"
COLORED_VINYL_TEMPLATE = files("foxnap_rpg.assets") / "template_colored_vinyl.png"

MCMETA: dict[str, Any] = {
    "pack": {
        "pack_format": 10,
        "description": [
            {"text": "Custom FoxNap Records", "color": "aqua"},
            {"text": "\nGenerated using FoxNapRPG", "color": "green"},
        ],
    }
}

__all__ = ["PACK_ICON", "RECORD_TEMPLATE", "COLORED_VINYL_TEMPLATE", "MCMETA"]
