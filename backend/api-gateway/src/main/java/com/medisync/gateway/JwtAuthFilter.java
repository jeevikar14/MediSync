package com.medisync.gateway;

import com.medisync.common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Validates the JWT, applies the role matrix and forwards the caller identity to the services as
 * X-User-Name / X-User-Role headers (any client supplied values are overwritten).
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtUtil jwt;

    public JwtAuthFilter(@Value("${medisync.jwt.secret}") String secret) {
        this.jwt = new JwtUtil(secret, 0);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        if ("OPTIONS".equals(method) || path.equals("/api/auth/login")) {
            return chain.filter(exchange);
        }

        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return reject(exchange, HttpStatus.UNAUTHORIZED);
        }

        Claims claims;
        try {
            claims = jwt.parse(header.substring(7));
        } catch (Exception e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return reject(exchange, HttpStatus.UNAUTHORIZED);
        }

        String role = claims.get("role", String.class);
        if (!RoleRules.allowed(method, path, role)) {
            log.info("Denied {} {} for role {}", method, path, role);
            return reject(exchange, HttpStatus.FORBIDDEN);
        }

        ServerHttpRequest mutated = request.mutate()
                .headers(h -> {
                    h.remove("X-User-Name");
                    h.remove("X-User-Role");
                    h.set("X-User-Name", claims.getSubject());
                    h.set("X-User-Role", role);
                }).build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() { return -1; }
}
