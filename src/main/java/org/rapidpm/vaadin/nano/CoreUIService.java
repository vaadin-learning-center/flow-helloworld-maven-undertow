/**
 * Copyright © 2017 Sven Ruppert (sven.ruppert@gmail.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rapidpm.vaadin.nano;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.redirect;
import static java.lang.Integer.valueOf;
import static java.lang.System.getProperty;
import static java.util.Collections.singleton;
import static org.rapidpm.frp.model.Result.failure;
import static org.rapidpm.frp.model.Result.success;

import java.util.Collections;
import java.util.Set;

import javax.servlet.ServletException;

import org.apache.commons.cli.*;
import org.rapidpm.dependencies.core.logger.HasLogger;
import org.rapidpm.frp.model.Result;
import com.vaadin.flow.server.startup.RouteRegistryInitializer;
import com.vaadin.flow.server.startup.ServletDeployer;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainerInitializerInfo;

/**
 *
 */
public class CoreUIService implements HasLogger {

  public static final String CORE_UI_SERVER_HOST_DEFAULT = "0.0.0.0";
  public static final String CORE_UI_SERVER_PORT_DEFAULT = "8080";

  public static final String CORE_UI_SERVER_HOST = "core-ui-server-host";
  public static final String CORE_UI_SERVER_PORT = "core-ui-server-port";

  public static final String CLI_HOST = "host";
  public static final String CLI_PORT = "port";


  public Result<Undertow> undertow = failure("not initialised so far");


  public static void main(String[] args) throws ParseException {

    Options options = new Options();
    options.addOption(CLI_HOST, true, "host to use");
    options.addOption(CLI_PORT, true, "port to use");

    CommandLineParser parser = new DefaultParser();
    CommandLine       cmd    = parser.parse(options, args);

    if( cmd.hasOption( CLI_HOST ) ) {
      System.setProperty(CORE_UI_SERVER_HOST, cmd.getOptionValue( CLI_HOST ));
    }
    if( cmd.hasOption( CLI_PORT ) ) {
      System.setProperty(CORE_UI_SERVER_PORT, cmd.getOptionValue( CLI_PORT));
    }

    new CoreUIService().startup();
  }

  public void startup() {
    final ClassLoader classLoader = CoreUIService.class.getClassLoader();
    DeploymentInfo servletBuilder
        = Servlets.deployment()
                  .setClassLoader(classLoader)
                  .setContextPath("/")
                  .setDeploymentName("ROOT.war")
                  .setDefaultEncoding("UTF-8")
                  .setResourceManager(new ClassPathResourceManager(classLoader, "META-INF/resources/"))
                  .addServletContainerInitializer(
                      new ServletContainerInitializerInfo(RouteRegistryInitializer.class ,
                                                          setOfRouteAnnotatedClasses())
                      )
                  .addListener(Servlets.listener(ServletDeployer.class));

    final DeploymentManager manager = Servlets
        .defaultContainer()
        .addDeployment(servletBuilder);
    manager.deploy();

    try {
      PathHandler path = path(redirect("/"))
          .addPrefixPath("/" , manager.start());
      Undertow u = Undertow.builder()
                           .addHttpListener(valueOf(getProperty(CORE_UI_SERVER_PORT ,
                                                                CORE_UI_SERVER_PORT_DEFAULT)) ,
                                            getProperty(CORE_UI_SERVER_HOST ,
                                                        CORE_UI_SERVER_HOST_DEFAULT)
                           )
                           .setHandler(path)
                           .build();
      u.start();

      u.getListenerInfo().forEach(e -> logger().info(e.toString()));

      undertow = success(u);
    } catch (ServletException e) {
      logger().warning(e.getMessage());
      undertow = failure(e.getMessage());
    }
  }

  public Set<Class<?>> setOfRouteAnnotatedClasses() {
    return singleton(VaadinApp.class);
  }
}
