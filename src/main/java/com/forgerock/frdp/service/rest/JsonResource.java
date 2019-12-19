/*
 * Copyright (c) 2018-2019, ForgeRock, Inc., All rights reserved
 * Use subject to license terms.
 */

package com.forgerock.frdp.service.rest;

import com.forgerock.frdp.common.BasicData;
import com.forgerock.frdp.common.ConstantsIF;
import com.forgerock.frdp.common.DataIF;
import com.forgerock.frdp.dao.DAOManager;
import com.forgerock.frdp.dao.DAOManagerIF;
import com.forgerock.frdp.dao.DataAccessIF;
import com.forgerock.frdp.dao.Operation;
import com.forgerock.frdp.dao.OperationIF;
import com.forgerock.frdp.dao.mongo.MongoDataAccess;
import com.forgerock.frdp.rest.Resource;
import com.forgerock.frdp.utils.STR;
import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Implementation of Data resource using the JAX-RS / Jersey REST API. The
 * deployment path is .../<deployment_path>/data
 *
 * <pre>
 * POST .../rest/json/{database}/{collection}
 * GET .../rest/json/{database}/{collection}
 * GET .../rest/json/{database}/{collection}/{id}
 * PUT .../rest/json/{database}/{collection}/{id}
 * DELETE .../rest/json/{database}/{collection}/{id}
 * </pre>
 * 
 * @author Scott Fehrman, ForgeRock, Inc.
 *
 */
@Path("{" + ConstantsIF.DATABASE + "}/{" + ConstantsIF.COLLECTION + "}")
public class JsonResource extends Resource {

   @Context
   private UriInfo _uriInfo;
   @Context
   private ServletContext _servletCtx;
   private final String CLASS = this.getClass().getName();
   private static final String CTX_ATTR_DAO_MGR = "com.forgerock.frdp.dao.mgr";
   private static final String PARAM_PUT_CREATE = "put.create";
   private static final Boolean DEFAULT_PUT_CREATE = false;

   /**
    * Default constructor
    */
   public JsonResource() {
      super();

      String METHOD = "JsonResource()";

      _logger.entering(CLASS, METHOD);
      _logger.exiting(CLASS, METHOD);

      return;
   }

