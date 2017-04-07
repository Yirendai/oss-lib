package com.yirendai.oss.lib.webmvc.api;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.lang.Boolean.parseBoolean;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.Slf4jRequestLog;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.MovedContextHandler;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.core.env.Environment;

import java.io.File;
import java.util.Collection;

public interface Jetty9ServerCustomizers {

  @SuppressWarnings("unchecked")
  static <T> Collection<T> connectorConnectionFactories(final Connector connector,
    final Class<T> ofType) {
    final Collection<T> connectionFactories = newLinkedHashSet();
    final ConnectionFactory defaultConnectionFactory = connector.getDefaultConnectionFactory();
    if (defaultConnectionFactory != null
      && ofType.isAssignableFrom(defaultConnectionFactory.getClass())) {
      connectionFactories.add((T) defaultConnectionFactory);
    }
    connectionFactories.addAll(connector.getConnectionFactories().stream()
      .filter(connectionFactory -> ofType.isAssignableFrom(connectionFactory.getClass()))
      .map(connectionFactory -> (T) connectionFactory).collect(toList()));
    return connectionFactories;
  }

  static <T> Collection<T> serverConnectionFactories(final Server server, final Class<T> ofType) {
    return newLinkedHashSet(asList(server.getConnectors()).stream()
      .flatMap(connector -> connectorConnectionFactories(connector, ofType).stream())
      .collect(toList()));
  }

  static Collection<ServerConnector> serverConnectors(final Server server) {
    return newLinkedHashSet(
      asList(server.getConnectors()).stream().filter(c -> c instanceof ServerConnector)
        .map(connector -> (ServerConnector) connector).collect(toList()));
  }

  static JettyEmbeddedServletContainerFactory jetty(
    final ConfigurableEmbeddedServletContainer container) {
    if (container instanceof JettyEmbeddedServletContainerFactory) {
      return (JettyEmbeddedServletContainerFactory) container;
    } else {
      return null;
    }
  }

  @AllArgsConstructor(access = PRIVATE)
  class RedirectToHostRoot implements JettyServerCustomizer {

    @NonNull
    private final String host;
    @NonNull
    private final String root;

    @Override
    public void customize(final Server server) {
      final HandlerCollection handlers = new HandlerCollection();
      final MovedContextHandler movedContextHandler = new MovedContextHandler();
      movedContextHandler.setContextPath("/");
      movedContextHandler.setNewContextURL(this.root);
      movedContextHandler.setPermanent(true);
      movedContextHandler.setDiscardPathInfo(false);
      movedContextHandler.setDiscardQuery(false);
      movedContextHandler.setVirtualHosts(new String[]{this.host});
      handlers.addHandler(movedContextHandler);

      asList(server.getHandlers()).forEach(handlers::addHandler);
      server.setHandler(handlers);
    }
  }

  /**
   * see:
   * http://stackoverflow.com/questions/3539143/redirect-non-www-version-of-domain-to-www-in-jetty
   *
   * @param container container
   * @param host      host
   * @param root      root
   */
  static void redirectRootDomainToHostRoot( //
    final ConfigurableEmbeddedServletContainer container, final String host, final String root //
  ) {
    final JettyEmbeddedServletContainerFactory jetty = jetty(container);
    if (jetty == null) {
      return;
    }

    jetty.addServerCustomizers(new RedirectToHostRoot(host, root));
  }

  @AllArgsConstructor(access = PRIVATE)
  class AccessLog implements JettyServerCustomizer {

    private String applicationName;

    @Override
    public void customize(final Server server) {
      final HandlerCollection handlers = new HandlerCollection();
      // final RequestLog requestLog = slf4jLog();
      final RequestLog requestLog = ncsaRequestLog(applicationName);
      final RequestLogHandler logHandler = new RequestLogHandler();
      logHandler.setRequestLog(requestLog);
      handlers.addHandler(logHandler);

      asList(server.getHandlers()).forEach(handlers::addHandler);
      server.setHandler(handlers);
    }

    public static RequestLog slf4jLog() {
      final Slf4jRequestLog requestLog = new Slf4jRequestLog();
      return requestLog;
    }

    @SneakyThrows
    public static RequestLog ncsaRequestLog(String applicationName) {
      final String logPath = "./logs";
      FileUtils.forceMkdir(new File(logPath));

      final String logPattern = logPath + "/" + applicationName + "_access.yyyy_mm_dd.log";
      final NCSARequestLog requestLog = new NCSARequestLog(logPattern);
      requestLog.setLogDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      requestLog.setRetainDays(30);
      requestLog.setAppend(true);
      requestLog.setExtended(true);
      requestLog.setLogTimeZone("GMT+8");
      requestLog.setLogLatency(true);
      return requestLog;
    }
  }

