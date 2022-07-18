package com.social.talent.webclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Configuration
public final class WebClientConfig {

    @Value("${api.baseurl}")
    private String BASE_URL;

    @Value("${api.timeout:5000}")
    public int TIMEOUT;

    private ObjectMapper jacksonDecoderMapper;
    private ObjectMapper jacksonEncoderMapper;
    private final ParserType parserType;

    public WebClientConfig(ParserType parserType) {
        this.parserType = parserType;
    }

    enum ParserType {JACKSON}


    @Bean
    public WebClient buildClient() {
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
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        return builder.build();
    }

    private ExchangeStrategies getExchangeStrategies() {
        if (WebClientConfig.ParserType.JACKSON.equals(parserType)) {

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
            throw new AppException("Unrecognized ParserType " + this.parserType);
        }

    }
}
