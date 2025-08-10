package com.banking;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Translate;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GaugeController {

    @FXML
    private Label AccountUser1;

    @FXML
    private PieChart donutChart;

    @FXML
    private StackPane chartPane;

    @FXML
    private AnimatedCircularProgressBar progressBar1;

    @FXML
    private AnimatedCircularProgressBar progressBar2;

    @FXML
    private AnimatedCircularProgressBar progressBar3;

    @FXML
    private LineChart<String, Number> lineChart;

    @FXML
    private VBox root;

    @FXML
    private VBox legendVBox;

    private final XYChart.Series<String, Number> series1 = new XYChart.Series<>();
    private final XYChart.Series<String, Number> series2 = new XYChart.Series<>();
    private final LinkedList<Integer> values1 = new LinkedList<>();
    private final LinkedList<Integer> values2 = new LinkedList<>();
    private final Random rand = new Random();
    private int index = 1;

    @FXML
    private StackPane chart;

    // Sidebar icons and labels
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



    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        String username = session.getUsername();
        AccountUser1.setText(username);

        int userId = database_BankSystem.getUserId(username);

        double totalBalance = database_BankSystem.getTotalBalance(username);

        LocalDate today = LocalDate.now();

        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String start = startOfMonth.format(formatter);
        String endDate = endOfMonth.format(formatter);

        List<Object[]> withdrawalData = database_BankSystem.getMonthlyTransactions(username, "expense", start, endDate);
        List<Object[]> depositData = database_BankSystem.getMonthlyTransactions(username, "income", start, endDate);

        double totalexpenses = 0.0;
        double totalincomes = 0.0;

        for (Object[] data : withdrawalData) {
            totalexpenses += (Double) data[1];
        }

        for (Object[] data : depositData) {
            totalincomes += (Double) data[1];
        }

        double totalTransactions = totalincomes + totalexpenses;
        if (totalBalance <= 0) {
            totalBalance = 1.0;
        }

        double expensePercentage = (totalBalance > 0) ? (totalexpenses / totalBalance) * 100 : 0.0;
        double incomePercentage = (totalBalance > 0) ? (totalincomes / totalBalance) * 100 : 0.0;

        double earningValue = totalBalance * (15.0 / 100);
        double earningPercentage = (totalBalance > 0) ? (earningValue / totalBalance) * 100 : 0.0;

        System.out.println("Total Deposits (Database): " + totalincomes);
        System.out.println("Total Withdrawals (Database): " + totalexpenses);
        System.out.println("Total Balance: " + totalBalance);
        System.out.println("expenses Percentage: " + expensePercentage + "%");
        System.out.println("income Percentage: " + incomePercentage + "%");
        System.out.println("Earning Value (15% of Total Balance): " + earningValue);
        System.out.println("Earning Percentage: " + earningPercentage + "%");

        progressBar1.setTarget(expensePercentage);
        progressBar2.setTarget(earningPercentage);
        progressBar3.setTarget(incomePercentage);

        progressBar1.setOnMouseClicked(event -> restartProgressBarAnimation(progressBar1));
        progressBar2.setOnMouseClicked(event -> restartProgressBarAnimation(progressBar2));
        progressBar3.setOnMouseClicked(event -> restartProgressBarAnimation(progressBar3));

        progressBar1.getProgressArc().setStroke(getColorBasedOnValue(expensePercentage));
        progressBar2.getProgressArc().setStroke(getColorBasedOnValue(earningPercentage));
        progressBar3.getProgressArc().setStroke(getColorBasedOnValue(incomePercentage));

        List<Object[]> categoryData = database_BankSystem.getPaymentTotalsByCategory(userId);

        donutChart.getData().clear();
        String[] categories = {"Investments", "Bills", "Mobile Top-Ups", "Credit Card", "Government", "Donations", "Education", "Insurance", "Other"};
        String[] colors = {"#FF5733", "#FFC107", "#28A745", "#17A2B8", "#6610F2", "#FD7E14", "#20C997", "#E83E8C", "#6F42C1"};

        if (!categoryData.isEmpty()) {
            for (Object[] data : categoryData) {
                String category = (String) data[0];
                double percentage = (Double) data[1];
                PieChart.Data pieData = new PieChart.Data(category + " (" + String.format("%.1f%%", percentage) + ")", percentage);
                donutChart.getData().add(pieData);

                int colorIndex = -1;
                for (int i = 0; i < categories.length; i++) {
                    if (categories[i].equals(category)) {
                        colorIndex = i;
                        break;
                    }
                }
                if (colorIndex != -1) {
                    pieData.getNode().setStyle("-fx-pie-color: " + colors[colorIndex] + ";");
                }

                pieData.getNode().setOnMouseEntered(event -> {
                    Scale scale = new Scale(1.1, 1.1, 0, 0);
                    pieData.getNode().getTransforms().setAll(scale);
                    String sliceColor = pieData.getNode().getStyle().replace("-fx-pie-color: ", "").replace(";", "").trim();

                    Tooltip tooltip = new Tooltip(String.format(category + " %.1f%%", percentage));
                    tooltip.setStyle(
                            "-fx-font-size: 14px;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-background-color: white;" +
                                    "-fx-text-fill: " + sliceColor + ";" +
                                    "-fx-padding: 5px;" +
                                    "-fx-border-color: " + sliceColor + ";" +
                                    "-fx-border-width: 1px;" +
                                    "-fx-border-radius: 5px;" +
                                    "-fx-background-radius: 5px;"
                    );
                    pieData.getNode().getProperties().put("storedTooltip", tooltip);
                    Tooltip.install(pieData.getNode(), tooltip);
                });
                pieData.getNode().setOnMouseExited(event -> {
                    pieData.getNode().getTransforms().clear();
                    Tooltip tooltip = (Tooltip) pieData.getNode().getProperties().get("storedTooltip");
                    if (tooltip != null) {
                        Tooltip.uninstall(pieData.getNode(), tooltip);
                    }
                });
            }
        } else {
            PieChart.Data data = new PieChart.Data("No Payments (0%)", 100);
            donutChart.getData().add(data);
            data.getNode().setStyle("-fx-pie-color: #6C757D;");
        }

        donutChart.setLegendVisible(false);
        donutChart.setLabelsVisible(false);
        donutChart.setStartAngle(90);

        Text centerText = (Text) chartPane.lookup("#centerText");
        if (centerText != null) {
            centerText.setText(!categoryData.isEmpty() ? "100.0%" : "0%");
            centerText.setFill(Color.WHITE);
            centerText.setFont(Font.font("System", FontWeight.BOLD, 24));
        }

        legendVBox.getChildren().clear();

        HBox columnsContainer = new HBox(30);
        columnsContainer.setAlignment(Pos.CENTER);

        List<VBox> columns = new ArrayList<>();
        int itemsPerColumn = 3;

        int totalItems = categoryData.size();
        int totalColumns = (int) Math.ceil((double) totalItems / itemsPerColumn);

        for (int i = 0; i < totalColumns; i++) {
            VBox column = new VBox(10);
            column.setAlignment(Pos.TOP_LEFT);
            columns.add(column);
            columnsContainer.getChildren().add(column);
        }

        for (int i = 0; i < totalItems; i++) {
            Object[] data = categoryData.get(i);
            String category = (String) data[0];
            double percentage = (Double) data[1];

            HBox legendItem = new HBox(5);
            legendItem.setAlignment(Pos.CENTER_LEFT);

            Circle colorCircle = new Circle(7);
            int colorIndex = -1;
            for (int j = 0; j < categories.length; j++) {
                if (categories[j].equals(category)) {
                    colorIndex = j;
                    break;
                }
            }
            if (colorIndex != -1) {
                colorCircle.setStyle("-fx-fill: " + colors[colorIndex] + ";");
            }

            Text categoryText = new Text(category);
            categoryText.setFill(Color.web("#f4f4f4"));
            categoryText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

            Text percentageText = new Text(String.format("%.1f%%", percentage));
            percentageText.setFill(Color.web("#f4f4f4"));
            percentageText.setStyle("-fx-font-size: 12px;");

            legendItem.getChildren().addAll(colorCircle, categoryText, percentageText);

            int columnIndex = i / itemsPerColumn;
            columns.get(columnIndex).getChildren().add(legendItem);
        }

        legendVBox.getChildren().add(columnsContainer);
        addTypingEffect(progressBar1, String.format("expenses for this month: %.1f", totalexpenses));
        addTypingEffect(progressBar2, String.format("Earning for this month: %.1f $", earningValue));
        addTypingEffect(progressBar3, String.format("incomes for this month: %.1f", totalincomes));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Profit/Loss (USD)");

        root.setStyle("-fx-background-color: rgba(50, 50, 50, 0.2);");

        lineChart.setLegendVisible(false);
        lineChart.setCreateSymbols(false);
        lineChart.setAnimated(false);
        lineChart.setHorizontalGridLinesVisible(true);
        lineChart.setVerticalGridLinesVisible(true);
        lineChart.setAlternativeRowFillVisible(false);
        lineChart.setAlternativeColumnFillVisible(false);
        lineChart.setStyle("-fx-background-color: transparent;");
        Platform.runLater(() -> {
            Node background = lineChart.lookup(".chart-plot-background");
            if (background != null) {
                background.setStyle("-fx-background-color: transparent;");
            }
        });

        lineChart.getData().addAll(series1, series2);
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis(10, 100, 2);

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Line Chart ");
        lineChart.setAnimated(false);

        series1.setName("Dollar");
        series2.setName("Gold");

        for (int i = 0; i < 10; i++) {
            values1.add(rand.nextInt(50) + 50);
            values2.add(rand.nextInt(50) + 50);
        }

        updateChart();

        Platform.runLater(() -> {
            Node line1 = series1.getNode().lookup(".chart-series-line");
            if (line1 != null) {
                line1.setStyle("-fx-stroke: green; -fx-stroke-width: 3px;");
            }
            Node line2 = series2.getNode().lookup(".chart-series-line");
            if (line2 != null) {
                line2.setStyle("-fx-stroke: gold; -fx-stroke-width: 3px;");
            }
        });

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            values1.removeFirst();
            values2.removeFirst();

            int newVal1 = values1.getLast() + (rand.nextInt(41) - 20);
            int newVal2 = values2.getLast() + (rand.nextInt(41) - 20);

            newVal1 = Math.max(30, Math.min(100, newVal1));
            newVal2 = Math.max(30, Math.min(100, newVal2));

            values1.add(newVal1);
            values2.add(newVal2);

            updateChart();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        double totalBalanceForChart = database_BankSystem.getTotalBalance(username);
        if (totalBalanceForChart <= 0) {
            totalBalanceForChart = 1.0; // Avoid division by zero
        }
        System.out.println("Total Balance for Chart: " + totalBalanceForChart);

        List<Object[]> dailyData = database_BankSystem.getDailyTransactions(username);

        LocalDate startDate = today.minusDays(6); // Start of the week (7 days total)

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE", new Locale("en"));

        Paint[] fillGradients = {
                new LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#ff7e5f")), new Stop(1, Color.web("#feb47b"))),
                new LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#6a11cb")), new Stop(1, Color.web("#2575fc"))),
                new LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#00c6ff")), new Stop(1, Color.web("#0072ff"))),
                new LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#43e97b")), new Stop(1, Color.web("#38f9d7"))),
                new LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#fa709a")), new Stop(1, Color.web("#fee140"))),
                new LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#f6d365")), new Stop(1, Color.web("#fda085"))),
                new LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#a1c4fd")), new Stop(1, Color.web("#c2e9fb")))
        };

        double chartHeight = 300;

        Pane axisPane = new Pane();
        axisPane.setPrefSize(800, chartHeight);

        for (int i = 0; i <= 5; i++) {
            double y = chartHeight - i * (chartHeight / 5) + 0;
            Line gridLine = new Line(50, y, 600, y);
            gridLine.setStroke(Color.DARKGRAY);
            gridLine.setStrokeWidth(0.5);

            Text label = new Text((i * 20) + "%");
            label.setFill(Color.WHITE);
            label.setFont(Font.font(10));
            label.setX(10);
            label.setY(y + 5);

            axisPane.getChildren().addAll(gridLine, label);
        }

        Line xAxisLine = new Line(50, chartHeight + 0, 600, chartHeight + 0);
        xAxisLine.setStroke(Color.GRAY);
        xAxisLine.setStrokeWidth(2);
        axisPane.getChildren().add(xAxisLine);

        HBox bars = new HBox(30);
        bars.setAlignment(Pos.BOTTOM_LEFT);
        bars.setPadding(new Insets(0, 0, 0, 70));

        Map<String, Double> dayTotals = new HashMap<>();
        for (Object[] data : dailyData) {
            String dateStr = (String) data[2];
            LocalDate date = LocalDate.parse(dateStr, formatter);
            String dayName = date.format(dayFormatter);
            double transactionValue = (Double) data[1];
            dayTotals.put(dayName, dayTotals.getOrDefault(dayName, 0.0) + transactionValue);
        }

        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            String dayName = currentDate.format(dayFormatter);
            double dailySpending = dayTotals.getOrDefault(dayName, 0.0);
            double percentage = (totalBalanceForChart > 0) ? Math.min(100.0, (dailySpending / totalBalanceForChart) * 100) : 0.0;
            System.out.println("Day: " + dayName + ", Daily Spending: " + dailySpending + ", Percentage: " + percentage + "%");
            double fillHeight = Math.min(chartHeight, (percentage / 100) * chartHeight);

            Rectangle base = new Rectangle(40, chartHeight);
            base.setArcWidth(10);
            base.setArcHeight(10);
            base.setFill(Color.LIGHTGRAY);

            Rectangle fill = new Rectangle(40, fillHeight);
            fill.setArcWidth(10);
            fill.setArcHeight(10);
            fill.setFill(fillGradients[i % fillGradients.length]);

            StackPane bar = new StackPane(base, fill);
            bar.setAlignment(Pos.BOTTOM_CENTER);

            Tooltip tooltip = new Tooltip(String.format("%.1f%%", percentage));
            bar.getProperties().put("storedTooltip", tooltip);
            Tooltip.install(bar, tooltip);

            Text dayLabel = new Text(dayName);
            dayLabel.setFill(Color.LIGHTGRAY);
            dayLabel.setFont(Font.font(13));

            VBox column = new VBox(5, bar, dayLabel);
            column.setAlignment(Pos.BOTTOM_CENTER);

            bars.getChildren().add(column);
        }

        chart.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        chart.setStyle("-fx-background-radius:15;");
        chart.getChildren().clear();
        chart.getChildren().addAll(axisPane, bars);

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

    private Paint getColorBasedOnValue(double percentage) {
        if (percentage < 20) {
            return new LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#ff0000")), new Stop(1, Color.web("#ff6666")));
        } else if (percentage < 50) {
            return new LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#ff7e5f")), new Stop(1, Color.web("#feb47b")));
        } else {
            return new LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#43e97b")), new Stop(1, Color.web("#38f9d7")));
        }
    }

    private void restartProgressBarAnimation(AnimatedCircularProgressBar progressBar) {
        progressBar.restartAnimation();
    }

    private String getColorForSegment(String segmentName) {
        switch (segmentName) {
            case "Investments":
                return "#FF5733";
            case "Bills":
                return "#FFC107";
            case "Mobile Top-Ups":
                return "#28A745";
            case "Credit Card":
                return "#17A2B8";
            case "Government":
                return "#6610F2";
            case "Donations":
                return "#FD7E14";
            case "Education":
                return "#20C997";
            case "Insurance":
                return "#E83E8C";
            case "Other":
                return "#6F42C1";
            case "No Payments":
                return "#6C757D";
            default:
                return "#000000";
        }
    }

    private void addTypingEffect(AnimatedCircularProgressBar progressBar, String message) {
        Label typingLabel = new Label();
        typingLabel.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-padding: 5;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(1);
        shadow.setOffsetY(1);
        shadow.setColor(Color.rgb(0, 0, 0, 0.6));
        typingLabel.setEffect(shadow);

        typingLabel.setVisible(false);

        ((Pane) progressBar.getParent()).getChildren().add(typingLabel);

        progressBar.setOnMouseClicked(e -> {
            typingLabel.setText("");
            typingLabel.setVisible(true);

            typingLabel.setLayoutX(progressBar.getLayoutX() + progressBar.getWidth() / 2 - 40);
            typingLabel.setLayoutY(progressBar.getLayoutY() - 25);

            Timeline timeline = new Timeline();
            for (int i = 0; i < message.length(); i++) {
                final int index = i;
                KeyFrame keyFrame = new KeyFrame(Duration.millis(60 * i), ev -> {
                    typingLabel.setText(typingLabel.getText() + message.charAt(index));
                });
                timeline.getKeyFrames().add(keyFrame);
            }

            timeline.setOnFinished(ev -> {
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(finish -> {
                    typingLabel.setVisible(false);
                });
                pause.play();
            });

            timeline.play();
        });
    }

    private void updateChart() {
        series1.getData().clear();
        series2.getData().clear();
        for (int i = 0; i < values1.size(); i++) {
            series1.getData().add(new XYChart.Data<>(String.valueOf(index + i), values1.get(i)));
            series2.getData().add(new XYChart.Data<>(String.valueOf(index + i), values2.get(i)));
        }
        index++;
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