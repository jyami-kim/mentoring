package com.jyami.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ScheduleConfig {

    private static final Logger log = LoggerFactory.getLogger(ScheduleConfig.class);

    @Scheduled(cron = "${scheduler.cron}")
    public void scheduleIndexDataSync(@Value("${scheduler.enabled}") boolean enabled) {
        if (!enabled) return;

        // 지수 설정 조회
        // 각 지수의 마지막 연동 날짜 ~ 오늘까지의 정보 open-api에서 조회
        // open-api 호출 > 데이터 파싱 > 저장
        log.info("time : {}", LocalDateTime.now());
    }

}
