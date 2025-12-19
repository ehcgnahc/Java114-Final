package bank.core;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BankSystem {
    protected Map<String, BaseBank> bankRegisterMap = new HashMap<>();
    private Map<String, Map<String, Double>> crossBankFeeMap = new HashMap<>();

    public void addBank(BaseBank bank){
        bankRegisterMap.put(bank.getBankID(), bank);
        crossBankFeeMap.put(bank.getBankID(), new HashMap<>());
        System.out.println("銀行系統: 已連接 " + bank.getBankID());
    }
    
    public void setCrossBankFee(String cardBankID, String ATMBankID, double fee){
        if(crossBankFeeMap.containsKey(cardBankID)){
            crossBankFeeMap.get(cardBankID).put(ATMBankID, fee);
            System.out.println("設定費率: " + cardBankID + " 的卡在 " + ATMBankID + " 提款，手續費為 " + fee);
        }
    }

    private double getFee(String sourceBankID, String targetBankID){
        if(crossBankFeeMap.containsKey(sourceBankID) && 
            crossBankFeeMap.get(sourceBankID).containsKey(targetBankID)){
            return crossBankFeeMap.get(sourceBankID).get(targetBankID);
        }
        return 15.0; 
    }

    public boolean verifyPassword(Card card, String password){
        BaseBank bank = bankRegisterMap.get(card.getBankID());

        if(bank == null) return false;
        return bank.verifyPassword(card, password);
    }

    public TransactionResult getBalance(Card card){
        if(card == null)
            return new TransactionResult(false, "卡片不存在", -1.0);
        
        BaseBank bank = bankRegisterMap.get(card.getBankID());
        if(bank == null)
            return new TransactionResult(false, "銀行不存在", -1.0);

        return bank.getBalance(card);
    }

    public TransactionResult deposit(Card card, double amount, String ATMBankID){
        if(card == null)
            return new TransactionResult(false, "卡片不存在", -1.0);

        BaseBank bank = bankRegisterMap.get(card.getBankID());
        if(bank == null)
            return new TransactionResult(false, "銀行不存在", -1.0);
        
        double fee = 0.0;

        if(!card.getBankID().equals(ATMBankID)){
            fee = getFee(card.getBankID(), ATMBankID);
            System.out.println("偵測到跨行存款(" + card.getBankID() + " -> " + ATMBankID + ")，手續費: " + fee);
        }

        double totalAmount = amount - fee;

        if(totalAmount < 0){
            return new TransactionResult(false, "存款金額不足以支付手續費", -1.0);
        }

        return bank.deposit(card, totalAmount);
    }

    public TransactionResult withdraw(Card card, double amount, String ATMBankID){
        if(card == null)
            return new TransactionResult(false, "卡片不存在", -1.0);
        
        BaseBank bank = bankRegisterMap.get(card.getBankID());
        if(bank == null)
            return new TransactionResult(false, "銀行不存在", -1.0);

        double fee = 0.0;

        if(!card.getBankID().equals(ATMBankID)){
            fee = getFee(card.getBankID(), ATMBankID);
            System.out.println("偵測到跨行提款(" + card.getBankID() + " -> " + ATMBankID + ")，手續費: " + fee);
        }

        double totalAmount = amount + fee;

        return bank.withdraw(card, totalAmount);
    }

    public TransactionResult transfer(Card sourceCard, String targetBankID, String targetAccID, double amount){
        if(sourceCard == null)
            return new TransactionResult(false, "卡片錯誤", -1.0);
        if(amount <= 0)
            return new TransactionResult(false, "轉帳金額必須大於 0", -1.0);

        BaseBank sourceBank = bankRegisterMap.get(sourceCard.getBankID());
        BaseBank targetBank = bankRegisterMap.get(targetBankID);

        if(sourceBank == null)
            return new TransactionResult(false, "發卡銀行不存在", -1.0);
        if(targetBank == null)
            return new TransactionResult(false, "轉入銀行代碼錯誤", -1.0);

        if(!targetBank.isAccountExist(targetAccID)){
            return new TransactionResult(false, "轉入帳號不存在，交易取消", -1.0);
        }

        double fee = 0;
        if(!sourceCard.getBankID().equals(targetBankID)){
            fee = getFee(sourceBank.getBankID(), targetBankID);
        }

        double totalDeduct = amount + fee;

        TransactionResult withdrawResult = sourceBank.withdraw(sourceCard, totalDeduct);

        if(!withdrawResult.isSuccess()){
            return new TransactionResult(false, "轉帳失敗: " + withdrawResult.getMessage(), -1.0);
        }

        TransactionResult depositResult = targetBank.depositByAccID(targetAccID, amount);

        if(!depositResult.isSuccess()){
            sourceBank.deposit(sourceCard, totalDeduct);
            return new TransactionResult(false, "轉入失敗，金額已退回", -1.0);
        }

        String msg = "轉帳成功！";
        if(fee > 0){
            msg += "(手續費: " + fee + ")";
        }
        
        return new TransactionResult(true, msg, withdrawResult.getBalance());
    }
}
