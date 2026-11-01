package org.routineslim.global.external.clova;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ClovaOcrProperties.class)
public class ClovaOcrConfig {
}
