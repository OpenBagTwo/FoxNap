"""Tests of the validation utils"""
import random

import pytest

from foxnap_rpg import utils

# TODO: ffmpeg tests


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
