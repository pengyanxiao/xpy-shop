package com.baidu.shop.web;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.business.OauthService;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.CookieUtils;
import com.baidu.shop.utils.JwtUtils;
import com.baidu.shop.utils.ObjectUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName UserOauthController
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/10/15
 * @Version V1.0
 **/
@RestController
@Api(tags = "用户认证")
public class UserOauthController extends BaseApiService {

    @Resource
    private OauthService oauthService;

    @Resource
    private JwtConfig jwtConfig;

    @ApiOperation(value = "用户登录")
    @PostMapping(value = "oauth/login")
    public Result<JSONObject> login(@RequestBody UserEntity userEntity, HttpServletRequest httpServletRequest
            , HttpServletResponse httpServletResponse){

        String token = oauthService.login(userEntity, jwtConfig);

        if (ObjectUtil.isNull(token)) {
            return this.setResultError(HTTPStatus.VALIDATE_PASSWORD_ERROR,"用户名或密码错误");
        }

        CookieUtils.setCookie(httpServletRequest,httpServletResponse,jwtConfig.getCookieName(),token
                ,jwtConfig.getCookieMaxAge(),true);

            return  this.setResultSuccess();
    }


    @GetMapping(value = "oauth/verify")
    public Result<UserInfo> verifyLogin(@CookieValue(value = "MRSHOP_TOKEN") String token
            ,HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse){

        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());

            token = JwtUtils.generateToken(new UserInfo(userInfo.getId(), userInfo.getUsername())
                    , jwtConfig.getPrivateKey(), jwtConfig.getExpire());

            //如果token过期时,用户还在访问,那么延长过期时间
            CookieUtils.setCookie(httpServletRequest,httpServletResponse
                    ,jwtConfig.getCookieName(),token,jwtConfig.getCookieMaxAge(),true);

            return this.setResultSuccess(userInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return this.setResultError(HTTPStatus.VERIFY_ERROR,"用户失效");
        }

    }

}
