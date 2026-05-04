package com.vo.repository.starter.actuator;

import javax.annotation.sql.DataSourceDefinition;

import com.vo.zframework.anno.ZConfigurationProperties;
import com.vo.zframework.validator.ZNotEmtpy;

/**
 * 
 * 配置管理员用户信息
 * 
 * @author zhangzhen
 * @date 2025年8月25日
 * 
 */
@ZConfigurationProperties(prefix = "repository.actuator")
public class AdminConfigurationProperties {
	
//	@ZNotEmtpy
	private String userName;
	
//	@ZNotEmtpy
	private String password;
	
	// FIXME 2025年8月25日 下午6:02:31 zhangzhen: 加一个过期时间，比如1个小时

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return this.password;
	} 

	public void setPassword(final String password) {
		this.password = password;
	}
	

}
