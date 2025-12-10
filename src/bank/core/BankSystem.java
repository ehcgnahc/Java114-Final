package bank.core;

import java.util.HashMap;
import java.util.Map;

public class BankSystem {
    protected Map<String, BaseBank> bankRegisterMap = new HashMap<>();
    
    public void addBank(BaseBank bank){
        bankRegisterMap.put(bank.getBankName(), bank);
        System.out.println("銀行系統：已連接 " + bank.getBankName());
    }
    
    public boolean verifyPassword(Card card, String password){
        BaseBank bank = bankRegisterMap.get(card.getBankID());

        if(bank == null){
            return false;
        }

        return bank.verifyPassword(card, password);
    }

    // 修改為ATM登入一次
    public void deposit(Card card, String password, double amount){
        if(card == null) return;

        String targetBankID = card.getBankID();
        
        BaseBank targetBank = bankRegisterMap.get(targetBankID);

        if(targetBank == null){
            System.out.println("ERROR: 找不到發卡銀行");
        }else{
            System.out.println("發卡銀行是: " + targetBankID);
            targetBank.deposit(card, password, amount);
        }
    }

    public void withdraw(Card card, String password, double amount){
        if(card == null) return;

        String targetBankID = card.getBankID();
        
        BaseBank targetBank = bankRegisterMap.get(targetBankID);

        if(targetBank == null){
            System.out.println("ERROR: 找不到發卡銀行");
        }else{
            System.out.println("發卡銀行是: " + targetBankID);
            targetBank.withdraw(card, password, amount);
        }
    }

    public void withdraw(Card card, String password, double amount){
        if(card == null) return;

        String targetBankID = card.getBankID();
        
        BaseBank targetBank = bankRegisterMap.get(targetBankID);

        if(targetBank == null){
            System.out.println("ERROR: 找不到發卡銀行");
        }else{
            System.out.println("發卡銀行是: " + targetBankID);
            targetBank.withdraw(card, password, amount);
        }
    }
}
