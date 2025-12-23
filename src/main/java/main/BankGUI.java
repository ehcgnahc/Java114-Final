package main;

import bank.core.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Optional;

public class BankGUI extends Application {
    private enum ATMState {
        IDLE,
        CHECK_PIN,
        SELECT_SERVICE,
        ENTER_DEPOSIT_AMT,
        ENTER_WITHDRAW_AMT,
        ENTER_TRANSFER_AMT
    }

    private ATMState currentState = ATMState.IDLE;
    private String tempTargetBank = null;
    private String tempTargetAcc = null;
    private BankSystem bankSystem;
    private Bank bankA, bankB;
    private ATM currentATM;
    private User currentUser;
    private ATM atmA, atmB;
    private User user1, user2;
    private TextArea screenArea;
    private TextArea logArea;
    private TextArea userStatusArea;
    private ComboBox<Card> cardSelector;
    private ComboBox<User> userSelector;
    private Button insertBtn;
    private StringBuilder inputBuffer = new StringBuilder();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        initBackend();
        setupConsoleRedirect();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f0f0f0;");

        root.setLeft(createControlPanel());
        root.setCenter(createATMPanel());
        root.setBottom(createLogPanel());

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("銀行模擬系統");
        primaryStage.setScene(scene);
        primaryStage.show();

