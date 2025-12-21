package main;

import bank.core.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Optional;

public class BankSimulationApp extends Application {

    // --- 核心系統資料 ---
    private BankSystem bankSystem;
    private Bank bankA, bankB;
    private ATM atmA, atmB;
    private User user1, user2;

    // --- UI 狀態控制 ---
    private User currentUser;       // 目前扮演的角色
    private ATM currentTargetATM;   // 目前使用的 ATM
    private Bank currentTargetBank; // 目前所在的銀行

    // --- UI 容器 (用於切換頁面) ---
    private StackPane rootLayout;
    private VBox dashboardView;
    private VBox bankView;
    private VBox atmView;

    // --- UI 元件 (需要動態更新的) ---
    private Label dashboardUserInfo;
    private TextArea atmScreen;
    private TextArea systemLogArea; // 顯示後端 System.out 的內容

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // 1. 初始化後端系統 (模擬現實環境建立)
        initBackend();

        // 2. 設定 System.out 攔截 (讓後端訊息顯示在 GUI)
        setupConsoleRedirect();

        // 3. 建立各個視圖 (Views)
        createDashboardView();
        createBankView(); // 初始化容器，內容會動態改變
        createATMView();  // 初始化容器，內容會動態改變

        // 4. 設定主場景
        rootLayout = new StackPane();
        rootLayout.getChildren().addAll(dashboardView, bankView, atmView);
        
        // 預設顯示首頁，隱藏其他
        bankView.setVisible(false);
        atmView.setVisible(false);

        Scene scene = new Scene(rootLayout, 1000, 700);
        primaryStage.setTitle("虛擬銀行城市 - 114年期末專案");
        primaryStage.setScene(scene);
        primaryStage.show();

