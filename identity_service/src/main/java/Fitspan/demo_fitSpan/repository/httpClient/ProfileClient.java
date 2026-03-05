package Fitspan.demo_fitSpan.repository.httpClient;

import Fitspan.demo_fitSpan.configuration.AuthenticationRequestInterceptor;
import Fitspan.demo_fitSpan.dto.ApiResponse;
import Fitspan.demo_fitSpan.dto.request.ProfileRequest;
import Fitspan.demo_fitSpan.dto.response.ProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "profile-service", url = "${app.services.profile}",
        configuration = { AuthenticationRequestInterceptor.class})
public interface ProfileClient {
    @PostMapping(value = "/internal/users", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<ProfileResponse> createProfile(@RequestBody ProfileRequest request);
}
