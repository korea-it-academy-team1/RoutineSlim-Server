package org.routineslim.domain.subscription.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.routineslim.domain.subscription.dto.SubscriptionCreateRequest;
import org.routineslim.domain.subscription.dto.SubscriptionResponse;
import org.routineslim.domain.subscription.dto.SubscriptionScanResponse;
import org.routineslim.domain.subscription.dto.SubscriptionStatusRequest;
import org.routineslim.domain.subscription.dto.SubscriptionUpdateRequest;
import org.routineslim.domain.subscription.entity.SubscriptionStatus;
import org.routineslim.domain.subscription.service.ReceiptScanService;
import org.routineslim.domain.subscription.service.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
/**
 * 구독 API.
 * 인증(JWT)이 붙기 전까지는 X-Member-Id 헤더(회원 UUID)로 회원을 구분한다.
 * TODO 인증 구현 후 헤더 대신 로그인 정보에서 회원 ID를 꺼내도록 바꾼다.
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private static final String MEMBER_HEADER = "X-Member-Id";
    private final SubscriptionService subscriptionService;
    private final ReceiptScanService receiptScanService;

    @GetMapping
    public List<SubscriptionResponse> getSubscriptions(
            @RequestHeader(MEMBER_HEADER) UUID memberId,
            @RequestParam(name = "status", required = false) SubscriptionStatus status) {
        return subscriptionService.getSubscriptions(memberId, status);
    }

    @GetMapping("/{id}")
    public SubscriptionResponse getSubscription(
            @RequestHeader(MEMBER_HEADER) UUID memberId,
            @PathVariable("id") UUID subscriptionId) {
        return subscriptionService.getSubscription(memberId, subscriptionId);
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(
            @RequestHeader(MEMBER_HEADER) UUID memberId,
            @Validated @RequestBody SubscriptionCreateRequest request) {
        SubscriptionResponse response = subscriptionService.create(memberId, request);
        return ResponseEntity.created(URI.create("/api/v1/subscriptions/" + response.id())).body(response);
    }
    /**
     * 영수증·결제 내역 이미지로 등록 초안을 만든다 (저장하지 않음).
     * multipart/form-data, 파일 필드 이름은 image. jpg, png, pdf, 최대 10MB.
     */
    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SubscriptionScanResponse scan(
            @RequestHeader(MEMBER_HEADER) UUID memberId,
            @RequestPart("image") MultipartFile image) {
        return receiptScanService.scan(image);
    }

    @PutMapping("/{id}")
    public SubscriptionResponse update(
            @RequestHeader(MEMBER_HEADER) UUID memberId,
            @PathVariable("id") UUID subscriptionId,
            @Validated @RequestBody SubscriptionUpdateRequest request) {
        return subscriptionService.update(memberId, subscriptionId, request);
    }

    @PatchMapping("/{id}/status")
    public SubscriptionResponse changeStatus(
            @RequestHeader(MEMBER_HEADER) UUID memberId,
            @PathVariable("id") UUID subscriptionId,
            @Validated @RequestBody SubscriptionStatusRequest request) {
        return subscriptionService.changeStatus(memberId, subscriptionId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader(MEMBER_HEADER) UUID memberId,
            @PathVariable("id") UUID subscriptionId) {
        subscriptionService.delete(memberId, subscriptionId);
    }
}
