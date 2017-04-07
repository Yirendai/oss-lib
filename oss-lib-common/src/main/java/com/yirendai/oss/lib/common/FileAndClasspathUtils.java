package com.yirendai.oss.lib.common;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newLinkedHashSetWithExpectedSize;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import lombok.SneakyThrows;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class FileAndClasspathUtils {

  private FileAndClasspathUtils() {
  }

  /**
   * Create if not exists.
   *
   * @param fileFullPath path
   */
  @SneakyThrows
  public static void ensureFile(final String fileFullPath) {
    checkState(isNotBlank(fileFullPath), "file undefined. file: %s", fileFullPath);
    final File file = new File(fileFullPath);
    if (!file.exists()) {
      checkState(file.createNewFile(), "file create failed. file: %s", fileFullPath);
    }
    checkState(file.canRead() && file.canWrite(),
      "file can't read or write. file: %s", fileFullPath);
  }

  /**
   * Parse file lines.
   *
   * @param resource resource
   * @param mapper   line mapper
   * @param <T>      type
   * @return list
   */
  @SneakyThrows
  public static <T> List<T> parseFile(final Resource resource, final Function<String, T> mapper) {
    return parseFile(resource.getInputStream(), mapper);
  }

  /**
   * Parse file lines.
   *
   * @param inputStream stream
   * @param mapper      line mapper
   * @param <T>         type
   * @return list
   */
  @SneakyThrows
  public static <T> List<T> parseFile(final InputStream inputStream, final Function<String, T> mapper) {
    return IOUtils.readLines(inputStream, UTF_8).stream() //
      .filter(line -> isNotBlank(line) && line.trim().charAt(0) != '#') //
      .map(mapper) //
      .collect(toList());
  }

  public static class InterfaceFilter extends AbstractClassTestingTypeFilter {

    private final Class<?> type;
    private final boolean includeInterface;

    /**
     * filter find types of interface type.
     *
     * @param type             type
     * @param includeInterface include interface
     */
    public InterfaceFilter(final Class<?> type, final boolean includeInterface) {
      super();
      this.type = type;
      this.includeInterface = includeInterface;
    }

    @Override
    protected boolean match(final ClassMetadata metadata) {
      return contains(metadata.getInterfaceNames(), this.type.getName())
        && (this.includeInterface || !metadata.isInterface());
    }
  }

  public static class AssignableFilter extends AssignableTypeFilter {

    private final boolean includeInterface;
    private final boolean includeAbstract;

    public AssignableFilter(final Class<?> targetType, final boolean includeInterface, final boolean includeAbstract) {
      super(targetType);
      this.includeInterface = includeInterface;
      this.includeAbstract = includeAbstract;
    }

    @Override
    public boolean match(final MetadataReader reader, final MetadataReaderFactory readerFactory) throws IOException {
      final ClassMetadata metadata = reader.getClassMetadata();
      final boolean match = super.match(reader, readerFactory);
      return match && //
        (this.includeInterface || !metadata.isInterface()) && //
        (this.includeAbstract || !metadata.isAbstract());
    }
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  private static <T> Class<T> classForName(final String className) {
    return (Class<T>) Class.forName(className);
  }

  /**
   * scan.
   *
   * @param basePackage   from where (package)
   * @param includeFilter filter
   * @param <T>           type
   * @return classes found
   */
  public static <T> Set<Class<T>> scan(final String basePackage, final TypeFilter includeFilter) {
    checkArgument(isNotBlank(basePackage));
    // log.info("domainEnums basePackage: {}", basePackage);

    final ClassPathScanningCandidateProvider provider = new ClassPathScanningCandidateProvider();
    provider.addIncludeFilter(includeFilter);
    final Set<BeanDefinition> beanDefinitions =
      provider.findCandidateComponents(basePackage.replaceAll("\\.", "/"));

    final Set<Class<T>> result = newLinkedHashSetWithExpectedSize(beanDefinitions.size());
    for (final BeanDefinition beanDefinition : beanDefinitions) {
      result.add(classForName(beanDefinition.getBeanClassName()));
    }
    return result;
  }

  /**
   * A ClassPathScanningCandidateComponentProvider.
   *
   * @author zhanghaolun
   */
  public static class ClassPathScanningCandidateProvider
    extends ClassPathScanningCandidateComponentProvider {

    /**
     * new a ClassPathScanningCandidateProvider.
     */
    public ClassPathScanningCandidateProvider() {
      super(false);
    }

    @Override
    protected boolean isCandidateComponent(final AnnotatedBeanDefinition beanDefinition) {
      return beanDefinition.getMetadata().isIndependent();
    }
  }
}
