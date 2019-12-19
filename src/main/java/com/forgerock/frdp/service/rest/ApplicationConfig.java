/*
 * Copyright (c) 2018-2019, ForgeRock, Inc., All rights reserved
 * Use subject to license terms.
 */

package com.forgerock.frdp.service.rest;

import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * @author Scott Fehrman, ForgeRock, Inc.
 */
@ApplicationPath("rest")
public class ApplicationConfig extends Application {

   @Override
   public Set<Class<?>> getClasses() {
      Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
      addRestResourceClasses(resources);
      return resources;
   }

   /**
    * Do not modify addRestResourceClasses() method. It is automatically populated
    * with all resources defined in the project. If required, comment out calling
    * this method in getClasses().
    */
   private void addRestResourceClasses(Set<Class<?>> resources) {
      resources.add(com.forgerock.frdp.service.rest.JsonResource.class);
   }

}
