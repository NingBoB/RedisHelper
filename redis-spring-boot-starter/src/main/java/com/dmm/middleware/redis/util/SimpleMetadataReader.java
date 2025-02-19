package com.dmm.middleware.redis.util;

import org.springframework.asm.ClassReader;
import org.springframework.core.NestedIOException;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.AnnotationMetadataReadingVisitor;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Mean
 * @date 2025/1/16 21:57
 * @description 字节码读取器
 */
public class SimpleMetadataReader implements MetadataReader {

	private final Resource resource;

	private final ClassMetadata classMetadata;

	private final AnnotationMetadata annotationMetadata;

	public SimpleMetadataReader(Resource resource, ClassLoader classLoader) throws IOException {
		// 用于加载类的类加载器
		ClassReader classReader;
		try (InputStream is = new BufferedInputStream(resource.getInputStream())) {
			classReader = new ClassReader(is);
		} catch (IllegalArgumentException ex) {
			throw new NestedIOException("ASM ClassReader failed to parse class file - " +
					"probably due to a new Java class file version that isn't supported yet: " + resource, ex);
		}

		// 解析字节码
		AnnotationMetadataReadingVisitor visitor = new AnnotationMetadataReadingVisitor(classLoader);
		classReader.accept(visitor, ClassReader.SKIP_DEBUG);

		// 得到注解信息
		this.annotationMetadata = visitor;
		// (since AnnotationMetadataReadingVisitor extends ClassMetadataReadingVisitor)
		// 元数据
		this.classMetadata = visitor;
		this.resource = resource;
	}


	@Override
	public Resource getResource() {
		return this.resource;
	}

	@Override
	public ClassMetadata getClassMetadata() {
		return this.classMetadata;
	}

	@Override
	public AnnotationMetadata getAnnotationMetadata() {
		return this.annotationMetadata;
	}
}
