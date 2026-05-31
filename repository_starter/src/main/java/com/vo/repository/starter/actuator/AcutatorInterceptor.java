package com.vo.repository.starter.actuator;

import vo.zframework.anno.ZComponent;
import vo.zframework.anno.ZOrder;
import vo.zframework.aop.InterceptorParameter;
import vo.zframework.core.ContentTypeEnum;
import vo.zframework.core.ZRequest;
import vo.zframework.core.ZResponse;
import vo.zframework.http.HttpStatusEnum;
import vo.zframework.http.ZCookie;
import vo.zframework.scanner.ZHandlerInterceptor;

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
	public boolean preHandle(final ZRequest request, final ZResponse response, final InterceptorParameter interceptorParameter) {

		final ZCookie token = request.getCookie("token");
		if (token == null ) {
			// FIXME 2025年8月25日 下午5:55:13 zhangzhen: 这只是简单测试，记得login接口更名和加入严谨逻辑，并且此处同步修改
			response.httpStatus(HttpStatusEnum.HTTP_403.getStatus())
			.contentType(ContentTypeEnum.TEXT_PLAIN.getType())
			.body("无访问权限");
			return false;
		}
		return ZHandlerInterceptor.super.preHandle(request, response, interceptorParameter);
	}

}
