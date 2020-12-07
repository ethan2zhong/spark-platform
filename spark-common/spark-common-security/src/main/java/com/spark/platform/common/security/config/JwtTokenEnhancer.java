package com.spark.platform.common.security.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spark.platform.common.security.model.LoginUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: wangdingfeng
 * @ProjectName: spark-platform
 * @Package: com.spark.platform.common.security.config
 * @ClassName: JwtTokenEnhancer
 * @Description: 自定义token生成携带的信息
 * @Version: 1.0
 */
public class JwtTokenEnhancer implements TokenEnhancer {

    private DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        final Map<String, Object> additionalInfo = Maps.newHashMap();
        if(authentication.getPrincipal() instanceof String){
            // 如果不是密码模式登陆的没有用户信息 所以不用定制token
            return accessToken;
        }
        // 给/oauth/token接口加属性roles,author
        LoginUser user = (LoginUser) authentication.getPrincipal();
        List<GrantedAuthority> authorities = user.getAuthorities();
        List<String> roleList = Lists.newArrayList();
        List<String> permissions = Lists.newArrayList();
        for (GrantedAuthority authority : authorities) {
            if(authority.getAuthority().startsWith("ROLE_")){
                roleList.add(authority.getAuthority());
            }else{
                permissions.add(authority.getAuthority());
            }
        }
        String roles = StringUtils.join(roleList,",");
        additionalInfo.put("roles", roles);
        additionalInfo.put("permissions", permissions);
        additionalInfo.put("author", "spark-auth");
        additionalInfo.put("createTime", df.format(LocalDateTime.now()));
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }
}