        updateCardList();
        updateUserStatus();
        updateScreen("歡迎使用 ATM\n請選擇卡片並插入");
    }

    private void initBackend() {
        bankSystem = new BankSystem();
        bankA = new Bank("BankA");
        bankB = new Bank("BankB");

        bankSystem.addBank(bankA);
        bankSystem.addBank(bankB);

        bankSystem.setCrossBankFee("BankA", "BankB", 5.0);
        bankSystem.setCrossBankFee("BankB", "BankA", 15.0);

        atmA = new ATM(bankSystem, bankA);
        atmB = new ATM(bankSystem, bankB);
        currentATM = atmA;

        user1 = new User("User1(Alice)", 5000);
        user2 = new User("User2(Bob)", 10000);
        currentUser = user1;

        bankA.createAcc(user1, "1234");
        bankB.createAcc(user2, "5678");
    }

    private VBox createControlPanel() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        box.setPrefWidth(300);

        Label title = new Label("控制台");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        Label lblATM = new Label("選擇ATM機台:");
        ComboBox<String> atmBox = new ComboBox<>();
        atmBox.getItems().addAll("ATM A(BankA)", "ATM B(BankB)");
        atmBox.setValue("ATM A(BankA)");
        atmBox.setMaxWidth(Double.MAX_VALUE);
        atmBox.setOnAction(e -> {
            forceEject();
            currentATM = atmBox.getValue().contains("BankA") ? atmA : atmB;
            updateScreen("已切換至 " + atmBox.getValue() + "\n請插卡");
        });

        Label lblUser = new Label("目前使用者:");
        userSelector = new ComboBox<>();
        userSelector.getItems().addAll(user1, user2);
        userSelector.setValue(user1);
        userSelector.setMaxWidth(Double.MAX_VALUE);
        userSelector.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User u) {
                return u.getName();
            }
            @Override
            public User fromString(String string) {
                return null;
            }
        });
        userSelector.setOnAction(e -> {
            forceEject();
            currentUser = userSelector.getValue();
            updateCardList();
            updateUserStatus();
            updateScreen("使用者切換為: " + currentUser.getName());
        });

        Button openAccBtn = new Button("申請開戶(Create Account)");
        openAccBtn.setMaxWidth(Double.MAX_VALUE);
        openAccBtn.setStyle("-fx-base: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        openAccBtn.setOnAction(e -> handleOpenAccount());

        Label lblCard = new Label("選擇要插入的卡片:");
        cardSelector = new ComboBox<>();
        cardSelector.setMaxWidth(Double.MAX_VALUE);
        cardSelector.setConverter(new StringConverter<Card>() {
            @Override
            public String toString(Card c) {
                if (c == null) return "無卡片";
                return String.format("[%s] %s", c.getBankID(), c.getCardID());
            }
            @Override
            public Card fromString(String s) {
                return null;
            }
        });

        HBox btnBox = new HBox(10);
        insertBtn = new Button("插入選定卡片");
        insertBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        insertBtn.setOnAction(e -> handleInsertCard());

        Button ejectBtn = new Button("退卡");
        ejectBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        ejectBtn.setOnAction(e -> forceEject());

        btnBox.getChildren().addAll(insertBtn, ejectBtn);

        userStatusArea = new TextArea();
        userStatusArea.setEditable(false);
        userStatusArea.setWrapText(true);
        userStatusArea.setPrefHeight(150);
        userStatusArea.setStyle("-fx-control-inner-background: #eee; -fx-font-family: 'Consolas';");

        box.getChildren().addAll(
            title, new Separator(),
            lblATM, atmBox,
            lblUser, userSelector,
            openAccBtn,
            lblCard, cardSelector,
            btnBox, new Separator(),
            new Label("使用者狀態:"), userStatusArea
        );
        return box;
    }

    private VBox createATMPanel() {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);

        VBox machineFrame = new VBox(15);
        machineFrame.setMaxWidth(400);
        machineFrame.setPadding(new Insets(20));
        machineFrame.setStyle("-fx-background-color: #444; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 0);");

        Label brand = new Label("BANK ATM SYSTEM");
        brand.setTextFill(javafx.scene.paint.Color.WHITE);
        brand.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        brand.setAlignment(Pos.CENTER);

        screenArea = new TextArea();
        screenArea.setPrefRowCount(5);
        screenArea.setEditable(false);
        screenArea.setStyle("-fx-control-inner-background: #000; -fx-text-fill: #0f0; -fx-font-family: 'Monospaced'; -fx-font-size: 16px; -fx-highlight-fill: #00ff00;");
        screenArea.setText("系統啟動中...");

        GridPane keypad = new GridPane();
        keypad.setHgap(10);
        keypad.setVgap(10);
        keypad.setAlignment(Pos.CENTER);

        String[] keys = {
            "1", "2", "3", "存款",
            "4", "5", "6", "提款",
            "7", "8", "9", "餘額",
            "C", "0", "OK", "轉帳"
        };

        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            Button btn = new Button(key);
            btn.setPrefSize(70, 50);
            btn.setStyle("-fx-font-weight: bold; -fx-base: #ddd;");

            if (key.equals("C")) btn.setStyle("-fx-base: #ffcccb; -fx-font-weight: bold;");
            if (key.equals("OK")) btn.setStyle("-fx-base: #90ee90; -fx-font-weight: bold;");
            if (key.length() > 1 && !key.equals("OK")) btn.setStyle("-fx-base: #add8e6; -fx-font-weight: bold;");

            btn.setOnAction(e -> handleKeypadInput(key));
            keypad.add(btn, i % 4, i / 4);
        }

        machineFrame.getChildren().addAll(brand, screenArea, keypad);
        box.getChildren().add(machineFrame);
        return box;
    }

    private VBox createLogPanel() {
        VBox box = new VBox(5);
        Label lbl = new Label("System Log:");
        logArea = new TextArea();
        logArea.setPrefHeight(120);
        logArea.setEditable(false);
        logArea.setStyle("-fx-font-family: 'Consolas';");
        box.getChildren().addAll(lbl, logArea);
        return box;
    }

    private void updateCardList() {
        cardSelector.getItems().clear();
        if (currentUser != null && !currentUser.getCard().isEmpty()) {
            cardSelector.getItems().addAll(currentUser.getCard());
            cardSelector.getSelectionModel().selectFirst();
        }
    }

    private void updateUserStatus() {
        if (currentUser == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("姓名: ").append(currentUser.getName()).append("\n");
        sb.append("現金: ").append(String.format("%.0f", currentUser.getCash())).append("\n");
        sb.append("持有帳戶:\n");
        for (PassBook passBook : currentUser.getPassBook()) {
            sb.append(" - ").append(passBook.getBankID())
              .append("(").append(passBook.getAccID()).append(")\n");
        }
        userStatusArea.setText(sb.toString());
    }

    private void handleOpenAccount() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("BankA", "BankA", "BankB");
        dialog.setTitle("申請開戶");
        dialog.setHeaderText("請選擇開戶銀行");
        dialog.setContentText("銀行:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(bankName -> {
            TextInputDialog pwdDialog = new TextInputDialog();
            pwdDialog.setTitle("設定密碼");
            pwdDialog.setHeaderText("正在 " + bankName + " 開戶");
            pwdDialog.setContentText("請設定您的密碼:");

            pwdDialog.showAndWait().ifPresent(password -> {
                if (!password.matches("\\d+")) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("格式錯誤");
                    alert.setHeaderText("密碼設定失敗");
                    alert.setContentText("密碼只能包含數字 (0-9)，請重新操作");
                    alert.showAndWait();
                    return;
                }

                Bank targetBank = bankName.equals("BankA") ? bankA : bankB;
                targetBank.createAcc(currentUser, password);
                updateCardList();
                updateUserStatus();
                cardSelector.getSelectionModel().selectLast();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("開戶成功");
                alert.show();
            });
        });
    }

    private void handleInsertCard() {
        Card selected = cardSelector.getValue();
        if (selected != null) {
            currentATM.insertCard(selected);
            currentState = ATMState.CHECK_PIN;
            inputBuffer.setLength(0);
            updateScreen("卡片已插入\n[" + selected.getBankID() + "]\n請輸入密碼後按 OK");
            insertBtn.setDisable(true);
        } else {
            updateScreen("錯誤: 請先選擇一張卡片");
        }
    }

    private void forceEject() {
        if (currentATM != null) currentATM.ejectCard();
        currentState = ATMState.IDLE;
        inputBuffer.setLength(0);
        insertBtn.setDisable(false);
        tempTargetBank = null;
        tempTargetAcc = null;
        updateScreen("卡片已退出\n歡迎下次光臨");
    }

    private void handleKeypadInput(String key) {
        if (key.equals("C")) {
            if (inputBuffer.length() > 0) {
                inputBuffer.setLength(0);
                updateScreen("輸入已清除\n請重新輸入");
            } else {
                if (currentState == ATMState.ENTER_DEPOSIT_AMT ||
                    currentState == ATMState.ENTER_WITHDRAW_AMT ||
                    currentState == ATMState.ENTER_TRANSFER_AMT) {

                    currentState = ATMState.SELECT_SERVICE;
                    updateScreen("操作已取消\n請選擇服務");
                }
            }
            return;
        }

        if (key.equals("存款") || key.equals("提款") || key.equals("轉帳") || key.equals("餘額")) {
            if (currentState != ATMState.SELECT_SERVICE) {
                return;
            }

            inputBuffer.setLength(0);

            switch (key) {
                case "餘額":
                    String balanceMsg = currentATM.checkBalance();
                    updateScreen(balanceMsg + "\n\n按其他鍵選擇服務\n或按退卡離開");
                    break;
                case "存款":
                    currentState = ATMState.ENTER_DEPOSIT_AMT;
                    updateScreen("【存款服務】\n請用鍵盤輸入金額\n完成後按 OK");
                    break;
                case "提款":
                    currentState = ATMState.ENTER_WITHDRAW_AMT;
                    updateScreen("【提款服務】\n請用鍵盤輸入金額\n完成後按 OK");
                    break;
                case "轉帳":
                    prepareTransfer();
                    break;
            }
            return;
        }

        if (key.equals("OK")) {
            handleOKButton();
            return;
        }

        if (currentState == ATMState.CHECK_PIN ||
            currentState == ATMState.ENTER_DEPOSIT_AMT ||
            currentState == ATMState.ENTER_WITHDRAW_AMT ||
            currentState == ATMState.ENTER_TRANSFER_AMT) {

            inputBuffer.append(key);

            if (currentState == ATMState.CHECK_PIN) {
                updateScreen("輸入密碼: " + "*".repeat(inputBuffer.length()));
            } else {
                updateScreen("輸入金額: " + inputBuffer.toString());
            }
        }
    }

    private void handleOKButton() {
        String inputStr = inputBuffer.toString();
        inputBuffer.setLength(0);

        switch (currentState) {
            case CHECK_PIN:
                boolean success = currentATM.login(inputStr);
                if (success) {
                    currentState = ATMState.SELECT_SERVICE;
                    updateScreen("登入成功\n請選擇服務(存款/提款/轉帳/餘額)");
                } else {
                    updateScreen("密碼錯誤\n請重新輸入");
                }
                break;

            case ENTER_DEPOSIT_AMT:
                if (inputStr.isEmpty()) return;
                try {
                    double amt = Double.parseDouble(inputStr);
                    if (currentUser.giveCash(amt)) {
                        String msg = currentATM.deposit(amt);

                        if (msg.contains("成功")) {
                            updateScreen(msg + "\n\n請選擇其他服務");
                            updateUserStatus();
                            currentState = ATMState.SELECT_SERVICE;
                        } else {
                            updateScreen(msg + "\n請重新輸入金額");
                        }
                    } else {
                        updateScreen("交易失敗: 口袋現金不足!\n請重新輸入金額");
                    }
                } catch (NumberFormatException e) {
                    updateScreen("金額格式錯誤\n請重新輸入");
                }
                break;

            case ENTER_WITHDRAW_AMT:
                if (inputStr.isEmpty()) return;
                try {
                    double amt = Double.parseDouble(inputStr);
                    String msg = currentATM.withdraw(amt);

                    if (msg.contains("成功")) {
                        currentUser.takeCash(amt);
                        updateUserStatus();
                        updateScreen(msg + "\n\n請選擇其他服務");
                        currentState = ATMState.SELECT_SERVICE;
                    } else {
                        updateScreen(msg + "\n請重新輸入金額");
                    }
                } catch (NumberFormatException e) {
                    updateScreen("金額格式錯誤\n請重新輸入");
                }
                break;

            case ENTER_TRANSFER_AMT:
                if (inputStr.isEmpty()) return;
                try {
                    double amt = Double.parseDouble(inputStr);
                    updateScreen("轉帳處理中...");
                    String msg = currentATM.transfer(tempTargetBank, tempTargetAcc, amt);

                    if (msg.contains("成功")) {
                        updateScreen(msg + "\n\n請選擇其他服務");
                        updateUserStatus();
                        tempTargetBank = null;
                        tempTargetAcc = null;
                        currentState = ATMState.SELECT_SERVICE;
                    } else {
                        updateScreen(msg + "\n請重新輸入金額");
                    }
                } catch (NumberFormatException e) {
                    updateScreen("金額格式錯誤\n請重新輸入");
                }
                break;

            default:
                break;
        }
    }

    private void prepareTransfer() {
        TextInputDialog bankDialog = new TextInputDialog("BankB");
        bankDialog.setTitle("轉帳步驟 1/2");
        bankDialog.setHeaderText("請輸入對方銀行代碼");
        bankDialog.setContentText("銀行(BankA / BankB):");

        Optional<String> bankRes = bankDialog.showAndWait();
        if (bankRes.isEmpty()) {
            updateScreen("轉帳已取消");
            return;
        }

        TextInputDialog accDialog = new TextInputDialog();
        accDialog.setTitle("轉帳步驟 2/2");
        accDialog.setHeaderText("請輸入對方帳號");
        accDialog.setContentText("帳號:");

        Optional<String> accRes = accDialog.showAndWait();
        if (accRes.isEmpty()) {
            updateScreen("轉帳已取消");
            return;
        }

        this.tempTargetBank = bankRes.get().trim();
        this.tempTargetAcc = accRes.get().trim();

        currentState = ATMState.ENTER_TRANSFER_AMT;
        updateScreen("【轉帳服務】\n轉入: " + tempTargetBank + " - " + tempTargetAcc + "\n\n請用鍵盤輸入轉帳金額\n完成後按 OK");
    }

    private void updateScreen(String msg) {
        screenArea.setText(msg);
    }

    private void setupConsoleRedirect() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                Platform.runLater(() -> logArea.appendText(String.valueOf((char) b)));
            }
            @Override
            public void write(byte[] b, int off, int len) {
                String s = new String(b, off, len);
                Platform.runLater(() -> logArea.appendText(s));
            }
        };
        System.setOut(new PrintStream(out, true));
    }
}