package com.banking;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class Home2Controller {

    @FXML
    private ImageView HomeImage;

    @FXML
    private Label HomeText1;

    @FXML
    private VBox welcomeBox;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label careersLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private ImageView homeGif;

    private final String WELCOME_TEXT = "Welcome to";
    private final String CAREERS_TEXT = "YourBank Careers!";
    private final String DESCRIPTION_TEXT = "Join our team and embark on a rewarding journey in the banking industry. At YourBank, we are committed to fostering a culture of excellence and providing opportunities for professional growth. With a focus on innovation, customer service, and integrity, we strive to make a positive impact in the lives of our customers and communities. Join us today and be a part of our mission to shape the future of banking.";

    private MediaPlayer mediaPlayer;

    @FXML
    public void initialize() {


        UserSession session = UserSession.getInstance();
        String username = session.getUsername();
        HomeText1.setText(username);
        HomeText1.setTextFill(Color.web("White"));
        HomeText1.setStyle("-fx-underline: true;");

        database_BankSystem.UserDetails userDetails = database_BankSystem.getUserDetails(username);
        String imagePath = userDetails.getProfileImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            HomeImage.setImage(new Image("file:" + imagePath));
        }
        try {
            String audioPath = getClass().getResource("/audio/Welcome4.mp3").toExternalForm();
            System.out.println("Audio path: " + audioPath);
            if (getClass().getResource("/audio/Welcome4.mp3") == null) {
                System.err.println("Audio file not found!");
                return;
            }
            Media media = new Media(audioPath);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAudioSpectrumInterval(0.1);
            mediaPlayer.setAudioSpectrumNumBands(10);
            mediaPlayer.setOnReady(() -> {
                double durationSeconds = media.getDuration().toSeconds();
                System.out.println("Audio duration: " + durationSeconds + " seconds (" + (durationSeconds / 60) + " minutes)");
                mediaPlayer.play();
            });
            mediaPlayer.setOnPlaying(() -> System.out.println("Audio started playing"));
            mediaPlayer.setOnEndOfMedia(() -> System.out.println("Audio finished playing after " + mediaPlayer.getCurrentTime().toSeconds() + " seconds"));
            mediaPlayer.setOnError(() -> {
                System.err.println("MediaPlayer error: " + mediaPlayer.getError());
                if (mediaPlayer.getError() != null) {
                    mediaPlayer.getError().printStackTrace();
                }
            });
            mediaPlayer.setOnStopped(() -> System.out.println("Audio stopped unexpectedly at " + mediaPlayer.getCurrentTime().toSeconds() + " seconds"));
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (newTime.toSeconds() % 5 < 0.1) {
                    System.out.println("Playback progress: " + newTime.toSeconds() + " seconds");
                }
            });
            mediaPlayer.setCycleCount(1);
        } catch (Exception e) {
            System.err.println("Error loading audio: " + e.getMessage());
            e.printStackTrace();
        }



        welcomeBox.setTranslateY(-100);
        welcomeBox.setOpacity(0);
        welcomeBox.setRotate(-10);
        welcomeLabel.setText("");
        careersLabel.setText("");
        descriptionLabel.setText("");
        descriptionLabel.setTranslateY(20);
        descriptionLabel.setOpacity(0);

        if (homeGif != null) {
            homeGif.setOpacity(0);
            homeGif.setScaleX(0.8);
            homeGif.setScaleY(0.8);
        }

        TranslateTransition boxTranslate = new TranslateTransition(Duration.millis(800), welcomeBox);
        boxTranslate.setFromY(-100);
        boxTranslate.setToY(0);
        boxTranslate.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition boxFade = new FadeTransition(Duration.millis(800), welcomeBox);
        boxFade.setFromValue(0);
        boxFade.setToValue(1);

        RotateTransition boxRotate = new RotateTransition(Duration.millis(800), welcomeBox);
        boxRotate.setFromAngle(-10);
        boxRotate.setToAngle(0);
        boxRotate.setInterpolator(Interpolator.EASE_BOTH);

        ParallelTransition boxEntrance = new ParallelTransition(boxTranslate, boxFade, boxRotate);

        FadeTransition gifFade = null;
        ScaleTransition gifScale = null;
        ParallelTransition gifEntrance = null;
        if (homeGif != null) {
            gifFade = new FadeTransition(Duration.millis(800), homeGif);
            gifFade.setFromValue(0);
            gifFade.setToValue(1);

            gifScale = new ScaleTransition(Duration.millis(800), homeGif);
            gifScale.setFromX(0.8);
            gifScale.setFromY(0.8);
            gifScale.setToX(1);
            gifScale.setToY(1);
            gifScale.setInterpolator(Interpolator.EASE_BOTH);

            gifEntrance = new ParallelTransition(gifFade, gifScale);
        }

        Timeline welcomeWordFade = new Timeline();
        String[] welcomeWords = WELCOME_TEXT.split(" ");
        for (int i = 0; i < welcomeWords.length; i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(
                    Duration.millis(500 * i),
                    event -> {
                        welcomeLabel.setText(String.join(" ", java.util.Arrays.copyOfRange(welcomeWords, 0, index + 1)));
                        FadeTransition fade = new FadeTransition(Duration.millis(300), welcomeLabel);
                        fade.setFromValue(0);
                        fade.setToValue(1);
                        ScaleTransition scale = new ScaleTransition(Duration.millis(300), welcomeLabel);
                        scale.setFromX(0.8);
                        scale.setFromY(0.8);
                        scale.setToX(1);
                        scale.setToY(1);
                        ParallelTransition wordAnim = new ParallelTransition(fade, scale);
                        wordAnim.play();
                    }
            );
            welcomeWordFade.getKeyFrames().add(keyFrame);
        }

        Timeline careersSlide = new Timeline();
        for (int i = 0; i <= CAREERS_TEXT.length(); i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(
                    Duration.millis(100 * i),
                    event -> {
                        careersLabel.setText(CAREERS_TEXT.substring(0, index));
                        careersLabel.setTranslateX(20 - index * 2);
                        FadeTransition fade = new FadeTransition(Duration.millis(200), careersLabel);
                        fade.setFromValue(0.5);
                        fade.setToValue(1);
                        fade.play();
                    }
            );
            careersSlide.getKeyFrames().add(keyFrame);
        }
        Glow glow = new Glow(0.8);
        careersSlide.setOnFinished(event -> {
            careersLabel.setEffect(glow);
            Timeline glowFade = new Timeline(
                    new KeyFrame(Duration.millis(1000), new KeyValue(glow.levelProperty(), 0))
            );
            glowFade.play();
        });

        FadeTransition descFade = new FadeTransition(Duration.millis(1000), descriptionLabel);
        descFade.setFromValue(0);
        descFade.setToValue(1);
        TranslateTransition descSlide = new TranslateTransition(Duration.millis(1000), descriptionLabel);
        descSlide.setFromY(20);
        descSlide.setToY(0);
        descSlide.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition descAnim = new ParallelTransition(descFade, descSlide);
        descriptionLabel.setText(DESCRIPTION_TEXT);

        SequentialTransition fullAnimation = new SequentialTransition(
                new ParallelTransition(boxEntrance, gifEntrance != null ? gifEntrance : new PauseTransition(Duration.ZERO)),
                welcomeWordFade,
                careersSlide,
                descAnim
        );

        fullAnimation.play();

        if (homeGif != null) {
            homeGif.setOnMouseEntered(event -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(300), homeGif);
                scale.setToX(1.1);
                scale.setToY(1.1);
                scale.play();
            });
            homeGif.setOnMouseExited(event -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(300), homeGif);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();
            });
        }
    }

    @FXML
    protected void ToAccount(MouseEvent event) throws IOException {
        UserSession session = UserSession.getInstance();
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/maged/Account.fxml"));
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

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("FindUs");
        stage.setWidth(1550);
        stage.setHeight(840);
        stage.centerOnScreen();
        stage.show();
    }

    @FXML
    protected void ToChat(MouseEvent event) throws IOException {
        UserSession session = UserSession.getInstance();
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/maged/Chatbot.fxml"));
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

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("FindUs");
        stage.setWidth(1550);
        stage.setHeight(840);
        stage.centerOnScreen();
        stage.show();
    }

    @FXML
    protected void ToFindUs(MouseEvent event) throws IOException {
        UserSession session = UserSession.getInstance();
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/maged/Map.fxml"));
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

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("FindUs");
        stage.setWidth(1550);
        stage.setHeight(840);
        stage.centerOnScreen();
        stage.show();
    }
    @FXML
    protected void ToHelp(MouseEvent event) throws IOException {
        UserSession session = UserSession.getInstance();
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/maged/Help.fxml"));
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

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("FindUs");
        stage.setWidth(1550);
        stage.setHeight(840);
        stage.centerOnScreen();
        stage.show();
    }


    public void stop() {
        if (mediaPlayer != null) {
            System.out.println("Stopping audio at " + mediaPlayer.getCurrentTime().toSeconds() + " seconds");
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }
}