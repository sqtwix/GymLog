package com.example.GymLogCore.config;

import com.example.GymLogCore.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Ищем заголовок Authorization
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Если заголовка нет или он не начинается с "Bearer ", пропускаем запрос дальше
        // (возможно, это запрос на регистрацию, там токен не нужен)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Отрезаем слово "Bearer " (оно занимает 7 символов), чтобы получить сам токен
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);

        // 4. Если email есть в токене, и мы еще не авторизованы в текущем контексте
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Достаем юзера из БД
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Проверяем токен на валидность (тот ли юзер, не просрочен ли)
                if (jwtService.isTokeValid(jwt, (com.example.GymLogCore.domain.User) userDetails)) {

                    // Создаем объект аутентификации (пропуск в систему)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Кладем пропуск в SecurityContext (говорим Spring, что этот клиент проверен)
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // If the user does not exist in the DB, ignore the token and let Spring Security reject it with 403
            }
        }

        // Передаем запрос дальше по цепочке
        filterChain.doFilter(request, response);
    }
}