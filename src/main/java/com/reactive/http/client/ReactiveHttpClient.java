package com.reactive.http.client;

import com.reactive.http.config.HttpClientConfig;
import com.reactive.http.exception.HttpRequestFailedException;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public class ReactiveHttpClient {
    //This is a reactive http client

    private final WebClient httpClient;

    public ReactiveHttpClient(HttpClientConfig httpClientConfig) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, httpClientConfig.getConnectionTimeoutInMillis())
                .responseTimeout(Duration.ofMillis(httpClientConfig.getResponseTimeoutInMillis()))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(httpClientConfig.getReadTimeOutInMillis(), TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(httpClientConfig.getWriteTimeoutInMillis(), TimeUnit.MILLISECONDS)));

        this.httpClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    private Function<ClientResponse, Mono<? extends Throwable>> getErrorResponseHandler() {
        return clientResponse -> Mono.just(new HttpRequestFailedException(String.format("Client returned %d status code", clientResponse.statusCode().value())));
    }

    private Predicate<HttpStatus> getErrorStatusPredicate() {
        return httpStatusCode -> httpStatusCode.is4xxClientError() || httpStatusCode.is5xxServerError();
    }

    public <T> Mono<T> doGet(String url, Map<String, String> headers, Class<T> responseClass) throws HttpRequestFailedException {
        return buildRequestWithoutBody(HttpMethod.GET, url, headers)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(getErrorStatusPredicate(), getErrorResponseHandler())
                .bodyToMono(responseClass);
    }

    public <T> Mono<T> doPostWithFormParam(String url, Map<String, String> headers, MultiValueMap<String, String> formParam,
                                           Class<T> responseClass) throws HttpRequestFailedException {
        return buildRequestWithoutBody(HttpMethod.POST, url, headers)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromFormData(formParam))
                .retrieve().onStatus(getErrorStatusPredicate(), getErrorResponseHandler())
                .bodyToMono(responseClass);
    }


    public <T> Mono<T> doPostWithMultipart(String url, Map<String, String> headers, MultiValueMap<String, HttpEntity<?>> multipart,
                                           Class<T> responseClass) throws HttpRequestFailedException {
        return buildRequestWithoutBody(HttpMethod.POST, url, headers)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromMultipartData(multipart))
                .retrieve().onStatus(getErrorStatusPredicate(), getErrorResponseHandler())
                .bodyToMono(responseClass);
    }

    public <T, R> Mono<T> doPostWithRequestBody(String url, Map<String, String> headers, R requestBody, Class<T> responseClass) {
        return buildRequestWithoutBody(HttpMethod.POST, url, headers)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve().onStatus(getErrorStatusPredicate(), getErrorResponseHandler())
                .bodyToMono(responseClass);
    }

    private WebClient.RequestBodySpec buildRequestWithoutBody(HttpMethod httpMethod, String url, Map<String, String> headers) {
        return httpClient.method(httpMethod).uri(url).headers((httpHeaders) -> {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpHeaders.add(header.getKey(), header.getValue());
            }
        });
    }
}
