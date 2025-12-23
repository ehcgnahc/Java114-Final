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

    private enum ATMState{
        IDLE, CHECK_PIN, SELECT_SERVICE, ENTER_DEPOSIT_AMT, ENTER_WITHDRAW_AMT, ENTER_TRANSFER_AMT
    }

    private ATMState currentState = ATMState.IDLE;
    private String tempTargetBank = null;
    private String tempTargetAcc = null;
    private BankSystem bankSystem;
    private Bank bankA, bankB;
    private ATM atmA, atmB;
    private ATM currentATM;
    private User user1, user2;
    private User currentUser;
    private TextArea screenArea;
    private TextArea logArea;
    private TextArea userStatusArea;
    private ComboBox<Card> cardSelector;
    private ComboBox<User> userSelector;
    private ComboBox<String> atmBox;
    private Button insertBtn;
    private StringBuilder inputBuffer = new StringBuilder();

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage){
        initBackend();
        setupConsoleRedirect();
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f4f4f4;");
        root.setLeft(createControlPanel());
        root.setCenter(createATMPanel());
        root.setBottom(createLogPanel());
        Scene scene = new Scene(root, 1100, 750);
        primaryStage.setTitle("Java 銀行模擬系統(Bank Simulation)");
        primaryStage.setScene(scene);
        primaryStage.show();
        updateCardList();
        updateUserStatus();
        updateScreen("歡迎使用 ATM\n請選擇卡片並插入");
    }

    private void initBackend(){
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
        bankB.createAcc(user2, "1234");
    }

    private VBox createControlPanel(){
        VBox box = new VBox(12);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 1;");
        box.setPrefWidth(320);
        Label title = new Label("控制面板 & 銀行櫃檯");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        TitledPane envPane = new TitledPane();
        envPane.setText("環境設定(Environment)");
        envPane.setCollapsible(false);
        VBox envBox = new VBox(10);
        Label lblUser = new Label("切換使用者(Current User):");
        userSelector = new ComboBox<>();
        userSelector.getItems().addAll(user1, user2);
        userSelector.setValue(user1);
        userSelector.setMaxWidth(Double.MAX_VALUE);
        userSelector.setConverter(new StringConverter<User>(){
            @Override
            public String toString(User u){
                return u.getName();
            }
            @Override
            public User fromString(String string){
                return null;
            }
        });
        userSelector.setOnAction(e -> handleUserChange());
        Label lblATM = new Label("前往 ATM(Current ATM):");
        atmBox = new ComboBox<>();
        atmBox.getItems().addAll("ATM A(BankA)", "ATM B(BankB)");
        atmBox.setValue("ATM A(BankA)");
        atmBox.setMaxWidth(Double.MAX_VALUE);
        atmBox.setOnAction(e -> handleATMChange());
        envBox.getChildren().addAll(lblUser, userSelector, lblATM, atmBox);
        envPane.setContent(envBox);
        TitledPane tellerPane = new TitledPane();
        tellerPane.setText("銀行櫃檯業務(Teller Services)");
        tellerPane.setCollapsible(false);
        VBox tellerBox = new VBox(10);
        Button openAccBtn = new Button("申請開戶(Open Account)");
        openAccBtn.setMaxWidth(Double.MAX_VALUE);
        openAccBtn.setStyle("-fx-base: #2196F3; -fx-text-fill: white;");
        openAccBtn.setOnAction(e -> handleOpenAccount());
        Button updatePassBookBtn = new Button("補登存摺(Update Passbook)");
        updatePassBookBtn.setMaxWidth(Double.MAX_VALUE);
        updatePassBookBtn.setOnAction(e -> handleUpdatePassbook());
        Button unlockBtn = new Button("臨櫃解鎖(Unlock Account)");
        unlockBtn.setMaxWidth(Double.MAX_VALUE);
        unlockBtn.setStyle("-fx-base: #FF9800; -fx-text-fill: white;");
        unlockBtn.setOnAction(e -> handleUnlockAccount());
        tellerBox.getChildren().addAll(openAccBtn, updatePassBookBtn, unlockBtn);
        tellerPane.setContent(tellerBox);
        TitledPane itemPane = new TitledPane();
        itemPane.setText("使用者錢包(Wallet)");
        itemPane.setCollapsible(false);
        VBox itemBox = new VBox(10);
        Label lblCard = new Label("選擇卡片:");
        cardSelector = new ComboBox<>();
        cardSelector.setMaxWidth(Double.MAX_VALUE);
        cardSelector.setConverter(new StringConverter<Card>(){
            @Override
            public String toString(Card c){
                if(c == null) return "無卡片";
                return String.format("[%s] %s", c.getBankID(), c.getCardID());
            }
            @Override
            public Card fromString(String s){
                return null;
            }
        });
        HBox btnBox = new HBox(10);
        insertBtn = new Button("插入卡片");
        insertBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        insertBtn.setOnAction(e -> handleInsertCard());
        Button ejectBtn = new Button("退卡");
        ejectBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        ejectBtn.setOnAction(e -> forceEject());
        btnBox.getChildren().addAll(insertBtn, ejectBtn);
        itemBox.getChildren().addAll(lblCard, cardSelector, btnBox);
        itemPane.setContent(itemBox);
        userStatusArea = new TextArea();
        userStatusArea.setEditable(false);
        userStatusArea.setWrapText(true);
        userStatusArea.setPrefHeight(200);
        userStatusArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        box.getChildren().addAll(envPane, tellerPane, itemPane, new Label("狀態概覽:"), userStatusArea);
        return box;
    }

    private VBox createATMPanel(){
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        VBox machineFrame = new VBox(15);
        machineFrame.setMaxWidth(450);
        machineFrame.setPadding(new Insets(25));
        machineFrame.setStyle("-fx-background-color: #333; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 0);");
        Label brand = new Label("ATM SYSTEM");
        brand.setTextFill(javafx.scene.paint.Color.WHITE);
        brand.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        brand.setAlignment(Pos.CENTER);
        screenArea = new TextArea();
        screenArea.setPrefRowCount(6);
        screenArea.setEditable(false);
        screenArea.setStyle("-fx-control-inner-background: #000; -fx-text-fill: #0f0; -fx-font-family: 'Monospaced'; -fx-font-size: 16px;");
        screenArea.setText("系統啟動中...");
        GridPane keypad = new GridPane();
        keypad.setHgap(15);
        keypad.setVgap(15);
        keypad.setAlignment(Pos.CENTER);
        String[] keys ={
            "1", "2", "3", "存款",
            "4", "5", "6", "提款",
            "7", "8", "9", "餘額",
            "C", "0", "OK", "轉帳"
        };
        for(int i = 0; i < keys.length; i++){
            String key = keys[i];
            Button btn = new Button(key);
            btn.setPrefSize(80, 60);
            btn.setFont(Font.font("System", FontWeight.BOLD, 14));
            if(key.equals("C")) btn.setStyle("-fx-base: #ffcccb;");
            else if(key.equals("OK")) btn.setStyle("-fx-base: #90ee90;");
            else if(key.matches("\\d")) btn.setStyle("-fx-base: #e0e0e0;");
            else btn.setStyle("-fx-base: #b3e5fc;");
            btn.setOnAction(e -> handleKeypadInput(key));
            keypad.add(btn, i % 4, i / 4);
        }
        machineFrame.getChildren().addAll(brand, screenArea, keypad);
        box.getChildren().add(machineFrame);
        return box;
    }

    private VBox createLogPanel(){
        VBox box = new VBox(5);
        Label lbl = new Label("系統日誌(System Log):");
        logArea = new TextArea();
        logArea.setPrefHeight(120);
        logArea.setEditable(false);
        logArea.setStyle("-fx-font-family: 'Consolas'; -fx-text-fill: #555;");
        box.getChildren().addAll(lbl, logArea);
        return box;
    }

    private void handleUserChange(){
        forceEject();
        currentUser = userSelector.getValue();
        updateCardList();
        updateUserStatus();
        updateScreen("使用者切換為: " + currentUser.getName());
    }

    private void handleATMChange(){
        forceEject();
        currentATM = atmBox.getValue().contains("BankA") ? atmA : atmB;
        updateScreen("已前往 " + atmBox.getValue() + "\n請插卡");
    }

    private void handleOpenAccount(){
        ChoiceDialog<String> dialog = new ChoiceDialog<>("BankA", "BankA", "BankB");
        dialog.setTitle("銀行櫃檯 - 申請開戶");
        dialog.setHeaderText("請選擇開戶銀行");
        dialog.setContentText("銀行:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(bankName ->{
            TextInputDialog pwdDialog = new TextInputDialog();
            pwdDialog.setTitle("設定密碼");
            pwdDialog.setHeaderText("正在 " + bankName + " 開戶");
            pwdDialog.setContentText("請設定您的 6 位數晶片卡密碼:");
            pwdDialog.showAndWait().ifPresent(password ->{
                if(!password.matches("\\d+")){
                    showAlert(Alert.AlertType.ERROR, "格式錯誤", "密碼只能包含數字");
                    return;
                }
                Bank targetBank = bankName.equals("BankA") ? bankA : bankB;
                targetBank.createAcc(currentUser, password);
                updateCardList();
                updateUserStatus();
                cardSelector.getSelectionModel().selectLast();
                showAlert(Alert.AlertType.INFORMATION, "開戶成功", "您已獲得新存摺與金融卡！");
            });
        });
    }

    private void handleUpdatePassbook(){
        if(currentUser == null) return;
        int count = 0;
        for(PassBook pb : currentUser.getPassBook()){
            Bank bank = pb.getBankID().equals("BankA") ? bankA : bankB;
            bank.updatePassBook(pb);
            count++;
        }
        updateUserStatus();
        System.out.println(">>> 櫃檯: 已為 " + currentUser.getName() + " 補登 " + count + " 本存摺。");
        showAlert(Alert.AlertType.INFORMATION, "補登完成", "您的存摺交易明細已更新至最新狀態。");
    }

    private void handleUnlockAccount(){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("銀行櫃檯 - 解鎖帳戶");
        dialog.setHeaderText("請出示您的身分證件(模擬)");
        dialog.setContentText("請輸入要解鎖的帳號(Account ID):");
        dialog.showAndWait().ifPresent(accID ->{
            boolean unlockedA = bankA.unlockAccount(accID);
            boolean unlockedB = bankB.unlockAccount(accID);
            if(unlockedA || unlockedB){
                showAlert(Alert.AlertType.INFORMATION, "解鎖成功", "帳號 " + accID + " 已解除鎖定，請重試密碼。");
                System.out.println(">>> 櫃檯: 帳號 " + accID + " 解鎖成功。");
            }else{
                showAlert(Alert.AlertType.ERROR, "解鎖失敗", "查無此帳號，請確認輸入是否正確。");
            }
        });
    }

    private void handleInsertCard(){
        Card selected = cardSelector.getValue();
        if(selected != null){
            currentATM.insertCard(selected);
            currentState = ATMState.CHECK_PIN;
            inputBuffer.setLength(0);
            updateScreen("卡片已插入\n[" + selected.getBankID() + "] " + selected.getCardID() + "\n請輸入密碼後按 OK");
            insertBtn.setDisable(true);
            userSelector.setDisable(true);
            atmBox.setDisable(true);
        }else{
            updateScreen("錯誤: 請先在錢包中選擇一張卡片");
        }
    }

    private void forceEject(){
        if(currentATM != null) currentATM.ejectCard();
        currentState = ATMState.IDLE;
        inputBuffer.setLength(0);
        insertBtn.setDisable(false);
        userSelector.setDisable(false);
        atmBox.setDisable(false);
        tempTargetBank = null;
        tempTargetAcc = null;
        updateScreen("卡片已退出\n歡迎下次光臨");
    }

    private void handleKeypadInput(String key){
        if(key.equals("C")){
            if(inputBuffer.length() > 0){
                inputBuffer.setLength(0);
                updateScreen("輸入已清除\n請重新輸入");
            }else if(currentState != ATMState.IDLE && currentState != ATMState.SELECT_SERVICE){
                currentState = ATMState.SELECT_SERVICE;
                updateScreen("操作已取消\n請選擇服務");
            }
            return;
        }
        if(key.matches("存款|提款|轉帳|餘額")){
            if(currentState != ATMState.SELECT_SERVICE){
                return;
            }
            inputBuffer.setLength(0);
            switch(key){
                case "餘額":
                    String balanceMsg = currentATM.checkBalance();
                    updateScreen(balanceMsg + "\n\n按其他功能鍵繼續\n或按退卡離開");
                    break;
                case "存款":
                    currentState = ATMState.ENTER_DEPOSIT_AMT;
                    updateScreen("【存款服務】\n請輸入金額(100的倍數)\n完成後按 OK");
                    break;
                case "提款":
                    currentState = ATMState.ENTER_WITHDRAW_AMT;
                    updateScreen("【提款服務】\n請輸入金額\n完成後按 OK");
                    break;
                case "轉帳":
                    prepareTransfer();
                    break;
            }
            return;
        }
        if(key.equals("OK")){
            handleOKButton();
            return;
        }
        if(currentState == ATMState.CHECK_PIN ||
            currentState == ATMState.ENTER_DEPOSIT_AMT ||
            currentState == ATMState.ENTER_WITHDRAW_AMT ||
            currentState == ATMState.ENTER_TRANSFER_AMT){
            inputBuffer.append(key);
            if(currentState == ATMState.CHECK_PIN){
                updateScreen("輸入密碼: " + "*".repeat(inputBuffer.length()));
            }else{
                updateScreen("輸入金額: " + inputBuffer.toString());
            }
        }
    }

    private void handleOKButton(){
        String inputStr = inputBuffer.toString();
        inputBuffer.setLength(0);
        switch(currentState){
            case CHECK_PIN:
                boolean success = currentATM.login(inputStr);
                if(success){
                    currentState = ATMState.SELECT_SERVICE;
                    updateScreen("登入成功\n請選擇服務項目");
                }else{
                    updateScreen("登入失敗(密碼錯誤或帳戶鎖定)\n請重新輸入或洽詢櫃檯");
                }
                break;
            case ENTER_DEPOSIT_AMT:
                if(inputStr.isEmpty()) return;
                try{
                    double amt = Double.parseDouble(inputStr);
                    if(amt <= 0) throw new NumberFormatException();
                    if(currentUser.giveCash(amt)){
                        String msg = currentATM.deposit(amt);
                        if(msg.contains("成功")){
                            updateScreen(msg + "\n\n請選擇其他服務");
                            updateUserStatus();
                            currentState = ATMState.SELECT_SERVICE;
                        }else{
                            currentUser.takeCash(amt);
                            updateScreen(msg + "\n請重新操作");
                        }
                    }else{
                        updateScreen("交易失敗: 您身上的現金不足!\n請重新輸入金額");
                    }
                }catch(NumberFormatException e){
                    updateScreen("金額格式錯誤\n請重新輸入");
                }
                break;
            case ENTER_WITHDRAW_AMT:
                if(inputStr.isEmpty()) return;
                try{
                    double amt = Double.parseDouble(inputStr);
                    if(amt <= 0) throw new NumberFormatException();
                    String msg = currentATM.withdraw(amt);
                    if(msg.contains("成功")){
                        currentUser.takeCash(amt);
                        updateUserStatus();
                        updateScreen(msg + "\n\n請選擇其他服務");
                        currentState = ATMState.SELECT_SERVICE;
                    }else{
                        updateScreen(msg + "\n請重新輸入金額");
                    }
                }catch(NumberFormatException e){
                    updateScreen("金額格式錯誤\n請重新輸入");
                }
                break;
            case ENTER_TRANSFER_AMT:
                if(inputStr.isEmpty()) return;
                try{
                    double amt = Double.parseDouble(inputStr);
                    updateScreen("轉帳處理中...");
                    String msg = currentATM.transfer(tempTargetBank, tempTargetAcc, amt);
                    if(msg.contains("成功")){
                        updateScreen(msg + "\n\n請選擇其他服務");
                        updateUserStatus();
                        currentState = ATMState.SELECT_SERVICE;
                    }else{
                        updateScreen(msg + "\n請重新輸入金額");
                    }
                    tempTargetBank = null;
                    tempTargetAcc = null;
                }catch(NumberFormatException e){
                    updateScreen("金額格式錯誤\n請重新輸入");
                }
                break;
            default:
                break;
        }
    }

    private void prepareTransfer(){
        TextInputDialog bankDialog = new TextInputDialog("BankB");
        bankDialog.setTitle("轉帳步驟 1/2");
        bankDialog.setHeaderText("請輸入對方銀行代碼");
        bankDialog.setContentText("銀行(BankA / BankB):");
        Optional<String> bankRes = bankDialog.showAndWait();
        if(bankRes.isEmpty()){
            updateScreen("操作取消");
            return;
        }
        TextInputDialog accDialog = new TextInputDialog();
        accDialog.setTitle("轉帳步驟 2/2");
        accDialog.setHeaderText("請輸入對方帳號");
        accDialog.setContentText("帳號(Account ID):");
        Optional<String> accRes = accDialog.showAndWait();
        if(accRes.isEmpty()){
            updateScreen("操作取消");
            return;
        }
        this.tempTargetBank = bankRes.get().trim();
        this.tempTargetAcc = accRes.get().trim();
        currentState = ATMState.ENTER_TRANSFER_AMT;
        updateScreen("【轉帳服務】\n轉入: " + tempTargetBank + " - " + tempTargetAcc + "\n\n請用鍵盤輸入轉帳金額\n完成後按 OK");
    }

    private void updateCardList(){
        cardSelector.getItems().clear();
        if(currentUser != null && !currentUser.getCard().isEmpty()){
            cardSelector.getItems().addAll(currentUser.getCard());
            cardSelector.getSelectionModel().selectFirst();
        }
    }

    private void updateUserStatus(){
        if(currentUser == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("姓名: ").append(currentUser.getName()).append("\n");
        sb.append("口袋現金: $").append(String.format("%,.0f", currentUser.getCash())).append("\n");
        sb.append("--------------------------\n");
        sb.append("持有存摺(內容需補登才更新):\n");
        for(PassBook passBook : currentUser.getPassBook()){
            sb.append(String.format(" %s(%s)\n", passBook.getBankID(), passBook.getAccID()));
            var history = passBook.getHistory();
            if(!history.isEmpty()){
                sb.append("  └ 最新: ").append(history.get(history.size() - 1).toString().split("\\|")[1].trim()).append("\n");
            }else{
                sb.append("  └(無紀錄)\n");
            }
        }
        userStatusArea.setText(sb.toString());
    }

    private void updateScreen(String msg){
        screenArea.setText(msg);
    }

    private void showAlert(Alert.AlertType type, String title, String content){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void setupConsoleRedirect(){
        OutputStream out = new OutputStream(){
            @Override
            public void write(int b){
                Platform.runLater(() -> logArea.appendText(String.valueOf((char) b)));
            }
            @Override
            public void write(byte[] b, int off, int len){
                String s = new String(b, off, len);
                Platform.runLater(() -> logArea.appendText(s));
            }
        };
        System.setOut(new PrintStream(out, true));
    }
}