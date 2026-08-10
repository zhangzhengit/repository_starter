package vo.zrepository.starter.vortex;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Sets;

import vo.vortex.ZProperties;
import vo.vortex.core.ZContext;
import vo.vortex.exception.StartupException;
import vo.vortex.starter.ZStarter;
import vo.vortex.zclass.ZClass;
import vo.zrepository.actuator.SqlInvocationLogsConfigurationProperties;
import vo.zrepository.conn.Env;
import vo.zrepository.conn.EnvEnum;
import vo.zrepository.core.ScanPackage;
import vo.zrepository.starter.spring.actuator.ZRepositoryStarter;

/**
 * 通过 vortex.factories 指定的启动类
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

		final String ns = String.valueOf(scanPackageNameObject);
		final String[] pas = ns.split(",");

		ScanPackage.set(Sets.newHashSet(pas));

		final Map<Class, ZClass> clsMap = ZRepositoryStarter.startZRepository(pas);

		for (final Entry<Class, ZClass> entry : clsMap.entrySet()) {
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
