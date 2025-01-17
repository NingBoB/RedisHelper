package com.dmm.middleware.redis.config;

import com.dmm.middleware.redis.annotation.XRedis;
import com.dmm.middleware.redis.reflect.XRedisFactoryBean;
import com.dmm.middleware.redis.util.SimpleMetadataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.beans.Introspector;
import java.io.IOException;
import java.util.List;

/**
 * @author Mean
 * @date 2025/1/16 21:57
 * @description 创建Redis链接和扫描(注解)注册 Bean
 */
@Configuration
@EnableConfigurationProperties(XRedisProperties.class)
public class XRedisRegisterAutoConfiguration implements InitializingBean {

	private Logger logger = LoggerFactory.getLogger(XRedisRegisterAutoConfiguration.class);

	@Bean
	@ConditionalOnMissingBean
	public Jedis jdis(XRedisProperties properties) {
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxIdle(5);
		jedisPoolConfig.setTestOnBorrow(false);
		JedisPool jedisPool = new JedisPool(jedisPoolConfig, properties.getHost(), properties.getPort());
		return jedisPool.getResource();
	}

	public static class XRedisRegister implements BeanFactoryAware, ImportBeanDefinitionRegistrar {

		private BeanFactory beanFactory;

		@Override
		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			this.beanFactory = beanFactory;
		}

		// 扫描所有配置了注解的接口方法
		@Override
		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
			try {
				if (!AutoConfigurationPackages.has(this.beanFactory)) {
					return;
				}

				List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
				String basePackage = StringUtils.collectionToCommaDelimitedString(packages);

				// 获取扫描路径
				String packageSearchPath = "classpath*:" + basePackage.replace('.', '/') + "/**/*.class";
				// 扫描所有class文件
				ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
				Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);

				// 获取所有带有注解的类，并为其创建bean对象
				for (Resource resource : resources) {
					// 解析 .class 文件，提取类的元数据和注解信息。
					MetadataReader metadataReader = new SimpleMetadataReader(resource, ClassUtils.getDefaultClassLoader());
					// 判断是否有注解
					XRedis annotation = Class.forName(metadataReader.getClassMetadata().getClassName()).getAnnotation(XRedis.class);
					if (null == annotation) continue;

					ScannedGenericBeanDefinition beanDefinition = new ScannedGenericBeanDefinition(metadataReader);
					String beanName = Introspector.decapitalize(ClassUtils.getShortName(beanDefinition.getBeanClassName()));

					beanDefinition.setResource(resource);
					beanDefinition.setSource(resource);
					beanDefinition.setScope("singleton");
					beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition.getBeanClassName());
					beanDefinition.setBeanClass(XRedisFactoryBean.class);

					BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
					registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());
				}


			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	@Configuration
	@Import(XRedisRegister.class)
	public static class MapperScannerRegistrarNotFoundConfiguration implements InitializingBean {

		@Override
		public void afterPropertiesSet() throws Exception {

		}
	}



	@Override
	public void afterPropertiesSet() throws Exception {

	}
}
