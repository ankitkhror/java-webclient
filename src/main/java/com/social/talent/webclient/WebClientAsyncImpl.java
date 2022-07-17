package com.social.talent.webclient;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.MediaType.APPLICATION_JSON;


final class WebClientAsyncImpl implements WebClientAsync {

    @Value("${api.baseurl}")
    private String BASE_URL;

    @Value("${api.timeout:5000}")
    public int TIMEOUT;

    private ObjectMapper jacksonDecoderMapper;
    private ObjectMapper jacksonEncoderMapper;
    private final ParserType parserType;

    public WebClientAsyncImpl(ParserType parserType) {
        this.parserType = parserType;
    }

    enum ParserType {JACKSON}

    @Override
    public <T> Mono<T> getForEntity(Class<T> clazz, Map<String, Set<String>> headers, String uri, Object... uriVariables) {
        return buildClient(headers)
                .get()
                .uri(uri, uriVariables)
                .retrieve()
                .bodyToMono(clazz);

    }

    @Override
    public <T> Flux<T> getForList(Class<T> clazz, Map<String, Set<String>> headers, String uri, Object... uriVariables) {
        return buildClient(headers)
                .get()
                .uri(uri, uriVariables)
                .retrieve()
                .bodyToFlux(clazz);
    }

    @Override
    public <T, R> Mono<T> postForEntity(Class<T> clazz, Map<String, Set<String>> headers, String uri, R body, Object... uriVariables) {
        return buildClient(headers)
                .post()
                .uri(uri, uriVariables)
                .body(body, clazz)
                .retrieve()
                .bodyToMono(clazz);

    }

    @Override
    public <T, R> Mono<T> putForEntity(Class<T> clazz, Map<String, Set<String>> headers, String uri, R body, Object... uriVariables) {
        return buildClient(headers)
                .put()
                .uri(uri, uriVariables)
                .body(body, clazz)
                .retrieve()
                .bodyToMono(clazz);

    }

    @Override
    public <T> Mono<T> delete(Class<T> clazz, Map<String, Set<String>> headers, String uri, Object... uriVariables) {
        return buildClient(headers)
                .delete()
                .uri(uri, uriVariables)
                .retrieve()
                .bodyToMono(clazz);
    }


    private WebClient buildClient(Map<String, Set<String>> headers) {
        final ExchangeStrategies strategies = getExchangeStrategies();

        final HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT)
                .responseTimeout(Duration.ofMillis(TIMEOUT))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS)));

        final WebClient.Builder builder = WebClient
                .builder()
                .exchangeStrategies(strategies)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(BASE_URL);

        headers.keySet().forEach(key -> builder.defaultHeader(key, headers.get(key).toArray(new String[0])));

        return builder.build();
    }

    private ExchangeStrategies getExchangeStrategies() {
        if (ParserType.JACKSON.equals(parserType)) {

            if (jacksonEncoderMapper == null) {
                jacksonEncoderMapper = new ObjectMapper();
                jacksonEncoderMapper.registerModule(new Jdk8Module());
            }

            if (jacksonDecoderMapper == null) {
                jacksonDecoderMapper = new ObjectMapper();
                jacksonDecoderMapper.registerModule(new Jdk8Module());
            }

            return ExchangeStrategies
                    .builder()
                    .codecs(codecConfigure -> {
                        final ClientCodecConfigurer.ClientDefaultCodecs defaultCodecs = codecConfigure.defaultCodecs();
                        defaultCodecs.jackson2JsonEncoder(new Jackson2JsonEncoder(jacksonEncoderMapper, APPLICATION_JSON));
                        defaultCodecs.jackson2JsonDecoder(new Jackson2JsonDecoder(jacksonDecoderMapper, APPLICATION_JSON));
                    }).build();
        } else {
            throw new RuntimeException("Unrecognized ParserType " + this.parserType);
        }

    }
}
