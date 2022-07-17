package com.social.talent.webclient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

public interface WebClientAsync {
    public <T> Mono<T> getForEntity(Class<T> clazz, Map<String, Set<String>> headers, String uri, Object... uriVariables);

    public <T> Flux<T> getForList(Class<T> clazz, Map<String, Set<String>> headers, String uri, Object... uriVariables);

    public <T, R> Mono<T> postForEntity(Class<T> clazz, Map<String, Set<String>> headers, String uri, R body, Object... uriVariables);

    public <T, R> Mono<T> putForEntity(Class<T> clazz, Map<String, Set<String>> headers, String uri, R body, Object... uriVariables);

    public <T> Mono<T> delete(Class<T> clazz, Map<String, Set<String>> headers, String uri, Object... uriVariables);
}
