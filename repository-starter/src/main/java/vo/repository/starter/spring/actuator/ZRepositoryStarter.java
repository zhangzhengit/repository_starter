package vo.repository.starter.spring.actuator;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import vo.log.core.ZLog2;
import vo.repository.anno.ZQuery;
import vo.repository.conn.Env;
import vo.repository.conn.EnvEnum;
import vo.repository.conn.ZRepositoryMain;
import vo.repository.core.ScanPackage;
import vo.repository.core.SqlResult;
import vo.repository.core.ZEntityHandlerScanner;
import vo.repository.core.ZRSqlMap;
import vo.repository.core.ZRepository;
import vo.repository.enums.DMLEnum;
import vo.vortex.common.CU;
import vo.vortex.zclass.ZClass;

/**
 *
 *
 * @author zhangzhen
 * @date 2023年6月16日
 *
 */
// FIXME 2023年10月15日 下午7:16:49 zhanghen: TODO 区分mysql、postgresql等等，现在默认实现是mysql
@Component
public class ZRepositoryStarter implements InstantiationAwareBeanPostProcessor {

	private static final ZLog2 LOG = ZLog2.getInstance();


	private final AtomicBoolean gZRepository = new AtomicBoolean(false);

	@Value(value = "${zrepository.scanPackageName}")
	private Set<String> scanPackageName;


	@Value(value = "${repository.actuator.enable:false}")
	private boolean actuatorEnable;

	@Override
	public boolean postProcessAfterInstantiation(final Object bean, final String beanName) throws BeansException {
		System.out.println(LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "ZRepositoryStarter.postProcessAfterInstantiation()");
		
		
		Env.ENV = EnvEnum.SPRING;
		Env.ACTUATOR_ENABLE = this.actuatorEnable;

		if (!this.gZRepository.get()) {

			if (CU.isEmpty(this.scanPackageName)) {
				throw new IllegalArgumentException("zrepository.scanPackageName 未配置！");
			}

			LOG.info("zrepository.scanPackageName={}", this.scanPackageName);

			ScanPackage.set(this.scanPackageName);
			final String[] array = this.scanPackageName.toArray(new String[0]);

			ZRepositoryStarter.gZRepository(array);
			this.gZRepository.set(true);
		}

		return InstantiationAwareBeanPostProcessor.super.postProcessAfterInstantiation(bean, beanName);
	}

	private static void gZRepository(final String... packageName) {

		final Map<Class, ZClass> clsMap = startZRepository(packageName);
		final Set<Entry<Class, ZClass>> es = clsMap.entrySet();
		for (final Entry<Class, ZClass> entry : es) {

			LOG.info("开始注入实现类[{}]", entry.getValue().getName());
			BFPP.beanFactory.registerSingleton(entry.getKey().getName(), entry.getValue().newInstance());
			LOG.info("注入实现类[{}]成功", entry.getValue().getName());
		}
	}

