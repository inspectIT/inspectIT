package info.novatec.inspectit.cmr.usermanagement;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

public class InitNotify implements BeanPostProcessor, Ordered {

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		System.out.println("Initalizing Bean: " + beanName);
	    return bean;  // you can return any other object as well
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;  // you can return any other object as well
	}

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

}
