package fitSnap.product_service.configuration;

import java.util.Optional;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        assert servletRequestAttributes != null;
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");

        log.info("Header: {}", Optional.ofNullable(authHeader));
        if (StringUtils.hasText(authHeader)) template.header("Authorization", (String) authHeader);
    }
}