   /**
    * Create a JSON document. Data is provided in the body.
    * 
    * @param data String representation of JSON document
    * @return Response HTTP response
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   public Response create(String data) {
      String METHOD = Thread.currentThread().getStackTrace()[1].getMethodName();
      Response response = null;
      DataAccessIF dao = null;
      JSONObject jsonData = null;
      JSONObject jsonInput = null;
      OperationIF operInput = null;
      OperationIF operOutput = null;
      JSONParser parser = null;

      _logger.entering(CLASS, METHOD);

      if (_logger.isLoggable(DEBUG_LEVEL)) {
         _logger.log(DEBUG_LEVEL, "json=''{0}''", new Object[] { data != null ? data : NULL });
      }

      if (STR.isEmpty(data)) {
         this.abort(CLASS + ": " + METHOD, "Payload string is empty", Status.BAD_REQUEST);
      }

      // Convert the String (data) into a JSONObject

      parser = this.getParserFromCtx(_servletCtx);

      try {
         jsonData = (JSONObject) parser.parse(data);
      } catch (Exception ex) {
         this.abort(CLASS + ": " + METHOD, "Could not parser String to JSON: '" + data + "'", Status.BAD_REQUEST);
      }

      // Build "create" Operation and execute using DAO

      dao = this.getDAO();

      jsonInput = new JSONObject();
      jsonInput.put(ConstantsIF.DATA, jsonData);

      operInput = new Operation(OperationIF.TYPE.CREATE);
      operInput.setJSON(jsonInput);

      this.setParameters(operInput);

      operOutput = dao.execute(operInput);

      // Get the HTTP response

      response = this.getResponseFromJSON(_uriInfo, operOutput);

      _logger.exiting(CLASS, METHOD);

      return response;
   }

   /**
    * Search JSON documents. This implementation currently returns all documents.
    *
    * @return Response HTTP response
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Response search() {
      String METHOD = Thread.currentThread().getStackTrace()[1].getMethodName();
      Response response = null;
      DataAccessIF dao = null;
      JSONObject jsonQuery = null;
      JSONObject jsonInput = null;
      OperationIF operInput = null;
      OperationIF operOutput = null;

      _logger.entering(CLASS, METHOD);

      // Build "search" Operation and execute using DAO

      dao = this.getDAO();

      jsonQuery = new JSONObject();
      jsonQuery.put(ConstantsIF.OPERATOR, ConstantsIF.ALL);

      jsonInput = new JSONObject();
      jsonInput.put(ConstantsIF.QUERY, jsonQuery);

      operInput = new Operation(OperationIF.TYPE.SEARCH);
      operInput.setJSON(jsonInput);

      this.setParameters(operInput);

      operOutput = dao.execute(operInput);

      // Get the HTTP response

      response = this.getResponseFromJSON(_uriInfo, operOutput);

      _logger.exiting(CLASS, METHOD);

      return response;
   }

   /**
    * Read a JSON document.
    * 
    * @param uid String document uid, via path parameter
    * @return Response http response
    */
   @GET
   @Path("{" + ConstantsIF.UID + "}")
   @Produces(MediaType.APPLICATION_JSON)
   public Response read(@PathParam(ConstantsIF.UID) String uid) {
      String METHOD = Thread.currentThread().getStackTrace()[1].getMethodName();
      Response response = null;
      DataAccessIF dao = null;
      JSONObject jsonInput = null;
      OperationIF operInput = null;
      OperationIF operOutput = null;

      _logger.entering(CLASS, METHOD);

      if (_logger.isLoggable(DEBUG_LEVEL)) {
         _logger.log(DEBUG_LEVEL, "uid=''{0}''", new Object[] { uid != null ? uid : NULL });
      }

      if (STR.isEmpty(uid)) {
         this.abort(CLASS + ": " + METHOD, "Path parameter '" + ConstantsIF.UID + "' is empty", Status.BAD_REQUEST);
      }

      // Build "read" Operation and execute using DAO

      dao = this.getDAO();

      jsonInput = new JSONObject();
      jsonInput.put(ConstantsIF.UID, uid);

      operInput = new Operation(OperationIF.TYPE.READ);
      operInput.setJSON(jsonInput);

      this.setParameters(operInput);

      operOutput = dao.execute(operInput);

      // Get the HTTP response

      response = this.getResponseFromJSON(_uriInfo, operOutput);

      _logger.exiting(CLASS, METHOD);

      return response;
   }

