package com.vo.actuator;

import java.time.LocalDateTime;

import com.vo.anno.ZComponent;
import com.vo.anno.ZOrder;
import com.vo.aop.InterceptorParameter;
import com.vo.core.ContentTypeEnum;
import com.vo.core.ZRequest;
import com.vo.core.ZResponse;
import com.vo.http.HttpStatusEnum;
import com.vo.http.ZCookie;
import com.vo.scanner.ZHandlerInterceptor;

/**
 * 监控器相关接口的权限校验拦截器
 *
 * @author zhangzhen
 * @date 2025年8月25日
 * 
 */
@ZComponent
@ZOrder(value = 0)
public class AcutatorInterceptor implements ZHandlerInterceptor {

	@Override
	public String[] interceptionPathRegex() {
		return new String[] { "^/repository/.*$" };
	}

	@Override
	public boolean preHandle(ZRequest request, ZResponse response, InterceptorParameter interceptorParameter) {
		
		ZCookie token = request.getCookie("token");
		if (token == null ) {
			// FIXME 2025年8月25日 下午5:55:13 zhangzhen: 这只是简单测试，记得login接口更名和加入严谨逻辑，并且此处同步修改
			response.httpStatus(HttpStatusEnum.HTTP_403.getCode())
			.contentType(ContentTypeEnum.TEXT_PLAIN.getType())
			.body("无访问权限");
			return false;
		}
		return ZHandlerInterceptor.super.preHandle(request, response, interceptorParameter);
	}

}
