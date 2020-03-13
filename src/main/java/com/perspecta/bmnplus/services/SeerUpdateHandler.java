package com.perspecta.bmnplus.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;
import org.drools.core.util.StringUtils;
import org.jbpm.process.workitem.rest.RESTServiceException;
import org.jbpm.process.workitem.rest.RESTWorkItemHandler;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * 
 * ${baseurl}/seer/Condition/{resourceId}?clinicalStatus=X&verificationStatus=X&onsetDateTime=X
 * 
 * 
 * https://seer-mapper.dev.openplatform.healthcare/seer?
 */
@SuppressWarnings("rawtypes")
public class SeerUpdateHandler extends RESTWorkItemHandler {

	private static final Logger logger = LoggerFactory.getLogger(SeerUpdateHandler.class);

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

		logger.info("START SeerUpdateHandler");

		Object missingInformationParameter = workItem.getParameter("MissingInformation");

		String seeURL = (String) workItem.getParameter("Url");

		if (missingInformationParameter instanceof ArrayList) {

			ArrayList missingInformationObjects = (ArrayList) missingInformationParameter;

			for (Object o : missingInformationObjects) {
				if (o instanceof HashMap) {
					HashMap missingInformationObject = (HashMap) o;

					if (missingInformationObject.containsKey("id")
							&& !StringUtils.isEmpty((String) missingInformationObject.get("id"))) {
						String[] result = ((String) missingInformationObject.get("id")).split("/");
						String clinicalStatement = (String) missingInformationObject.get("clinicalStatement");
						String path = (String) missingInformationObject.get("path");
						String value = (String) missingInformationObject.get("value");
						String updateUrl = seeURL + clinicalStatement + "/" + result[0] + "?" + path + "=" + value;
						logger.info("SEER Update URL is " + updateUrl);
						workItem.getParameters().put("Url", updateUrl);
						this.executeRest(workItem, manager);
					}
				}

			}

		} else {

			try {

				String missingInformation = (String) workItem.getParameter("MissingInformation");

				logger.info(missingInformation);

				ObjectMapper mapper = new ObjectMapper();

				List<com.perspecta.bmnplus.services.DataRequirement> dataRequirements;


				dataRequirements = Arrays.asList(
						mapper.readValue(missingInformation, com.perspecta.bmnplus.services.DataRequirement[].class));


				if (dataRequirements.isEmpty()) {
					logger.error("dataRequirements is empty");
				}
				for (com.perspecta.bmnplus.services.DataRequirement dr : dataRequirements) {


					if (!StringUtils.isEmpty(dr.getId())) {
						/*
						 * The ID has the id an history-add a split as it is not currently needed by
						 * SEER API
						 */
						String[] result = dr.getId().split("/");
						workItem.getParameters().put("Url", seeURL + dr.getClinicalStatement() + "/" + result[0] + "?"
								+ dr.getPath() + "=" + dr.getValue());

						logger.info((String) workItem.getParameter("Url"));

						this.executeRest(workItem, manager);
					}

				}


			} catch (Exception e) {

				logger.error("PARSING ERROR", e);

				e.printStackTrace();
			}

		}

		/*
		 * At this point, just set the item as complete as we do not have error handling
		 */
		Map<String, Object> results = new HashMap<String, Object>();
		manager.completeWorkItem(workItem.getId(), results);

	}

	/*
	 * This is a duplicate of the executeRest from base class except we pulled the manager.completeWorkItem so we can execute multiple rest
	 * The initial expectation from the UI was all the updates but these are being sent back in piecemeal so this might not be needed 
	 */
	public void executeRest(WorkItem workItem, WorkItemManager manager) {
		boolean handleException = false;
// extract required parameters
		String urlStr = (String) workItem.getParameter("Url");
		String method = (String) workItem.getParameter("Method");
		String handleExceptionStr = (String) workItem.getParameter("HandleResponseErrors");
		String resultClass = (String) workItem.getParameter("ResultClass");
		String acceptHeader = (String) workItem.getParameter("AcceptHeader");
		String acceptCharset = (String) workItem.getParameter("AcceptCharset");
		String headers = (String) workItem.getParameter(PARAM_HEADERS);

		if (urlStr == null) {
			throw new IllegalArgumentException("Url is a required parameter");
		}
		if (method == null || method.trim().length() == 0) {
			method = "GET";
		}
		if (handleExceptionStr != null) {
			handleException = Boolean.parseBoolean(handleExceptionStr);
		}
		Map<String, Object> params = workItem.getParameters();

// authentication type from parameters
		AuthenticationType authType = AuthenticationType.NONE;
		if (params.get(PARAM_AUTH_TYPE) != null) {
			authType = AuthenticationType.valueOf((String) params.get(PARAM_AUTH_TYPE));
		}

// optional timeout config parameters, defaulted to 60 seconds
		Integer connectTimeout = getParamAsInt(params.get(PARAM_CONNECT_TIMEOUT));
		if (connectTimeout == null) {
			connectTimeout = 60000;
		}
		Integer readTimeout = getParamAsInt(params.get(PARAM_READ_TIMEOUT));
		if (readTimeout == null) {
			readTimeout = 60000;
		}
		if (headers == null) {
			headers = "";
		}

		HttpClient httpClient = getHttpClient(readTimeout, connectTimeout);

		Object methodObject = configureRequest(method, urlStr, params, acceptHeader, acceptCharset, headers);
		try {
			HttpResponse response = doRequestWithAuthorization(httpClient, methodObject, params, authType);
			StatusLine statusLine = response.getStatusLine();
			int responseCode = statusLine.getStatusCode();
			Map<String, Object> results = new HashMap<String, Object>();
			HttpEntity respEntity = response.getEntity();
			String responseBody = null;
			String contentType = null;
			if (respEntity != null) {
				responseBody = EntityUtils.toString(respEntity, acceptCharset);

				if (respEntity.getContentType() != null) {
					contentType = respEntity.getContentType().getValue();
				}
			}
			if (responseCode >= 200 && responseCode < 300) {
				postProcessResult(responseBody, resultClass, contentType, results);
				results.put(PARAM_STATUS_MSG,
						"request to endpoint " + urlStr + " successfully completed " + statusLine.getReasonPhrase());
			} else {
				if (handleException) {
					handleException(new RESTServiceException(responseCode, responseBody, urlStr));
				} else {
					logger.warn("Unsuccessful response from REST server (status: {}, endpoint: {}, response: {}",
							responseCode, urlStr, responseBody);
					results.put(PARAM_STATUS_MSG, "endpoint " + urlStr + " could not be reached: " + responseBody);
				}
			}
			results.put(PARAM_STATUS, responseCode);

// notify manager that work item has been completed

		} catch (Exception e) {
			handleException(e);
		} finally {
			try {
				close(httpClient, methodObject);
			} catch (Exception e) {
				handleException(e);
			}
		}
	}
}
