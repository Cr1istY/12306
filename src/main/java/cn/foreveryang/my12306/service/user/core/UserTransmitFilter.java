/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.foreveryang.my12306.service.user.core;


import cn.foreveryang.my12306.common.constant.UserConstant;
import cn.foreveryang.my12306.dto.resp.UserInfoDTO;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import java.net.URLDecoder;

import java.io.IOException;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;


@Slf4j
public class UserTransmitFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String userId = httpServletRequest.getHeader(UserConstant.USER_ID_KEY);
        Cookie[] cookies = null;
        if (StrUtil.isBlank(userId)) {
            cookies = httpServletRequest.getCookies();
            userId = Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(UserConstant.USER_ID_KEY))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        // log.info("UserId: {}", userId);
        if (StringUtils.hasText(userId)) {
            String userName = httpServletRequest.getHeader(UserConstant.USER_NAME_KEY);
            if (StrUtil.isBlank(userName) && cookies != null) {
                userName = Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equals(UserConstant.USER_NAME_KEY))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElse(null);
            }
            String realName = httpServletRequest.getHeader(UserConstant.REAL_NAME_KEY);
            if (StrUtil.isBlank(realName) && cookies != null) {
                realName = Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equals(UserConstant.REAL_NAME_KEY))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElse(null);
            }
            if (StringUtils.hasText(userName)) {
                userName = URLDecoder.decode(userName, UTF_8);
            }
            if (StringUtils.hasText(realName)) {
                realName = URLDecoder.decode(realName, UTF_8);
            }
            String token = httpServletRequest.getHeader(UserConstant.USER_TOKEN_KEY);
            if (StrUtil.isBlank(token) && cookies != null) {
                token = Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equals(UserConstant.USER_TOKEN_KEY))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElse(null);
            }
            UserInfoDTO userInfoDTO = UserInfoDTO.builder()
                    .userId(userId)
                    .username(userName)
                    .realName(realName)
                    .token(token)
                    .build();
            log.info("UserInfoDTO: {}", userInfoDTO);
            UserContext.setUser(userInfoDTO);
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }
}
