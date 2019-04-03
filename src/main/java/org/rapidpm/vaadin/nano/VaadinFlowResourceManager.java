package org.rapidpm.vaadin.nano;

import java.io.IOException;
import java.net.URL;

import io.undertow.UndertowMessages;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceChangeListener;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.resource.URLResource;

public class VaadinFlowResourceManager implements ResourceManager {

  /**
   * The class loader that is used to load resources
   */
  private final ClassLoader classLoader;
  /**
   * The prefix that is appended to resources that are to be loaded.
   */
  private final String prefix;

  public VaadinFlowResourceManager(final ClassLoader loader , final Package p) {
    this(loader , p.getName().replace("." , "/"));
  }

  public VaadinFlowResourceManager(final ClassLoader classLoader , final String prefix) {
    this.classLoader = classLoader;
    if (prefix.isEmpty()) {
      this.prefix = "";
    } else if (prefix.endsWith("/")) {
      this.prefix = prefix;
    } else {
      this.prefix = prefix + "/";
    }
  }

  public VaadinFlowResourceManager(final ClassLoader classLoader) {
    this(classLoader , "");
  }

  @Override
  public Resource getResource(final String path) throws IOException {
    String modPath = path;
    if (modPath.startsWith("/")) {
      modPath = path.substring(1);
    }
    if (modPath.startsWith("webjars")) {
      modPath = "META-INF/resources/" + modPath;
    }
    if (modPath.startsWith("VAADIN/static/client")) {
      modPath = "META-INF/resources/" + modPath;
    }
    final String realPath = prefix + modPath;
    final URL resource = classLoader.getResource(realPath);
    if (resource == null) {
      return null;
    } else {
      return new URLResource(resource , path);
    }

  }

  @Override
  public boolean isResourceChangeListenerSupported() {
    return false;
  }

  @Override
  public void registerResourceChangeListener(ResourceChangeListener listener) {
    throw UndertowMessages.MESSAGES.resourceChangeListenerNotSupported();
  }

  @Override
  public void removeResourceChangeListener(ResourceChangeListener listener) {
    throw UndertowMessages.MESSAGES.resourceChangeListenerNotSupported();
  }

  @Override
  public void close() throws IOException {
  }
}