        updateDashboardInfo();
    }

    // ==========================================
    // 1. 初始化與後端邏輯
    // ==========================================
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

        // 初始化使用者 (尚未開戶狀態)
        user1 = new User("Alice", 5000); // 手上有 5000 現金
        user2 = new User("Bob", 10000);  // 手上有 10000 現金
        
        currentUser = user1; // 預設角色
    }

    // ==========================================
    // 2. 首頁 (Dashboard) - 選擇角色與目的地
    // ==========================================
    private void createDashboardView() {
        dashboardView = new VBox(20);
        dashboardView.setAlignment(Pos.TOP_CENTER);
        dashboardView.setPadding(new Insets(30));
        dashboardView.setStyle("-fx-background-color: #f4f4f4;");

        // 標題
        Label title = new Label("歡迎來到模擬銀行城市");
        title.setFont(Font.font("Microsoft JhengHei", FontWeight.BOLD, 28));

        // --- 角色狀態區 ---
        VBox userPanel = new VBox(10);
        userPanel.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        userPanel.setMaxWidth(600);
        
        Label userTitle = new Label("第一步：選擇你的角色");
        userTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        ComboBox<User> userSelector = new ComboBox<>();
        userSelector.getItems().addAll(user1, user2);
        userSelector.setValue(user1);
        userSelector.setConverter(new javafx.util.StringConverter<User>() {
            public String toString(User u) { return u.getName(); }
            public User fromString(String s) { return null; }
        });
        userSelector.setOnAction(e -> {
            currentUser = userSelector.getValue();
            updateDashboardInfo();
        });

        dashboardUserInfo = new Label();
        dashboardUserInfo.setFont(Font.font("Consolas", 14));
        
        userPanel.getChildren().addAll(userTitle, userSelector, new Separator(), dashboardUserInfo);

        // --- 地點選擇區 ---
        HBox locationPanel = new HBox(20);
        locationPanel.setAlignment(Pos.CENTER);
        
        // 銀行區塊
        VBox bankSection = createDestinationCard("前往銀行櫃檯", "辦理開戶", 
            "BankA 櫃檯", e -> switchToBank(bankA),
            "BankB 櫃檯", e -> switchToBank(bankB)
        );

        // ATM 區塊
        VBox atmSection = createDestinationCard("使用 ATM", "存提款、轉帳", 
            "ATM A (BankA)", e -> switchToATM(atmA),
            "ATM B (BankB)", e -> switchToATM(atmB)
        );

        locationPanel.getChildren().addAll(bankSection, atmSection);

        // --- 系統 Log 區 ---
        VBox logBox = new VBox(5);
        logBox.setMaxWidth(800);
        Label logTitle = new Label("系統後台紀錄 (System Output):");
        systemLogArea = new TextArea();
        systemLogArea.setEditable(false);
        systemLogArea.setPrefHeight(100);
        systemLogArea.setStyle("-fx-font-family: 'Consolas';");
        logBox.getChildren().addAll(logTitle, systemLogArea);

        dashboardView.getChildren().addAll(title, userPanel, locationPanel, new Separator(), logBox);
    }

    private VBox createDestinationCard(String title, String desc, String btn1Text, javafx.event.EventHandler<?> evt1, String btn2Text, javafx.event.EventHandler<?> evt2) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #ddd; -fx-border-radius: 10;");
        card.setAlignment(Pos.CENTER);

        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        Label lblDesc = new Label(desc);
        lblDesc.setTextFill(Color.GRAY);

        Button btn1 = new Button(btn1Text);
        btn1.setPrefWidth(200);
        btn1.getStyleClass().add("btn-primary"); // 可加 CSS
        btn1.setOnAction((javafx.event.EventHandler<javafx.event.ActionEvent>)evt1);

        Button btn2 = new Button(btn2Text);
        btn2.setPrefWidth(200);
        btn2.setOnAction((javafx.event.EventHandler<javafx.event.ActionEvent>)evt2);

        card.getChildren().addAll(lblTitle, lblDesc, new Separator(), btn1, btn2);
        return card;
    }

    // ==========================================
    // 3. 銀行櫃檯視圖 (Bank View)
    // ==========================================
    private void createBankView() {
        bankView = new VBox(30);
        bankView.setAlignment(Pos.CENTER);
        bankView.setStyle("-fx-background-color: #e3f2fd;"); // 淺藍色背景
        bankView.setVisible(false); // 初始隱藏
    }

    private void switchToBank(Bank bank) {
        currentTargetBank = bank;
        bankView.getChildren().clear(); // 重繪內容

        Label header = new Label("銀行櫃檯 - " + bank.getBankID());
        header.setFont(Font.font("Microsoft JhengHei", FontWeight.BOLD, 36));
        header.setTextFill(Color.DARKBLUE);

        VBox content = new VBox(20);
        content.setMaxWidth(500);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
        content.setAlignment(Pos.CENTER);

        Label welcome = new Label("您好，" + currentUser.getName());
        welcome.setFont(Font.font(18));

        Button openAccBtn = new Button("申請開戶");
        openAccBtn.setStyle("-fx-font-size: 16px; -fx-padding: 10 20; -fx-background-color: #2196F3; -fx-text-fill: white;");
        openAccBtn.setOnAction(e -> handleOpenAccount());

        Button backBtn = new Button("離開銀行 (返回首頁)");
        backBtn.setStyle("-fx-font-size: 14px;");
        backBtn.setOnAction(e -> {
            updateDashboardInfo();
            showView(dashboardView);
        });

        // 狀態顯示
        Label statusLabel = new Label();
        if (currentUser.getCard() != null && currentUser.getCard().getBankID().equals(bank.getBankID())) {
            openAccBtn.setDisable(true);
            openAccBtn.setText("您已在本行開戶");
            statusLabel.setText("您的帳號: " + currentUser.getAccID());
        } else if (currentUser.getCard() != null) {
            statusLabel.setText("您已在 " + currentUser.getCard().getBankID() + " 開戶");
        } else {
            statusLabel.setText("尚未開戶");
        }

        content.getChildren().addAll(welcome, statusLabel, new Separator(), openAccBtn, backBtn);
        bankView.getChildren().addAll(header, content);
        
        showView(bankView);
    }

    private void handleOpenAccount() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("銀行開戶");
        dialog.setHeaderText("請設定您的帳戶密碼");
        dialog.setContentText("密碼:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(password -> {
            // 呼叫後端邏輯
            currentTargetBank.createAcc(currentUser, password);
            
            // 重新整理頁面
            switchToBank(currentTargetBank);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("開戶成功！");
            alert.setContentText("您的帳號是: " + currentUser.getAccID() + "\n卡片已發放。");
            alert.show();
        });
    }

    // ==========================================
    // 4. ATM 視圖 (ATM View)
    // ==========================================
    private void createATMView() {
        atmView = new VBox(20);
        atmView.setAlignment(Pos.CENTER);
        atmView.setStyle("-fx-background-color: #2c3e50;"); // 深色背景
        atmView.setVisible(false);
    }

    private void switchToATM(ATM atm) {
        currentTargetATM = atm;
        atmView.getChildren().clear();

        // --- ATM 機台外觀 ---
        VBox machine = new VBox(15);
        machine.setMaxWidth(400);
        machine.setPadding(new Insets(20));
        machine.setStyle("-fx-background-color: #95a5a6; -fx-background-radius: 10; -fx-border-color: #7f8c8d; -fx-border-width: 5;");
        machine.setAlignment(Pos.CENTER);

        // 1. 螢幕
        StackPane screenFrame = new StackPane();
        Rectangle bg = new Rectangle(360, 150, Color.BLACK);
        atmScreen = new TextArea();
        atmScreen.setPrefRowCount(5);
        atmScreen.setMaxWidth(350);
        atmScreen.setEditable(false);
        atmScreen.setStyle("-fx-control-inner-background: black; -fx-text-fill: #00ff00; -fx-font-family: 'Consolas'; -fx-font-size: 14px; -fx-highlight-fill: #00ff00;");
        atmScreen.setText("歡迎使用 ATM\n請插入金融卡");
        
        screenFrame.getChildren().addAll(bg, atmScreen);

        // 2. 插卡口
        HBox slotPanel = new HBox(10);
        slotPanel.setAlignment(Pos.CENTER);
        Button insertCardBtn = new Button("插入卡片");
        Button ejectCardBtn = new Button("退卡 / 離開");
        
        insertCardBtn.setStyle("-fx-background-color: #f1c40f;");
        ejectCardBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        insertCardBtn.setOnAction(e -> {
            if (currentUser.getCard() != null) {
                currentTargetATM.insertCard(currentUser.getCard());
                atmScreen.setText("卡片已插入\n請輸入密碼按下 OK");
                insertCardBtn.setDisable(true);
            } else {
                atmScreen.setText("錯誤：您沒有卡片！\n請先去銀行開戶。");
            }
        });

        ejectCardBtn.setOnAction(e -> {
            currentTargetATM.ejectCard();
            updateDashboardInfo();
            showView(dashboardView); // 退卡直接回首頁
        });

        slotPanel.getChildren().addAll(insertCardBtn, ejectCardBtn);

        // 3. 鍵盤區
        GridPane keypad = new GridPane();
        keypad.setHgap(10);
        keypad.setVgap(10);
        keypad.setAlignment(Pos.CENTER);
        
        // 數字鍵盤按鈕產生
        StringBuilder pinBuffer = new StringBuilder();
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
            btn.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-base: #ecf0f1;");
            
            // 鍵盤邏輯
            btn.setOnAction(e -> handleATMInput(key, pinBuffer));

            keypad.add(btn, i % 4, i / 4);
        }

        machine.getChildren().addAll(new Label("ATM"), screenFrame, slotPanel, new Separator(), keypad);
        atmView.getChildren().add(machine);

        showView(atmView);
    }

    private void handleATMInput(String key, StringBuilder buffer) {
        switch (key) {
            case "C":
                buffer.setLength(0);
                atmScreen.setText("已清除\n請重新輸入");
                break;
            case "OK": // 登入邏輯
                atmScreen.setText("驗證中...");
                currentTargetATM.login(buffer.toString());
                buffer.setLength(0); // 清除暫存密碼
                break;
            case "餘額":
                currentTargetATM.checkBalance(); 
                // 結果會透過 ConsoleRedirect 顯示在下方 Log，我們也可以更新螢幕
                atmScreen.appendText("\n(請見下方收據/Log)");
                break;
            case "存款":
                askAmount("存款", amt -> {
                    if(currentUser.giveCash(amt)) { // 從口袋扣錢
                        currentTargetATM.deposit(amt);
                        updateDashboardInfo();
                        atmScreen.setText("存款 $" + amt + " 完成");
                    } else {
                        atmScreen.setText("現金不足！");
                    }
                });
                break;
            case "提款":
                askAmount("提款", amt -> {
                    currentTargetATM.withdraw(amt);
                    // 這裡假設ATM成功 (簡化)，實際應檢查回傳值
                    // 模擬拿到錢
                    currentUser.takeCash(amt);
                    updateDashboardInfo();
                    atmScreen.setText("提款 $" + amt + " 完成\n請取鈔");
                });
                break;
            case "轉帳":
                handleTransfer();
                break;
            default: // 數字鍵
                buffer.append(key);
                atmScreen.setText("輸入: " + "*".repeat(buffer.length()));
                break;
        }
    }

    private void handleTransfer() {
         TextInputDialog bankDialog = new TextInputDialog("BankB");
         bankDialog.setTitle("ATM 轉帳");
         bankDialog.setHeaderText("步驟 1/3");
         bankDialog.setContentText("輸入對方銀行代碼 (BankA/BankB):");
         
         bankDialog.showAndWait().ifPresent(targetBank -> {
             TextInputDialog accDialog = new TextInputDialog();
             accDialog.setTitle("ATM 轉帳");
             accDialog.setHeaderText("步驟 2/3");
             accDialog.setContentText("輸入對方帳號:");
             
             accDialog.showAndWait().ifPresent(targetAcc -> {
                 askAmount("轉帳", amt -> {
                     currentTargetATM.transfer(targetBank, targetAcc, amt);
                     atmScreen.setText("轉帳交易完成");
                 });
             });
         });
    }

    private void askAmount(String action, java.util.function.Consumer<Double> callback) {
        TextInputDialog d = new TextInputDialog();
        d.setTitle(action);
        d.setHeaderText("請輸入" + action + "金額");
        d.setContentText("金額:");
        d.showAndWait().ifPresent(s -> {
            try {
                double val = Double.parseDouble(s);
                if(val > 0) callback.accept(val);
                else atmScreen.setText("金額必須大於 0");
            } catch(NumberFormatException e) {
                atmScreen.setText("輸入錯誤");
            }
        });
    }

    // ==========================================
    // 通用輔助方法
    // ==========================================
    
    // 切換顯示的 View
    private void showView(VBox view) {
        dashboardView.setVisible(false);
        bankView.setVisible(false);
        atmView.setVisible(false);
        view.setVisible(true);
    }

    // 更新 Dashboard 上面的使用者狀態資訊
    private void updateDashboardInfo() {
        if(currentUser == null) return;
        String cardTxt = (currentUser.getCard() == null) ? "無" : currentUser.getCard().getCardID() + " (" + currentUser.getCard().getBankID() + ")";
        String accTxt = (currentUser.getAccID() == null) ? "尚未開戶" : currentUser.getAccID();
        
        String info = String.format(
            "姓名: %s\n口袋現金: $%.0f\n銀行帳號: %s\n持有卡片: %s",
            currentUser.getName(), currentUser.getCash(), accTxt, cardTxt
        );
        dashboardUserInfo.setText(info);
    }

    // 將 System.out.println 導向到 GUI 下方的 Log 區
    private void setupConsoleRedirect() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                appendText(String.valueOf((char) b));
            }
            @Override
            public void write(byte[] b, int off, int len) {
                appendText(new String(b, off, len));
            }
            private void appendText(String str) {
                Platform.runLater(() -> systemLogArea.appendText(str));
            }
        };
        System.setOut(new PrintStream(out, true));
    }
}