package com.yy.exam.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * SPA路由过滤器
 * 解决Vue history模式路由在Spring Boot中返回404的问题
 * 将所有非API、非静态资源的GET请求转发到index.html
 */
@Component
public class SpaRoutingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        // API请求和静态资源请求直接放行
        if (requestUri.startsWith("/exam/api/")
                || requestUri.startsWith("/upload/")
                || requestUri.startsWith("/doc.html")
                || requestUri.startsWith("/swagger")
                || requestUri.startsWith("/v2/")
                || requestUri.startsWith("/webjars")
                || requestUri.contains(".")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 非GET请求直接放行
        if (!"GET".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 尝试加载index.html
        ClassPathResource resource = new ClassPathResource("static/index.html");
        if (resource.exists()) {
            response.setStatus(200);
            response.setContentType(MediaType.TEXT_HTML_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            try (InputStream inputStream = resource.getInputStream();
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
        } else {
            // index.html不存在，继续正常处理
            filterChain.doFilter(request, response);
        }
    }
}