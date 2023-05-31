"""Run integration tests on built executable"""
import argparse
import subprocess
from pathlib import Path

import yaml


def test_version(executable: Path) -> None:
    """Test that the version string is not '0+unknown'

    Parameters
    ----------
    executable : path
        the path to the executable to test

    Raises
    ------
    AssertionError
        If the version string is '0+unknown', indicating that the versioneer-managed
        auto-version failed to freeze, or if running the executable returned a nonzero
        error code
    """
    result = subprocess.run(
        [executable.absolute(), "--version"],
        capture_output=True,
        encoding="utf-8",
        cwd=executable.parent,
    )

    message = f"FoxNapRPG --version returned exit code {result.returncode}"
    assert result.returncode == 0, message

    message = "FoxNapRPG --version did not generate the expected version string"
    assert result.stdout.startswith("FoxNapRPG v"), message

    message = "FoxNapRPG versioning failed to freeze"
    assert "unknown" not in result.stdout, message

    message = "FoxNapRPG executable was generated with uncommitted code"
    assert "dirty" not in result.stdout, message


def test_resource_pack_generation(executable: Path, expected_mod_config: dict) -> None:
    """Test actually generating a resource pack

    Parameters
    ----------
    executable : path
        the path to the executable to test
    expected_mod_config : dict
        the expected values that should be read from the resulting mod config file

    Raises
    ------
    AssertionError
        If resourcep ack generation fails to complete successfully or if one of the
        expected outputs is not present (or is wrong)
    FileNotFoundError
        If, explicitly, foxnap.yaml cannot be found
    yaml.parser.ParserError
        If, explicitly, foxnap.yaml cannot be parsed
    """
    working_directory = executable.parent
    log_path = working_directory / "integration_test.log"
    with log_path.open("w") as log_file:
        result = subprocess.run(
            [executable.absolute()],
            stdout=log_file,
            stderr=subprocess.STDOUT,
            encoding="utf-8",
            cwd=executable.parent,
        )

    message = f"FoxNapRPG returned exit code {result.returncode}"
    assert result.returncode == 0, message

    assert (working_directory / "FoxNapRP.zip").exists()

    # more of a meta-test
    assert (working_directory / "integration_test.log").exists()

    with (working_directory / "foxnap.yaml").open() as mod_config_file:
        mod_config = yaml.load(mod_config_file, Loader=yaml.SafeLoader)

    message = (
        "Generated mod config did not match expected."
        f"\nExpected: {expected_mod_config}"
        f"\nGenerated: {mod_config}"
    )
    # remove any non-shared keys
    mod_config = {k: v for k, v in mod_config.items() if k in expected_mod_config}
    assert mod_config == expected_mod_config, message


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "executable",
        help="the path of the Resource Pack Generator executable to test",
        type=Path,
    )
    parser.add_argument(
        "n_discs",
        help="the expected value of the n_discs setting in the resulting mod config",
        type=int,
    )
    args = parser.parse_args()

    test_version(args.executable)
    test_resource_pack_generation(args.executable, {"n_discs": args.n_discs})
