package controller;


import static formatter.PhoneNumberFormatter.*;
import static java.lang.Integer.*;
import static javafx.scene.control.Alert.AlertType.*;
import static view.PopupView.*;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

import repository.MemberRepository;
import repository.StampRepository;
import service.MailService;
import service.StampService;
import view.PopupView;

import java.sql.*;

public class KeypadController {
	@FXML private TextField phoneNumberField;
	@FXML private TextField stampCountField;

	private boolean isPhoneNumberInput = true;
	private StringBuilder stampCount = new StringBuilder();
	private StringBuilder phoneNumber = new StringBuilder();
	private PopupView popupView = new PopupView();
	private final StampService stampService;

	public KeypadController() {
		StampRepository stampRepository = new StampRepository();
		MemberRepository memberRepository = new MemberRepository();
		MailService mailService = new MailService();

		stampService = new StampService(stampRepository, memberRepository, mailService);
	}

	@FXML
	public void initialize() {
		phoneNumberField.setEditable(false);
		stampCountField.setEditable(false);
		updateDisplayFields();

		// 마우스 클릭으로 전환
		phoneNumberField.setOnMouseClicked(event -> setPhoneNumberInput(true));
		stampCountField.setOnMouseClicked(event -> setPhoneNumberInput(false));
	}

	// 마우스 클릭 전환 메서드
	private void setPhoneNumberInput(boolean isPhoneNumber) {
		isPhoneNumberInput = isPhoneNumber;
		updateDisplayFields();
	}

	@FXML
	private void handleNumberClick(ActionEvent event) {
		String digit = ((Button) event.getSource()).getText();
		if (isPhoneNumberInput) {
			phoneNumber.append(digit);
		} else {
			stampCount.append(digit);
		}
		updateDisplayFields();
	}

	@FXML
	private void handleClearClick() {
		if (isPhoneNumberInput) {
			phoneNumber.setLength(0);
		} else {
			stampCount.setLength(0);
		}
		updateDisplayFields();
	}

	@FXML
	private void handleToggleInput() {
		isPhoneNumberInput = !isPhoneNumberInput;
		updateDisplayFields();
	}

	@FXML
	private void handleSubmitClick() {
		if (phoneNumber.length() == 0 || stampCount.length() == 0) {
			popupView.showFailPopup("전화번호와 스탬프 개수를 모두 입력해주세요.");
			return;
		}
		try {
			// LoggedCafeDto cafe = CafeSession.getInstance().getLoggedCafeDto();
			stampService.saveStamp(1l, formatPhoneNumber(phoneNumber.toString()), parseInt(stampCount.toString()));
			popupView.showSuccessPopup("스탬프 적립 성공");
		} catch (IllegalArgumentException | SQLException e) {
			popupView.showFailPopup(e.getMessage());
		}

		phoneNumber.setLength(0);
		stampCount.setLength(0);
		updateDisplayFields();
	}

	@FXML
	private void GoToHome() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CouponPage.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			Stage stage = (Stage) phoneNumberField.getScene().getWindow();
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
			failGoToHome("화면 전환 중 오류가 발생했습니다.");
		}
	}

	private void failGoToHome(String message) {
		Alert alert = new Alert(ERROR);
		alert.setTitle("오류");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void updateDisplayFields() {
		phoneNumberField.setText(phoneNumber.toString());
		stampCountField.setText(stampCount.toString());
		phoneNumberField.setStyle(isPhoneNumberInput ? "-fx-background-color: #e0e0e0;" : "");
		stampCountField.setStyle(!isPhoneNumberInput ? "-fx-background-color: #e0e0e0;" : "");
	}
}