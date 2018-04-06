package com.example.suggestusername.model;

import com.example.suggestusername.config.YAMLConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class SuggestionResult {

	private boolean isValid;
	private boolean isForbidden;
	private Set<String> suggestions;
	private Set<String> existingNames;
	private Set<String> restrictedWords;
	private YAMLConfig yamlConfig;

	private final String filePath = "usernames" + File.separator;

	public SuggestionResult(YAMLConfig yamlConfig) throws IOException {
		this.yamlConfig = yamlConfig;
		this.suggestions = new HashSet<>();
		this.existingNames = new HashSet<>();
		this.restrictedWords = new HashSet<>(FileUtils.readLines(new ClassPathResource(filePath +
						"restricted.txt").getFile(), Charset.defaultCharset()));
	}

	public boolean isValid() {
		return isValid;
	}

	public boolean isForbidden() {
		return isForbidden;
	}

	public Set<String> getSuggestions() {
		return suggestions.stream()
				.limit(yamlConfig.getMaxResults())
				.collect(Collectors.toCollection(TreeSet::new));
	}

	public void checkInput(String input) throws IllegalArgumentException, IOException {
		input = input.trim();
		if (!StringUtils.isBlank(input)) {
			if (input.length() < yamlConfig.getMinInputLength()) {
				throw new IllegalArgumentException(String.format("Input must be greater than %d characters.",
						yamlConfig.getMinInputLength()));
			}

			checkUsername(input);
		}
	}

	private void checkUsername(String input) throws IOException {
		input = input.toLowerCase();

		File usernamesFile = new ClassPathResource(filePath + "usernames.txt").getFile();
		this.existingNames.addAll(FileUtils.readLines(usernamesFile, Charset.defaultCharset()));

		String finalInput = input;
		Optional<String> foundOptional = this.existingNames.stream().filter(existingName ->
				StringUtils.equals(finalInput, existingName) || isContainedInSet(finalInput, this.restrictedWords))
				.findAny();

		if (foundOptional.isPresent()) {
			// suggest
			this.isValid = false;
			this.suggestions = generateSuggestions(finalInput);
		} else {
			// add to file
			FileUtils.writeStringToFile(usernamesFile, System.lineSeparator() + finalInput, Charset.defaultCharset(), true);
			this.isValid = true;
		}
	}

	private Set<String> generateSuggestions(String input) {

		Set<String> suggested = new HashSet<>();
		String tempInput = input;

		if (isContainedInSet(tempInput, this.restrictedWords)) {
			// generate new input
			tempInput = "default";
			this.isForbidden = true;
		}

		generateNumberPrefixSuggestions(suggested, tempInput);

		generateNumberSuffixSuggestions(suggested, tempInput);

		generateStringRepetitionSuggestions(suggested, tempInput);

		return suggested;
	}

	private void generateStringRepetitionSuggestions(Set<String> suggested, String tempInput) {
		int i;
		i = 0;
		int suffixLetter = 0;
		String generatedTempInput;
		while (i < 5) {
			generatedTempInput = tempInput.concat(tempInput.substring(0, suffixLetter));

			if (!isContainedInSet(generatedTempInput, this.restrictedWords) && !isEqualInSet(generatedTempInput, this.existingNames)) {
				suggested.add(generatedTempInput);
				i++;
			}

			suffixLetter++;

			if (suffixLetter > tempInput.length()) {
				break;
			}
		}
	}

	private void generateNumberSuffixSuggestions(Set<String> suggested, String tempInput) {
		int i = 0;
		int suffixId = 0;

		while (i < 5) { // so that number suggestions reach a theoretical end
			String generatedTempInput;
			generatedTempInput = tempInput + String.valueOf(suffixId);

			// Verify if input is contained in the restricted list or in the existing list
			// if it's false, add to suggestion list and increase i
			if (!isEqualInSet(generatedTempInput, this.existingNames)) {
				suggested.add(generatedTempInput);
				i++;
			}

			suffixId++;
		}
	}

	private void generateNumberPrefixSuggestions(Set<String> suggested, String tempInput) {
		int i = 0;
		int prefixId = 0;

		while (i < 4) { // so that number suggestions reach a theoretical end
			String generatedTempInput;
			generatedTempInput = String.valueOf(prefixId) + tempInput;

			// Verify if input is contained in the restricted list or in the existing list
			// if it's false, add to suggestion list and increase i
			if (!isEqualInSet(generatedTempInput, this.existingNames)) {
				suggested.add(generatedTempInput);
				i++;
			}

			prefixId++;
		}
	}

	private boolean isContainedInSet(String word, Set<String> setToCheck) {
		for (String wordToCheck : setToCheck) {
			if (StringUtils.contains(word, wordToCheck)) {
				return true;
			}
		}

		return false;
	}

	private boolean isEqualInSet(String word, Set<String> setToCheck) {
		for (String wordToCheck : setToCheck) {
			if (StringUtils.equals(word, wordToCheck)) {
				return true;
			}
		}

		return false;
	}
}
