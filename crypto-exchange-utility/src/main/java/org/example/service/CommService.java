package org.example.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Service
public class CommService {

    @Value("${coingecko.base-url:https://api.coingecko.com/api/v3}")
    private String baseUrl;
    @Value("${coingecko.api-key}")
    private String apiKey; //customization points: should be fed from secrets, etc.
    private WebClient webClient;

    private static final Logger log = LoggerFactory.getLogger(CommService.class);

    @PostConstruct
    void initialize() {
        log.info("api key: {}", apiKey);
        log.info("base url: {}", baseUrl);

        //copypasted alleged workaround for connection reset issues - seems to be working
        ConnectionProvider provider = ConnectionProvider.builder("fixed")
                .maxConnections(500)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120)).build();

        webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-cg-api-key", apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create(provider)))
                .filter(logRequest())
                .build();
    }

    //bit more verbose request logging
    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("{} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("{} = {}", name, value)));
            return Mono.just(clientRequest);
        });
    }

    public String callSimplePrice(String id, String filtersMergedToCommaSeparatedString) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/simple/price")
                        .queryParam("ids", id)
                        .queryParam("vs_currencies", filtersMergedToCommaSeparatedString)
                        .build())
                .retrieve().bodyToMono(String.class).block();
    }
}
