package com.medisync.gateway;

import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Set;

/**
 * Role based access matrix, enforced centrally at the gateway.
 * First matching rule wins. A request that matches no rule only needs a valid JWT.
 */
final class RoleRules {

    private record Rule(Set<String> methods, String pattern, Set<String> roles) {}

    private static final String A = "ADMIN", D = "DOCTOR", N = "NURSE", L = "LAB_TECHNICIAN", R = "RECEPTIONIST";
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private static final List<Rule> RULES = List.of(
            // patients
            new Rule(Set.of("DELETE"), "/api/patients/**", Set.of(A)),
            new Rule(Set.of("POST", "PUT"), "/api/patients/**", Set.of(A, R, D, N)),
            // clinical records & diagnostics
            new Rule(Set.of("POST"), "/api/clinical/lab-orders", Set.of(A, D)),
            new Rule(Set.of("POST"), "/api/clinical/lab-reports/*/review", Set.of(A, D)),
            new Rule(Set.of("POST"), "/api/clinical/lab-reports", Set.of(A, L)),
            new Rule(Set.of("POST"), "/api/clinical/notes", Set.of(A, D, N)),
            new Rule(Set.of("GET"), "/api/clinical/**", Set.of(A, D, N, L)),
            // appointments
            new Rule(Set.of("POST"), "/api/appointments/**", Set.of(A, R, D, N)),
            // care orchestration
            new Rule(Set.of("GET", "POST"), "/api/care/**", Set.of(A, D, N)),
            // handoffs
            new Rule(Set.of("POST"), "/api/handoffs/**", Set.of(A, D)),
            new Rule(Set.of("GET"), "/api/handoffs/**", Set.of(A, D, N))
    );

    private RoleRules() {}

    static boolean allowed(String method, String path, String role) {
        for (Rule rule : RULES) {
            if (rule.methods().contains(method) && MATCHER.match(rule.pattern(), path)) {
                return rule.roles().contains(role);
            }
        }
        return true;
    }
}
