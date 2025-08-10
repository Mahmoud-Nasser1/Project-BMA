package com.banking;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;

public class DashboardController {

    private boolean isDarkMode = true;

    @FXML
    private void goToHome(javafx.scene.input.MouseEvent event) {
        loadPage(event, "/com/banking/Home.fxml");
    }

    @FXML
    private void goToAccounts(javafx.scene.input.MouseEvent event) {
        loadPage(event, "/com/banking/Accounts.fxml");
    }

    @FXML
    private void goToTransactions(javafx.scene.input.MouseEvent event) {
        loadPage(event, "/com/banking/Transactions.fxml");
    }

    @FXML
    private void goToPayments(javafx.scene.input.MouseEvent event) {
        loadPage(event, "/com/banking/Payments.fxml");
    }

    @FXML
    private void goToDashboard(javafx.scene.input.MouseEvent event) {
        loadPage(event, "/com/example/maged/DashboardContent.fxml");
    }

    @FXML
    private void goToSettings(javafx.scene.input.MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/maged/Settings.fxml"));
            Parent root = loader.load();

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
            scene.getStylesheets().add(isDarkMode
                    ? "/com/example/maged/DarkMode.css"
                    : "/com/example/maged/LightMode.css");

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Settings");
            stage.setWidth(800);
            stage.setHeight(600);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToHelp(javafx.scene.input.MouseEvent event) {
        loadPage(event, "/com/banking/Help.fxml");
    }

    private void loadPage(javafx.scene.input.MouseEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}