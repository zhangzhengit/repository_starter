package vo.zrepository.starter.spring.actuator;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 *
 *
 * @author zhangzhen
 * @date 2023年6月16日
 *
 */
@Component
public class BFPP
//implements BeanFactoryPostProcessor 
{

	private final ConfigurableApplicationContext applicationContext;

	// 通过构造器注入 ApplicationContext
	public BFPP(ConfigurableApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		beanFactory = applicationContext.getBeanFactory();
	}

	public static ConfigurableListableBeanFactory beanFactory;

//	@Override
//	public void postProcessBeanFactory(final ConfigurableListableBeanFactory bf) throws BeansException {
//		beanFactory = bf;
//	}

}
