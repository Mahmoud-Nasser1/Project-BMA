package com.banking;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class ConfirmPassword {
    public PasswordField p1, p2, p3, p4, p5, p6;
    public ImageView im1, im2, im3, im4, im5, im6;
    public Button ShwPs;
    public Label lp1, lp2, lp3, lp4, lp5, lp6;
    public Label WrPs;
    public Button CnPsBn;
    private int[] arr = new int[6];
    private String sr1, sr2, sr3;
    private String username;
    private String paymentRecipient;
    private double paymentAmount;
    private String paymentType;
    private String serviceDetail;
    private int cnt = 0;

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    public void initialize() {
        im1.setVisible(false);
        im2.setVisible(false);
        im3.setVisible(false);
        im4.setVisible(false);
        im5.setVisible(false);
        im6.setVisible(false);
        lp1.setVisible(false);
        lp2.setVisible(false);
        lp3.setVisible(false);
        lp4.setVisible(false);
        lp5.setVisible(false);
        lp6.setVisible(false);
        WrPs.setVisible(false);
        setupField(p1, p2);
        setupField(p2, p3);
        setupField(p3, p4);
        setupField(p4, p5);
        setupField(p5, p6);
        setupField(p6, null);
        p1.textProperty().addListener((observable, oldValue, newValue1) -> {
            im1.setVisible(!newValue1.isEmpty());
            arr[0] = newValue1.isEmpty() ? 0 : Integer.parseInt(newValue1);
        });
        p2.textProperty().addListener((observable, oldValue, newValue2) -> {
            im2.setVisible(!newValue2.isEmpty());
            arr[1] = newValue2.isEmpty() ? 0 : Integer.parseInt(newValue2);
        });
        p3.textProperty().addListener((observable, oldValue, newValue3) -> {
            im3.setVisible(!newValue3.isEmpty());
            arr[2] = newValue3.isEmpty() ? 0 : Integer.parseInt(newValue3);
        });
        p4.textProperty().addListener((observable, oldValue, newValue4) -> {
            im4.setVisible(!newValue4.isEmpty());
            arr[3] = newValue4.isEmpty() ? 0 : Integer.parseInt(newValue4);
        });
        p5.textProperty().addListener((observable, oldValue, newValue5) -> {
            im5.setVisible(!newValue5.isEmpty());
            arr[4] = newValue5.isEmpty() ? 0 : Integer.parseInt(newValue5);
        });
        p6.textProperty().addListener((observable, oldValue, newValue6) -> {
            im6.setVisible(!newValue6.isEmpty());
            arr[5] = newValue6.isEmpty() ? 0 : Integer.parseInt(newValue6);
        });
    }

    private void setupField(PasswordField current, PasswordField next) {
        current.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() == 1 && next != null) {
                next.requestFocus();
            }
            if (newVal.length() > 1) {
                current.setText(newVal.substring(0, 1));
            }
        });
    }

    public void getTxt(String s1, String s2, String s3, String username) {
        this.sr1 = s1;
        this.sr2 = s2;
        this.sr3 = s3;
        this.username = username;
    }

    public void setPaymentDetails(String username, String recipient, double amount, String type, String serviceDetail) {
        this.username = username;
        this.paymentRecipient = recipient;
        this.paymentAmount = amount;
        this.paymentType = type;
        this.serviceDetail = serviceDetail; // تخزين تفاصيل الخدمة (مثل billType أو network)
    }

    public void Clicking4(ActionEvent actionEvent) {
        if (cnt % 2 == 0) {
            im1.setVisible(true);
            im2.setVisible(true);
            im3.setVisible(true);
            im4.setVisible(true);
            im5.setVisible(true);
            im6.setVisible(true);
            lp1.setVisible(false);
            lp2.setVisible(false);
            lp3.setVisible(false);
            lp4.setVisible(false);
            lp5.setVisible(false);
            lp6.setVisible(false);
            p1.setVisible(true);
            p2.setVisible(true);
            p3.setVisible(true);
            p4.setVisible(true);
            p5.setVisible(true);
            p6.setVisible(true);
        } else {
            im1.setVisible(false);
            im2.setVisible(false);
            im3.setVisible(false);
            im4.setVisible(false);
            im5.setVisible(false);
            im6.setVisible(false);
            lp1.setVisible(true);
            lp2.setVisible(true);
            lp3.setVisible(true);
            lp4.setVisible(true);
            lp5.setVisible(true);
            lp6.setVisible(true);
            lp1.setText(String.valueOf(arr[0]));
            lp2.setText(String.valueOf(arr[1]));
            lp3.setText(String.valueOf(arr[2]));
            lp4.setText(String.valueOf(arr[3]));
            lp5.setText(String.valueOf(arr[4]));
            lp6.setText(String.valueOf(arr[5]));
            p1.setVisible(false);
            p2.setVisible(false);
            p3.setVisible(false);
            p4.setVisible(false);
            p5.setVisible(false);
            p6.setVisible(false);
        }
        cnt++;
    }

    private String hashPassword(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("❌ Error hashing password: " + e.getMessage(), e);
        }
    }

    public void Clicking5(ActionEvent actionEvent) {
        if (username == null || username.trim().isEmpty()) {
            WrPs.setText("Username not provided!");
            WrPs.setVisible(true);
            return;
        }

        StringBuilder inputPassword = new StringBuilder();
        for (int digit : arr) {
            inputPassword.append(digit);
        }

        String[] storedData = database_BankSystem.getUserPasswordAndSalt(username);
        if (storedData == null) {
            WrPs.setText("User not found or database error!");
            WrPs.setVisible(true);
            p1.clear();
            p2.clear();
            p3.clear();
            p4.clear();
            p5.clear();
            p6.clear();
            p1.requestFocus();
            return;
        }

        String storedHash = storedData[0];
        String saltBase64 = storedData[1];
        byte[] salt = Base64.getDecoder().decode(saltBase64);

        String hashedInputPassword = hashPassword(inputPassword.toString(), salt);

        if (hashedInputPassword.equals(storedHash)) {
            WrPs.setVisible(false);
            boolean operationSuccess = false;
            switch (paymentType) {
                case "Deposit":
                    operationSuccess = database_BankSystem.deposit(username, paymentAmount);
                    if (operationSuccess) {
                        System.out.println("✅ Deposit completed successfully! Amount: " + paymentAmount + " | User: " + username);
                    }
                    break;

                case "Withdraw":
                    operationSuccess = database_BankSystem.withdraw(username, paymentAmount);
                    if (operationSuccess) {
                        System.out.println("✅ Withdrawal completed successfully! Amount: " + paymentAmount + " | User: " + username);
                    }
                    break;

                case "Transfer":
                    String recipientUsername = database_BankSystem.getUsernameById(paymentRecipient);
                    if (recipientUsername == null) {
                        WrPs.setText("❌ No user found with ID: " + paymentRecipient);
                        WrPs.setVisible(true);
                        return;
                    }
                    operationSuccess = database_BankSystem.transfer(username, recipientUsername, paymentAmount);
                    if (operationSuccess) {
                        System.out.println("✅ Transfer completed successfully! Amount: " + paymentAmount + " | Recipient: " + recipientUsername);
                    }
                    break;

                case "Bills":
                    operationSuccess = database_BankSystem.transferBill(username, serviceDetail, paymentRecipient, paymentAmount);
                    if (operationSuccess) {
                        System.out.println("✅ Bill payment completed successfully! Type: " + serviceDetail + " | Customer: " + paymentRecipient);
                    }
                    break;

                case "Mobile Top-Up":
                    operationSuccess = database_BankSystem.transferMobileTopUp(username, serviceDetail, paymentRecipient, paymentAmount);
                    if (operationSuccess) {
                        System.out.println("✅ Mobile top-up completed successfully! Network: " + serviceDetail + " | Number: " + paymentRecipient);
                    }
                    break;

                case "Credit Card":
                    operationSuccess = database_BankSystem.transferCreditCard(username, serviceDetail, paymentAmount);
                    if (operationSuccess) {
                        System.out.println("✅ Credit card payment completed successfully! Type: " + serviceDetail);
                    }
                    break;

                case "Government Service":
                    operationSuccess = database_BankSystem.transferGovernmentService(username, serviceDetail, paymentRecipient, paymentAmount);
                    if (operationSuccess) {
                        System.out.println("✅ Government service payment completed successfully! Type: " + serviceDetail);
                    }
                    break;

                case "Donation":
                    operationSuccess = database_BankSystem.transferDonation(username, serviceDetail, paymentAmount);
                    if (operationSuccess) {
                        System.out.println("✅ Donation completed successfully! Organization: " + serviceDetail);
                    }
                    break;

                case "Education Payments":
                    operationSuccess = database_BankSystem.transferEducationPayment(username, serviceDetail, paymentRecipient, paymentAmount);
                    if (operationSuccess) {
                        System.out.println("✅ Education payment completed successfully! Institution: " + serviceDetail);
                    }
                    break;

                case "Insurance Payments":
                    operationSuccess = database_BankSystem.transferInsurancePayment(username, serviceDetail, paymentRecipient, paymentAmount);
                    if (operationSuccess) {
                        System.out.println("✅ Insurance payment completed successfully! Company: " + serviceDetail);
                    }
                    break;

                case "Other Payments":
                    operationSuccess = database_BankSystem.transferOtherPayment(username, serviceDetail, paymentRecipient, paymentAmount);
                    if (operationSuccess) {
                        System.out.println("✅ Payment completed successfully! Category: " + serviceDetail);
                    }
                    break;

                default:
                    WrPs.setText("❌ Unknown transaction type: " + paymentType);
                    WrPs.setVisible(true);
                    return;
            }


            if (operationSuccess) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/maged/OperationSucceeded.fxml"));
                Parent root;
                try {
                    root = fxmlLoader.load();
                    OperationSucceeded operationSucceeded = fxmlLoader.getController();
                    operationSucceeded.getT(sr1, sr2, sr3);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Stage stage = new Stage();
                stage.setTitle("Success!");
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
                ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
            } else {
                WrPs.setText("❌ Failed to process operation!");
                WrPs.setVisible(true);
            }
        } else {
            WrPs.setText("Wrong Password!");
            WrPs.setVisible(true);
            p1.setVisible(true);
            p2.setVisible(true);
            p3.setVisible(true);
            p4.setVisible(true);
            p5.setVisible(true);
            p6.setVisible(true);
            p1.clear();
            p2.clear();
            p3.clear();
            p4.clear();
            p5.clear();
            p6.clear();
            p1.requestFocus();
            im1.setVisible(false);
            im2.setVisible(false);
            im3.setVisible(false);
            im4.setVisible(false);
            im5.setVisible(false);
            im6.setVisible(false);
            lp1.setVisible(false);
            lp2.setVisible(false);
            lp3.setVisible(false);
            lp4.setVisible(false);
            lp5.setVisible(false);
            lp6.setVisible(false);
        }
    }
}