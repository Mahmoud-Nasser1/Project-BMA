package com.banking;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
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

public class VerifyEmailController {

    @FXML private TextField codeField;
    @FXML private Label codeError;

    private static final String SMTP_HOST = "smtp.gmail.com";

    private static final String SMTP_PORT = "587";
    private static final String SENDER_EMAIL = "mohamedamgad7777@gmail.com";
    private static final String SENDER_PASSWORD = "xnpvkxlplwtqscbg";

    @FXML
    protected void handleVerifyCode(ActionEvent event) throws IOException {
        String code = codeField.getText();
        UserSession session = UserSession.getInstance();
        String username = session.getUsername();

        if (code.isEmpty()) {
            codeError.setText("Please enter the verification code");
            return;
        }

        if (username == null || username.isEmpty()) {
            codeError.setText("Error: User session is invalid. Please try again.");
            System.err.println("Error in VerifyEmailController: Username is null or empty");
            return;
        }

        if (database_BankSystem.verifyCode(username, code)) {
            System.out.println("Request Source in VerifyEmailController: " + session.getRequestSource());

            String fxmlPath = "/com/example/maged/ChangePassword.fxml"; // الافتراضي (Forget Password)
            String title = "Change Password";
            String emailRecipient = database_BankSystem.getEmailByUsername(username);

            if (emailRecipient == null) {
                System.err.println("Error in VerifyEmailController: Could not retrieve email for username: " + username);
                codeError.setText("Error: Could not send confirmation email. Please try again.");
                return;
            }

            if ("Signup".equals(session.getRequestSource())) {
                fxmlPath = "/com/example/maged/Login.fxml";
                title = "Login";
                boolean emailSent = sendSignupConfirmationEmail(emailRecipient, username);
                if (!emailSent) {
                    System.err.println("Failed to send signup confirmation email to: " + emailRecipient);
                }
            } else if ("ForgetPassword".equals(session.getRequestSource())) {
                fxmlPath = "/com/example/maged/ChangePassword.fxml";
                title = "Change Password";
                boolean emailSent = sendPasswordChangeConfirmationEmail(emailRecipient, username);
                if (!emailSent) {
                    System.err.println("Failed to send password change confirmation email to: " + emailRecipient);
                }
            } else {
                System.err.println("Unknown request source: " + session.getRequestSource() + ". Defaulting to Login.");
                fxmlPath = "/com/example/maged/Login.fxml";
                title = "Login";
            }

            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
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
            stage.setTitle(title);
            stage.show();

            System.out.println("Successfully navigated to: " + fxmlPath);

            session.setRequestSource(null);
        } else {
            codeError.setText("Invalid verification code");
        }
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

        session.setRequestSource(null);

        System.out.println("Successfully navigated to: /com/example/maged/Login.fxml (via switchToLogin)");
    }

    private boolean sendSignupConfirmationEmail(String recipientEmail, String username) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("🎉 Welcome to BMA Bank - Registration Successful!");

            String htmlContent = """
                <div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px; background-color: #f9f9f9;'>
                    <h2 style='color: #2c3e50; text-align: center;'>🎉 Welcome to BMA Bank, %s! 🎉</h2>
                    <p style='color: #34495e; font-size: 16px; text-align: center;'>You’ve successfully registered with us!</p>
                    <div style='text-align: center; margin: 20px 0;'>
                        <p style='color: #34495e; font-size: 16px;'>We’re thrilled to have you on board. Start exploring your account and enjoy seamless banking with BMA Bank!</p>
                    </div>
                    <p style='color: #34495e; font-size: 16px; text-align: center;'>Log in now to get started:</p>
                    <div style='text-align: center;'>
                        <a href='#' style='background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block;'>
                            Log In to Your Account
                        </a>
                    </div>
                    <p style='color: #34495e; font-size: 16px; text-align: center; margin-top: 20px;'>
                        Need help? Feel free to reach out at 
                        <a href='mailto:support@bmabank.com' style='color: #4CAF50; text-decoration: none;'>support@bmabank.com</a>.
                    </p>
                    <p style='color: #34495e; font-size: 16px; text-align: center; margin-top: 30px;'>
                        Best regards,<br>
                        <strong style='color: #2c3e50;'>The BMA Bank Team</strong>
                    </p>
                </div>
                """.formatted(username);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✅ Signup confirmation email sent successfully to " + recipientEmail);
            return true;
        } catch (MessagingException e) {
            System.out.println("❌ Error sending signup confirmation email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean sendPasswordChangeConfirmationEmail(String recipientEmail, String username) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("🔒 Password Changed Successfully!");

            String htmlContent = """
                <div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px; background-color: #f9f9f9;'>
                    <h2 style='color: #2c3e50; text-align: center;'>🔒 Password Changed Successfully, %s!</h2>
                    <p style='color: #34495e; font-size: 16px; text-align: center;'>Your password has been updated successfully.</p>
                    <div style='text-align: center; margin: 20px 0;'>
                        <p style='color: #34495e; font-size: 16px;'>You’re all set to continue banking with us securely. If you didn’t make this change, please contact us immediately.</p>
                    </div>
                    <p style='color: #34495e; font-size: 16px; text-align: center;'>Log in with your new password to continue:</p>
                    <div style='text-align: center;'>
                        <a href='#' style='background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block;'>
                            Log In to Your Account
                        </a>
                    </div>
                    <p style='color: #34495e; font-size: 16px; text-align: center; margin-top: 20px;'>
                        Need help? Feel free to reach out at 
                        <a href='mailto:support@bmabank.com' style='color: #4CAF50; text-decoration: none;'>support@bmabank.com</a>.
                    </p>
                    <p style='color: #34495e; font-size: 16px; text-align: center; margin-top: 30px;'>
                        Best regards,<br>
                        <strong style='color: #2c3e50;'>The BMA Bank Team</strong>
                    </p>
                </div>
                """.formatted(username);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✅ Password change confirmation email sent successfully to " + recipientEmail);
            return true;
        } catch (MessagingException e) {
            System.out.println("❌ Error sending password change confirmation email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}