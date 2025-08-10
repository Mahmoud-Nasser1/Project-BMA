package com.banking;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.scene.shape.Rectangle;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.BarChart;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class Account {

    @FXML
    private  Label AccountUser;

    @FXML
    private Button addCardButton;

    @FXML
    private HBox cardContainer;

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private VBox cardInputBox;

    UserSession userSession= UserSession.getInstance();
    private int cardCount = database_BankSystem.getUserCardCount (userSession.getUsername());
    private final int maxCards = 4;

    @FXML
    private VBox transactionList;

    @FXML
    public Label AcTotBl;
    public Label AcInc;
    public Label AcExp;
    public Label cardbalance;
    @FXML
    private Image cardImage;
    private String name;
    private String number;
    private String type;
    private String amountValue;
    @FXML
    private TextField amountField;


    //------------------------------------------------------------------------------------------------------------------------------------------//
    //sidebar
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

    @FXML
    private ImageView HomeImage1;
    //-------------------------------------------------------------------------------------------------------------//

    @FXML
    public void initialize() {
        //Data Base
        UserSession userSession = UserSession.getInstance();
        String username = userSession.getUsername();
        AccountUser.setText(username);

        database_BankSystem.UserDetails userDetails = database_BankSystem.getUserDetails(username);
        String imagePath = userDetails.getProfileImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            HomeImage1.setImage(new Image("file:" + imagePath));
        }

        System.out.println(cardCount);
        updateTotalBalanceDisplay();
        int tempcount=0;

        updateIncomeExpenseLabels();

        initializeBarChart();

        List<com.banking.TransferConfirm.Transaction> transactions = database_BankSystem.getUserTransactions(username);
        transactions = transactions.subList(0, Math.min(6, transactions.size())); // Get only the first 6

        transactionList.getChildren().clear();

        for (int i = 0; i < transactions.size(); i += 2) {
            HBox row = new HBox(20);
            row.setAlignment(Pos.CENTER);
            row.setPrefWidth(Region.USE_COMPUTED_SIZE);

            com.banking.TransferConfirm.Transaction t1 = transactions.get(i);
            HBox card1 = createTransactionCard(t1);
            HBox.setHgrow(card1, Priority.ALWAYS);
            card1.setMaxWidth(Double.MAX_VALUE);
            row.getChildren().add(card1);

            if (i + 1 < transactions.size()) {
                com.banking.TransferConfirm.Transaction t2 = transactions.get(i + 1);
                HBox card2 = createTransactionCard(t2);
                HBox.setHgrow(card2, Priority.ALWAYS);
                card2.setMaxWidth(Double.MAX_VALUE);
                row.getChildren().add(card2);
            } else {
                Region filler = new Region();
                HBox.setHgrow(filler, Priority.ALWAYS);
                row.getChildren().add(filler);
            }

            transactionList.getChildren().add(row);
        }

        barChart.setLegendVisible(false);
        barChart.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent;");

        Node chartTitle = barChart.lookup(".chart-title");
        if (chartTitle != null) {
            chartTitle.setStyle("-fx-text-fill: white;");
        }

        barChart.lookup(".axis-label").setStyle("-fx-text-fill: white;");
        barChart.lookup(".axis").setStyle("-fx-tick-label-fill: white;");

        cardInputBox.setVisible(false);
        cardInputBox.setManaged(false);

        if (tempcount<=cardCount) {
            Image img = new Image(getClass().getResourceAsStream("/s2.png"));
            ImageView newCard = new ImageView(img);
            newCard.setFitWidth(250);
            newCard.setFitHeight(170);
            HBox.setMargin(newCard, new Insets(0, 0, 0, 0));
            double balance = database_BankSystem.getBalance(userSession.getUsername());

            List<database_BankSystem.Card> userCards = database_BankSystem.getUserCards(userSession.getUsername());
            if (!userCards.isEmpty()) {
                database_BankSystem.Card firstCard = userCards.get(0);
                double totalBalance = database_BankSystem.getBalance(userSession.getUsername());
                newCard.setOnMouseClicked(event -> showFloatingCardWindow(img, userSession.getUsername(), "5671 9860 8300 0202", firstCard.getCardType(), String.format("%.2f", totalBalance)));
            }

            if(cardCount==0) {
                database_BankSystem.addCard(userSession.getUsername(), "Debit Card", balance);
                cardCount++;
            }
            cardContainer.getChildren().add(newCard);
            tempcount++;
        }
        if (tempcount<cardCount) {
            Image img = new Image(getClass().getResourceAsStream("/s4.png"));
            ImageView newCard = new ImageView(img);
            newCard.setFitWidth(250);
            newCard.setFitHeight(170);
            HBox.setMargin(newCard, new Insets(0, 0, 0, -170));

            // Get the second card details
            List<database_BankSystem.Card> userCards = database_BankSystem.getUserCards(userSession.getUsername());
            if (userCards.size() > 1) {
                database_BankSystem.Card secondCard = userCards.get(1);
                newCard.setOnMouseClicked(event -> showFloatingCardWindow(img, userSession.getUsername(), "4536 0001 2345 6789", secondCard.getCardType(), String.valueOf(secondCard.getAmount())));
            }

            cardContainer.getChildren().add(newCard);
            tempcount++;
        }
        if (tempcount<cardCount) {
            Image img = new Image(getClass().getResourceAsStream("/s3.png"));
            ImageView newCard = new ImageView(img);
            newCard.setFitWidth(250);
            newCard.setFitHeight(170);
            HBox.setMargin(newCard, new Insets(0, 0, 0, -170));

            List<database_BankSystem.Card> userCards = database_BankSystem.getUserCards(userSession.getUsername());
            if (userCards.size() > 2) {
                database_BankSystem.Card thirdCard = userCards.get(2);
                newCard.setOnMouseClicked(event -> showFloatingCardWindow(img, userSession.getUsername(), "5169 0333 9988 0008", thirdCard.getCardType(), String.valueOf(thirdCard.getAmount())));
            }

            cardContainer.getChildren().add(newCard);
            tempcount++;
        }
        if (tempcount<cardCount) {
            Image img = new Image(getClass().getResourceAsStream("/s1.png"));
            ImageView newCard = new ImageView(img);
            newCard.setFitWidth(250);
            newCard.setFitHeight(170);
            HBox.setMargin(newCard, new Insets(0, 0, 0, -170));

            List<database_BankSystem.Card> userCards = database_BankSystem.getUserCards(userSession.getUsername());
            if (userCards.size() > 3) {
                database_BankSystem.Card fourthCard = userCards.get(3);
                newCard.setOnMouseClicked(event -> showFloatingCardWindow(img, userSession.getUsername(), "1234 5678 9876 5432", fourthCard.getCardType(), String.valueOf(fourthCard.getAmount())));
            }

            cardContainer.getChildren().add(newCard);
            cardCount++;
            addCardButton.setVisible(false);
            TranslateTransition moveLeft = new TranslateTransition(Duration.millis(500), cardContainer);
            moveLeft.setByX(-60); // Move the HBox left by 60 pixels
            moveLeft.play();
        }


        //-------------------------------------------------------------------------------------------------------------------------------------------//
        //sidebar
        setupHomeAnimation(homeIcon, homeLabel);
        setupUserAnimation(userIcon, userLabel);
        setupExchangeAnimation(exchangeIcon, exchangeLabel);
        setupMoneyAnimation(moneyIcon, moneyLabel);
        setupChartAnimation(chartIcon, chartLabel);
        setupMapAnimation(mapIcon, mapLabel);
        setupCogAnimation(cogIcon, cogLabel);
        setupHelpAnimation(helpIcon, helpLabel);
        setupCommentAnimation(commentIcon, commentLabel);
        // Top bar icons
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
        // GIF animation
        if (homeGif != null) {
            setupGifAnimation(homeGif);
        } else {
            System.out.println("Warning: homeGif is null");
        }
        //---------------------------------------------------------------------------------------------------------------------------------------------//

    }

    public void print(MouseEvent event) {
        System.out.println("Hello World");
    }

    private void showFloatingCardWindow(Image cardImage, String name, String number, String type, String amount) {
        // Get the actual card amount from database
        List<database_BankSystem.Card> userCards = database_BankSystem.getUserCards(name);
        String actualAmount = "0.0";
        for (database_BankSystem.Card card : userCards) {
            if (card.getCardType().equals(type)) {
                actualAmount = String.format("%.2f EGP", card.getAmount());
                break;
            }
        }

        Stage popupStage = new Stage();
        popupStage.initStyle(StageStyle.TRANSPARENT);

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setStyle("""
            -fx-padding: 20;
            -fx-background-color:rgba(0, 0, 0, 0.3);
            -fx-border-radius: 20;
            -fx-background-radius: 20;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0), 20, 0.5, 0, 4);
        """);

        content.setEffect(new BoxBlur(15, 15, 3));

        ImageView imgView = new ImageView(cardImage);
        imgView.setFitWidth(250);
        imgView.setFitHeight(150);

        Label nameLabel = new Label("Name: " + name);
        Label numberLabel = new Label("Card Number: " + number);
        Label typeLabel = new Label("Type: " + type);
        Label amountLabel = new Label("Amount: " + actualAmount);

        nameLabel.setFont(Font.font(16));
        nameLabel.setStyle("-fx-text-fill: #ffffff;");
        numberLabel.setFont(Font.font(16));
        numberLabel.setStyle("-fx-text-fill: #ffffff;");
        typeLabel.setFont(Font.font(16));
        typeLabel.setStyle("-fx-text-fill: #ffffff;");
        amountLabel.setFont(Font.font(16));
        amountLabel.setStyle("-fx-text-fill: #ffffff;");

        Button closeBtn = new Button("❌");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18; -fx-text-fill: red;");
        closeBtn.setOnAction(e -> popupStage.close());

        VBox header = new VBox(closeBtn);
        header.setAlignment(Pos.TOP_RIGHT);

        content.getChildren().addAll(header, imgView, nameLabel, numberLabel, typeLabel, amountLabel);

        StackPane root = new StackPane(content);
        root.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(root);
        scene.setFill(null);

        popupStage.setScene(scene);
        popupStage.show();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        popupStage.setX((screenBounds.getWidth() - scene.getWidth()) / 2);
        popupStage.setY((screenBounds.getHeight() - scene.getHeight()) / 2);
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


    @FXML
    private void onAddCardClicked() {

        if (cardCount >= maxCards) return;

        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setTitle("Add New Card");

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        form.setAlignment(Pos.CENTER);
        form.setStyle("""
                    -fx-background-color: transparent;
                    -fx-border-radius: 30;
                    -fx-background-radius: 30;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 30, 0.2, 0, 8);
                """);
        form.setEffect(new BoxBlur(10, 10, 3));

        StackPane root = new StackPane(form);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");

        Rectangle clip = new Rectangle(320, 300);
        clip.setArcWidth(50);
        clip.setArcHeight(50);
        root.setClip(clip);

        ComboBox<String> cardTypeComboBox = new ComboBox<>();
        cardTypeComboBox.getItems().addAll("Debit Card", "Credit Card", "Prepaid Card", "Virtual Card");
        cardTypeComboBox.setPromptText("Select Card Type");
        cardTypeComboBox.setPrefWidth(250);

        TextField amountField = new TextField();
        amountField.setPromptText("Enter Amount");
        amountField.setPrefWidth(250);

        cardTypeComboBox.setStyle("-fx-background-radius: 12; -fx-font-size: 14px;");
        amountField.setStyle("-fx-background-radius: 12; -fx-font-size: 14px;");

        Button addBtn = new Button("Add Card");
        addBtn.setStyle("""
                    -fx-background-color: linear-gradient(to right, #008cff,#6DD5FA);
                    -fx-text-fill: white;
                    -fx-font-size: 15px;
                    -fx-font-weight: bold;
                    -fx-background-radius: 30;
                    -fx-padding: 8 20 8 20;
                    -fx-cursor: hand;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.3, 0, 4);
                """);

        Button closeBtn = new Button("❌");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18; -fx-text-fill: red;");
        closeBtn.setOnAction(e -> dialog.close());

        HBox topBar = new HBox(closeBtn);
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.setPrefWidth(Double.MAX_VALUE);

        Label title = new Label("Enter card info:");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        form.getChildren().addAll(topBar, title, cardTypeComboBox, amountField, addBtn);

        Scene dialogScene = new Scene(root, 320, 300);
        dialogScene.setFill(Color.TRANSPARENT);
        dialog.setScene(dialogScene);
        dialog.show();

        addBtn.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                if (amount < 0) {
                    showErrorDialog("Failed to add card. Invalid Negative Amount.");
                    return;
                }
            }
            catch (NumberFormatException ex) {
                showErrorDialog("Failed to add card. Invalid amount entered.");
                return;
            }
            String cardType = cardTypeComboBox.getValue();
            String amount = amountField.getText();

            if (cardType == null || amount.isEmpty()) {
                showErrorDialog("Please fill in all fields.");
                return;
            }

            UserSession session = UserSession.getInstance();
            String username = session.getUsername();

            List<database_BankSystem.Card> existingCards = database_BankSystem.getUserCards(username);
            for (database_BankSystem.Card card : existingCards) {
                if (card.getCardType().equals(cardType)) {
                    showErrorDialog("A card of type '" + cardType + "' already exists.");
                    return;
                }
            }

            if (!database_BankSystem.addCard(username, cardType, Double.parseDouble(amount))) {
                showErrorDialog("Failed to add card. Please try again.");
                return;
            }
            database_BankSystem.updateBalance(username, Double.parseDouble(amount) + database_BankSystem.getBalance(username));

            String name = cardType; // Use card type as name
            String number = "**** **** **** ****"; // Placeholder for card number
            String type = cardType; // Card type
            String amountValue = amount; // Amount with currency

            if (cardCount == 0) {
                Image img = new Image(getClass().getResourceAsStream("/s2.png"));
                ImageView newCard = new ImageView(img);
                newCard.setFitWidth(250);
                newCard.setFitHeight(170);
                HBox.setMargin(newCard, new Insets(0, 0, 0, 0));
                double totalBalance = database_BankSystem.getBalance(username);
                newCard.setOnMouseClicked(event -> showFloatingCardWindow(img, username, "4536 0001 2345 6789", type, String.format("%.2f", totalBalance)));
                cardContainer.getChildren().add(newCard);
                cardCount++;
            } else if (cardCount == 1) {
                Image img = new Image(getClass().getResourceAsStream("/s4.png"));
                ImageView newCard = new ImageView(img);
                newCard.setFitWidth(250);
                newCard.setFitHeight(170);
                HBox.setMargin(newCard, new Insets(0, 0, 0, -170));
                newCard.setOnMouseClicked(event -> showFloatingCardWindow(img, username, "4536 0001 2345 6789", type, amountValue));
                cardContainer.getChildren().add(newCard);
                cardCount++;
            } else if (cardCount == 2) {
                Image img = new Image(getClass().getResourceAsStream("/s3.png"));
                ImageView newCard = new ImageView(img);
                newCard.setFitWidth(250);
                newCard.setFitHeight(170);
                HBox.setMargin(newCard, new Insets(0, 0, 0, -170));
                newCard.setOnMouseClicked(event -> showFloatingCardWindow(img, username, "5169 0333 9988 0008", type, amountValue));
                cardContainer.getChildren().add(newCard);
                cardCount++;
            } else if (cardCount == 3) {
                Image img = new Image(getClass().getResourceAsStream("/s1.png"));
                ImageView newCard = new ImageView(img);
                newCard.setFitWidth(250);
                newCard.setFitHeight(170);
                HBox.setMargin(newCard, new Insets(0, 0, 0, -170));
                newCard.setOnMouseClicked(event -> showFloatingCardWindow(img, username, "1234 5678 9876 5432", type, amountValue));
                cardContainer.getChildren().add(newCard);
                cardCount++;
                addCardButton.setVisible(false);
                TranslateTransition moveLeft = new TranslateTransition(Duration.millis(500), cardContainer);
                moveLeft.setByX(-60);
                moveLeft.play();
            }

            updateTotalBalanceDisplay();
            dialog.close();
        });
    }
    private HBox createTransactionCard(com.banking.TransferConfirm.Transaction t) {
        String name = t.getRecipient();
        String type = t.getType();
        double amount = t.getAmount();
        boolean isIncoming = type.contains("Received") || type.contains("income");

        String amountStr = String.format("%s$%.2f", isIncoming ? "+" : "-", amount);
        String imgPath = isIncoming ? "/bank23.jpeg" : "/bank23.jpeg";
        Color color = isIncoming ? Color.LIMEGREEN : Color.RED;

        HBox box = new HBox(10);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 12;");
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(280);

        box.setPrefWidth(Region.USE_COMPUTED_SIZE);
        box.setMaxWidth(Double.MAX_VALUE);

        ImageView img = new ImageView(new Image(getClass().getResourceAsStream(imgPath)));
        img.setFitWidth(40);
        img.setFitHeight(40);
        img.setClip(new Circle(20, 20, 20));

        VBox texts = new VBox(3);
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;-fx-font-size: 14;");
        Label detailLabel = new Label(type);
        detailLabel.setStyle("-fx-text-fill: #F0F0F0; -fx-font-size: 12;");
        texts.getChildren().addAll(nameLabel, detailLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amountLabel = new Label(amountStr);
        amountLabel.setTextFill(color);
        amountLabel.setStyle("-fx-font-size: 14;");

        box.getChildren().addAll(img, texts, spacer, amountLabel);
        return box;
    }

    public  double calculateMonthlyIncome() {
        UserSession userSession = UserSession.getInstance();
        String username = userSession.getUsername();
        String currentMonth = String.valueOf(java.time.LocalDate.now().getMonthValue());
        return database_BankSystem.getMonthlyIncome(username, currentMonth);
    }

    public double calculateMonthlyExpenses() {
        UserSession userSession = UserSession.getInstance();
        String username = userSession.getUsername();
        String currentMonth = String.valueOf(java.time.LocalDate.now().getMonthValue());
        return database_BankSystem.getMonthlyExpenses(username, currentMonth);
    }
    public void updateIncomeExpenseLabels() {
        double monthlyIncome = calculateMonthlyIncome();
        double monthlyExpenses = calculateMonthlyExpenses();

        AcInc.setText(String.format("$%.2f", monthlyIncome));
        AcExp.setText(String.format("$%.2f", monthlyExpenses));
    }
    private void updateTotalBalanceDisplay() {
        System.out.println(calculateMonthlyIncome());
        UserSession userSession = UserSession.getInstance();
        if (AcTotBl != null && userSession != null) {

            String username = userSession.getUsername();
            if (username != null && !username.isEmpty()) {
                double balance = database_BankSystem.getBalance(username);
                if (balance >= 0) {
                    AcTotBl.setText(String.format("$%.2f", balance));
                    cardbalance.setText(AcTotBl.getText());

                } else {
                    AcTotBl.setText("$0.00");
                }
            } else {
                AcTotBl.setText("$0.00");
            }
        }
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------//
    //sidebar
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
    //---------------------------------------------------------------------------------------------------------------------------------------------//
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

    private void initializeBarChart() {
        XYChart.Series<String, Number> incomes = new XYChart.Series<>();
        incomes.setName("Incomes");

        XYChart.Series<String, Number> expenses = new XYChart.Series<>();
        expenses.setName("Expenses");

        java.time.LocalDate currentDate = java.time.LocalDate.now();
        String[] months = new String[6];
        double[] incomeValues = new double[6];
        double[] expenseValues = new double[6];

        double maxValue = 0;
        for (int i = 0; i < 6; i++) {
            java.time.LocalDate date = currentDate.minusMonths(5 - i);
            String month = String.valueOf(date.getMonthValue());
            String monthName = date.getMonth().toString().substring(0, 3);
            months[i] = monthName;

            UserSession session = UserSession.getInstance();
            String username = session.getUsername();

            double income = database_BankSystem.getMonthlyIncome(username, month);
            double expense = database_BankSystem.getMonthlyExpenses(username, month);

            incomeValues[i] = income;
            expenseValues[i] = expense;

            maxValue = Math.max(maxValue, Math.max(income, expense));
        }

        barChart.getData().clear();

        barChart.setAnimated(false);
        barChart.setHorizontalGridLinesVisible(false);
        barChart.setVerticalGridLinesVisible(false);
        barChart.setBarGap(1);
        barChart.setCategoryGap(55);

        barChart.setMinHeight(300);
        barChart.setPrefHeight(300);

        for (int i = 0; i < 6; i++) {
            double incomePercentage = (maxValue > 0) ? (incomeValues[i] / maxValue) * 100 : 0;
            double expensePercentage = (maxValue > 0) ? (expenseValues[i] / maxValue) * 100 : 0;

            incomes.getData().add(new XYChart.Data<>(months[i], incomePercentage));
            expenses.getData().add(new XYChart.Data<>(months[i], expensePercentage));
        }

        barChart.getData().addAll(incomes, expenses);

        for (XYChart.Series<String, Number> series : barChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    node.setStyle("-fx-bar-width: 1.5px;");
                    node.setOnMouseEntered(e -> {
                        node.setStyle("-fx-opacity: 0.8; -fx-bar-width: 1.5px;");
                    });
                    node.setOnMouseExited(e -> {
                        node.setStyle("-fx-opacity: 1; -fx-bar-width: 1.5px;");
                    });
                }
            }
        }

        AcInc.setText(String.format("$%.2f", incomeValues[5]));
        AcExp.setText(String.format("$%.2f", expenseValues[5]));
    }

}