package com.banking;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;

public class DepositeWithDrawController {
    @FXML
    public TextField DepositeField;
    @FXML
    private Label AccountUser;
    @FXML
    private ImageView HomeImage1;
    @FXML
    private RadioButton depositRadio;
    @FXML
    private RadioButton withdrawRadio;
    @FXML
    private Label statusLabel;

    // Sidebar
    @FXML
    private FontAwesomeIconView homeIcon;
    @FXML
    private Label homeLabel;
    @FXML
    private FontAwesomeIconView userIcon;
    @FXML
    private Label userLabel;
    @FXML
    private FontAwesomeIconView exchangeIcon;
    @FXML
    private Label exchangeLabel;
    @FXML
    private FontAwesomeIconView moneyIcon;
    @FXML
    private Label moneyLabel;
    @FXML
    private FontAwesomeIconView chartIcon;
    @FXML
    private Label chartLabel;
    @FXML
    private FontAwesomeIconView mapIcon;
    @FXML
    private Label mapLabel;
    @FXML
    private FontAwesomeIconView cogIcon;
    @FXML
    private Label cogLabel;
    @FXML
    private FontAwesomeIconView helpIcon;
    @FXML
    private Label helpLabel;
    @FXML
    private FontAwesomeIconView commentIcon;
    @FXML
    private Label commentLabel;
    @FXML
    private FontAwesomeIconView searchIcon;
    @FXML
    private FontAwesomeIconView bellIcon;
    @FXML
    private ImageView homeGif;

    public void initialize() {
        UserSession userSession = UserSession.getInstance();
        String username = userSession.getUsername();
        AccountUser.setText(username);

        database_BankSystem.UserDetails userDetails = database_BankSystem.getUserDetails(username);
        String imagePath = userDetails.getProfileImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            HomeImage1.setImage(new Image("file:" + imagePath));
        }

