package bank.core;

public class ATM {
    private String ownerBankID;
    private BankSystem bankSystem;
    
    // private boolean isUsing = false;
    private Card currentCard = null;
    private boolean loggedIn = false;

    public ATM(BankSystem bankSystem, Bank ownerBank){
        this.bankSystem = bankSystem;
        this.ownerBankID = ownerBank.getBankID();
    }

    // 待處理
    public boolean isCrossBank(Card card){
        return !card.getBankID().equals(ownerBankID);
    }

    public void insertCard(Card card){
        if(currentCard != null){
            System.out.println("此ATM正在使用");
            return;
        }

        if(card.isUsing()){
            System.out.println("錯誤: 此卡片目前正在其他機台使用中");
            return;
        }

        this.currentCard = card;
        card.setStatus(true);
        this.loggedIn = false;
        // this.isUsing = true;
        System.out.println("已插入卡片: " + card.getCardID());
    }

    public boolean login(String password){
        if(currentCard == null){
            System.out.println("請先插卡");
            return false;
        }

        if(loggedIn){
            System.out.println("此ATM已登入");
            return true;
        }

        int status = bankSystem.verifyPasswordStatus(currentCard, password);
        // boolean ok = bankSystem.verifyPassword(currentCard, password);

        if(status == 0){
            this.loggedIn = true;
            System.out.println("登入成功");
            return true;
        }else if(status == 1){
            System.out.println("登入失敗太多次，帳戶已被鎖定，請洽發卡銀行臨櫃解鎖");
            return false;
        }else{
            System.out.println("密碼錯誤");
            return false;
        }
    }
    
    private boolean checkLogin(){
        if(currentCard == null){
            System.out.println("請先插卡");
            return false;
        }
        if(!loggedIn){
            System.out.println("尚未登入，請先輸入密碼");
            return false;
        }

        return true;
    }

    public String checkBalance(){
        if(!checkLogin()) return "請先登入";
        
        TransactionResult result = bankSystem.getBalance(currentCard);

        String msg;

        if(result.isSuccess()){
            msg = result.getMessage() + ": " + result.getBalance();
        }else{
            msg = "查詢失敗: " + result.getMessage();
        }
        System.out.println(msg);
        return msg;
    }

    public String deposit(double amount){
        if(!checkLogin()) return "請先登入";

        TransactionResult result = bankSystem.deposit(currentCard, amount, this.ownerBankID);

        String msg;
        if(result.isSuccess()){
            msg = result.getMessage();
        }else{
            msg = "錯誤: " + result.getMessage();
        }
        System.out.println(msg);
        return msg;
    }

    public String withdraw(double amount){
        if(!checkLogin()) return "請先登入";
        
        TransactionResult result = bankSystem.withdraw(currentCard, amount, this.ownerBankID);

        String msg;
        if(result.isSuccess()){
            msg = result.getMessage();
        }else{
            msg = "錯誤: " + result.getMessage();
        }
        System.out.println(msg);
        return msg;
    }

    public String transfer(String targetBankID, String targetAccID, double amount){
        if(!checkLogin()) return "請先登入";

        StringBuilder sb = new StringBuilder();
        if(!currentCard.getBankID().equals(targetBankID)){
            sb.append("【跨行轉帳】\n");
        }

        TransactionResult result = bankSystem.transfer(currentCard, targetBankID, targetAccID, amount);

        if(result.isSuccess()){
            sb.append("轉帳成功\n");
            sb.append("金額: ").append(amount).append("\n");
            sb.append("餘額: ").append(result.getBalance());
        }else{
            sb.append("交易失敗:\n").append(result.getMessage());
        }
        
        String finalMsg = sb.toString();
        System.out.println(finalMsg);
        return finalMsg;
    }

    public void ejectCard(){
        if (this.currentCard == null) {
            return;
        }

        this.currentCard.setStatus(false);
        this.currentCard = null;
        this.loggedIn = false;
        // this.isUsing = false;
        System.out.println("卡片已退出");
    }
}
