package khai.api_gateway.service;

import khai.api_gateway.dto.ApiResponse;
import khai.api_gateway.dto.request.IntrospectRequest;
import khai.api_gateway.dto.response.IntrospectResponse;
import khai.api_gateway.repository.IdentityClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdentityService {
    IdentityClient identityClient;

    public Mono<ApiResponse<IntrospectResponse>> Introspect(String token) {
        return identityClient.introspect(IntrospectRequest.builder()
                .token(token)
                .build());
    }
}
