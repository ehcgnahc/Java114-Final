package bank.core;

import java.util.HashMap;
import java.util.Map;

public class BankSystem {
    protected Map<String, BaseBank> bankRegisterMap = new HashMap<>();
    
    public void addBank(BaseBank bank){
        bankRegisterMap.put(bank.getBankName(), bank);
        System.out.println("銀行系統：已連接 " + bank.getBankName());
    }
    
    // 讀取卡片資訊
    public void deposit(Card card, double amount){
        if(card == null) return;
        
        String cardID = card.getCardID();
        String accID = card.getAccID();
        String targetBankID = card.getBankID();
        BaseBank targetBank = bankRegisterMap.get(targetBankID);

        if(targetBank == null){
            System.out.println("ERROR: 找不到發卡銀行");
        }else{
            System.out.println("發卡銀行是: " + targetBankID);
            targetBank.deposit(accID, amount);
        }
    }
}
