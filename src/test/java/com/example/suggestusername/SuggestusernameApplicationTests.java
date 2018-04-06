package com.example.suggestusername;

import com.example.suggestusername.config.YAMLConfig;
import com.example.suggestusername.model.SuggestionResult;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = { "classpath:application-test.yml" })
public class SuggestusernameApplicationTests {

	@Autowired
	private YAMLConfig yamlConfig;

	private SuggestionResult suggestionResult;
	private static File usernameFile;

	@BeforeClass
	public static void setUp() throws IOException {
		usernameFile = new ClassPathResource("usernames" + File.separator + "usernames.txt").getFile();
	}

	@Before
	public void testSetUp() throws IOException {
		suggestionResult = new SuggestionResult(yamlConfig);

		// add basic seeds
		suggestionResult.checkInput("existing");
		suggestionResult.checkInput("existing2");
		suggestionResult.checkInput("existingex");
	}

	@Test
	public void contextLoads() {
	}

	@Test
	public void addNonExisting() throws IOException {
		String wordToAdd = "nonExisting";
		suggestionResult.checkInput(wordToAdd);

		assertTrue(suggestionResult.isValid());
		assertFalse(suggestionResult.isForbidden());
		assertTrue(suggestionResult.getSuggestions().isEmpty());
		assertTrue(FileUtils.readLines(usernameFile, Charset.defaultCharset()).contains(wordToAdd.toLowerCase()));
	}

	@Test
	public void addNonExistingAndAddAgain() throws IOException {
		String wordToAdd = "nonExisting";
		suggestionResult.checkInput(wordToAdd);

		assertTrue(suggestionResult.isValid());
		assertFalse(suggestionResult.isForbidden());
		assertTrue(suggestionResult.getSuggestions().isEmpty());
		assertTrue(FileUtils.readLines(usernameFile, Charset.defaultCharset()).contains(wordToAdd.toLowerCase()));

		suggestionResult.checkInput(wordToAdd);
		assertFalse(suggestionResult.isValid());
		assertFalse(suggestionResult.isForbidden());
		assertTrue(suggestionResult.getSuggestions().contains(wordToAdd.toLowerCase() + "0"));
		assertTrue(FileUtils.readLines(usernameFile, Charset.defaultCharset()).contains(wordToAdd.toLowerCase()));
	}

	@Test
	public void addExisting() throws IOException {
		String wordToAdd = "existing";
		suggestionResult.checkInput(wordToAdd);

		assertFalse(suggestionResult.isForbidden());
		assertFalse(suggestionResult.isValid());
		assertTrue(suggestionResult.getSuggestions().contains(wordToAdd.toLowerCase() + "0")); // example
		assertTrue(FileUtils.readLines(usernameFile, Charset.defaultCharset()).contains(wordToAdd.toLowerCase()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void addShortWord() throws IOException {
		String wordToAdd = "a";
		suggestionResult.checkInput(wordToAdd);
	}

	@Test
	public void addForbidden() throws IOException {
		String wordToAdd = "crack123";
		suggestionResult.checkInput(wordToAdd);

		assertTrue(suggestionResult.isForbidden());
		assertFalse(suggestionResult.isValid());
		assertTrue(suggestionResult.getSuggestions().contains("default0")); // example
		assertFalse(FileUtils.readLines(usernameFile, Charset.defaultCharset()).contains(wordToAdd.toLowerCase()));
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.write(usernameFile, "", Charset.defaultCharset());
	}
}
