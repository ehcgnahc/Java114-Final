package bank.core;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseBank {
    private static final SecureRandom secureRandom = new SecureRandom();
    
    private String bankID;

    public BaseBank(String name){
        this.bankID = name;
    }

    public String getBankID(){
        return bankID;
    }

    protected Map<String, Account> accMap = new HashMap<>();
    
    // 要加入hash 以及人數問題和隨機重複效率
    private String generateAccID(){
        StringBuilder sb = new StringBuilder("25942759");
        for(int i=0; i<8; i++){
            sb.append(secureRandom.nextInt(10));
        }
        
        return sb.toString();
    }

    public Card createAcc(User user, String password){
        // String name = user.getName();
        String accID = generateAccID();

        Account newAcc = new Account(accID, password);
        accMap.put(accID, newAcc);

        String newCardID = "CARD-" + accID;
        Card newCard = new Card(newCardID, accID, bankID);
        
        user.setCard(newCard); //開戶的同時將卡交給使用者
        user.setAccID(accID);

        System.out.println("開戶成功，卡號是: " + newCardID);
        return newCard;
    }

    protected Account findAcc(String accID){
        if(accMap.containsKey(accID)){
            return accMap.get(accID);
        }else{
            return null;
        }
    }

    public boolean isAccountExist(String accID) {
        return accMap.containsKey(accID);
    }

    public boolean verifyPassword(Card card, String password){
        Account acc = findAcc(card.getAccID());

        if(acc == null){
            return false;
        }

        return acc.checkPassword(password);
    }

    public TransactionResult getBalance(Card card){
        Account acc = findAcc(card.getAccID());

        if(acc == null)
            return new TransactionResult(false, "請確認帳戶正確", -1.0);
            
        return new TransactionResult(true, "目前餘額", acc.getBalance());
    }

    public TransactionResult deposit(Card card, double amount){
        Account acc = findAcc(card.getAccID());
        
        if(acc == null)
            return new TransactionResult(false, "請確認帳戶正確", -1.0);

        acc.deposit(amount);
        return new TransactionResult(true, "存款成功", acc.getBalance());
        
    }

    public TransactionResult depositByAccID(String accID, double amount) {
        Account acc = findAcc(accID);
        if (acc == null) {
            return new TransactionResult(false, "轉入帳號不存在", -1.0);
        }
        acc.deposit(amount);
        
        return new TransactionResult(true, "轉入成功", acc.getBalance());
    }

    public TransactionResult withdraw(Card card, double amount){
        Account acc = findAcc(card.getAccID());
        
        if(acc == null)
            return new TransactionResult(false, "請確認帳戶正確", -1.0);
        
        if(acc.getBalance() < amount)
            return new TransactionResult(false, "餘額不足", acc.getBalance());
        
        acc.withdraw(amount);
        return new TransactionResult(true, "提款成功", acc.getBalance());
    }
}
