package khai.api_gateway.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import khai.api_gateway.dto.ApiResponse;
import khai.api_gateway.service.IdentityService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerResponse;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {
    IdentityService identityService;
    ObjectMapper objectMapper;

    @NonFinal
    public String[] PUBLIC_ENDPOINTS = {
            "/identity/auth/.*",
            "/identity/users/registration",
            "/file/media/.*"
    };

    @NonFinal
    @Value("${app.api-prefix}")
    private String apiPrefix;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        log.info("Enter authentication filter....");
        if (isPublicEndpoint(exchange.getRequest()))
            return chain.filter(exchange);
        // Get token from authentication header

        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeader))
            return unauthenticated(exchange.getResponse());

        String token = authHeader.getFirst().replace("Bearer ", "");
        log.info("Token: {}" + token);
        // Verify token
        // Delegate identity_service
        return identityService.Introspect(token).flatMap(introspectResponseApiResponse -> {
            if (introspectResponseApiResponse.getResult().isValid())
                return chain.filter(exchange);
            else
                return unauthenticated(exchange.getResponse());
            // Chan luon request neu co loi khac trong qua trinh request
        }).onErrorResume(throwable -> unauthenticated(exchange.getResponse()));
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        return Arrays.stream(PUBLIC_ENDPOINTS)
                .anyMatch(s -> request.getURI().getPath().matches(apiPrefix + s));
    }

    @Override
    public int getOrder() {
        return -1;
    }

    Mono<Void> unauthenticated(ServerHttpResponse httpServerResponse) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(1401)
                .message("Unauthenticated")
                .build();

        String body = null;
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        httpServerResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
        httpServerResponse.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return httpServerResponse
                .writeWith(Mono.just(httpServerResponse.bufferFactory()
                        .wrap(body.getBytes())));
    }
}
