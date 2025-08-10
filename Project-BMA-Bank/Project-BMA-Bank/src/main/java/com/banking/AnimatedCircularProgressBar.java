package com.banking;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class AnimatedCircularProgressBar extends Pane {

    private final DoubleProperty targetProperty = new SimpleDoubleProperty(0);
    private final DoubleProperty progressProperty = new SimpleDoubleProperty(0);
    private final Arc backgroundArc;
    private final Arc progressArc;
    private final Text percentText;
    private Timeline timeline;

    public AnimatedCircularProgressBar() {
        backgroundArc = new Arc();
        backgroundArc.setRadiusX(90);
        backgroundArc.setRadiusY(90);
        backgroundArc.setStartAngle(0);
        backgroundArc.setLength(360);
        backgroundArc.setType(ArcType.OPEN);
        backgroundArc.setStroke(Color.LIGHTGRAY);
        backgroundArc.setStrokeWidth(15);
        backgroundArc.setStrokeLineCap(StrokeLineCap.ROUND);
        backgroundArc.setFill(null);

        progressArc = new Arc();
        progressArc.setRadiusX(90);
        progressArc.setRadiusY(90);
        progressArc.setStartAngle(90);
        progressArc.setLength(0);
        progressArc.setType(ArcType.OPEN);
        progressArc.setStrokeWidth(15);
        progressArc.setStrokeLineCap(StrokeLineCap.ROUND);
        progressArc.setFill(null);
        progressArc.setStroke(Color.DODGERBLUE);

        percentText = new Text("0%");
        percentText.setFont(new Font("Arial", 24));
        percentText.setFill(Color.DARKGRAY);
        percentText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        getChildren().addAll(backgroundArc, progressArc, percentText);

        getStyleClass().add("progress-bar");

        targetProperty.addListener((obs, oldVal, newVal) -> setupAnimation());
        progressProperty.addListener((obs, oldVal, newVal) -> updateProgress());

        setupListeners();
        setupAnimation();
    }

    public Arc getProgressArc() {
        return progressArc;
    }

    private void setupListeners() {
        widthProperty().addListener((obs, oldVal, newVal) -> updateLayout());
        heightProperty().addListener((obs, oldVal, newVal) -> updateLayout());
    }

    private void updateLayout() {
        double size = Math.min(getWidth(), getHeight()) - 20;
        double centerX = getWidth() / 2;
        double centerY = getHeight() / 2;

        backgroundArc.setCenterX(centerX);
        backgroundArc.setCenterY(centerY);
        backgroundArc.setRadiusX(size / 2);
        backgroundArc.setRadiusY(size / 2);

        progressArc.setCenterX(centerX);
        progressArc.setCenterY(centerY);
        progressArc.setRadiusX(size / 2);
        progressArc.setRadiusY(size / 2);

        percentText.setFont(new Font("Arial", size / 6));
        percentText.setFill(Color.WHITE);
        Platform.runLater(() -> {
            double textWidth = percentText.getBoundsInLocal().getWidth();
            double textHeight = percentText.getBoundsInLocal().getHeight();

            percentText.setX(centerX - textWidth / 2);
            percentText.setY(centerY + textHeight / 4);
        });
    }

    private void setupAnimation() {
        if (timeline != null) {
            timeline.stop();
        }

        double start = progressProperty.get();
        double end = targetProperty.get();

        timeline = new Timeline();
        KeyValue kv = new KeyValue(progressProperty, end, javafx.animation.Interpolator.EASE_BOTH);
        KeyFrame kf = new KeyFrame(Duration.seconds(1), kv); // مدة الأنيميشن 1 ثانية

        timeline.getKeyFrames().add(kf);
        timeline.play();
    }


    private void updateProgress() {
        double currentProgress = progressProperty.get();
        progressArc.setLength(-(360 * currentProgress / 100.0));
        percentText.setText(String.format("%.1f%%", currentProgress));


    }

    public DoubleProperty targetProperty() {
        return targetProperty;
    }

    public double getTarget() {
        return targetProperty.get();
    }

    public void setTarget(double target) {
        targetProperty.set(Math.max(0, Math.min(100, target)));
    }

    public DoubleProperty progressProperty() {
        return progressProperty;
    }

    public double getProgress() {
        return progressProperty.get();
    }

    public void setProgress(double progress) {
        progressProperty.set(Math.max(0, Math.min(100, progress)));
    }

    public void restartAnimation() {
        if (timeline != null) {
            timeline.stop();
        }
        progressProperty.set(0);
        setupAnimation();
    }

    @Override
    protected double computePrefWidth(double height) {
        return 200;
    }

    @Override
    protected double computePrefHeight(double width) {
        return 200;
    }

    public void draw() {
        updateProgress();
    }
}