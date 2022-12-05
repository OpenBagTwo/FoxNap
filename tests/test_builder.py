"""Tests of the track builder"""
import logging
from pathlib import Path

import pytest

from foxnap_rpg import utils
from foxnap_rpg.builder import Spec, TrackBuilder
from foxnap_rpg.pack_generator import Track


@pytest.fixture
def lock_built_in_track_count(monkeypatch):
    """Just so we don't have to rewrite tests every mod update"""
    monkeypatch.setattr(utils, "BUILT_IN_DISC_COUNT", 4)


class TestInstantiation:
    def test_no_args_or_kwargs_are_required_to_init_a_builder(self):
        _ = TrackBuilder()

    def test_specs_are_an_empty_list_by_default(self):
        """Just because we're gonna be relying on this a lot for testing purposes"""

        assert TrackBuilder()._specs == []

    def test_creating_a_builder_with_one_spec(self):
        assert TrackBuilder(Spec(Path("hello")))._specs[0].path_spec == Path("hello")

    def test_creating_a_builder_with_multiple_spec(self):
        track_builder = TrackBuilder(
            Spec(Path("hello")), Spec(Path("i love you.mp3"), num=4)
        )
        assert [spec.path_spec for spec in track_builder._specs] == [
            Path("hello"),
            Path("i love you.mp3"),
        ]

    def test_builders_instantiate_with_defaults(self):
        assert TrackBuilder().defaults.keys() == {
            "required",
            "hue",
            "use_album_art",
            "license",
            "unspecified_file_handling",
            "enforce_contiguous_track_numbers",
            "strict_file_checking",
        }

    @pytest.mark.parametrize(
        "option",
        (
            "required",
            "hue",
            "use_album_art",
            "license",
            "unspecified_file_handling",
            "enforce_contiguous_track_numbers",
            "strict_file_checking",
        ),
    )
    def test_builder_defaults_can_be_overridden_via_kwargs(self, option):
        assert TrackBuilder(**{option: "override"}).defaults[option] == "override"

    @pytest.mark.parametrize("unnatural_number", (3.14, -1, 0))
    def test_start_at_must_be_a_natural_number(self, unnatural_number):
        with pytest.raises(TypeError):
            _ = TrackBuilder(start_at=unnatural_number)


class TestValidateSpecs:
    def test_invalid_nums_raise_value_error(self):
        with pytest.raises(ValueError, match="invalid"):
            TrackBuilder().validate_specs(Spec(Path("hello.mp3"), num=-3))

    def test_contiguousness_is_not_validated_by_default(self):
        TrackBuilder().validate_specs(Spec(Path("hello.mp3"), num=12))

    def test_non_contiguousness_will_raise_if_checked_for(self):
        with pytest.raises(RuntimeError, match="missing"):
            TrackBuilder().validate_specs(
                Spec(Path("hello.mp3"), num=12), check_contiguous=True
            )

    def test_duplicate_track_numbers_will_always_raise(self):
        with pytest.raises(RuntimeError, match="duplicates"):
            TrackBuilder().validate_specs(
                Spec(Path("hello.mp3"), num=12), Spec(Path("i love you.mp3"), num=12)
            )

    def test_conflicting_paths_raise_runtime_error(self):
        with pytest.raises(RuntimeError, match="Files matching"):
            TrackBuilder().validate_specs(Spec(Path("hello.mp3")), Spec(Path("hello")))

    def test_possibly_conflicting_paths_do_not_raise_if_builder_is_not_strict(self):
        TrackBuilder(strict_file_checking=False).validate_specs(
            Spec(Path("hello.mp3")), Spec(Path("i say") / "hello")
        )

    def test_possibly_conflicting_path_raise_if_builder_is_strict(self):
        with pytest.raises(RuntimeError, match="may also match"):
            TrackBuilder(strict_file_checking=True).validate_specs(
                Spec(Path("hello.mp3")), Spec(Path("i say") / "hello")
            )

    def test_non_distinct_specs_are_not_currently_implemented(self):
        with pytest.raises(NotImplementedError):
            TrackBuilder().validate_specs(Spec(Path("hello"), distinct=False))


class TestInstantiationSpecValidation:
    def tests_specs_are_validated_on_instantiation(self):
        with pytest.raises(RuntimeError, match="Files matching"):
            TrackBuilder(Spec(Path("hello.mp3")), Spec(Path("hello")))

    def test_specs_are_not_checked_for_contiguousness_on_instantiation(self):
        TrackBuilder(Spec(Path("hello.mp3"), num=12))


