package Fitspan.demo_fitSpan.service;

import Fitspan.demo_fitSpan.dto.request.AuthenticationRequest;
import Fitspan.demo_fitSpan.dto.request.IntrospectRequest;
import Fitspan.demo_fitSpan.dto.request.LogoutRequest;
import Fitspan.demo_fitSpan.dto.request.RefreshRequest;
import Fitspan.demo_fitSpan.dto.response.AuthenticationResponse;
import Fitspan.demo_fitSpan.dto.response.IntrospectResponse;
import Fitspan.demo_fitSpan.entity.InvalidatedToken;
import Fitspan.demo_fitSpan.entity.User;
import Fitspan.demo_fitSpan.exception.AppException;
import Fitspan.demo_fitSpan.exception.ErrorCode;
import Fitspan.demo_fitSpan.repository.InvalidatedTokenRepository;
import Fitspan.demo_fitSpan.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;
    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;
    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;
    public IntrospectResponse introspect(IntrospectRequest request) {
        boolean isValid = true;
        try {
            verifyToken(request.getToken(),false);
        } catch (Exception e) {
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        var user = userRepository.findByUsername(authenticationRequest.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOTEXISITED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
        if (!authenticated)

            throw new AppException(ErrorCode.UNAUTHENTICATED);
        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .Authenticated(true)
                .build();
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws Exception {
        var signedJWT = verifyToken(request.getToken(),true);
        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var username = signedJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username).orElseThrow(()-> new AppException(ErrorCode.USER_NOTEXISITED));
        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .Authenticated(true)
                .build();
    }
    @PreAuthorize("returnObject==authentication.name")
    public void logout (LogoutRequest request) throws Exception {
        var signedJWT = verifyToken(request.getToken(),true);
        try {
            var jit = signedJWT.getJWTClaimsSet().getJWTID();
            var expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jit)
                    .expiredTime(expirationTime)
                    .build();
            invalidatedTokenRepository.save(invalidatedToken);
        }
        catch (AppException e)
        {
            log.info("Token already expired time");
        }
    }
    private SignedJWT verifyToken(String token, boolean isRefresh) throws Exception {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);
        if (!(verified && expiryTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        if (invalidatedTokenRepository.existsById(token)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("test.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cant create token",e);
            throw new RuntimeException(e);
        }
    }
    private String buildScope(User user)
    {
        StringJoiner stringJoiner =new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role->{

                stringJoiner.add("ROLE_" + role.getName());

                if(!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().
                            forEach(permission -> stringJoiner.add(permission.getName()));
            });

        return stringJoiner.toString();
    }

}
