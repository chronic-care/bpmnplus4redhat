package com.myspace.diabetesstrawman;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jbpm.process.workitem.rest.RESTWorkItemHandler;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MDMIWorkItemHandler extends RESTWorkItemHandler {

	private static final Logger logger = LoggerFactory.getLogger(MDMIWorkItemHandler.class);
	
	 String resultClass;
	 String content;
	 
	  String source ; 
      protected void setSource(String source) {
		this.source = source;
	}


	protected void setTarget(String target) {
		this.target = target;
	}


	String target;
	
	Object sourceInstance = null;
	
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		
		
        source = (String) workItem.getParameter("source");
        target = (String) workItem.getParameter("target");
        sourceInstance =  workItem.getParameter("sourceInstance");
        resultClass = (String) workItem.getParameter("ResultClass");
        
         logger.info("url is "+(String) workItem.getParameter("Url"));
      
         
        logger.info("source is "+source);
        logger.info("target is "+target);
        logger.info("sourceInstance is "+sourceInstance);
        logger.info("resultClass is "+resultClass);
        super.executeWorkItem(workItem, manager);
	}
	
	
	@Override
	protected Object transformResult(Class<?> clazz, String contentType, String content) throws Exception {
		
		this.content = content;
		if (sourceInstance == null) {
			return super.transformResult(clazz, "application/json", runTransformation(content));	
		} else
		{
			 logger.info("contentType is "+contentType);
			 logger.info("content is "+content);
			 
			return mergeInstances(resultClass, super.transformResult(clazz, "application/json", runTransformation(content)),sourceInstance );
		}
		
	}


	public String runTransformation(String content) throws Exception  {

		final String API_URI = "http://transform.mdixinc.net:8080/nydemo/transformation"; //"http://localhost:8180//org.mdmi.rt.service/transformation"; 
		//"http://35.169.86.146:8080/org.mdmi.rt.service/transformation"; //"http://transform.mdixinc.net:8080/nydemo/transformation"; //
	        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
 	    		
	    		if (StringUtils.isEmpty(source)) {
	    			throw new Exception("SOURCE IS NULL");
	    		}
	    		
	    		if (StringUtils.isEmpty(target)) {
	    			throw new Exception("TARGET IS NULL");
	    		}
	    		
	    		if (StringUtils.isEmpty(content)) {
	    			throw new Exception("CONTENT IS NULL");
	    		}
 				
	            HttpEntity data = MultipartEntityBuilder.create()
	      
	                    .addTextBody("source", source, ContentType.TEXT_PLAIN)
	                    .addTextBody("target", target, ContentType.TEXT_PLAIN)
	                    .addTextBody("message", content, ContentType.TEXT_PLAIN)
	                    .build();

	            // build http request and assign multipart upload data
	            HttpUriRequest request = RequestBuilder
	                    .post(API_URI)
	                    .setEntity(data)
	                    .build();

	            
	           logger.debug("Executing request " + request.getRequestLine());

	            // Create a custom response handler
	            ResponseHandler<String> responseHandler = response -> {
	                int status = response.getStatusLine().getStatusCode();
	                if (status >= 200 && status < 300) {
	                    HttpEntity entity = response.getEntity();
	                    return entity != null ? EntityUtils.toString(entity) : null;
	                } else {
	                    throw new ClientProtocolException("Unexpected response status: " + status);
	                }
	            };
	            String responseBody = httpclient.execute(request, responseHandler);
	            logger.debug("----------------------------------------");
	            logger.debug(responseBody);
	            return responseBody; 
	        }
	    
	    }
	
	

	@Override
	protected void postProcessResult(String result, String resultClass, String contentType,
			Map<String, Object> results) {
		super.postProcessResult(result, resultClass, contentType, results);
		logger.info("postProcessResult "+this.content);
		results.put("Bundle", this.content);
		logger.info("postProcessResult "+results.get("Bundle"));
	}


	/**
	 * Poor man's merge - bringing in merge from apache caused runtime issues
	 * fields need to be public, replace with correct reflection routines
	 * @TODO refactor!!!
	 * @param resultClass
	 * @param source
	 * @param target
	 * @return
	 */
	
	public static Object mergeInstances(String resultClass, Object source, Object target) {
		try {
			logger.debug("mergeInstances " + resultClass);
			if (source != null && target != null) {
				logger.debug("mergeInstances " + resultClass);
				Class<?> resultClassDefinition = Class.forName(resultClass);
				Field[] fields = resultClassDefinition.getDeclaredFields();
				for (Field field : fields) {
					logger.debug("mergeInstances " + field.getName());
					field.getModifiers();
					if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))  {
						continue;
					}
					Object value = field.get(source);
					if (value != null) {
						
						if (value instanceof List) {
							List targetList = (List) field.get(target);
							targetList.addAll((List) value);
						} else {						
							if (value instanceof Double) {
								Double d = (Double) value;
								if (Double.compare(d, Double.valueOf(0.0)) > 0) {
									field.set(target, value);
								}
							} else {

								field.set(target, value);
							}
						}
					}
				}
			} else {
				logger.debug("mergeInstances source == null && target == null " + resultClass);
			}
		} catch (IllegalArgumentException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return target;
	}
	
}