	/**
	 * 启动ZRepository程序，扫描 ZRepository 子接口并且生成代理类
	 * @param pas
	 *
	 * @return 返回<ZRepository的子接口的Class,生成的ZRepository的子接口的ZClass代理类>
	 *
	 */
	public static Map<Class, ZClass> startZRepository(final String... pas) {
		ScanPackage.set(Sets.newHashSet(pas));

		LOG.info("ZRepositoryStarter启动,packageName=[{}]", Arrays.toString(pas));

		ZEntityHandlerScanner.scan(pas);

		LOG.info("ZRepositoryStarter开始扫描[{}]的子接口", ZRepository.class.getCanonicalName());
		// 1 查找ZRepository的子接口
		final Set<Class<?>> zrSubinterfaceSet = ZRepositoryMain.scanZRepositorySubinterface(pas);
		if (CU.isEmpty(zrSubinterfaceSet)) {
			LOG.info("ZRepositoryStarter没有[{}]的子接口", ZRepository.class.getCanonicalName());
			return Collections.emptyMap();
		}

		LOG.info("ZRepositoryStarter[{}]的子接口个数={}", ZRepository.class.getCanonicalName(), zrSubinterfaceSet.size());

		// 1.1 验证ZRepository子接口指定的泛型类的@ZEntity指定的tableName是否存在
		ZRepositoryMain.checkTableExist(zrSubinterfaceSet);

		// 1.11校验：@ZQuery 标记的方法前缀必须和dml指定的关键字一致
		checkZQuery(zrSubinterfaceSet);

		// 1.2  create table 语句
		//		ZRepositoryMain.showCreateTable(zrSubinterfaceSet);
		// 1.3 show 支持的声明式方法形式
		ZRepositoryMain.showSupportedMethod();

		// 2 给ZRepository的子接口的每个方法生成 SQL
		final List<SqlResult> sqlForZRSubclassList = ZRepositoryMain.generateSqlForZRSubclass(zrSubinterfaceSet);
		for (final SqlResult sqlResult : sqlForZRSubclassList) {
			ZRSqlMap.put(sqlResult.getZRepositorySubClassName(), sqlResult.getMethodName(), sqlResult.getSqlFinal());
		}

		// 3 给ZRepository的子接口生成动态代理类，ZClass
		final Map<Class, ZClass> clsMap = ZRepositoryMain.generateClassForZRSubinterface(zrSubinterfaceSet);
		return clsMap;
	}

	private static void checkZQuery(final Set<Class<?>> zrSubinterfaceSet) {
		for (final Class<?> cls : zrSubinterfaceSet) {
			final Method[] ms = cls.getDeclaredMethods();
			for (final Method m  : ms) {
				final ZQuery q = m.getAnnotation(ZQuery.class);
				if(q==null) {
					continue;
				}
				final String methodNameUpperTrim = m.getName().toUpperCase().trim();
				final DMLEnum dml = q.dml();

				if (methodNameUpperTrim.startsWith(ZQuery.SELECT_METHOD_NAME_PREFIX)) {
					if (dml != DMLEnum.SELECT) {
						final String message = cls.getName() + "." + m.getName()
						+ " 方法名称声明错误："
						+ "修改为 @" + ZQuery.class.getSimpleName() + ".dml = " + DMLEnum.SELECT
						+ "和DML语句(SELECT/INSERT/UPDATE/DELETE)一致"
						;
						throw new IllegalArgumentException(message);
					}
				} else if (methodNameUpperTrim.startsWith(ZQuery.INSERT_METHOD_NAME_PREFIX)) {
					if (dml != DMLEnum.INSERT) {
						final String message = cls.getName() + "." + m.getName()
						+ " 方法名称声明错误："
						+ "修改为 @" + ZQuery.class.getSimpleName() + ".dml = " + DMLEnum.SELECT
						+ "和DML语句(SELECT/INSERT/UPDATE/DELETE)一致"
						;
						throw new IllegalArgumentException(message);
					}

				} else if (methodNameUpperTrim.startsWith(ZQuery.UPDATE_METHOD_NAME_PREFIX)) {
					if (dml != DMLEnum.UPDATE) {
						final String message = cls.getName() + "." + m.getName()
						+ " 方法名称声明错误："
						+ "修改为 @" + ZQuery.class.getSimpleName() + ".dml = " + DMLEnum.SELECT
						+ "和DML语句(SELECT/INSERT/UPDATE/DELETE)一致"
						;
						throw new IllegalArgumentException(message);
					}

				} else if (methodNameUpperTrim.startsWith(ZQuery.DELETE_METHOD_NAME_PREFIX)) {
					if (dml != DMLEnum.DELETE) {
						final String message = cls.getName() + "." + m.getName()
						+ " 方法名称声明错误："
						+ "修改为 @" + ZQuery.class.getSimpleName() + ".dml 属性值"
						+ "和DML语句(SELECT/INSERT/UPDATE/DELETE)一致"
						;
						throw new IllegalArgumentException(message);
					}
				} else {

					final String message = cls.getName() + "." + m.getName()
					+ " 方法名称声明错误："
					+ "只支持DML语句(select/insert/update/delete)作为前缀"
					;
					throw new IllegalArgumentException(message);
				}
			}
		}
	}

}
