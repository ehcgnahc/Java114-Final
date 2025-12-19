package bank.core;

public class ATM {
    private String ownerBankID;
    private BankSystem bankSystem;

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
        this.currentCard = card;
        this.loggedIn = false;
        System.out.println("已插入卡片: " + card.getCardID());
    }

    public boolean login(String password){
        if(currentCard == null){
            System.out.println("請先插卡");
            return false;
        }

        boolean ok = bankSystem.verifyPassword(currentCard, password);

        if(ok){
            this.loggedIn = true;
            System.out.println("登入成功");
        }else{
            System.out.println("密碼錯誤");
        }

        return ok;
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

    public void checkBalance(){
        if(!checkLogin()) return;
        
        TransactionResult result = bankSystem.getBalance(currentCard);

        if(result.isSuccess()){
            System.out.println(result.getMessage() + ": " + result.getBalance());
        }else{
            System.out.println("錯誤: " + result.getMessage());
        }
    }

    public void deposit(double amount){
        if(!checkLogin()) return;

        TransactionResult result = bankSystem.deposit(currentCard, amount, this.ownerBankID);

        if(result.isSuccess()){
            System.out.println(result.getMessage());
        }else{
            System.out.println("錯誤: " + result.getMessage());
        }
    }

    public void withdraw(double amount){
        if(!checkLogin()) return;
        
        TransactionResult result = bankSystem.withdraw(currentCard, amount, this.ownerBankID);

        if(result.isSuccess()){
            System.out.println(result.getMessage());
        }else{
            System.out.println("錯誤: " + result.getMessage());
        }
    }

    public void transfer(String targetBankID, String targetAccID, double amount){
        if(!checkLogin()) return;

        if(!currentCard.getBankID().equals(targetBankID)){
            System.out.println("【提醒】您正在進行跨行轉帳，將收取額外手續費");
        }

        TransactionResult result = bankSystem.transfer(currentCard, targetBankID, targetAccID, amount);

        if(result.isSuccess()){
            System.out.println(result.getMessage());
            System.out.println("帳戶餘額: " + result.getBalance());
        }else{
            System.out.println("交易失敗: " + result.getMessage());
        }
    }

    public void ejectCard(){
        this.currentCard = null;
        this.loggedIn = false;
        System.out.println("卡片已退出");
    }
}
