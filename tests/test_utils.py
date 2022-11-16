"""Tests of the validation utils"""
import os
import random
from copy import deepcopy
from itertools import product
from pathlib import Path

import pytest

from foxnap_rpg import utils

# TODO: ffmpeg tests


class TestSpecMatchesPath:
    @staticmethod
    def munge_path(
        path: os.PathLike | str | tuple[str, ...], how: str
    ) -> os.PathLike | str | tuple[str, ...]:
        """Convert a given path into a different format"""
        if isinstance(path, tuple):
            path = utils._reassemble_path(*path)
        if how == "to_string":
            return os.fspath(path)
        elif how == "to_path":
            return Path(path)
        elif how == "to_parts":
            return utils._part_out_path(path)
        else:
            raise NotImplementedError

    @pytest.mark.parametrize("munge", ("to_string", "to_path", "to_parts"))
    @pytest.mark.parametrize("which", ("spec", "file"))
    @pytest.mark.parametrize(
        "spec, file",
        product(
            (
                "ello",
                "hello",
                "world.ogg",
                "world.mp3",
                "Album/01 track.mp3",
                Path.home() / "Music" / "Best Album Ever" / "01 Best Song Ever.aac",
                (".m4a", "song", "album", "artist", "collection"),
            ),
            repeat=2,
        ),
    )
    def test_simple_equality_comparison(self, spec, file, which, munge):
        args = {"spec": deepcopy(spec), "file": deepcopy(file)}

        args[which] = self.munge_path(args[which], how=munge)

        assert utils.spec_matches_path(args["spec"], args["file"]) == (spec == file)

    @pytest.mark.parametrize("munge", ("to_string", "to_path", "to_parts"))
    @pytest.mark.parametrize("start_level", range(1, 4))
    def test_spec_matches_against_path_tail(self, start_level, munge):
        path_parts = (Path.home(), "Music", "Best Album Ever", "01 Best Song Ever.aac")

        full_path = Path(path_parts[0])
        short_path = Path(path_parts[start_level])
        for level, path_part in enumerate(path_parts):
            full_path /= path_part
            if level > start_level:
                short_path /= path_part
        full_path = os.fspath(full_path)
        short_path = os.fspath(short_path)

        assert utils.spec_matches_path(
            self.munge_path(short_path, how=munge), full_path
        )

    @pytest.mark.parametrize("munge", ("to_string", "to_path", "to_parts"))
    @pytest.mark.parametrize("with_folder", (False, True))
    def test_extensionless_spec_matching(self, with_folder, munge):
        spec = Path("world")
        if with_folder:
            spec = Path("folder") / spec
        assert utils.spec_matches_path(
            self.munge_path(spec, how=munge), Path("folder") / "world.mp3"
        )


