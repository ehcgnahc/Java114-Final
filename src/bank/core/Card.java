package bank.core;

class Card {
    private String cardID;
    private String accID;
    private String bankID;
    // private String name;
    private boolean access;

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
    }

    // 新增ATM開卡流程
    void accessCard(){
        this.access = true;
    }

    public String getCardID(){
        return cardID;
    }

    public String getAccID(){
        return accID;
    }

    public String getBankID(){
        return bankID;
    }

    // public String getName(){
    //     return name;
    // }

    boolean getAccess(){
        return access;
    }
}
