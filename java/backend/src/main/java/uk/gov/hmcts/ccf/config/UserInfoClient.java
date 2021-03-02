package uk.gov.hmcts.ccf.config;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.Map;

@FeignClient
public interface UserInfoClient {

    @RequestLine("GET /userinfo")
    @Headers({
            "Accept: application/json",
            "Authorization: {authorization}"
    })
    Map<String, Object> userInfo(@Param("authorization") String var1);
}
