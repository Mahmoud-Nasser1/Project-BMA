package com.banking;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.FileChooser;
import java.io.IOException;
import java.io.File;
import java.util.List;


public class TransferConfirm {
    public Label AmCn;
    public Label AmFeCn;
    public Button TrCnf;
    public Label Lb1;
    public Label Lb2;
    public Label lb3;
    @FXML
    Label TransToCn;
    @FXML
    private Button exportReportButton;
    String str1, str2, str3;

    public void LabelText(String s1, String s2, String s3, String s4) {
        TransToCn.setText(s1);
        AmCn.setText(s2);
        AmFeCn.setText(s3);
        if (s4.equals("Transfer")) {
            this.str1 = s1;
            this.str2 = s3;
            this.str3 = s4;
        } else if (s4.equals("Bills")) {
            Lb1.setText("Bill Type:");
            Lb2.setText("Customer ID:");
            lb3.setText("Bill Amount:");
            this.str1 = s1;
            this.str2 = s3;
            this.str3 = s4;
        } else if (s4.equals("Mobile Top-Up")) {
            Lb1.setText("Network:");
            Lb2.setText("Mobile Number:");
            lb3.setText("Top-Up Amount:");
            this.str1 = s1;
            this.str2 = s3;
            this.str3 = s4;
        } else if (s4.equals("Credit Card")) {
            Lb1.setText("Chosen Card:");
            Lb2.setText("Amount:");
            lb3.setText("Amount with Fees:");
            this.str1 = s1;
            this.str2 = s3;
            this.str3 = s4;
        } else if (s4.equals("Government Service")) {
            Lb1.setText("Chosen Service:");
            Lb2.setText("Amount:");
            lb3.setText("Amount with Fees:");
            this.str1 = s1;
            this.str2 = s3;
            this.str3 = s4;
        } else if (s4.equals("Donation")) {
            Lb1.setText("Chosen Charity:");
            Lb2.setText("Donated Amount:");
            lb3.setText("Amount with Fees:");
            this.str1 = s1;
            this.str2 = s3;
            this.str3 = s4;
        } else if (s4.equals("Education Payments")) {
            Lb1.setText("Chosen Facility:");
            Lb2.setText("Student ID:");
            lb3.setText("Amount with Fees:");
            this.str1 = s1;
            this.str2 = s3;
            this.str3 = s4;
        } else if (s4.equals("Insurance Payments")) {
            Lb1.setText("Insurance Provider:");
            Lb2.setText("Policy Number:");
            lb3.setText("Amount with Fees:");
            this.str1 = s1;
            this.str2 = s3;
            this.str3 = s4;
        } else if (s4.equals("Other Payments")) {
            Lb1.setText("Payment Category:");
            Lb2.setText("Payee Name:");
            lb3.setText("Amount with Fees:");
            this.str1 = s1;
            this.str2 = s3;
            this.str3 = s4;
        } else if (s4.equals("Deposit")) {
            Lb1.setText("Type:");
            Lb2.setText("Amount:");
            lb3.setText("Amount with Fees:");
            this.str1 = s1;
            this.str2 = s3;
            this.str3 = s4;
        } else if (s4.equals("Withdraw")) {
            Lb1.setText("Type:");
            Lb2.setText("Amount:");
            lb3.setText("Amount with Fees:");
            this.str1 = s1;
            this.str2 = s3;
            this.str3 = s4;
        }
    }
    public void Clicking3(ActionEvent actionEvent) {
        UserSession session = UserSession.getInstance();
        String username = session.getUsername();

        if (str1 == null || str1.isEmpty()) {
            System.out.println("❌ ID or data is missing!");
            return;
        }

        if (str2 == null || str2.isEmpty()) {
            System.out.println("❌ Amount is empty!");
            return;
        }

        String amountString;
        if (str2.contains("EGP")) {
            amountString = str2.split("EGP")[0].trim();
        } else {
            amountString = str2.trim();
        }

        double amount;
        try {
            amount = Double.parseDouble(amountString);
            if (amount <= 0) {
                System.out.println("❌ The amount must be greater than zero!");

                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid amount format: " + str2);
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/maged/ConfirmPassword.fxml"));
        Parent root = null;
        try {
            root = fxmlLoader.load();
            ConfirmPassword confirmPassword = fxmlLoader.getController();
            confirmPassword.setPaymentDetails(username, str1, amount, str3, TransToCn.getText());
            confirmPassword.getTxt(str1, str2, str3, username);
            System.out.println("✅ Password confirmation window loaded successfully.");
        } catch (IOException e) {
            System.out.println("❌ Error loading confirmation window: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        Stage stage = new Stage();
        stage.setTitle("Payment Confirmation");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        ((Stage)((Node)actionEvent.getSource()).getScene().getWindow()).close();
    }



    public static class Transaction {
        private int id;
        private String type;
        private double amount;
        private String date;
        private String recipient;

        public Transaction(int id, String type, double amount, String date, String recipient) {
            this.id = id;
            this.type = type;
            this.amount = amount;
            this.date = date;
            this.recipient = recipient;
        }

        public int getId() { return id; }
        public String getType() { return type; }
        public double getAmount() { return amount; }
        public String getDate() { return date; }
        public String getRecipient() { return recipient; }
    }
}