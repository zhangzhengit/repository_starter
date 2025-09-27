package com.vo.actuator;

import javax.annotation.sql.DataSourceDefinition;

import com.vo.anno.ZConfigurationProperties;
import com.vo.validator.ZNotEmtpy;

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
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	

}
