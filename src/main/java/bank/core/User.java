package bank.core;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String name;
    private double cash;
    private List<Card> card;
    private List<PassBook> passBook;

    public User(String name, double cash){
        this.name = name;
        this.cash = cash;
        this.card = new ArrayList<>();
        this.passBook = new ArrayList<>();
    }

    public String getName(){
        return this.name;
    }
    
    void addCard(Card card){
        this.card.add(card);
        System.out.println(this.name + " 收到了新卡片，卡號是: " + card.getCardID() + " (發卡行: " + card.getBankID() + ")");
    }
    
    void addPassBook(PassBook passBook){
        this.passBook.add(passBook);
    }

    public List<PassBook> getPassBook(){
        return this.passBook;
    }

    public PassBook getPassBookByBank(String bankID){
        for(PassBook acc: passBook){
            if (acc.getBankID().equals(bankID)){
                return acc;
            }
        }
        return null;
    }

    public List<Card> getCard(){
        return this.card;
    }

    public Card getCardByBank(String bankID){
        for(Card c: card){
            if (c.getBankID().equals(bankID)){
                return c;
            }
        }
        return null;
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