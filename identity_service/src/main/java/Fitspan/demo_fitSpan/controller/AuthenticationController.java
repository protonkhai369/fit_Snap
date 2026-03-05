package Fitspan.demo_fitSpan.controller;

import Fitspan.demo_fitSpan.dto.ApiResponse;
import Fitspan.demo_fitSpan.dto.request.AuthenticationRequest;
import Fitspan.demo_fitSpan.dto.request.IntrospectRequest;
import Fitspan.demo_fitSpan.dto.request.LogoutRequest;
import Fitspan.demo_fitSpan.dto.request.RefreshRequest;
import Fitspan.demo_fitSpan.dto.response.AuthenticationResponse;
import Fitspan.demo_fitSpan.dto.response.IntrospectResponse;
import Fitspan.demo_fitSpan.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticated(@RequestBody AuthenticationRequest authenticationRequest) {
        var result = authenticationService.authenticate(authenticationRequest);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }
    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> authenticated(@RequestBody RefreshRequest request) throws Exception {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect")
     ApiResponse<IntrospectResponse> authenticated(@RequestBody IntrospectRequest introspectRequest) throws ParseException, JOSEException {
        var result = authenticationService.introspect(introspectRequest);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }
    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws Exception {
        authenticationService.  logout(request);
        return ApiResponse.<Void>builder()
                .build();
    }
}
