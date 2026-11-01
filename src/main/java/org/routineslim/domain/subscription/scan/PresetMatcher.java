package org.routineslim.domain.subscription.scan;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.routineslim.domain.subscription.repository.PresetCandidate;

/**
 * 영수증 글자(상호, 품목)를 서비스 프리셋과 맞춰 본다.
 * 대소문자·공백·기호를 무시하고, 영수증 글자에 프리셋 이름이나 별칭이 들어 있으면 맞은 것으로 본다.
 * 여러 개가 맞으면 더 긴 이름(더 구체적인 쪽)을 고른다.
 */
public final class PresetMatcher {

    /**
     * 영수증에 영문·다른 표기로 찍히는 이름.
     * TODO service_preset 에 별칭 컬럼(aliases)을 추가하면 이 표를 DB로 옮긴다.
     */
    private static final Map<String, List<String>> ALIASES = Map.of(
            "넷플릭스", List.of("netflix"),
            "디즈니+", List.of("disney", "디즈니플러스"),
            "YouTube Premium", List.of("youtube", "유튜브"),
            "스포티파이", List.of("spotify"),
            "iCloud+", List.of("icloud", "아이클라우드"),
            "쿠팡 와우", List.of("와우멤버십", "wow멤버십", "쿠팡", "coupang")
    );

    /** 이보다 짧은 이름은 우연히 겹치기 쉬워서 매칭에 쓰지 않는다 */
    private static final int MIN_KEYWORD_LENGTH = 2;

    private PresetMatcher() {
    }

    public static Optional<PresetCandidate> match(List<PresetCandidate> presets, List<String> receiptTexts) {
        for (String text : receiptTexts) {
            String normalizedText = normalize(text);
            if (normalizedText.isEmpty()) {
                continue;
            }
            PresetCandidate best = null;
            int bestLength = 0;
            for (PresetCandidate preset : presets) {
                for (String keyword : keywords(preset)) {
                    if (keyword.length() >= MIN_KEYWORD_LENGTH && keyword.length() > bestLength
                            && normalizedText.contains(keyword)) {
                        best = preset;
                        bestLength = keyword.length();
                    }
                }
            }
            if (best != null) {
                return Optional.of(best); // 상호명에서 맞으면 품목은 보지 않는다
            }
        }
        return Optional.empty();
    }

    /** 소문자로 바꾸고 영문·숫자·한글만 남긴다 ("NETFLIX.COM" → "netflixcom", "디즈니+" → "디즈니") */
    static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9가-힣]", "");
    }

    private static List<String> keywords(PresetCandidate preset) {
        List<String> keywords = new ArrayList<>();
        keywords.add(normalize(preset.getServiceName()));
        ALIASES.getOrDefault(preset.getServiceName(), List.of()).stream()
                .map(PresetMatcher::normalize)
                .forEach(keywords::add);
        return keywords;
    }
}
