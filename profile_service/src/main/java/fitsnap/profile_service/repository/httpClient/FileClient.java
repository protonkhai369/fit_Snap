package fitsnap.profile_service.repository.httpClient;

import java.io.IOException;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import fitsnap.profile_service.configuration.AuthenticationRequestInterceptor;
import fitsnap.profile_service.dto.ApiResponse;
import fitsnap.profile_service.dto.response.FileMgmtResponse;

@FeignClient(
        name = "file-service",
        url = "http://localhost:8083",
        configuration = {AuthenticationRequestInterceptor.class})
public interface FileClient {
    @PostMapping(value = "file/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<FileMgmtResponse> uploadMedia(@RequestPart("file") MultipartFile file) throws IOException;
}