  static void accessLog(final Environment environment,
    final ConfigurableEmbeddedServletContainer container) {
    final JettyEmbeddedServletContainerFactory jetty = jetty(container);
    if (jetty == null) {
      return;
    }
    String applicationName =
      environment.getProperty("spring.application.name", "$spring.application.name");
    jetty.addServerCustomizers(new AccessLog(applicationName));
  }

  @NoArgsConstructor(access = PRIVATE)
  @Slf4j
  class Gzip implements JettyServerCustomizer {

    @Override
    public void customize(final Server server) {
      try {
        Class.forName("org.eclipse.jetty.server.handler.gzip.GzipHandler");
      } catch (final ClassNotFoundException ignored) {
        log.debug("org.eclipse.jetty.server.handler.gzip.GzipHandler is not in classpath.", ignored);
        return;
      }

      final HandlerCollection handlers = new HandlerCollection();

      final GzipHandler gzipHandler = new GzipHandler();
      gzipHandler.addIncludedMethods(HttpMethod.GET.asString(), HttpMethod.POST.asString(),
        HttpMethod.PUT.asString(), HttpMethod.DELETE.asString());
      gzipHandler.setCompressionLevel(5);
      handlers.addHandler(gzipHandler);

      asList(server.getHandlers()).forEach(handlers::addHandler);
      server.setHandler(handlers);
    }
  }

  /**
   * 最后添加gzip.
   *
   * @param environment environment
   * @param container   container
   */
  static void gzip( //
    final Environment environment, final ConfigurableEmbeddedServletContainer container //
  ) {
    final JettyEmbeddedServletContainerFactory jetty = jetty(container);
    final Boolean gzipEnabled =
      parseBoolean(environment.getProperty("spring.http.gzip.enabled", "false"));
    if (gzipEnabled && jetty != null) {
      jetty.addServerCustomizers(new Gzip());
    }
  }

  @NoArgsConstructor(access = PRIVATE)
  class ForwardedRequest implements JettyServerCustomizer {

    @Override
    public void customize(final Server server) {
      final Collection<HttpConnectionFactory> httpConnectionFactories =
        serverConnectionFactories(server, HttpConnectionFactory.class);
      httpConnectionFactories.forEach(httpConnectionFactory -> {
        final HttpConfiguration httpConfiguration = httpConnectionFactory.getHttpConfiguration();
        httpConfiguration.addCustomizer(new ForwardedRequestCustomizer());
      });
    }
  }

  static void forwardedRequest(final Environment environment,
    final ConfigurableEmbeddedServletContainer container) {
    final JettyEmbeddedServletContainerFactory jetty = jetty(container);
    if (jetty != null) {
      jetty.addServerCustomizers(new ForwardedRequest());
    }
  }

  @NoArgsConstructor(access = PRIVATE)
  class HideServerInfo implements JettyServerCustomizer {

    @Override
    public void customize(final Server server) {
      final Collection<HttpConnectionFactory> httpConnectionFactories =
        serverConnectionFactories(server, HttpConnectionFactory.class);
      httpConnectionFactories.forEach(httpConnectionFactory -> {
        final HttpConfiguration httpConfiguration = httpConnectionFactory.getHttpConfiguration();
        httpConfiguration.setSendServerVersion(false);
        httpConfiguration.setSendXPoweredBy(false);
        httpConfiguration.setSendDateHeader(false);
      });
    }
  }

  static void hideServerInfo(final Environment environment,
    final ConfigurableEmbeddedServletContainer container) {
    final JettyEmbeddedServletContainerFactory jetty = jetty(container);
    if (jetty != null) {
      jetty.addServerCustomizers(new HideServerInfo());
    }
  }

  @NoArgsConstructor(access = PRIVATE)
  class NullSessionIdManager implements JettyServerCustomizer {

    @Override
    public void customize(final Server server) {
      server.setSessionIdManager(null);
    }
  }

  static void nullSessionIdManager(final Environment environment,
    final ConfigurableEmbeddedServletContainer container) {
    final JettyEmbeddedServletContainerFactory jetty = jetty(container);
    if (jetty != null) {
      jetty.addServerCustomizers(new NullSessionIdManager());
    }
  }

  @Deprecated
  @AllArgsConstructor
  class Pool implements JettyServerCustomizer {

    // @org.springframework.beans.factory.annotation.Value("${jetty.threadPool.maxThreads:200}")
    private final String maxThreads;
    // @org.springframework.beans.factory.annotation.Value("${jetty.threadPool.minThreads:8}")
    private final String minThreads;
    // @org.springframework.beans.factory.annotation.Value("${jetty.threadPool.idleTimeout:60000}")
    private final String idleTimeout;

    @Override
    public void customize(final Server server) {
      final QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
      threadPool.setMaxThreads(Integer.parseInt(this.maxThreads));
      threadPool.setMinThreads(Integer.parseInt(this.minThreads));
      threadPool.setIdleTimeout(Integer.parseInt(this.idleTimeout));
    }

    public static Pool smallPool() {
      return new Pool("1", "8", "60000");
    }
  }
}
