package org.routineslim.domain.subscription.service;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.routineslim.domain.subscription.dto.SubscriptionScanResponse;
import org.routineslim.domain.subscription.entity.BillingCycle;
import org.routineslim.domain.subscription.repository.PresetCandidate;
import org.routineslim.domain.subscription.repository.SubscriptionRepository;
import org.routineslim.domain.subscription.scan.ParsedReceipt;
import org.routineslim.domain.subscription.scan.PresetMatcher;
import org.routineslim.domain.subscription.scan.ReceiptParser;
import org.routineslim.global.external.clova.ClovaOcrClient;
import org.routineslim.global.external.clova.ClovaOcrException;
import org.routineslim.global.external.clova.ClovaReceiptResponse;
import org.routineslim.global.util.BillingDateCalculator;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

/**
 * 영수증·결제 내역 이미지로 구독 등록 초안을 만든다.
 * 이미지는 CLOVA OCR 호출에만 쓰고 저장하지 않는다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceiptScanService {

    /** 확장자 → CLOVA format */
    private static final Map<String, String> FORMAT_BY_EXTENSION = Map.of(
            "jpg", "jpg", "jpeg", "jpeg", "png", "png", "pdf", "pdf");

    /** 파일 이름에 확장자가 없을 때 Content-Type 으로 판단 */
    private static final Map<String, String> FORMAT_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", "jpg", "image/png", "png", "application/pdf", "pdf");

    private static final HttpStatusCode UNPROCESSABLE = HttpStatusCode.valueOf(422);

    private final ClovaOcrClient clovaOcrClient;
    private final SubscriptionRepository subscriptionRepository;
    private final Clock clock;

    public SubscriptionScanResponse scan(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일(image)을 보내 주세요.");
        }
        String format = resolveFormat(image)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        "jpg, png, pdf 파일만 인식할 수 있어요."));

        ClovaReceiptResponse.Image ocrResult = callOcr(image, format);
        ParsedReceipt receipt = ReceiptParser.parse(ocrResult)
                .orElseThrow(() -> new ResponseStatusException(UNPROCESSABLE,
                        "영수증 내용을 읽지 못했어요. 더 선명한 이미지로 다시 시도하거나 직접 입력해 주세요."));

        Optional<PresetCandidate> preset =
                PresetMatcher.match(subscriptionRepository.findActivePresetCandidates(), receipt.matchTexts());

        LocalDate startDate = receipt.paidDate();
        LocalDate nextBillingDate = startDate == null ? null
                : BillingDateCalculator.firstOnOrAfter(startDate, BillingCycle.MONTHLY, 1,
                        startDate.getDayOfMonth(), LocalDate.now(clock));

        return new SubscriptionScanResponse(
                image.getOriginalFilename(),
                preset.map(PresetCandidate::getServiceName).orElse(receipt.storeName()),
                preset.map(PresetCandidate::getPresetId).orElse(null),
                preset.map(PresetCandidate::getCategoryId).orElse(null),
                receipt.amount(),
                BillingCycle.MONTHLY,
                startDate,
                nextBillingDate,
                preset.isPresent(),
                new SubscriptionScanResponse.Recognized(receipt.storeName(), receipt.itemNames())
        );
    }

    private ClovaReceiptResponse.Image callOcr(MultipartFile image, String format) {
        try {
            return clovaOcrClient.analyzeReceipt(image.getBytes(), format);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일을 읽지 못했어요.", e);
        } catch (ClovaOcrException e) {
            throw switch (e.getReason()) {
                case NOT_CONFIGURED -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "영수증 인식 기능이 아직 설정되지 않았어요.", e);
                case TIMEOUT -> new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,
                        "영수증 인식이 오래 걸리고 있어요. 잠시 후 다시 시도해 주세요.", e);
                case ERROR_RESPONSE -> new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "영수증 인식 서비스에 문제가 있어요. 잠시 후 다시 시도해 주세요.", e);
            };
        }
    }

    private static Optional<String> resolveFormat(MultipartFile image) {
        String fileName = image.getOriginalFilename();
        if (fileName != null && fileName.lastIndexOf('.') >= 0) {
            String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
            if (FORMAT_BY_EXTENSION.containsKey(extension)) {
                return Optional.of(FORMAT_BY_EXTENSION.get(extension));
            }
        }
        return Optional.ofNullable(image.getContentType())
                .map(type -> type.toLowerCase(Locale.ROOT))
                .map(FORMAT_BY_CONTENT_TYPE::get);
    }
}
