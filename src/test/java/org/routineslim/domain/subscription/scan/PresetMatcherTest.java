package org.routineslim.domain.subscription.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.routineslim.domain.subscription.repository.PresetCandidate;

class PresetMatcherTest {

    private static final List<PresetCandidate> PRESETS = List.of(
            preset(1L, 1L, "넷플릭스"),
            preset(2L, 1L, "디즈니+"),
            preset(3L, 1L, "YouTube Premium"),
            preset(4L, 2L, "스포티파이"),
            preset(5L, 3L, "iCloud+"),
            preset(6L, 4L, "쿠팡 와우"));

    @Test
    void 영문_상호를_별칭으로_찾는다() {
        assertEquals(1L, match("NETFLIX.COM").orElseThrow().getPresetId());
        assertEquals(4L, match("Spotify AB").orElseThrow().getPresetId());
        assertEquals(5L, match("Apple iCloud+ 50GB").orElseThrow().getPresetId());
    }

    @Test
    void 한글_이름과_기호_공백을_무시한다() {
        assertEquals(2L, match("디즈니 플러스").orElseThrow().getPresetId());
        assertEquals(6L, match("쿠팡(주) 와우 멤버십").orElseThrow().getPresetId());
    }

    @Test
    void 상호에서_못_찾으면_품목에서_찾는다() {
        assertEquals(3L, PresetMatcher.match(PRESETS, List.of("Apple", "YouTube Premium 개인")).orElseThrow()
                .getPresetId());
    }

    @Test
    void 맞는_프리셋이_없으면_비어있다() {
        assertTrue(match("**마트").isEmpty());
    }

    private static java.util.Optional<PresetCandidate> match(String text) {
        return PresetMatcher.match(PRESETS, List.of(text));
    }

    private static PresetCandidate preset(Long presetId, Long categoryId, String serviceName) {
        return new PresetCandidate() {
            @Override
            public Long getPresetId() {
                return presetId;
            }

            @Override
            public Long getCategoryId() {
                return categoryId;
            }

            @Override
            public String getServiceName() {
                return serviceName;
            }
        };
    }
}
