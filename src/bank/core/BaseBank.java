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

    public Card createAcc(String name, String password){
        String accID = generateAccID();

        Account newAcc = new Account(accID, password);
        accMap.put(accID, newAcc);

        String newCardID = "CARD-" + accID;
        Card newCard = new Card(newCardID, accID, name, bankID);
        
        System.out.println("開戶成功，卡號是:" + newCardID);

        return newCard;
    }

    protected Account findAcc(String accID){
        if(accMap.containsKey(accID)){
            return accMap.get(accID);
        }else{
            return null;
        }
    }

    public boolean verifyPassword(Card card, String password){
        Account acc = findAcc(card.getAccID());

        if(acc == null){
            return false;
        }

        return acc.checkPassword(password);
    }

    public void deposit(Card card, double amount){
        Account acc = findAcc(card.getAccID());
        
        if(acc == null){
            System.out.println("錯誤，請確認帳戶正確");
        }else{
            acc.deposit(amount);
            System.out.println("存款成功");
        }
    }

    public void getBalance(Card card){
        Account acc = findAcc(card.getAccID());

        if(acc == null){
            System.out.println("錯誤，請確認帳戶正確");
        }else{
            System.out.println("帳戶餘額: " + acc.getBalance());
        }
    }

    public void withdraw(Card card, double amount){
        Account acc = findAcc(card.getAccID());
        
        if(acc == null){
            System.out.println("錯誤，請確認帳戶正確");
        }else{
            if(acc.getBalance() < amount){
                System.out.println("餘額不足");
            }else{
                acc.withdraw(amount);
                System.out.println("提款成功");
            }
        }
    }
}
