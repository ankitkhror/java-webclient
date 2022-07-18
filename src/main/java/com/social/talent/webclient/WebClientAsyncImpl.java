package com.social.talent.webclient;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
final class WebClientAsyncImpl implements WebClientAsync {

    @Autowired
    private WebClient buildClient;


    @Override
    public <T> Mono<T> getForEntity(Class<T> clazz, String uri, Object... uriVariables) {
        return buildClient
                .get()
                .uri(uri, uriVariables)
                .retrieve()
                .bodyToMono(clazz);

    }

    @Override
    public <T> Flux<T> getForList(Class<T> clazz, String uri, Object... uriVariables) {
        return buildClient
                .get()
                .uri(uri, uriVariables)
                .retrieve()
                .bodyToFlux(clazz);
    }

    @Override
    public <T, R> Mono<T> postForEntity(Class<T> clazz, String uri, R body, Object... uriVariables) {
        return buildClient
                .post()
                .uri(uri, uriVariables)
                .body(body, clazz)
                .retrieve()
                .bodyToMono(clazz);

    }

    @Override
    public <T, R> Mono<T> putForEntity(Class<T> clazz, String uri, R body, Object... uriVariables) {
        return buildClient
                .put()
                .uri(uri, uriVariables)
                .body(body, clazz)
                .retrieve()
                .bodyToMono(clazz);

    }

    @Override
    public <T> Mono<T> delete(Class<T> clazz, String uri, Object... uriVariables) {
        return buildClient
                .delete()
                .uri(uri, uriVariables)
                .retrieve()
                .bodyToMono(clazz);
    }


}
