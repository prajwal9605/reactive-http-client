package com.reactive.http.config;

import lombok.Data;

@Data
public class HttpClientConfig {
    private int connectionTimeoutInMillis = 10000;
    private int responseTimeoutInMillis = 10000;
    private int readTimeOutInMillis = 10000;
    private int writeTimeoutInMillis = 2000;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final HttpClientConfig httpClientConfig;

        Builder() {
            this.httpClientConfig = new HttpClientConfig();
        }

        public Builder connectionTimeoutInMillis(int connectionTimeoutInMillis) {
            httpClientConfig.setConnectionTimeoutInMillis(connectionTimeoutInMillis);
            return this;
        }

        public Builder responseTimeoutInMillis(int responseTimeoutInMillis) {
            httpClientConfig.setResponseTimeoutInMillis(responseTimeoutInMillis);
            return this;
        }

        public Builder readTimeOutInMillis(int readTimeOutInMillis) {
            httpClientConfig.setReadTimeOutInMillis(readTimeOutInMillis);
            return this;
        }

        public Builder writeTimeoutInMillis(int writeTimeoutInMillis) {
            httpClientConfig.setWriteTimeoutInMillis(writeTimeoutInMillis);
            return this;
        }

        public HttpClientConfig build() {
            return httpClientConfig;
        }
    }
}
