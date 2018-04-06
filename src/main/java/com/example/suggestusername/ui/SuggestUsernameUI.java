package com.example.suggestusername.ui;

import com.example.suggestusername.config.YAMLConfig;
import com.example.suggestusername.model.SuggestionResult;
import com.vaadin.data.HasValue;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@SpringUI
public class SuggestUsernameUI extends UI {

	@Autowired
	private YAMLConfig yamlConfig;

	private Label resultLabel = new Label();
	private Button sendInputButton = new Button("Check username");
	private TextField inputField = new TextField("place a username here");

	@Override
	protected void init(VaadinRequest request) {
		HorizontalLayout layout = new HorizontalLayout();

		VerticalLayout inputLayout = new VerticalLayout();
		inputField.addValueChangeListener(enableDisableButton());
		inputLayout.addComponent(inputField);

		sendInputButton.addClickListener(checkUsername());
		sendInputButton.setEnabled(false);
		inputLayout.addComponent(sendInputButton);

		VerticalLayout resultLayout = new VerticalLayout();
		resultLayout.addComponent(resultLabel);

		layout.addComponent(inputLayout);
		layout.addComponent(resultLayout);

		setContent(layout);
	}

	private HasValue.ValueChangeListener<String> enableDisableButton() {
		return (HasValue.ValueChangeListener<String>) event -> {
			if (!StringUtils.isBlank(event.getValue())) {
				sendInputButton.setEnabled(true);
				resultLabel.setValue("");
				resultLabel.setStyleName("");
			} else {
				sendInputButton.setEnabled(false);
			}
		};
	}

	private Button.ClickListener checkUsername() {
		return (Button.ClickListener) clickEvent -> {
			try {
				SuggestionResult result = new SuggestionResult(yamlConfig);
				result.checkInput(inputField.getValue());

				if (result.isValid()) {
					resultLabel.setStyleName(ValoTheme.LABEL_SUCCESS);
					resultLabel.setValue(String.format("Username %s has been added.", inputField.getValue()));
				} else if (result.isForbidden()) {
					resultLabel.setStyleName(ValoTheme.LABEL_FAILURE);
					resultLabel.setValue(String.format("%s is a forbidden value. Here are some suggestions: %s",
							inputField.getValue(), String.join(", ", result.getSuggestions())));
				} else {
					resultLabel.setStyleName(ValoTheme.LABEL_FAILURE);
					resultLabel.setValue(String.format("Username %s is taken. Here are some suggestions: %s",
						inputField.getValue(), String.join(", ", result.getSuggestions())));
				}

			} catch (IllegalArgumentException | IOException e) {
				resultLabel.setValue(e.getMessage());
				resultLabel.setStyleName(ValoTheme.LABEL_FAILURE);
			}
		};
	}
}