        setupHomeAnimation(homeIcon, homeLabel);
        setupUserAnimation(userIcon, userLabel);
        setupExchangeAnimation(exchangeIcon, exchangeLabel);
        setupMoneyAnimation(moneyIcon, moneyLabel);
        setupChartAnimation(chartIcon, chartLabel);
        setupMapAnimation(mapIcon, mapLabel);
        setupCogAnimation(cogIcon, cogLabel);
        setupHelpAnimation(helpIcon, helpLabel);
        setupCommentAnimation(commentIcon, commentLabel);
        if (searchIcon != null) {
            setupSearchAnimation(searchIcon);
        } else {
            System.out.println("Warning: searchIcon is null");
        }
        if (bellIcon != null) {
            setupBellAnimation(bellIcon);
        } else {
            System.out.println("Warning: bellIcon is null");
        }
        if (homeGif != null) {
            setupGifAnimation(homeGif);
        } else {
            System.out.println("Warning: homeGif is null");
        }
    }

    public void showErrorDialog(String errorMessage) {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);
        box.setStyle("""
        -fx-background-color: rgba(255, 50, 50, 0.9);
        -fx-background-radius: 20;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0.3, 0, 8);
                    """);
        box.setEffect(new BoxBlur(10, 10, 3));

        Label icon = new Label("❌");
        icon.setStyle("-fx-font-size: 30px; -fx-text-fill: white;");

        Label label = new Label(errorMessage);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        label.setWrapText(true);
        label.setMaxWidth(300);

        Button okBtn = new Button("OK");
        okBtn.setStyle("""
        -fx-background-color: white;
        -fx-text-fill: red;
        -fx-font-weight: bold;
        -fx-background-radius: 20;
        -fx-padding: 6 20 6 20;
    """);
        okBtn.setOnAction(e -> dialog.close());

        box.getChildren().addAll(icon, label, okBtn);

        StackPane root = new StackPane(box);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3); -fx-background-radius: 20;");

        Scene scene = new Scene(root, 350, 200);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.show();
    }

    String type;

    public void Type() {
        if (depositRadio.isSelected()) {
            type = "Deposit";
        } else if (withdrawRadio.isSelected()) {
            type = "Withdraw";
        }
    }
    double fees;
    double amount;

    @FXML
    private void onConfirmButtonClicked() {
        UserSession userSession = UserSession.getInstance();
        String username = userSession.getUsername();

        try {
            amount = Double.parseDouble(DepositeField.getText().trim());
            fees = amount;
            fees += (amount * 3.0 / 100.0);
            Type();
            if (amount <= 0) {
                showErrorDialog("Failed to add card. Invalid Negative Amount.");
                return;
            }

            if (!depositRadio.isSelected() && !withdrawRadio.isSelected()) {
                showErrorDialog("         Please select a deposit or withdraw.   ");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/maged/TransferConfirm.fxml"));
            Parent root = fxmlLoader.load();
            TransferConfirm controller = fxmlLoader.getController();
            controller.LabelText(type, amount + " EGP", fees + " EGP", type);
            Stage stage = new Stage();
            stage.setTitle("Transfer Confirmation");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            DepositeField.clear();
            depositRadio.setSelected(false);
            withdrawRadio.setSelected(false);
            statusLabel.setVisible(false);

        } catch (NumberFormatException e) {
            showErrorDialog("                   Invalid amount format       ");

        } catch (IOException e) {
            showErrorDialog(" Error loading transfer confirmation window");
            e.printStackTrace();
        }
    }

    private void setupHomeAnimation(FontAwesomeIconView icon, Label label) {
        if (icon == null || label == null) {
            System.out.println("Warning: homeIcon or homeLabel is null");
            return;
        }
        Rotate rotate = new Rotate(0, 0, icon.getLayoutY(), 0, Rotate.Y_AXIS);
        icon.getTransforms().add(rotate);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(rotate.angleProperty(), 0)),
                new KeyFrame(Duration.millis(300), new KeyValue(rotate.angleProperty(), 60)),
                new KeyFrame(Duration.millis(600), new KeyValue(rotate.angleProperty(), 60)),
                new KeyFrame(Duration.millis(900), new KeyValue(rotate.angleProperty(), 0))
        );
        timeline.setCycleCount(1);

        label.setOnMouseEntered(event -> {
            icon.setEffect(new DropShadow(10, Color.GRAY));
            timeline.playFromStart();
        });
        label.setOnMouseExited(event -> {
            icon.setEffect(null);
            rotate.setAngle(0);
        });
    }

    private void setupUserAnimation(FontAwesomeIconView icon, Label label) {
        if (icon == null || label == null) {
            System.out.println("Warning: userIcon or userLabel is null");
            return;
        }
        Scale scale = new Scale(1, 1);
        icon.getTransforms().add(scale);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(scale.xProperty(), 1), new KeyValue(scale.yProperty(), 1)),
                new KeyFrame(Duration.millis(300), new KeyValue(scale.xProperty(), 1.3), new KeyValue(scale.yProperty(), 1.3)),
                new KeyFrame(Duration.millis(600), new KeyValue(scale.xProperty(), 1.3), new KeyValue(scale.yProperty(), 1.3)),
                new KeyFrame(Duration.millis(900), new KeyValue(scale.xProperty(), 1), new KeyValue(scale.yProperty(), 1))
        );
        timeline.setCycleCount(1);

        label.setOnMouseEntered(event -> {
            icon.setEffect(new DropShadow(10, Color.GRAY));
            timeline.playFromStart();
        });
        label.setOnMouseExited(event -> {
            icon.setEffect(null);
            scale.setX(1);
            scale.setY(1);
        });
    }

    private void setupExchangeAnimation(FontAwesomeIconView icon, Label label) {
        if (icon == null || label == null) {
            System.out.println("Warning: exchangeIcon or exchangeLabel is null");
            return;
        }
        Translate translate = new Translate(0, 0);
        icon.getTransforms().add(translate);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(translate.xProperty(), 0)),
                new KeyFrame(Duration.millis(300), new KeyValue(translate.xProperty(), 10)),
                new KeyFrame(Duration.millis(600), new KeyValue(translate.xProperty(), 10)),
                new KeyFrame(Duration.millis(900), new KeyValue(translate.xProperty(), 0))
        );
        timeline.setCycleCount(1);

        label.setOnMouseEntered(event -> {
            icon.setEffect(new DropShadow(10, Color.GRAY));
            timeline.playFromStart();
        });
        label.setOnMouseExited(event -> {
            icon.setEffect(null);
            translate.setX(0);
        });
    }

    private void setupMoneyAnimation(FontAwesomeIconView icon, Label label) {
        if (icon == null || label == null) {
            System.out.println("Warning: moneyIcon or moneyLabel is null");
            return;
        }
        Rotate rotate = new Rotate(0, Rotate.Z_AXIS);
        icon.getTransforms().add(rotate);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(rotate.angleProperty(), 0)),
                new KeyFrame(Duration.millis(300), new KeyValue(rotate.angleProperty(), 180)),
                new KeyFrame(Duration.millis(600), new KeyValue(rotate.angleProperty(), 180)),
                new KeyFrame(Duration.millis(900), new KeyValue(rotate.angleProperty(), 360))
        );
        timeline.setCycleCount(1);

        label.setOnMouseEntered(event -> {
            icon.setEffect(new DropShadow(10, Color.GRAY));
            timeline.playFromStart();
        });
        label.setOnMouseExited(event -> {
            icon.setEffect(null);
            rotate.setAngle(0);
        });
    }

    private void setupChartAnimation(FontAwesomeIconView icon, Label label) {
        if (icon == null || label == null) {
            System.out.println("Warning: chartIcon or chartLabel is null");
            return;
        }
        Translate translate = new Translate(0, 0);
        icon.getTransforms().add(translate);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(translate.yProperty(), 0)),
                new KeyFrame(Duration.millis(300), new KeyValue(translate.yProperty(), -10)),
                new KeyFrame(Duration.millis(600), new KeyValue(translate.yProperty(), -10)),
                new KeyFrame(Duration.millis(900), new KeyValue(translate.yProperty(), 0))
        );
        timeline.setCycleCount(1);

        label.setOnMouseEntered(event -> {
            icon.setEffect(new DropShadow(10, Color.GRAY));
            timeline.playFromStart();
        });
        label.setOnMouseExited(event -> {
            icon.setEffect(null);
            translate.setY(0);
        });
    }

    private void setupMapAnimation(FontAwesomeIconView icon, Label label) {
        if (icon == null || label == null) {
            System.out.println("Warning: mapIcon or mapLabel is null");
            return;
        }
        Scale scale = new Scale(1, 1);
        icon.getTransforms().add(scale);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(scale.xProperty(), 1),
                        new KeyValue(scale.yProperty(), 1),
                        new KeyValue(icon.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(scale.xProperty(), 1.2),
                        new KeyValue(scale.yProperty(), 1.2),
                        new KeyValue(icon.opacityProperty(), 0.7)),
                new KeyFrame(Duration.millis(600),
                        new KeyValue(scale.xProperty(), 1.2),
                        new KeyValue(scale.yProperty(), 1.2),
                        new KeyValue(icon.opacityProperty(), 0.7)),
                new KeyFrame(Duration.millis(900),
                        new KeyValue(scale.xProperty(), 1),
                        new KeyValue(scale.yProperty(), 1),
                        new KeyValue(icon.opacityProperty(), 1.0))
        );
        timeline.setCycleCount(1);

        label.setOnMouseEntered(event -> {
            icon.setEffect(new DropShadow(10, Color.GRAY));
            timeline.playFromStart();
        });
        label.setOnMouseExited(event -> {
            icon.setEffect(null);
            scale.setX(1);
            scale.setY(1);
            icon.setOpacity(1.0);
        });
    }

    private void setupCogAnimation(FontAwesomeIconView icon, Label label) {
        if (icon == null || label == null) {
            System.out.println("Warning: cogIcon or cogLabel is null");
            return;
        }
        Rotate rotate = new Rotate(0, Rotate.Z_AXIS);
        icon.getTransforms().add(rotate);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(rotate.angleProperty(), 0)),
                new KeyFrame(Duration.millis(300), new KeyValue(rotate.angleProperty(), 180)),
                new KeyFrame(Duration.millis(600), new KeyValue(rotate.angleProperty(), 180)),
                new KeyFrame(Duration.millis(900), new KeyValue(rotate.angleProperty(), 360))
        );
        timeline.setCycleCount(1);

        label.setOnMouseEntered(event -> {
            icon.setEffect(new DropShadow(10, Color.GRAY));
            timeline.playFromStart();
        });
        label.setOnMouseExited(event -> {
            icon.setEffect(null);
            rotate.setAngle(0);
        });
    }

    private void setupHelpAnimation(FontAwesomeIconView icon, Label label) {
        if (icon == null || label == null) {
            System.out.println("Warning: helpIcon or helpLabel is null");
            return;
        }
        Translate translate = new Translate(0, 0);
        icon.getTransforms().add(translate);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(translate.yProperty(), 0)),
                new KeyFrame(Duration.millis(150), new KeyValue(translate.yProperty(), -8)),
                new KeyFrame(Duration.millis(300), new KeyValue(translate.yProperty(), 0)),
                new KeyFrame(Duration.millis(450), new KeyValue(translate.yProperty(), -8)),
                new KeyFrame(Duration.millis(600), new KeyValue(translate.yProperty(), 0)),
                new KeyFrame(Duration.millis(750), new KeyValue(translate.yProperty(), -8)),
                new KeyFrame(Duration.millis(900), new KeyValue(translate.yProperty(), 0))
        );
        timeline.setCycleCount(1);

        label.setOnMouseEntered(event -> {
            icon.setEffect(new DropShadow(10, Color.GRAY));
            timeline.playFromStart();
        });
        label.setOnMouseExited(event -> {
            icon.setEffect(null);
            translate.setY(0);
        });
    }

    private void setupCommentAnimation(FontAwesomeIconView icon, Label label) {
        if (icon == null || label == null) {
            System.out.println("Warning: commentIcon or commentLabel is null");
            return;
        }
        Translate translate = new Translate(0, 0);
        Scale scale = new Scale(1, 1);
        icon.getTransforms().addAll(translate, scale);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(translate.yProperty(), 0),
                        new KeyValue(scale.xProperty(), 1),
                        new KeyValue(scale.yProperty(), 1)),
                new KeyFrame(Duration.millis(150),
                        new KeyValue(translate.yProperty(), -6),
                        new KeyValue(scale.xProperty(), 1.1),
                        new KeyValue(scale.yProperty(), 1.1)),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(translate.yProperty(), 0),
                        new KeyValue(scale.xProperty(), 1.0),
                        new KeyValue(scale.yProperty(), 1.0)),
                new KeyFrame(Duration.millis(450),
                        new KeyValue(translate.yProperty(), -6),
                        new KeyValue(scale.xProperty(), 1.1),
                        new KeyValue(scale.yProperty(), 1.1)),
                new KeyFrame(Duration.millis(600),
                        new KeyValue(translate.yProperty(), 0),
                        new KeyValue(scale.xProperty(), 1.0),
                        new KeyValue(scale.yProperty(), 1.0)),
                new KeyFrame(Duration.millis(750),
                        new KeyValue(translate.yProperty(), -6),
                        new KeyValue(scale.xProperty(), 1.1),
                        new KeyValue(scale.yProperty(), 1.1)),
                new KeyFrame(Duration.millis(900),
                        new KeyValue(translate.yProperty(), 0),
                        new KeyValue(scale.xProperty(), 1.0),
                        new KeyValue(scale.yProperty(), 1.0))
        );
        timeline.setCycleCount(1);

        label.setOnMouseEntered(event -> {
            icon.setEffect(new DropShadow(10, Color.GRAY));
            timeline.playFromStart();
        });
        label.setOnMouseExited(event -> {
            icon.setEffect(null);
            translate.setY(0);
            scale.setX(1);
            scale.setY(1);
        });
    }

    private void setupSearchAnimation(FontAwesomeIconView icon) {
        if (icon == null) {
            System.out.println("Warning: searchIcon is null");
            return;
        }
        Scale scale = new Scale(1, 1);
        Rotate rotate = new Rotate(0, Rotate.Z_AXIS);
        icon.getTransforms().addAll(scale, rotate);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(scale.xProperty(), 1),
                        new KeyValue(scale.yProperty(), 1),
                        new KeyValue(rotate.angleProperty(), 0)),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(scale.xProperty(), 1.5),
                        new KeyValue(scale.yProperty(), 1.5),
                        new KeyValue(rotate.angleProperty(), 15)),
                new KeyFrame(Duration.millis(600),
                        new KeyValue(scale.xProperty(), 1.5),
                        new KeyValue(scale.yProperty(), 1.5),
                        new KeyValue(rotate.angleProperty(), 15)),
                new KeyFrame(Duration.millis(900),
                        new KeyValue(scale.xProperty(), 1),
                        new KeyValue(scale.yProperty(), 1),
                        new KeyValue(rotate.angleProperty(), 0))
        );
        timeline.setCycleCount(1);

        icon.setOnMouseEntered(event -> {
            icon.setEffect(new DropShadow(10, Color.GRAY));
            timeline.playFromStart();
        });
        icon.setOnMouseExited(event -> {
            icon.setEffect(null);
            scale.setX(1);
            scale.setY(1);
            rotate.setAngle(0);
        });
    }

    private void setupBellAnimation(FontAwesomeIconView icon) {
        if (icon == null) {
            System.out.println("Warning: bellIcon is null");
            return;
        }
        Rotate rotate = new Rotate(0, Rotate.Z_AXIS);
        icon.getTransforms().add(rotate);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(rotate.angleProperty(), 0)),
                new KeyFrame(Duration.millis(150), new KeyValue(rotate.angleProperty(), 15)),
                new KeyFrame(Duration.millis(300), new KeyValue(rotate.angleProperty(), -15)),
                new KeyFrame(Duration.millis(450), new KeyValue(rotate.angleProperty(), 10)),
                new KeyFrame(Duration.millis(600), new KeyValue(rotate.angleProperty(), -10)),
                new KeyFrame(Duration.millis(750), new KeyValue(rotate.angleProperty(), 5)),
                new KeyFrame(Duration.millis(900), new KeyValue(rotate.angleProperty(), 0))
        );
        timeline.setCycleCount(1);

        icon.setOnMouseEntered(event -> {
            icon.setEffect(new DropShadow(10, Color.GRAY));
            timeline.playFromStart();
        });
        icon.setOnMouseExited(event -> {
            icon.setEffect(null);
            rotate.setAngle(0);
        });
    }

    private void setupGifAnimation(ImageView gif) {
        if (gif == null) {
            System.out.println("Warning: homeGif is null");
            return;
        }
        Scale scale = new Scale(1, 1);
        gif.getTransforms().add(scale);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(scale.xProperty(), 1),
                        new KeyValue(scale.yProperty(), 1)),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(scale.xProperty(), 1.1),
                        new KeyValue(scale.yProperty(), 1.1)),
                new KeyFrame(Duration.millis(600),
                        new KeyValue(scale.xProperty(), 1.1),
                        new KeyValue(scale.yProperty(), 1.1)),
                new KeyFrame(Duration.millis(900),
                        new KeyValue(scale.xProperty(), 1),
                        new KeyValue(scale.yProperty(), 1))
        );
        timeline.setCycleCount(1);

        gif.setOnMouseEntered(event -> {
            gif.setEffect(new DropShadow(10, Color.GRAY));
            timeline.playFromStart();
        });
        gif.setOnMouseExited(event -> {
            gif.setEffect(null);
            scale.setX(1);
            scale.setY(1);
        });
    }

    @FXML
    protected void ToHome2(MouseEvent event) throws IOException {
        UserSession session = UserSession.getInstance();
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/maged/Home2.fxml"));
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
        stage.setTitle("Payment");
        stage.setWidth(1550);
        stage.setHeight(840);
        stage.centerOnScreen();
        stage.show();
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
    protected void ToDepositeWithDraw(MouseEvent event) throws IOException {
        UserSession session = UserSession.getInstance();
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/maged/DepositeWithDraw.fxml"));
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
        stage.setTitle("Payment");
        stage.setWidth(1550);
        stage.setHeight(840);
        stage.centerOnScreen();
        stage.show();
    }

    @FXML
    protected void ToPayment(MouseEvent event) throws IOException {
        UserSession session = UserSession.getInstance();
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/maged/Payment.fxml"));
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
        stage.setTitle("Payment");
        stage.setWidth(1550);
        stage.setHeight(840);
        stage.centerOnScreen();
        stage.show();
    }

    @FXML
    protected void ToDashBoard(MouseEvent event) throws IOException {
        UserSession session = UserSession.getInstance();
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/maged/Gauge.fxml"));
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
    protected void ToSettings(MouseEvent event) throws IOException {
        UserSession session = UserSession.getInstance();
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/maged/Settings.fxml"));
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
}