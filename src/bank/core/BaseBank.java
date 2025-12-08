package bank.core;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseBank {
    private String bankName;

    public BaseBank(String name){
        this.bankName = name;
    }

    public String getBankName(){
        return bankName;
    }

    protected Map<String, Account> accMap = new HashMap<>();
    
    public Card createAcc(String accID){
        Account newAcc = new Account(accID);
        accMap.put(accID, newAcc);

        String newCardID = "CARD-" + accID;
        Card newCard = new Card(newCardID, accID, bankName);
        
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

    public void deposit(String accID, double amount){
        Account acc = findAcc(accID);
        
        if(acc == null){
            System.out.println("錯誤，請確認帳戶輸入正確");
        }else{
            acc.deposit(amount);
            System.out.println("存款成功");
        }
    }

    public void withdraw(String accID, double amount){
        Account acc = findAcc(accID);
        
        if(acc == null){
            System.out.println("錯誤，請確認帳戶輸入正確");
        }else{
            if(acc.getBalance() < amount){
                System.out.println("餘額不足");
            }else{
                acc.withdraw(amount);
                System.out.println("提款成功");
            }
        }
    }

    public void getBalance(String accID){
        Account acc = findAcc(accID);

        if(acc == null){
            System.out.println("錯誤，請確認帳戶輸入正確");
        }else{
            System.out.println("帳戶餘額: " + acc.getBalance());
        }
    }
}
