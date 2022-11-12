"""Just a quick test of internal consistency"""
import re
from pathlib import Path

from foxnap_rpg import _start_at

java_config_class = (
    Path("src")
    / "main"
    / "java"
    / "net"
    / "openbagtwo"
    / "foxnap"
    / "config"
    / "Config.java"
)


def test_starts_at_is_up_to_date():
    with java_config_class.open() as java_file:
        for line in java_file:
            if match := re.match(r"(?:.*\"n_discs\", )([0-9]*)", line):
                mod_n_discs = int(match.group(1))
                break
        else:
            raise RuntimeError("Could not find disc number in Config.java")

    # because there is no track 0
    assert mod_n_discs + 1 == _start_at
