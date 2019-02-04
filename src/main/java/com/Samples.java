package com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class Samples implements java.io.Serializable {

	static final long serialVersionUID = 1L;
	
	public static String getMedicationStatementsBundle() throws IOException {

		try (BufferedReader buffer = new BufferedReader(
				new InputStreamReader(ClassLoader.getSystemResourceAsStream("MedicationStatements.json")))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		}
	}
	
	public static String getConditionsBundle() throws IOException {

		try (BufferedReader buffer = new BufferedReader(
				new InputStreamReader(ClassLoader.getSystemResourceAsStream("Conditions.json")))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		}
	}

}
