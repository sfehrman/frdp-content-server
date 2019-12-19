/*
 * Copyright (c) 2018-2019, ForgeRock, Inc., All rights reserved
 * Use subject to license terms.
 */

package com.forgerock.frdp.contentserver.rest;

import com.forgerock.frdp.common.ConstantsIF;
import com.forgerock.frdp.common.CoreIF;
import com.forgerock.frdp.dao.DataAccessIF;
import com.forgerock.frdp.dao.Operation;
import com.forgerock.frdp.dao.OperationIF;
import com.forgerock.frdp.dao.mongo.MongoDataAccess;
import com.forgerock.frdp.rest.Resource;
import com.forgerock.frdp.utils.STR;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Implementation of Data resource using the JAX-RS / Jersey REST API. The
 * deployment path is .../<deployment_path>/data
 * 
 * <pre>
 * POST .../data
 * GET .../data
 * GET .../data/{id}
 * PUT .../data/{id}
 * DELETE .../data/{id}
 * </pre>
 * 
 * @author Scott Fehrman, ForgeRock, Inc.
 *
 */
@Path(ConstantsIF.DATA)
public class DataResource extends Resource {

   @Context
   private UriInfo _uriInfo;
   @Context
   private ServletContext _servletCtx;
   private String _databaseName = null;
   private String _collectionName = null;
   private final String CLASS = this.getClass().getName();
   private static final String PROP_FILE = "content-server.properties";
   private static final String CTX_ATTR_DAO = "com.forgerock.frdp.dao";
   // private static final String CTX_ATTR_PARSER = "com.forgerock.frdp.parser";
   private static final String CTX_ATTR_MONGO_DATABASE = "com.forgerock.frdp.mongo.database";
   private static final String CTX_ATTR_MONGO_COLLECTION = "com.forgerock.frdp.mongo.collection";
   private static final String PARAM_APPLICATION_DATABASE = "application.database";
   private static final String PARAM_APPLICAITON_COLLECTION = "application.collection";

   public DataResource() {
      super();

      String METHOD = "DataResource()";

      _logger.entering(CLASS, METHOD);
      _logger.exiting(CLASS, METHOD);

      return;
   }

   /**
    * diable the copying of object
    */
   @Override
   public CoreIF copy() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Create a JSON document. Data is provided in the body.
    * 
    * @param str String representation of JSON document
    * @return Response HTTP response
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   public Response create(String str) {
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
         _logger.log(DEBUG_LEVEL, "json=''{0}''", new Object[] { str != null ? str : NULL });
      }

      if (STR.isEmpty(str)) {
         this.abort(METHOD, "Payload string is empty", Status.BAD_REQUEST);
      }

      // Convert the String (data) into a JSONObject

      parser = this.getParserFromCtx(_servletCtx);

      try {
         jsonData = (JSONObject) parser.parse(str);
      } catch (Exception ex) {
         this.abort(METHOD, "Could not parser String to JSON: '" + str + "'", Status.INTERNAL_SERVER_ERROR);
      }

      // Build "create" Operation and execute using DAO

      dao = this.getDAOFromCtx();

      jsonInput = new JSONObject();
      jsonInput.put(ConstantsIF.DATA, jsonData);

      operInput = new Operation(OperationIF.TYPE.CREATE);
      operInput.setParam(MongoDataAccess.PARAM_DATABASE, _databaseName);
      operInput.setParam(MongoDataAccess.PARAM_COLLECTION, _collectionName);
      operInput.setJSON(jsonInput);

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

      dao = this.getDAOFromCtx();

      jsonQuery = new JSONObject();
      jsonQuery.put(ConstantsIF.OPERATOR, ConstantsIF.ALL);

      jsonInput = new JSONObject();
      jsonInput.put(ConstantsIF.QUERY, jsonQuery);

