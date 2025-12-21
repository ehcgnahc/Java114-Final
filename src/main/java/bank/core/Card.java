package bank.core;

public class Card {
    private String cardID;
    private String accID;
    private String bankID;
    // private String name;
    private boolean access;
    private boolean isUsing;

    // Card(String cardID, String accID, String name, String bankID){
    //     this.cardID = cardID;
    //     this.accID = accID;
    //     this.name = name;
    //     this.bankID = bankID;
    // }

    Card(String cardID, String accID, String bankID){
        this.cardID = cardID;
        this.accID = accID;
        this.bankID = bankID;
        this.isUsing = false;
    }

    // 新增ATM開卡流程
    void accessCard(){
        this.access = true;
    }

    void setStatus(boolean isUsing){
        this.isUsing = isUsing;
    }

    public String getCardID(){
        return this.cardID;
    }

    public String getAccID(){
        return this.accID;
    }

    public String getBankID(){
        return this.bankID;
    }

    // public String getName(){
    //     return name;
    // }

    public boolean isUsing(){
        return this.isUsing;
    }

    boolean getAccess(){
        return this.access;
    }
}
