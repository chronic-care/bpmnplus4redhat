package com.myspace.diabetesstrawman;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.formats.IParser;
import org.hl7.fhir.dstu3.hapi.ctx.FhirServerDstu3;
import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.Before;
import org.junit.Test;

import com.CodedElement;
import com.Patient;
import com.SampleTest2;
import com.Samples;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.api.server.IFhirVersionServer;

public class MDMITransformTEst {

	@Before
	public void setUp() throws Exception {
	}

	String s = new String("http://localhost:8090/");
	
	@Test
	public void testsample() throws Exception {
		System.out.println(SampleTest2.getMedicationStatementsBundle());;
		System.out.println(Samples.getConditionsBundle());;
		
//	kcontext.setVariable("medicationStatementsBundle",com.SampleBundles.getMedicationStatementsBundle());
//	kcontext.setVariable("conditionsBundle",com.SampleBundles.getConditionsBundle());
//	kcontext.setVariable("resultsBundle",com.SampleBundles.getResultsBundle());
//	kcontext.setVariable("patientBundle",com.SampleBundles.getPatientBundle());
				
	}
	@Test
	public void test() throws Exception {
		
		MDMIWorkItemHandler handler = new MDMIWorkItemHandler();
		
		handler.source = "FHIRSTU3.AllergyIntolerance";
		handler.target = "FHIRSTU3.AllergyIntolerance";
		
		InputStream stream = new FileInputStream(
				"src/test/resources/bpmnvitals.xml");

		String content = IOUtils.toString(stream, StandardCharsets.UTF_8);
		
		System.out.println(handler.runTransformation(content));
		
		fail("Not yet implemented");
	}
	
	
	@Test
	public void testParse() throws JsonParseException, JsonMappingException, IOException {
		
		  ObjectMapper mapper = new ObjectMapper();

          System.out.println(mapper.readValue("{  \"bloodSugar\": 76.0 }",
        		  Patient.class));
	}
	
	
	@Test
	public void testPatientSerialization() throws JsonGenerationException, JsonMappingException, IOException  {
		ObjectMapper objectMapper = new ObjectMapper();
		Patient patient = new Patient( );
		patient.setConditions(new ArrayList<CodedElement>());
		
		patient.setMedications(new ArrayList<CodedElement>());
		
		patient.getMedications().add(new CodedElement("mbbb","222"));
		patient.getMedications().add(new CodedElement("mccc","333"));
		
		CodedElement hhh = new CodedElement("aaa","111");
		
		patient.getConditions().add(hhh);
		patient.getConditions().add(new CodedElement("bbb","222"));
		patient.getConditions().add(new CodedElement("ccc","333"));
		patient.getConditions().add(new CodedElement("ddd","444"));
		patient.getConditions().add(new CodedElement("eee","555"));
		Patient patient2 = new Patient( );
		patient2.setBloodSugar(100);
		patient2.setConditions(new ArrayList<CodedElement>());
		patient2.getConditions().add(new CodedElement("fff","666"));
		
		Object foo1 = patient;
		Object foo2  = patient2;
		com.myspace.diabetesstrawman.MDMIWorkItemHandler.mergeInstances("com.Patient",foo2, foo1);
		
		objectMapper.writeValue(new File("patient.json"), patient);
		
	
		
	}
	
	@Test
	public void foobar() {
		
		new com.Patient();
		
		
		/*
		 * kcontext.setVariable("patient",new com.Patient());
		 */
	}

//	@Test
//	public void testPatientSerializationXML() throws JAXBException {
//		JAXBContext context = JAXBContext.newInstance(Patient.class);
//	    Marshaller m = context.createMarshaller();
//	    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//	    
//	    Patient patient = new Patient( );
//		patient.setConditions(new ArrayList<String>());
//		patient.getConditions().add("aaaaa");
//		patient.getConditions().add("aaaaa");
//		patient.getConditions().add("aaaaa");
//		patient.getConditions().add("aaaaa");
//		patient.getConditions().add("aaaaa");
//		patient.getConditions().add("aaaaa");
//		patient.getConditions().add("aaaaa");
////		patient.setBloodSugar(100);
//		Patient patient2 = new Patient( );
//		patient2.setBloodSugar(100);
//		patient.getConditions().add("1111");
//		com.myspace.diabetesstrawman.MDMIWorkItemHandler.mergeInstances(patient2, patient);
//
//	    StringWriter sw = new StringWriter();
//	    m.marshal(patient, sw);
//	    
////	    m.marshal(new JAXBElement<Patient>(new QName("uri","local"), Patient.class, patient), System.out);
//
//	    String result = sw.toString();
//	    System.out.println(result);
//	}
	
	
	@Test
	 public void testFHIR() throws DataFormatException, IOException {
		
		com.helger.schematron.ISchematronResource asdf;
		
		org.codehaus.stax2.io.EscapingWriterFactory asdf;
		
//		FhirServerDstu3 asdf;
		
//		org.hl7.fhir.dstu3.hapi.ctx.IFhirVersionServer asdf;
		
		IFhirVersionServer www;
		
		// Create a context
				FhirContext ctx = FhirContext.forDstu3();
 				ca.uhn.fhir.parser.IParser parser =  ctx.newJsonParser();
				Bundle bundle = parser.parseResource(Bundle.class, com.SampleBundles.getConditionsBundle());
 				String encoded = ctx.newXmlParser().encodeResourceToString(bundle);
				System.out.println(encoded);
				
//				IParser jsonParser = ctx.newJsonParser();
//				encoded = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
//				System.out.println(encoded);
				
	}
	
}
