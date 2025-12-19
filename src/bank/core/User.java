package bank.core;

public class User {
    private String name;
    private double cash;
    private Card card;
    private String accID;

    public User(String name, double cash){
        this.name = name;
        this.cash = cash;
        this.card = null;
        this.accID = null;
    }

    public String getName(){
        return this.name;
    }
    
    void setCard(Card card){
        this.card = card;
        System.out.println(this.name + " 收到了卡片，卡號是: " + card.getCardID());
    }
    
    void setAccID(String accID){
        this.accID = accID;
    }

    public String getAccID(){
        return this.accID;
    }

    public Card getCard(){
        return this.card;
    }

    public double getCash(){
        return this.cash;
    }

    public void takeCash(double amount){
        this.cash += amount;
    }

    public boolean giveCash(double amount){
        if(this.cash < amount){
            System.out.println("現金不足");
            return false;
        }

        this.cash -= amount;
        return true;
    }
}