   /**
    * Replace the JSON document.
    * 
    * @param str String representation of JSON document
    * @param uid String document uid, via path parameter
    * @return Response http response
    */
   @PUT
   @Path("{" + ConstantsIF.UID + "}")
   @Consumes(MediaType.APPLICATION_JSON)
   public Response replace(String data, @PathParam(ConstantsIF.UID) String uid) {
      Boolean bPutCreate = DEFAULT_PUT_CREATE;
      String METHOD = Thread.currentThread().getStackTrace()[1].getMethodName();
      String str = null;
      Response response = null;
      DataAccessIF dao = null;
      JSONObject jsonData = null;
      JSONObject jsonInput = null;
      OperationIF operInput = null;
      OperationIF operOutput = null;
      JSONParser parser = null;

      _logger.entering(CLASS, METHOD);

      if (_logger.isLoggable(DEBUG_LEVEL)) {
         _logger.log(DEBUG_LEVEL, "uid=''{0}'', json=''{1}''",
               new Object[] { uid != null ? uid : NULL, data != null ? data : NULL });
      }

      if (STR.isEmpty(data)) {
         this.abort(CLASS + ": " + METHOD, "Payload string is empty", Status.BAD_REQUEST);
      }

      if (STR.isEmpty(uid)) {
         this.abort(CLASS + ": " + METHOD, "Path parameter '" + ConstantsIF.UID + "' is empty", Status.BAD_REQUEST);
      }

      parser = this.getParserFromCtx(_servletCtx);

      try {
         jsonData = (JSONObject) parser.parse(data);
      } catch (Exception ex) {
         this.abort(CLASS + ": " + METHOD, "Could not parser String to JSON: '" + data + "', " + ex.getMessage(),
               Status.BAD_REQUEST);
      }

      // Build "replace" Operation and execute using DAO

      dao = this.getDAO();

      jsonInput = new JSONObject();
      jsonInput.put(ConstantsIF.UID, uid);
      jsonInput.put(ConstantsIF.DATA, jsonData);

      operInput = new Operation(OperationIF.TYPE.REPLACE);
      operInput.setJSON(jsonInput);

      this.setParameters(operInput);

      operOutput = dao.execute(operInput);

      /*
       * if the response is "NOTEXIST", then do a create
       */
      if (operOutput.getState() == STATE.NOTEXIST) {
         /*
          * Check DAO, is param "put.create" == true
          */

         str = dao.getParam(PARAM_PUT_CREATE);
         if (!STR.isEmpty(str)) {
            bPutCreate = Boolean.parseBoolean(str);
         }

         if (bPutCreate) {
            operInput = new Operation(OperationIF.TYPE.CREATE);
            operInput.setJSON(jsonInput);

            this.setParameters(operInput);

            operOutput = dao.execute(operInput);

            // Flag operation was replace, this is required to get proper response

            operOutput.setObject(true);
         }
      }

      // Get the HTTP response

      response = this.getResponseFromJSON(_uriInfo, operOutput);

      _logger.exiting(CLASS, METHOD);

      return response;
   }

   /**
    * Delete the JSON document.
    * 
    * @param uid String document uid, via path parameter
    * @return Response http response
    */
   @DELETE
   @Path("{" + ConstantsIF.UID + "}")
   public Response delete(@PathParam(ConstantsIF.UID) String uid) {
      String METHOD = Thread.currentThread().getStackTrace()[1].getMethodName();
      Response response = null;
      DataAccessIF dao = null;
      JSONObject jsonInput = null;
      OperationIF operInput = null;
      OperationIF operOutput = null;

      _logger.entering(CLASS, METHOD);

      if (_logger.isLoggable(DEBUG_LEVEL)) {
         _logger.log(DEBUG_LEVEL, "uid=''{0}''", new Object[] { uid != null ? uid : NULL });
      }

      if (STR.isEmpty(uid)) {
         this.abort(CLASS + ": " + METHOD, "Path parameter '" + ConstantsIF.UID + "' is empty", Status.BAD_REQUEST);
      }

      // Build "delete" Operation and execute using DAO

      dao = this.getDAO();

      jsonInput = new JSONObject();
      jsonInput.put(ConstantsIF.UID, uid);

      operInput = new Operation(OperationIF.TYPE.DELETE);
      operInput.setJSON(jsonInput);

      this.setParameters(operInput);

      operOutput = dao.execute(operInput);

      // Get the HTTP response

      response = this.getResponseFromJSON(_uriInfo, operOutput);

      _logger.exiting(CLASS, METHOD);

      return response;
   }

   /*
    * =============== PRIVATE METHODS ===============
    */

