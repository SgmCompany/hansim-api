package com.sgm.hansimapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3_000);   // 3초: 서버 연결 대기
        factory.setReadTimeout(10_000);     // 10초: 응답 데이터 수신 대기
        return new RestTemplate(factory);
    }

    /**
     * 배치 소환사 조회용 스레드 풀.
     * 소환사 최대 10명을 동시에 처리하므로 corePoolSize를 10으로 설정합니다.
     */
    @Bean
    public Executor batchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("batch-riot-");
        executor.initialize();
        return executor;
    }
}