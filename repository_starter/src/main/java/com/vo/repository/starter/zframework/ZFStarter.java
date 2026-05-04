package com.vo.repository.starter.zframework;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;
import com.vo.configuration.ZProperties;
import com.vo.core.ZClass;
import com.vo.core.ZContext;
import com.vo.exception.StartupException;
import com.vo.repository.actuator.SqlInvocationLogsConfigurationProperties;
import com.vo.repository.conn.Env;
import com.vo.repository.conn.EnvEnum;
import com.vo.repository.core.ScanPackage;
import com.vo.repository.starter.spring.actuator.ZRepositoryStarter;
import com.vo.starter.ZStarter;

/**
 * 通过 zframework.factories 指定的启动类
 *
 * @author zhangzhen
 * @date 2024年2月17日
 *
 */
public class ZFStarter implements ZStarter {

	private static final String SCAN_PACKAGE_NAME = "zrepository.scan.package.name";

	@Override
	public void start() {

		Env.ENV = EnvEnum.ZFRAMEWORK;

		final Object scanPackageNameObject = ZProperties.getInstance().getProperty(SCAN_PACKAGE_NAME);

		if (scanPackageNameObject == null) {
			throw new StartupException("[" + SCAN_PACKAGE_NAME + "]未配置,请先配置[" + SCAN_PACKAGE_NAME + "]");
		}

		ScanPackage.set(Sets.newHashSet(String.valueOf(scanPackageNameObject)));

		final Map<Class, ZClass> clsMap = ZRepositoryStarter.startZRepository(String.valueOf(scanPackageNameObject));
		final Set<Entry<Class, ZClass>> es = clsMap.entrySet();
		for (final Entry<Class, ZClass> entry : es) {
			ZContext.addBean(entry.getKey(), entry.getValue().newInstance());
		}

		final String string = SqlInvocationLogsConfigurationProperties.NAME;

		final String createTableSql = "CREATE TABLE \"sql_invocation_logs\" (\r\n"
				+ "  \"id\" integer NOT NULL PRIMARY SCAN_PACKAGE_NAME AUTOINCREMENT,\r\n"
				+ "  \"sql\" TEXT,\r\n"
				+ "  \"time_consuming\" integer,\r\n"
				+ "  \"invoke_time\" integer\r\n"
				+ ");";

		// FIXME 2024年6月2日 下午9:36:28 zhangzhen : 加一个开关，是每次启动都重建，还是不存在再重建
		//		SU.createTable(string, createTableSql);

	}

}
