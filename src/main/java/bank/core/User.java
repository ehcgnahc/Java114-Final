package bank.core;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String name;
    private double cash;
    private List<Card> card;
    private List<PassBook> passBooks;
    
    public User(String name, double cash){
        this.name = name;
        this.cash = cash;
        this.card = new ArrayList<>();
        this.passBooks = new ArrayList<>();
    }
    
    public String getName(){
        return this.name;
    }
    
    void addCard(Card card){
        this.card.add(card);
        System.out.println(this.name + " 收到了新卡片，卡號是: " + card.getCardID() + " (發卡行: " + card.getBankID() + ")");
    }

    public List<Card> getCard(){
        return this.card;
    }

    public List<Card> getCardListByBank(String bankID){
        List<Card> cardListByBank = new ArrayList<>();
        for(Card c: card){
            if(c.getBankID().equals(bankID)){
                cardListByBank.add(c);
            }
        }
        return cardListByBank;
    }
    
    public Card getCardByAccount(String bankID, String accID){
        for(Card c: card){
            if(c.getBankID().equals(bankID) && c.getAccID().equals(accID)){
                return c;
            }
        }
        return null;
    }

    void addPassBook(PassBook passBook){
        this.passBooks.add(passBook);
    }

    public List<PassBook> getPassBook(){
        return this.passBooks;
    }

    public List<PassBook> getPassBookListByBank(String bankID){
        List<PassBook> passBookListByBank = new ArrayList<>();
        for(PassBook acc: passBooks){
            if(acc.getBankID().equals(bankID)){
                passBookListByBank.add(acc);
            }
        }
        return passBookListByBank;
    }

    public PassBook getPassBookByAccount(String bankID, String accID){
        for(PassBook acc: passBooks){
            if(acc.getBankID().equals(bankID) && acc.getAccID().equals(accID)){
                return acc;
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