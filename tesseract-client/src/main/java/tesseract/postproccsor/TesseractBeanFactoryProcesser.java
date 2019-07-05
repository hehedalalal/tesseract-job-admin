package tesseract.postproccsor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.StringUtils;
import tesseract.core.annotation.ClientJobDetail;
import tesseract.core.annotation.TesseractJob;
import tesseract.core.handler.JobHandler;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TesseractBeanFactoryProcesser implements BeanFactoryPostProcessor {
    private final static AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);
    public static final String NAME_FORMATTER = "clientJobDetail-%d";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        Arrays.asList(beanDefinitionNames).parallelStream().forEach(beanDefinitionName -> {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (StringUtils.isEmpty(beanClassName)) {
                return;
            }

            try {
                Class<?> aClass = Class.forName(beanClassName);
                TesseractJob annotation;
                if (aClass != null && (annotation = aClass.getAnnotation(TesseractJob.class)) != null) {
                    if (!JobHandler.class.isAssignableFrom(aClass)) {
                        log.error("注解TesseractJob只能加在JobHandler实现类");
                        return;
                    }
                    String triggerName = annotation.triggerName();
                    ClientJobDetail clientJobDetail = new ClientJobDetail();
                    clientJobDetail.setClassName(beanClassName);
                    clientJobDetail.setTriggerName(triggerName);
                    beanFactory.registerSingleton(String.format(NAME_FORMATTER, ATOMIC_INTEGER.incrementAndGet()), clientJobDetail);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        });
    }
}