class TestAddSpec:
    @pytest.fixture
    def track_builder(self):
        yield TrackBuilder(Spec(Path("hello.mp3"), num=4))

    def test_new_specs_are_added_to_the_spec_list(self, track_builder):
        track_builder.add_spec(Spec(Path("i love you.mp3")))
        assert Path("i love you.mp3") in [
            spec.path_spec for spec in track_builder._specs
        ]

    def test_new_specs_are_checked_for_conflicts_when_added(self, track_builder):
        with pytest.raises(RuntimeError, match="Files matching"):
            track_builder.add_spec(Spec(Path("hello")))

    def test_new_specs_are_checked_for_duplicate_nums(self, track_builder):
        with pytest.raises(RuntimeError, match="duplicates"):
            track_builder.add_spec(Spec(Path("i love you.mp3"), num=4))

    def test_new_specs_are_not_checked_for_contiguousness(self, track_builder):
        track_builder.add_spec(Spec(Path("i love you.mp3"), num=7))

    def test_a_failed_validation_will_abort_the_add(self, track_builder):
        try:
            track_builder.add_spec(Spec(Path("i love you.mp3"), num=4))
            raise AssertionError("This should not have succeeded")
        except RuntimeError:
            pass

        assert Path("i love you.mp3") not in [
            spec.path_spec for spec in track_builder._specs
        ]


