package io.github.jhipster.grpcsample.grpc;

import io.github.jhipster.grpcsample.security.AuthoritiesConstants;
import io.grpc.*;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;

@Component
public class AuthenticationInterceptor implements ServerInterceptor {

    private final Logger log = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    private final AuthenticationManager authenticationManager;

    public AuthenticationInterceptor(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }


    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String authorizationValue = metadata.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER));
        if(authorizationValue == null) {
            // Some implementations don't support uppercased metadata keys
            authorizationValue = metadata.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));
        }
        if (StringUtils.hasText(authorizationValue) && authorizationValue.startsWith("Basic ")) {
            String token = authorizationValue.substring(6, authorizationValue.length());
            if (StringUtils.hasText(token)) {
                try {
                    String[] credentials = decodeToken(token);
                    Authentication authentication =
                        new UsernamePasswordAuthenticationToken(credentials[0], credentials[1]);
                    authenticationManager.authenticate(authentication);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    if (authentication.getAuthorities().stream()
                        .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(AuthoritiesConstants.ANONYMOUS))
                        ) {
                        return serverCallHandler.startCall(serverCall, metadata);
                    } else {
                        serverCall.close(Status.PERMISSION_DENIED, metadata);
                    }
                } catch (AuthenticationException e) {
                    log.error("Cannot authenticate", e);
                    serverCall.close(Status.UNAUTHENTICATED, metadata);
                }
            }
        }
        serverCall.close(Status.UNAUTHENTICATED, metadata);
        return null;
    }
    private String[] decodeToken(String base64Token) {
        byte[] decoded;
        try {
            decoded = Base64Utils.decode(base64Token.getBytes(Charset.forName("UTF-8")));
        }
        catch (IllegalArgumentException e) {
            throw new BadCredentialsException("Failed to decode basic authentication token");
        }
        String token = new String(decoded, Charset.forName("UTF-8"));
        int delim = token.indexOf(":");
        if (delim == -1) {
            throw new BadCredentialsException("Invalid basic authentication token");
        }
        return new String[] { token.substring(0, delim), token.substring(delim + 1) };
    }
}