   /**
    * Get the MongoDB Data Access Object from the Servlet Context.
    * 
    * @return DataAccessIF DAO for MongoDB
    */
   private DataAccessIF getDAO() {
      Object obj = null;
      String METHOD = Thread.currentThread().getStackTrace()[1].getMethodName();
      String pathDb = null;
      String pathColl = null;
      String daoName = null;
      DataAccessIF dao = null;
      DataIF config = null;
      DAOManagerIF daoMgr = null;

      _logger.entering(CLASS, METHOD);

      pathDb = this.getParamPathValue(ConstantsIF.DATABASE);
      pathColl = this.getParamPathValue(ConstantsIF.COLLECTION);

      daoName = pathDb + "/" + pathColl;

      obj = _servletCtx.getAttribute(CTX_ATTR_DAO_MGR);
      if (obj != null && obj instanceof DAOManagerIF) {
         daoMgr = (DAOManagerIF) obj;
      } else {
         daoMgr = new DAOManager();

         _servletCtx.setAttribute(CTX_ATTR_DAO_MGR, daoMgr);
      }

      if (daoMgr.containsDAO(daoName)) {
         dao = daoMgr.getDAO(daoName);
      } else {
         config = this.getConfigDataForDAO(pathDb, pathColl);
         try {
            dao = new MongoDataAccess();
            dao.setParams(config.getParams());
         } catch (Exception ex) {
            this.abort(METHOD, ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
         }

         daoMgr.setDAO(daoName, dao);
      }

      _logger.exiting(CLASS, METHOD);

      return dao;
   }

   /**
    * Get configuration data based on the URL path parameters.
    * 
    * <pre>
    * .../rest/{database}/{collection}
    * </pre>
    * 
    * @param database   String MongoDB database name
    * @param collection String MongoDB collection name
    * @return DataIF configuration data
    */
   private DataIF getConfigDataForDAO(final String database, final String collection) {
      boolean foundDb = false;
      boolean foundColl = false;
      byte[] bytes = null;
      Object obj = null;
      Boolean bPutCreate = DEFAULT_PUT_CREATE;
      String METHOD = Thread.currentThread().getStackTrace()[1].getMethodName();
      String value = null;
      String realPath = null;
      String configFolder = null;
      String configName = null;
      String configFile = null;
      java.nio.file.Path pathConfigs = null;
      java.nio.file.Path pathConfigFile = null;
      DirectoryStream<java.nio.file.Path> dirStreamThemes = null;
      JSONParser parser = null;
      JSONObject jsonConfig = null;
      JSONObject jsonAuthen = null;
      JSONObject jsonCollections = null;
      JSONObject jsonCollection = null;
      JSONObject jsonMethod = null;
      DataIF config = null;

      _logger.entering(CLASS, METHOD);

      if (_logger.isLoggable(DEBUG_LEVEL)) {
         _logger.log(DEBUG_LEVEL, "database=''{0}'', collection=''{1}''",
               new Object[] { database != null ? database : NULL, collection != null ? collection : NULL });
      }

      if (STR.isEmpty(database) || STR.isEmpty(collection)) {
         this.abort(CLASS + ": " + METHOD, "Argument 'database' or 'collection' is empty.", Status.BAD_REQUEST);
      }

      parser = this.getParserFromCtx(_servletCtx);

      realPath = _servletCtx.getRealPath("/");

      configFolder = realPath + "WEB-INF" + File.separator + ConstantsIF.CONFIG;

      pathConfigs = Paths.get(configFolder);

      try {
         dirStreamThemes = Files.newDirectoryStream(pathConfigs);
         for (java.nio.file.Path pathTheme : dirStreamThemes) {
            if (!Files.isDirectory(pathTheme) && !Files.isHidden(pathTheme)) {
               configName = pathTheme.getFileName().toString();

               configFile = configFolder + File.separator + configName;

               pathConfigFile = Paths.get(configFile);
               bytes = Files.readAllBytes(pathConfigFile);
               obj = parser.parse(new String(bytes));

               if (obj != null && obj instanceof JSONObject) {
                  jsonConfig = (JSONObject) obj;

                  obj = jsonConfig.get(ConstantsIF.DATABASE);
                  if (obj != null && obj instanceof String && !STR.isEmpty((String) obj)) {
                     value = (String) obj;
                     if (value.equalsIgnoreCase(database)) {
                        foundDb = true;
                        break;
                     }
                  } else {
                     this.abort(CLASS + ": " + METHOD, "Attribute '" + ConstantsIF.DATABASE + "' is missing.",
                           Status.BAD_REQUEST);
                  }
               } else {
                  this.abort(CLASS + ": " + METHOD, "Config file '" + configName + "' is not a JSON Object",
                        Status.BAD_REQUEST);
               }
            }
         }
      } catch (Exception ex) {
         this.abort(CLASS + ": " + METHOD, ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
      }

      if (foundDb) {
         /*
          * Find a matching "collection"
          */

         obj = jsonConfig.get(ConstantsIF.COLLECTIONS);
         if (obj != null && obj instanceof JSONObject) {
            jsonCollections = (JSONObject) obj;

            obj = jsonCollections.get(collection);
            if (obj != null && obj instanceof JSONObject) {
               foundColl = true;

               jsonCollection = (JSONObject) obj;

               obj = jsonCollection.get(ConstantsIF.PUT);
               if (obj != null && obj instanceof JSONObject) {
                  jsonMethod = (JSONObject) obj;

                  obj = jsonMethod.get(ConstantsIF.CREATE);
                  if (obj != null && obj instanceof Boolean) {
                     bPutCreate = (Boolean) obj;
                  }
               }

            } else {
               this.abort(CLASS + ": " + METHOD, "Collection '" + collection + "' does not exist", Status.BAD_REQUEST);
            }
         } else {
            this.abort(CLASS + ": " + METHOD, "Object '" + ConstantsIF.COLLECTIONS + "' is missing or empty",
                  Status.BAD_REQUEST);
         }

         if (foundColl) {
            config = new BasicData();

            config.setParam(ConstantsIF.DATABASE, database);
            config.setParam(ConstantsIF.COLLECTION, collection);
            config.setParam(PARAM_PUT_CREATE, bPutCreate.toString());

            obj = jsonConfig.get(ConstantsIF.HOST);
            if (obj != null && obj instanceof String && !STR.isEmpty((String) obj)) {
               config.setParam(ConstantsIF.HOST, (String) obj);
            } else {
               this.abort(CLASS + ": " + METHOD, "Attribute '" + ConstantsIF.HOST + "' is missing.",
                     Status.BAD_REQUEST);
            }

            obj = jsonConfig.get(ConstantsIF.PORT);
            if (obj != null && obj instanceof String && !STR.isEmpty((String) obj)) {
               config.setParam(ConstantsIF.PORT, (String) obj);
            } else {
               this.abort(CLASS + ": " + METHOD, "Attribute '" + ConstantsIF.PORT + "' is missing.",
                     Status.BAD_REQUEST);
            }

            obj = jsonConfig.get(ConstantsIF.AUTHEN);
            if (obj != null && obj instanceof JSONObject) {
               jsonAuthen = (JSONObject) obj;
            } else {
               this.abort(CLASS + ": " + METHOD, "JSON Object '" + ConstantsIF.AUTHEN + "' is missing.",
                     Status.BAD_REQUEST);
            }

            obj = jsonAuthen.get(ConstantsIF.USER);
            if (obj != null && obj instanceof String && !STR.isEmpty((String) obj)) {
               config.setParam(ConstantsIF.AUTHEN + "." + ConstantsIF.USER, (String) obj);
            } else {
               this.abort(CLASS + ": " + METHOD,
                     "Attribute '" + ConstantsIF.AUTHEN + "." + ConstantsIF.USER + "' is missing.", Status.BAD_REQUEST);
            }

            obj = jsonAuthen.get(ConstantsIF.PASSWORD);
            if (obj != null && obj instanceof String && !STR.isEmpty((String) obj)) {
               config.setParam(ConstantsIF.AUTHEN + "." + ConstantsIF.PASSWORD, (String) obj);
            } else {
               this.abort(CLASS + ": " + METHOD,
                     "Attribute '" + ConstantsIF.AUTHEN + "." + ConstantsIF.PASSWORD + "' is missing.",
                     Status.BAD_REQUEST);
            }

            obj = jsonAuthen.get(ConstantsIF.DATABASE);
            if (obj != null && obj instanceof String && !STR.isEmpty((String) obj)) {
               config.setParam(ConstantsIF.AUTHEN + "." + ConstantsIF.DATABASE, (String) obj);
            } else {
               this.abort(CLASS + ": " + METHOD,
                     "Attribute '" + ConstantsIF.AUTHEN + "." + ConstantsIF.DATABASE + "' is missing.",
                     Status.BAD_REQUEST);
            }
         } else {
            this.abort(CLASS + ": " + METHOD,
                  "Collection '" + collection + "' not found for database '" + database + "'",
                  Status.INTERNAL_SERVER_ERROR);
         }
      } else {
         this.abort(CLASS + ": " + METHOD, "Config file not found for database '" + database + "'",
               Status.INTERNAL_SERVER_ERROR);
      }

      _logger.exiting(CLASS, METHOD);

      return config;
   }

   /**
    * Get the value of the URL parameter path name
    * 
    * @param name String name of the parameter path
    * @return String parameter path value
    */
   private String getParamPathValue(final String name) {
      Object obj = null;
      String METHOD = Thread.currentThread().getStackTrace()[1].getMethodName();
      String value = null;
      MultivaluedMap mvMap = null;
      List list = null;

      _logger.entering(CLASS, METHOD);

      if (STR.isEmpty(name)) {
         this.abort(CLASS + ": " + METHOD, ": Path parameter name is empty.", Status.BAD_REQUEST);
      }

      mvMap = _uriInfo.getPathParameters();

      if (mvMap == null || mvMap.size() == 0) {
         this.abort(CLASS + ": " + METHOD, ": Path parameters are empty.", Status.BAD_REQUEST);
      }

      obj = mvMap.get(name);

      if (obj != null && obj instanceof List) {
         list = (List) obj;
         obj = list.get(0);
         if (obj != null && obj instanceof String && !STR.isEmpty((String) obj)) {
            value = (String) obj;
         } else {
            this.abort(CLASS + ": " + METHOD, ": Path value for '" + name + "' is empty.", Status.BAD_REQUEST);
         }
      } else {
         this.abort(CLASS + ": " + METHOD, ": Path for '" + name + "' is missing.", Status.BAD_REQUEST);
      }

      if (_logger.isLoggable(DEBUG_LEVEL)) {
         _logger.log(DEBUG_LEVEL, "name=''{0}'', value=''{1}''",
               new Object[] { name != null ? name : NULL, value != null ? value : NULL });
      }

      _logger.exiting(CLASS, METHOD);

      return value;
   }

   /**
    * Set the MongoDB database and collection in the OperationIF object
    * 
    * @param oper OpertaionIF object
    */
   private void setParameters(final OperationIF oper) {
      Object obj = null;
      String METHOD = Thread.currentThread().getStackTrace()[1].getMethodName();
      MultivaluedMap pathParams = null;
      List list = null;

      _logger.entering(CLASS, METHOD);

      pathParams = _uriInfo.getPathParameters();

      if (pathParams != null && !pathParams.isEmpty()) {
         obj = pathParams.get(ConstantsIF.DATABASE);
         if (obj != null && obj instanceof List && !((List) obj).isEmpty()) {
            list = (List) obj;
            obj = list.get(0);

            if (obj != null && obj instanceof String && !STR.isEmpty((String) obj)) {
               oper.setParam(MongoDataAccess.PARAM_DATABASE, (String) obj);
            }
         }
         obj = pathParams.get(ConstantsIF.COLLECTION);
         if (obj != null && obj instanceof List && !((List) obj).isEmpty()) {
            list = (List) obj;
            obj = list.get(0);

            if (obj != null && obj instanceof String && !STR.isEmpty((String) obj)) {
               oper.setParam(MongoDataAccess.PARAM_COLLECTION, (String) obj);
            }
         }
      }

      _logger.exiting(CLASS, METHOD);

      return;
   }
}
