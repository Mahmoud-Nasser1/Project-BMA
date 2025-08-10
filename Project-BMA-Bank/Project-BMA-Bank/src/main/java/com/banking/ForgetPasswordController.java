package com.banking;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.IOException;
import java.util.Properties;
public class ForgetPasswordController {

    @FXML private TextField emailField;
    @FXML private Label errorLabel;

    private String currentUsername;

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SENDER_EMAIL = "mohamedamgad7777@gmail.com";
    private static final String SENDER_PASSWORD = "xnpvkxlplwtqscbg";

    @FXML
    public void initialize() {
        if (emailField == null) {
            System.out.println("❌ emailField is null - FXML binding issue");
        }
        if (errorLabel == null) {
            System.out.println("❌ errorLabel is null - FXML binding issue");
        }
    }

    @FXML
    protected void handleSendCode(ActionEvent event) throws IOException {
        String email = emailField.getText();

        if (email.isEmpty()) {
            if (errorLabel != null) {
                errorLabel.setText("Please enter your email");
            } else {
                System.out.println("Error: Cannot set error message - errorLabel is null");
            }
            return;
        }

        currentUsername = database_BankSystem.getUsernameByEmail(email);
        if (currentUsername == null) {
            if (errorLabel != null) {
                errorLabel.setText("No user found with this email");
            } else {
                System.out.println("Error: Cannot set error message - errorLabel is null");
            }
            return;
        }

        String code = database_BankSystem.generateAndSaveVerificationCode(currentUsername);
        if (code == null) {
            if (errorLabel != null) {
                errorLabel.setText("Failed to generate verification code, please try again");
            } else {
                System.out.println("Error: Cannot set error message - errorLabel is null");
            }
            return;
        }

        boolean emailSent = sendVerificationEmail(email, code);
        if (!emailSent) {
            if (errorLabel != null) {
                errorLabel.setText("Failed to send email, please try again");
            } else {
                System.out.println("Error: Cannot set error message - errorLabel is null");
            }
            return;
        }

        UserSession session = UserSession.getInstance();
        session.setUsername(currentUsername);
        session.setPasswordReset(true);
        session.setRequestSource("ForgetPassword");
        System.out.println("Setting Request Source in ForgetPasswordController: " + session.getRequestSource());

        Parent root = FXMLLoader.load(getClass().getResource("/com/example/maged/VerifyEmail.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        Image backgroundImage = new Image(getClass().getResourceAsStream("/back.jpg"));
        ImageView backgroundView = new ImageView(backgroundImage);
        
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();
        
        backgroundView.setFitWidth(screenWidth);
        backgroundView.setFitHeight(screenHeight);
        backgroundView.setPreserveRatio(false);
        backgroundView.setEffect(new GaussianBlur(20));
        
        Region blueOverlay = new Region();
        blueOverlay.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 120, 255, 0.2),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));
        blueOverlay.setEffect(new GaussianBlur(20));
        blueOverlay.setPrefSize(screenWidth, screenHeight);
        
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(backgroundView, blueOverlay, root);
        
        Scene scene = new Scene(stackPane);
        scene.getStylesheets().clear();

        
        stage.setScene(scene);
        stage.setTitle("Verify Email");
        stage.show();
    }

    @FXML
    protected void switchToLogin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/maged/Login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        Image backgroundImage = new Image(getClass().getResourceAsStream("/back.jpg"));
        ImageView backgroundView = new ImageView(backgroundImage);
        
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();
        
        backgroundView.setFitWidth(screenWidth);
        backgroundView.setFitHeight(screenHeight);
        backgroundView.setPreserveRatio(false);
        backgroundView.setEffect(new GaussianBlur(20));
        
        Region blueOverlay = new Region();
        blueOverlay.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 120, 255, 0.2),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));
        blueOverlay.setEffect(new GaussianBlur(20));
        blueOverlay.setPrefSize(screenWidth, screenHeight);
        
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(backgroundView, blueOverlay, root);
        
        Scene scene = new Scene(stackPane);
        UserSession session = UserSession.getInstance();
        scene.getStylesheets().clear();

        
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
    }

    private boolean sendVerificationEmail(String recipientEmail, String verificationCode) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.debug", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Password Reset Verification Code");
            message.setText("Hello,\n\nYour verification code to reset your password is: " + verificationCode + "\n\nBest regards,\nBank Team");

            Transport.send(message);
            System.out.println("✅ Verification email sent successfully to " + recipientEmail);
            return true;
        } catch (MessagingException e) {
            System.out.println("❌ Error sending verification email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}