class TestBuildingTracks:
    @pytest.fixture
    def track_builder(self, lock_built_in_track_count):
        yield TrackBuilder(
            Spec(Path("hello.mp3"), num=4, required=True, use_album_art=False),
            Spec(Path("i love you.mp3"), required=False),
            use_album_art=True,
            unspecified_file_handling="use-defaults",
            enforce_contiguous_track_numbers="error",
        )

    def test_track_builder_sets_attributes_from_spec(self, track_builder):
        with track_builder:
            track = track_builder[Path.home() / "Music" / "hello.mp3"]

        assert (track.num, track.use_album_art, track_builder.n_discs) == (4, False, 4)

    def test_track_builder_errors_on_multi_matches(self, track_builder):
        with track_builder:
            _ = track_builder[Path.home() / "Music" / "hello.mp3"]
            with pytest.raises(RuntimeError, match="already used"):
                _ = track_builder[Path.home() / "Music" / "hello.mp3"]

        assert track_builder.n_discs == 4

    def test_track_builder_falls_back_to_builder_defaults(self, track_builder):
        track_builder.defaults["hue"] = 87

        with track_builder:
            track = track_builder[Path.home() / "Music" / "hello.mp3"]

        assert track.hue == 87

    def test_track_builder_auto_numbering_starts_at_one(self):
        with TrackBuilder() as track_builder:
            track_one = track_builder[Path.home() / "Music" / "first_track.mp3"]
            track_two = track_builder[Path.home() / "Music" / "second_track.mp3"]
            track_three = track_builder[Path.home() / "Music" / "third_track.mp3"]

        assert (
            track_one.num,
            track_two.num,
            track_three.num,
            track_builder.n_discs,
        ) == (1, 2, 3, 3)

    def test_track_builder_can_start_numbering_higher(self, lock_built_in_track_count):
        with TrackBuilder(start_at=4) as track_builder:
            track_one = track_builder[Path.home() / "Music" / "first_track.mp3"]
            track_two = track_builder[Path.home() / "Music" / "second_track.mp3"]
            track_three = track_builder[Path.home() / "Music" / "third_track.mp3"]

        assert (
            track_one.num,
            track_two.num,
            track_three.num,
            track_builder.n_discs,
        ) == (4, 5, 6, 6)

    def test_track_builder_auto_numbering_skips_reserved_numbers(self, track_builder):
        track_builder.start_at = 4
        with track_builder:
            track = track_builder[Path.home() / "Music" / "i love you.mp3"]
            _ = track_builder[Path.home() / "Music" / "hello.mp3"]

        assert (track.num, track_builder.n_discs) == (5, 5)

    def test_track_builder_re_validates_on_enter(self, track_builder):
        track_builder._specs.append(Spec(Path("Music/hello.mp3")))

        with pytest.raises(RuntimeError, match="would also match"):
            with track_builder:
                raise AssertionError("Should not be reached")
            raise AssertionError("Definitely should not be reached")

    def test_track_builder_raises_on_exit_if_a_required_spec_goes_unused(
        self, track_builder
    ):
        # to avoid contiguousness check
        track_builder._specs.append(Spec(Path("Music/world.aac"), required=True))

        with pytest.raises(RuntimeError, match=r"(world.aac).*(required)"):
            with track_builder:
                track_1 = track_builder[Path.home() / "Music" / "i love you.mp3"]
                track_4 = track_builder[Path.home() / "Music" / "hello.mp3"]

        # to check explicitly that the raise is on exit
        assert (track_4.num, track_1.num, track_builder.n_discs) == (4, 1, 4)

    def test_track_builder_checks_for_skipped_track_numbers_on_exit(
        self, track_builder, lock_built_in_track_count
    ):
        # explicitly not required
        track_builder.add_spec(Spec(Path("Music/world.aac"), num=5))
        track_builder.start_at = 4

        with pytest.raises(RuntimeError, match="5"):
            with track_builder:
                track = track_builder[Path.home() / "Music" / "i love you.mp3"]
                _ = track_builder[Path.home() / "Music" / "hello.mp3"]

        # to check explicitly that the raise is on exit
        assert (track.num, track_builder.n_discs) == (6, 6)

    def test_track_builder_doesnt_consider_built_in_tracks_to_be_missing(
        self, track_builder, lock_built_in_track_count
    ):
        # explicitly not required
        track_builder.add_spec(Spec(Path("Music/world.aac"), num=2))

        with track_builder:
            track_1 = track_builder[Path.home() / "Music" / "i love you.mp3"]
            track_4 = track_builder[Path.home() / "Music" / "hello.mp3"]

        # to check explicitly that the raise is on exit
        assert (track_1.num, track_4.num, track_builder.n_discs) == (1, 4, 4)

    def test_skipped_track_numbers_can_be_set_to_warn_instead(
        self, track_builder, caplog, lock_built_in_track_count
    ):
        caplog.set_level(logging.WARNING)

        track_builder.add_spec(Spec(Path("Music/world.aac"), num=5))
        track_builder.defaults["enforce_contiguous_track_numbers"] = "warn"
        track_builder.start_at = 4

        with track_builder:
            track = track_builder[Path.home() / "Music" / "i love you.mp3"]
            _ = track_builder[Path.home() / "Music" / "hello.mp3"]

        assert (track.num, track_builder.n_discs) == (6, 6)

        assert len(caplog.record_tuples) == 1
        assert "5" in caplog.record_tuples[0][2]

    def test_skipped_track_numbers_can_be_ignored_instead(
        self, track_builder, caplog, lock_built_in_track_count
    ):
        caplog.set_level(logging.WARNING)

        track_builder.add_spec(Spec(Path("Music/world.aac"), num=5))
        track_builder.defaults["enforce_contiguous_track_numbers"] = "ignore"
        track_builder.start_at = 4

        with track_builder:
            track = track_builder[Path.home() / "Music" / "i love you.mp3"]
            _ = track_builder[Path.home() / "Music" / "hello.mp3"]

        assert (track.num, track_builder.n_discs) == (6, 6)

        assert len(caplog.record_tuples) == 0

    def test_unspecified_input_files_get_track_builder_default_values(
        self, track_builder, caplog, lock_built_in_track_count
    ):
        caplog.set_level(logging.DEBUG)
        track_builder.defaults["hue"] = 87

        with track_builder:
            track = track_builder[Path.home() / "Music" / "what a wonderful world.m4a"]
            _ = track_builder[Path.home() / "Music" / "hello.mp3"]

        assert (track.num, track.use_album_art, track.hue) == (1, True, 87)
        assert track_builder.n_discs == 4

        assert len(caplog.record_tuples) >= 1
        assert any(("default spec" in record[2] for record in caplog.record_tuples))

    def test_unspecified_input_files_can_be_set_to_warn(
        self, track_builder, caplog, lock_built_in_track_count
    ):
        caplog.set_level(logging.WARNING)
        track_builder.defaults["hue"] = 87
        track_builder.defaults["unspecified_file_handling"] = "warn"

        with track_builder:
            track = track_builder[Path.home() / "Music" / "what a wonderful world.m4a"]
            _ = track_builder[Path.home() / "Music" / "hello.mp3"]

        assert (track.num, track.use_album_art, track.hue) == (1, True, 87)
        assert track_builder.n_discs == 4

        assert len(caplog.record_tuples) == 1
        assert "default spec" in caplog.record_tuples[0][2]

    def test_unspecified_input_files_can_be_set_to_error(self, track_builder):
        track_builder.defaults["unspecified_file_handling"] = "error"

        with track_builder:
            _ = track_builder[Path.home() / "Music" / "hello.mp3"]
            with pytest.raises(KeyError, match="Could not find matching spec for"):
                _ = track_builder[Path.home() / "Music" / "what a wonderful world.m4a"]

        assert track_builder.n_discs == 4

    def test_track_builder_increments_track_number(self, track_builder):
        tracks: list[Track] = []
        with track_builder:
            tracks.append(track_builder[Path.home() / "Music" / "hello.mp3"])
            tracks.append(track_builder[Path.home() / "Music" / "bonjour.mp3"])
            tracks.append(track_builder[Path.home() / "Music" / "hola.mp3"])
            tracks.append(track_builder[Path.home() / "Music" / "shalom.mp3"])

        assert (track_builder.n_discs, [track.num for track in tracks]) == (
            4,
            [4, 1, 2, 3],
        )

    def test_track_builder_outside_context_raises_helpful_error(self, track_builder):
        with pytest.raises(ValueError, match=r"(context manager)(.|\s)*(with)"):
            _ = track_builder[Path.home() / "Music" / "hello.mp3"]

    def test_n_discs_before_track_builder_use_raises_helpful_error(self, track_builder):
        with pytest.raises(ValueError, match=r"yet to be used"):
            _ = track_builder.n_discs

    def test_track_builders_can_be_reused(self, track_builder):
        track_ones = []
        for _ in range(100):
            with track_builder:
                track_ones.append(
                    track_builder[Path.home() / "Music" / "i love you.mp3"]
                )
                _ = track_builder[Path.home() / "Music" / "hello.mp3"]

        assert all((track.num == 1 for track in track_ones))
        assert track_builder.n_discs == 4
