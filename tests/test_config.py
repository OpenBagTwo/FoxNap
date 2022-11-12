"""Tests of config parsing"""
import csv
import json
import os
from pathlib import Path
from typing import Any, NamedTuple

import pytest

from foxnap_rpg.builder import Spec
from foxnap_rpg.config import read_specs_from_config_file
from foxnap_rpg.pack_generator import License


@pytest.fixture(autouse=True)
def fix_serializers(monkeypatch):
    def better_asdict(self: Spec) -> dict[str, Any]:
        as_dict: dict[str, Any] = {}
        for field in self._fields:
            if field == "path_spec":
                as_dict["path_spec"] = os.fspath(self.path_spec)
            elif field == "license_type" and self.license_type is not None:
                as_dict["license_type"] = self.license_type.name
            else:
                value = getattr(self, field)
                if value is not None:
                    as_dict[field] = value
        return as_dict

    monkeypatch.setattr(Spec, "_asdict", better_asdict)


class TestConfigParsing:
    @pytest.fixture
    def spec_list(self):
        yield [
            Spec(
                Path("hello"),
                True,
                License.UNRESTRICTED,
                False,
                "Saying Hello",
                1,
                False,
                False,
            ),
            Spec(
                Path("Music") / "axolotl_song.mp3",
                False,
                License.RESTRICTED,
                False,
                "This is the Axolotl Song",
            ),
            Spec(Path("basic.wav")),
        ]

    def test_parse_ini(self, tmp_path, spec_list):
        ini_as_str = ""
        for spec in spec_list:
            ini_as_str += f"[{spec.path_spec.name}]"
            for k, v in spec._asdict().items():
                ini_as_str += f"\n{k} = {v}"
            ini_as_str += "\n\n"
        with (tmp_path / "config.cfg").open("w") as config_file:
            config_file.write(ini_as_str)

        assert read_specs_from_config_file(tmp_path / "config.cfg") == spec_list

    def test_parse_json(self, tmp_path, spec_list):
        dict_list: list[dict] = []

        # a little bit of alternative field name testing
        for spec in spec_list:
            spec_dict = spec._asdict()
            spec_dict["filename"] = spec_dict.pop("path_spec")
            if "license_type" in spec_dict:
                spec_dict["usage"] = spec_dict.pop("license_type")
            dict_list.append(spec_dict)

        with (tmp_path / "config.json").open("w") as config_file:
            json.dump(dict_list, config_file)

        assert read_specs_from_config_file(tmp_path / "config.json") == spec_list

    @pytest.mark.parametrize("delimiter", (",", "\t"))
    def test_parse_csv(self, tmp_path, spec_list, delimiter):
        with (tmp_path / "config.csv").open("w", newline="") as config_file:
            writer = csv.DictWriter(
                config_file, fieldnames=Spec._fields, delimiter=delimiter
            )
            writer.writeheader()
            writer.writerows((spec._asdict() for spec in spec_list))

        assert read_specs_from_config_file(tmp_path / "config.csv") == spec_list
