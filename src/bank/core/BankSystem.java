package bank.core;

import java.util.HashMap;
import java.util.Map;

public class BankSystem {
    protected Map<String, BaseBank> bankRegisterMap = new HashMap<>();
    
    public void addBank(BaseBank bank){
        bankRegisterMap.put(bank.getBankID(), bank);
        System.out.println("銀行系統：已連接 " + bank.getBankID());
    }
    
    public boolean verifyPassword(Card card, String password){
        BaseBank bank = bankRegisterMap.get(card.getBankID());

        if(bank == null) return false;
        return bank.verifyPassword(card, password);
    }

    public void getBalance(Card card){
        if(card == null) return;
        
        BaseBank bank = bankRegisterMap.get(card.getBankID());
        if(bank == null) return;

        bank.getBalance(card);
    }

    public void deposit(Card card, double amount){
        if(card == null) return;

        BaseBank bank = bankRegisterMap.get(card.getBankID());
        if(bank == null) return;

        bank.deposit(card, amount);
    }

    public void withdraw(Card card, double amount){
        if(card == null) return;
        
        BaseBank bank = bankRegisterMap.get(card.getBankID());
        if(bank == null) return;

        bank.withdraw(card, amount);
    }
}