      operInput = new Operation(OperationIF.TYPE.SEARCH);
      operInput.setParam(MongoDataAccess.PARAM_DATABASE, _databaseName);
      operInput.setParam(MongoDataAccess.PARAM_COLLECTION, _collectionName);
      operInput.setJSON(jsonInput);

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
         this.abort(METHOD, "Path parameter '" + ConstantsIF.UID + "' is empty", Status.BAD_REQUEST);
      }

      // Build "read" Operation and execute using DAO

      dao = this.getDAOFromCtx();

      jsonInput = new JSONObject();
      jsonInput.put(ConstantsIF.UID, uid);

      operInput = new Operation(OperationIF.TYPE.READ);
      operInput.setParam(MongoDataAccess.PARAM_DATABASE, _databaseName);
      operInput.setParam(MongoDataAccess.PARAM_COLLECTION, _collectionName);
      operInput.setJSON(jsonInput);

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
   public Response replace(String str, @PathParam(ConstantsIF.UID) String uid) {
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
         _logger.log(DEBUG_LEVEL, "uid=''{0}''", new Object[] { uid != null ? uid : NULL });
      }

      if (STR.isEmpty(str)) {
         this.abort(METHOD, "Payload string is empty", Status.BAD_REQUEST);
      }

      if (STR.isEmpty(uid)) {
         this.abort(METHOD, "Path parameter '" + ConstantsIF.UID + "' is empty", Status.BAD_REQUEST);
      }

      parser = this.getParserFromCtx(_servletCtx);

      try {
         jsonData = (JSONObject) parser.parse(str);
      } catch (Exception ex) {
         this.abort(METHOD, "Could not parser String to JSON: '" + str + "', " + ex.getMessage(),
               Status.INTERNAL_SERVER_ERROR);
      }

      // Build "replace" Operation and execute using DAO

      dao = this.getDAOFromCtx();

      jsonInput = new JSONObject();
      jsonInput.put(ConstantsIF.UID, uid);
      jsonInput.put(ConstantsIF.DATA, jsonData);

      operInput = new Operation(OperationIF.TYPE.REPLACE);
      operInput.setParam(MongoDataAccess.PARAM_DATABASE, _databaseName);
      operInput.setParam(MongoDataAccess.PARAM_COLLECTION, _collectionName);
      operInput.setJSON(jsonInput);

      operOutput = dao.execute(operInput);

      // if the response is "NOTEXIST", then use "create"

      if (operOutput.getState() == STATE.NOTEXIST) {
         operInput = new Operation(OperationIF.TYPE.CREATE);
         operInput.setParam(MongoDataAccess.PARAM_DATABASE, _databaseName);
         operInput.setParam(MongoDataAccess.PARAM_COLLECTION, _collectionName);
         operInput.setJSON(jsonInput);

         operOutput = dao.execute(operInput);

         // Flag operation was replace, this is required to get proper response

         operOutput.setObject(true);
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
         this.abort(METHOD, "Path parameter '" + ConstantsIF.UID + "' is empty", Status.BAD_REQUEST);
      }

      // Build "delete" Operation and execute using DAO

      dao = this.getDAOFromCtx();

      jsonInput = new JSONObject();
      jsonInput.put(ConstantsIF.UID, uid);

      operInput = new Operation(OperationIF.TYPE.DELETE);
      operInput.setParam(MongoDataAccess.PARAM_DATABASE, _databaseName);
      operInput.setParam(MongoDataAccess.PARAM_COLLECTION, _collectionName);
      operInput.setJSON(jsonInput);

      operOutput = dao.execute(operInput);

      // Get the HTTP response

      response = this.getResponseFromJSON(_uriInfo, operOutput);

      _logger.exiting(CLASS, METHOD);

      return response;
   }

   /**
    * Get the MongoDB Data Access Object from the Servlet Context.
    * 
    * @return DataAccessIF DAO for MongoDB
    */
   private DataAccessIF getDAOFromCtx() {
      Object obj = null;
      String METHOD = Thread.currentThread().getStackTrace()[1].getMethodName();
      DataAccessIF dao = null;

      _logger.entering(CLASS, METHOD);

      obj = _servletCtx.getAttribute(CTX_ATTR_DAO);
      if (obj != null && obj instanceof DataAccessIF) {
         dao = (DataAccessIF) obj;

         if (dao.getState() != STATE.READY) {
            this.abort(METHOD, "Data Access Object is not ready", Status.INTERNAL_SERVER_ERROR);
         }

         _databaseName = (String) _servletCtx.getAttribute(CTX_ATTR_MONGO_DATABASE);
         _collectionName = (String) _servletCtx.getAttribute(CTX_ATTR_MONGO_COLLECTION);
      } else {
         try {
            this.loadParamsFromProperties(PROP_FILE);
            dao = new MongoDataAccess();
            dao.setParams(this.getParams());
            _databaseName = this.getParamNotEmpty(PARAM_APPLICATION_DATABASE);
            _collectionName = this.getParamNotEmpty(PARAM_APPLICAITON_COLLECTION);
         } catch (Exception ex) {
            this.abort(METHOD, ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
         }
         _servletCtx.setAttribute(CTX_ATTR_DAO, dao);
         _servletCtx.setAttribute(CTX_ATTR_MONGO_DATABASE, _databaseName);
         _servletCtx.setAttribute(CTX_ATTR_MONGO_COLLECTION, _collectionName);
      }

      _logger.exiting(CLASS, METHOD);

      return dao;
   }
}
