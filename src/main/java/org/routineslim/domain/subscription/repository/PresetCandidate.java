package org.routineslim.domain.subscription.repository;

/** 영수증 매칭용 프리셋 정보 (네이티브 쿼리 결과를 받는 인터페이스) */
public interface PresetCandidate {

    Long getPresetId();

    Long getCategoryId();

    String getServiceName();
}
