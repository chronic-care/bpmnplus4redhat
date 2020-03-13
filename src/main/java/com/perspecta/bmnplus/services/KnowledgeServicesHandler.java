package com.perspecta.bmnplus.services;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.util.StringUtils;
import org.jbpm.process.workitem.rest.RESTWorkItemHandler;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class KnowledgeServicesHandler extends RESTWorkItemHandler {

	private static final String PATH = "http://perspecta.com/fhir/km/StructureDefinition/requiredPath";
	private static final String ID = "http://perspecta.com/fhir/km/StructureDefinition/idFilter";
	private static final Logger logger = LoggerFactory.getLogger(KnowledgeServicesHandler.class);

	HashMap<String, List<String>> valuesMap = new HashMap<String, List<String>>();

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String url = (String) workItem.getParameter("Url");
		String patient = (String) workItem.getParameter("Patient");
		String context = (String) workItem.getParameter("Context");

		String ksurl = url + "$get-guidance" + "?patient=" + patient + "&use-context=" + context
				+ "&_format=application/fhir+json";

		workItem.getParameters().put("Url", ksurl);

		super.executeWorkItem(workItem, manager);
	}

	private void load() {

		if (valuesMap.isEmpty()) {

			valuesMap.put("Condition.clinicalStatus",
					Arrays.asList("active,recurrence,relapse,inactive,remission,resolved".split(",")));

		}

	}

	public void parseResults(String result, Map<String, Object> results) throws IOException {

//		

		
		results.put("GuidanceResponse", result);
		
		load();

		ObjectMapper mapper = new ObjectMapper();

		Reader reader = new StringReader(result);

		JsonNode guidanceResponse = mapper.readTree(reader);

		JsonNode node = guidanceResponse.path("dataRequirement");

		if (node.isMissingNode()) {
			ArrayNode contained = (ArrayNode) guidanceResponse.get("contained");

			for (int ctr = 0; ctr < contained.size(); ctr++) {
				if ("Parameters".equals(contained.get(ctr).get("resourceType").asText())) {

					;

					ArrayNode parameters = (ArrayNode) contained.get(ctr).get("parameter");

					if (parameters.get(0).has("valueBoolean")) {
						results.put("Result", parameters.get(0).get("valueBoolean").asText());
					}

					if (parameters.get(0).has("valueString")) {
						results.put("Result", parameters.get(0).get("valueString").asText());
					}

					logger.info("Result is " + results.get("Result"));

				}
			}
		} else {

			ArrayList<DataRequirement> dataRequirementsResponse = new ArrayList<DataRequirement>();
			ArrayNode dataRequirements = (ArrayNode) node;

			for (int ctr = 0; ctr < dataRequirements.size(); ctr++) {
				JsonNode dataRequirement = dataRequirements.get(ctr);
				ArrayList<String> paths = new ArrayList<String>();
				String id = "NEW" + ctr;
				String clinicalStatement = dataRequirement.get("type").asText();
				String clinicalStatementFilter = "";
				String clinicalStatementDisplay = "MISSING";
				String type = "String";

				ArrayNode extensions = (ArrayNode) dataRequirement.get("extension");
				for (int ectr = 0; ectr < extensions.size(); ectr++) {
					JsonNode extension = extensions.get(ectr);

					switch (extension.get("url").asText()) {
					case PATH:
						paths.add(extension.get("valueString").asText());
						;
						break;
					case ID:
						id = extension.get("valueId").asText();
						break;
					}
				}

				// valueSetString
				ArrayNode codeFilters = (ArrayNode) dataRequirement.get("codeFilter");
				for (int fctr = 0; fctr < codeFilters.size(); fctr++) {
					JsonNode filter = codeFilters.get(fctr);
					if ("code".equals(filter.get("path").asText())) {

						if (filter.has("valueSetString")) {
							clinicalStatementDisplay = filter.get("valueSetString").asText();
						}

						ArrayNode valueCodings = (ArrayNode) filter.get("valueCoding");
						for (int vctr = 0; vctr < valueCodings.size(); vctr++) {

							clinicalStatementFilter = valueCodings.get(vctr).get("code").asText();
							break;

						}

					}
				}

				if (paths.isEmpty()) {
        			   paths.add("id");   
				}

				if (!paths.isEmpty() && !StringUtils.isEmpty(id) && !StringUtils.isEmpty(clinicalStatement)
						&& !StringUtils.isEmpty(clinicalStatementFilter)) {
					for (String path : paths) {
						dataRequirementsResponse.add(new DataRequirement(clinicalStatement, id, clinicalStatementFilter,
								clinicalStatementDisplay, path, type, valuesMap.get(clinicalStatement + "." + path)));
					}

				}
			}

			if (!dataRequirementsResponse.isEmpty()) {
				String jsonInString = mapper.writeValueAsString(dataRequirementsResponse);
				logger.info(jsonInString);

				results.put("DataRequirement", jsonInString);
				results.put("Result", "nmd");

			} else {
				logger.info("Incorrect DataRequirements");
			}

		}

		
		
		
//		System.out.println(results.get("GuidanceResponse"));
		
//		System.out.println(results.get("DataRequirement"));
//		System.out.println(results.get("Result"));

	}

	@Override
	protected void postProcessResult(String result, String resultClass, String contentType,
			Map<String, Object> results) {
		super.postProcessResult(result, resultClass, contentType, results);
		logger.info("postProcessResult " + result);
		results.put("Bundle", result);
		try {
			parseResults(result, results);
		} catch (IOException e) {
			results.put("Result", "ERROR");
		}
		logger.info("postProcessResult " + results.get("Bundle"));
	}
}