class TestValidateTrackNumbers:
    @pytest.fixture(autouse=True)
    def lock_number_of_built_in_tracks(self, monkeypatch):
        monkeypatch.setattr(utils, "_start_at", 4)

    def test_empty_range_is_trivially_valid(self):
        utils.validate_track_numbers(check_contiguous=True)

    def test_range_of_natural_numbers_raise_no_problems(self):
        utils.validate_track_numbers(*range(1, 10000), check_contiguous=True)

    def test_validator_is_order_agnostic(self):
        # random in a test suite--even if I were to set the seed--is generally no bueno,
        # but it's preferable for this test case than building my own
        # platform-consistent PRNG
        nums = list(range(1, 10000))
        random.shuffle(nums)
        utils.validate_track_numbers(*nums, check_contiguous=True)

    def test_nones_are_ignored_for_basic_checks(self):
        utils.validate_track_numbers(
            None, 7, 14, None, 3, None, 12, None, check_contiguous=False
        )

    @pytest.mark.parametrize("invalid_nums", ((-5,), (0,), (-2, -3)))
    def test_raise_if_range_contains_non_natural_numbers(self, invalid_nums):
        expected = r"(.|\s)*".join(
            rf"({num} is not a valid)" for num in sorted(invalid_nums)
        )
        with pytest.raises(ValueError, match=expected):
            utils.validate_track_numbers(
                92, 14, *invalid_nums, 6, check_contiguous=False
            )

    def test_raise_if_range_contains_dupes(self):
        expected = r"(.|\s)*".join(
            (
                r"4 is included 2 times",
                r"16 is included 3 times",
                r"38 is included 2 times",
            )
        )
        with pytest.raises(RuntimeError, match=expected):
            utils.validate_track_numbers(
                24, 38, 16, 16, 16, 9, 15, 21, 38, 4, 4, check_contiguous=False
            )

    def test_raise_if_track_numbers_are_missing(self):
        with pytest.raises(
            RuntimeError, match=r"No tracks are specified with numbers \(4, 5\)"
        ):
            utils.validate_track_numbers(6, 8, 7, check_contiguous=True)

    @pytest.mark.parametrize("n_wildcards", (1, 2))
    def test_raise_if_number_of_missing_nums_exceeds_number_of_wildcards(
        self, n_wildcards
    ):
        expected = r"(\s|.)*".join(
            (
                r"No tracks are specified with numbers \(4, 5, 9\)",
                rf"only {n_wildcards}",
            )
        )

        with pytest.raises(RuntimeError, match=expected):
            utils.validate_track_numbers(
                6,
                *[
                    None,
                ]
                * n_wildcards,
                8,
                7,
                10,
                check_contiguous=True,
            )


class TestValidateTrackFileSpecs:
    def test_empty_list_is_trivially_valid(self):
        utils.validate_track_file_specs()

    def test_non_conflicting_set_of_specs_produces_no_error(self):
        utils.validate_track_file_specs(
            "hello",
            "world.mp3",
            "ello",
            os.fspath(
                Path.home() / "Music" / "Best Album Ever" / "01 Best Song Ever.aac"
            ),
        )

    def test_raise_when_specs_contain_literal_dupes(self):
        expected = r"(.|\s)*".join(
            (
                r"'hello' is included 2 times",
                r"'world.mp3' is included 3 times",
            )
        )

        with pytest.raises(RuntimeError, match=expected):
            utils.validate_track_file_specs(
                "world.mp3",
                "blah.aac",
                "hello",
                "world.mp3",
                "hello",
                "world.mp3",
                os.fspath(
                    Path.home() / "Music" / "Best Album Ever" / "01 Best Song Ever.aac"
                ),
            )

    @pytest.mark.parametrize("start_level", range(1, 4))
    def test_raise_when_specs_include_path_ambiguity(self, start_level):
        path_parts = (Path.home(), "Music", "Best Album Ever", "01 Best Song Ever.aac")

        full_path = Path(path_parts[0])
        short_path = Path(path_parts[start_level])
        for level, path_part in enumerate(path_parts):
            full_path /= path_part
            if level > start_level:
                short_path /= path_part
        full_path = os.fspath(full_path)
        short_path = os.fspath(short_path)

        expected = f"'{full_path}' would also match any files matching '{short_path}'"

        with pytest.raises(RuntimeError, match=expected):
            utils.validate_track_file_specs(full_path, short_path)

    def test_raise_when_theres_a_conflict_with_extensionless_files(self):
        expected = rf"'hello.m4a' would also match any files matching 'hello'"

        with pytest.raises(RuntimeError, match=expected):
            utils.validate_track_file_specs("hello", "hello.m4a")

    def test_raise_on_conflict_with_extensionless_files_with_path_ambiguity(self):
        expected = rf"'Music/hello.m4a' would also match any files matching 'hello'"

        with pytest.raises(RuntimeError, match=expected):
            utils.validate_track_file_specs("hello", "Music/hello.m4a")

    def test_ignore_possible_conflicts_by_default(self):
        utils.validate_track_file_specs("Music/hello", "hello.m4a")

    def test_raise_on_possible_conflict_when_strict(self):
        expected = rf"'Music/hello' may also match files matching 'hello.m4a'"

        with pytest.raises(RuntimeError, match=expected):
            utils.validate_track_file_specs("Music/hello", "hello.m4a", strict=True)
