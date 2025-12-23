package bank.core;

import java.util.ArrayList;
import java.util.List;

public class PassBook {
    private String accID;
    private String bankID;
    private List<TransactionHistory> history;


    public PassBook(String accID, String bankID){
        this.accID = accID;
        this.bankID = bankID;
        this.history = new ArrayList<>();
    }

    public String getAccID(){
        return this.accID;
    }
    
    public String getBankID(){
        return this.bankID;
    }

    public List<TransactionHistory> getHistory(){
        return this.history;
    }

    void setHistory(List<TransactionHistory> fullHistory){
        this.history.clear();
        this.history.addAll(fullHistory);
    }

    void updateHistory(List<TransactionHistory> newHistory){
        this.history.addAll(newHistory);
    }

    public void printHistory(){
        System.out.println("=== 存摺帳號: " + accID + " (" + bankID + ") ===");
        if(history.isEmpty()){
            System.out.println("(無交易紀錄)");
        }
        for(TransactionHistory r: history){
            System.out.println(r);
        }
        System.out.println("================================");
    }
}