"""Logic for parsing config files into spec lists"""

import csv
import json
import os
from configparser import ConfigParser
from pathlib import Path
from typing import Any

from .builder import Spec
from .pack_generator import License


def read_specs_from_config_file(config_path: str | os.PathLike) -> list[Spec]:
    """Read in a list of track specs from a config file

    Parameters
    ----------
    config_path : pathlike
        The path to the config file.

    Returns
    -------
    list of Specs
        The Specs parsed from the config file

    Notes
    -----
    - The following config formats are currently supported:
      - INI (.ini, .cfg, .config, .conf)
      - JSON (.json)
      - CSV (.csv, .tsv)
    - This method will attempt to read files ending in no extension or .txt as INI.
    """
    config_path = Path(config_path)
    if config_path.suffix.lower() in (".ini", ".cfg", ".config", ".conf", ".txt", ""):
        parser = _parse_ini
    elif config_path.suffix.lower() in (".json",):
        parser = _parse_json
    elif config_path.suffix.lower() in (".csv", ".tsv"):
        parser = _parse_csv

    try:
        spec_dicts = parser(config_path)
    except Exception as base_exception:
        raise ValueError(
            f"Could not parse '{os.fspath(config_path)}'"
        ) from base_exception

    spec_list: list[Spec] = []
    for i, spec_dict in enumerate(spec_dicts):
        try:
            spec_list.append(_convert_dict_to_spec(spec_dict))
        except Exception as base_exception:
            raise ValueError(
                f"Could not parse entry {i + 1} from '{os.fspath(config_path)}'"
            ) from base_exception
    return spec_list


def _parse_ini(config_path: Path) -> list[dict[str, Any]]:
    parser = ConfigParser()
    parser.read(config_path)
    return [dict(**parser[section], header=section) for section in parser.sections()]


def _parse_json(config_path: Path) -> list[dict[str, Any]]:
    with config_path.open() as json_file:
        raw_config = json.load(json_file)
    if isinstance(raw_config, list):
        return raw_config
    if isinstance(raw_config, dict):
        return [dict(**entry, header=key) for key, entry in raw_config.items()]
    raise ValueError(f"Could not parse '{os.fspath(config_path)}'")


def _parse_csv(config_path: Path) -> list[dict[str, Any]]:
    with config_path.open() as csv_file:
        dialect = csv.Sniffer().sniff(csv_file.read())
        csv_file.seek(0)
        reader = csv.DictReader(csv_file, dialect=dialect)
        return [row for row in reader]


def _convert_dict_to_spec(as_dict: dict[str, Any]) -> Spec:
    spec_fields = {field_name: None for field_name in Spec._fields}

    # TODO: error if k.lower() results in duplicate keys
    as_dict = {k.lower(): v for k, v in as_dict.items() if _check_none(v) is not None}

    def normalize(spec_field: str, *possible_key_names: str):
        for key_name in (spec_field, *possible_key_names):
            for key in {
                key_name,
                key_name.replace("_", "-"),
                key_name.replace("_", " "),
                key_name.replace("_", ""),
            }:
                if key in as_dict:
                    spec_fields[spec_field] = as_dict[key]
                    break
            else:
                continue
            break

    normalize("path_spec", "file_spec", "filename", "header")
    if spec_fields["path_spec"] is None:
        raise ValueError("entry does not specify a path spec")
    spec_fields["path_spec"] = Path(spec_fields["path_spec"])

    normalize("distinct", "unique", "is_distinct", "is_unique")
    try:
        spec_fields["distinct"] = _normalize_boolean(spec_fields["distinct"])
    except ValueError:
        raise ValueError(
            f"entry has invalid value for distinct: '{spec_fields['distinct']}'"
        )

    normalize("license_type", "license", "permission", "usage", "usage_rights")
    if spec_fields["license_type"] is not None:
        try:
            spec_fields["license_type"] = License[spec_fields["license_type"].upper()]
        except KeyError:
            raise ValueError(
                "entry has invalid value for license_type:"
                " '{spec_fields['license_type']}'"
            )

    normalize("required", "is_required")
    try:
        spec_fields["required"] = _normalize_boolean(spec_fields["required"])
    except ValueError:
        raise ValueError(
            f"entry has invalid value for required: '{spec_fields['required']}'"
        )

    normalize("description", "title", "desc")

    normalize("num", "number", "track_number", "track", "track_no")
    if spec_fields["num"] is not None:
        try:
            spec_fields["num"] = int(spec_fields["num"])
        except (TypeError, ValueError):
            raise ValueError(f"entry has invalid value for num: '{spec_fields['num']}'")

    normalize("hue", "hue_shift", "use_colored_vinyl")
    if isinstance(spec_fields["hue"], str):
        try:
            spec_fields["hue"] = _normalize_boolean(spec_fields["hue"])
        except ValueError:
            try:
                spec_fields["hue"] = float(spec_fields["hue"])
            except (TypeError, ValueError):
                raise ValueError(
                    "entry has invalid value for hue: '{spec_fields['hue']}'"
                )

    if spec_fields["hue"] is not None and not isinstance(spec_fields["hue"], bool):
        try:
            spec_fields["hue"] = float(spec_fields["hue"])
        except (TypeError, ValueError):
            raise ValueError(f"entry has invalid value for hue: '{spec_fields['hue']}'")

    normalize("use_album_art", "extract_album_art")
    try:
        spec_fields["use_album_art"] = _normalize_boolean(spec_fields["use_album_art"])
    except ValueError:
        raise ValueError(
            "entry has invalid value for use_album_art:"
            f" '{spec_fields['use_album_art']}'"
        )

    return Spec(
        **{field: value for field, value in spec_fields.items() if value is not None}
    )


def _check_none(value: Any) -> Any | None:
    try:
        if value == -1 or value.upper() in ("NULL", "", "NAN", "NONE"):
            return None
    except (AttributeError, TypeError, SyntaxError):
        pass
    return value


def _normalize_boolean(value: Any) -> bool | None:
    try:
        value = int(value)
    except (ValueError, TypeError):
        pass

    try:
        value = value.upper()
    except (AttributeError, SyntaxError):
        pass

    if value is None:
        return None

    if value == 1:
        return True
    if value == 0:
        return False
    if value == -1:
        return None

    if isinstance(value, str):
        if value.upper() in ("T", "TRUE", "YES", "Y"):
            return True
        if value.upper() in ("F", "FALSE", "NO", "N"):
            return False

    raise ValueError(f"invalid value for boolean field: '{value}'")
