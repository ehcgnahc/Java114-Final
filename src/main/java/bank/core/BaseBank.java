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
        return this.bankID;
    }

    protected Map<String, Account> accMap = new HashMap<>();
    
    private String generateAccID(){
        String prefix = "25942759";

        long nano = System.nanoTime();

        String timePart = String.format("%06d", nano % 1000000);

        int randomPart = secureRandom.nextInt(100); // 00~99
        String randomStr = String.format("%02d", randomPart);

        return prefix + timePart + randomStr;

        // StringBuilder sb = new StringBuilder("25942759");
        // for(int i=0; i<8; i++){
        //     sb.append(secureRandom.nextInt(10));
        // }
        
        // return sb.toString();
    }

    private String generateEasyAccID(){
        int randomPart = secureRandom.nextInt(100); // 00~99
        String randomStr = String.format("%02d", randomPart);

        return randomStr;

        // StringBuilder sb = new StringBuilder("25942759");
        // for(int i=0; i<8; i++){
        //     sb.append(secureRandom.nextInt(10));
        // }
        
        // return sb.toString();
    }

    public Card createAcc(User user, String password){
        // String name = user.getName();
        String accID;

        do{
            accID = generateEasyAccID();
        } while (accMap.containsKey(accID));

        Account newAcc = new Account(accID, password);
        accMap.put(accID, newAcc);

        String newCardID = "CARD-" + accID;
        Card newCard = new Card(newCardID, accID, bankID);
        
        PassBook newPassBook = new PassBook(accID, bankID);

        user.addCard(newCard); //開戶的同時將卡交給使用者
        user.addPassBook(newPassBook);

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

        if(acc == null)
            return false;

        return acc.checkPassword(password);
    }

    public int verifyPasswordStatus(Card card, String password){
        Account acc = findAcc(card.getAccID());
        
        if(acc == null)
            return -1;
        
        return acc.checkPasswordStatus(password);
    }

    public boolean unlockAccount(String accID){
        Account acc = findAcc(accID);
        
        if(acc == null)
            return false;
        
        acc.unlock();
        return true;
    }

    public TransactionResult getBalance(Card card){
        Account acc = findAcc(card.getAccID());

        if(acc == null)
            return new TransactionResult(false, "請確認帳戶正確", -1.0);
            
        return new TransactionResult(true, "目前餘額", acc.getBalance());
    }

    public TransactionResult deposit(Card card, double amount, String msg){
        Account acc = findAcc(card.getAccID());
        
        if(acc == null)
            return new TransactionResult(false, "請確認帳戶正確", -1.0);

        acc.deposit(amount, msg);
        return new TransactionResult(true, "存款成功", acc.getBalance());
        
    }

    public TransactionResult depositByAccID(String accID, double amount, String msg) {
        Account acc = findAcc(accID);
        if (acc == null) {
            return new TransactionResult(false, "轉入帳號不存在", -1.0);
        }
        acc.deposit(amount, msg);
        
        return new TransactionResult(true, "轉入成功", acc.getBalance());
    }

    public TransactionResult withdraw(Card card, double amount, String msg){
        Account acc = findAcc(card.getAccID());
        
        if(acc == null)
            return new TransactionResult(false, "請確認帳戶正確", -1.0);
        
        if(acc.getBalance() < amount)
            return new TransactionResult(false, "餘額不足", acc.getBalance());
        
        acc.withdraw(amount, msg);
        return new TransactionResult(true, "提款成功", acc.getBalance());
    }

    public TransactionResult updatePassBook(PassBook passBook){
        Account acc = findAcc(passBook.getAccID());
        
        if(acc == null)
            return new TransactionResult(false, "請確認存簿正確", -1.0);

        passBook.setHistory(acc.getTransactions());
        return new TransactionResult(true, "更新存摺成功", 0.0);
    }
}
