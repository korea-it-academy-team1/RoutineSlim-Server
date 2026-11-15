package org.routineslim.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() throws IOException {

        // Firebase가 이미 초기화되었다면 중복 실행 방지
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        // 환경 변수에서 JSON 파일 경로 가져오기
        String firebaseKeyPath = System.getenv("FIREBASE_CREDENTIALS_PATH");

        // JSON 파일 경로가 설정되지 않았다면 초기화 건너뛰기
        if (firebaseKeyPath == null || firebaseKeyPath.isBlank()) {
            log.info("Firebase 인증 정보가 없어 초기화를 건너뜁니다.");
            return;
        }

        // Firebase 서비스 계정 JSON 파일 읽기
        try (FileInputStream serviceAccount =
                     new FileInputStream(firebaseKeyPath)) {

            // Firebase 인증 정보 설정
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // Firebase 초기화
            FirebaseApp.initializeApp(options);

            log.info("Firebase 초기화 완료");
        }
    }
}