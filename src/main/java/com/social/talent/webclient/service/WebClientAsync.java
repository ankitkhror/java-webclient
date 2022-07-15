package com.social.talent.webclient.service;


import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class WebClientAsync {
    private final WebClient webClient;

    public Mono<Object> getObjectByIdAsync(final String uri) {
        return webClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Object>() {});
    }

    public Flux<Object> getObjectsByIdAsync(final String uri) {
        return webClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Object>() {});
    }
}
