package main;

import bank.core.*;

public class Main {
    public static void main(String[] args){
        BankSystem bankNetwork = new BankSystem();

        Bank bankA = new Bank("BankA");
        Bank bankB = new Bank("BankB");

        bankNetwork.addBank(bankA);
        bankNetwork.addBank(bankB);
        
        bankNetwork.setCrossBankFee("BankA", "BankB", 5.0);
        bankNetwork.setCrossBankFee("BankB", "BankA", 15.0);

        ATM atmA = new ATM(bankNetwork, bankA);
        ATM atmB = new ATM(bankNetwork, bankB);

        User User1 = new User("ehcgnahc", 5000);
        User User2 = new User("ehcgnahc2", 5000);

        //帶修正 加入user
        bankA.createAcc(User1, "password");
        bankB.createAcc(User2, "password2");

        // User1.setCard(User1.getCard());
        // User2.setCard(Card1);

        // atmA.insertCard(Card1); //待修正
        
        // atmA.checkBalance();
        // atmA.deposit(1234);
        // atmA.login("null");
        // atmA.deposit(1234);
        atmA.insertCard(User1.getCard());
        atmA.login("password");
        atmA.checkBalance();
        atmA.deposit(1000);
        atmA.checkBalance();
        atmA.transfer("BankB", User2.getAccID(), 100);
        atmA.checkBalance();

        atmB.insertCard(User2.getCard());
        atmB.login("password2");
        atmB.checkBalance();


        // bug
        // atmA.login("3333");
        // atmA.insertCard(User2.getCard());
        // atmA.login("password2");
        // atmA.checkBalance();
        
        // atmA.ejectCard();


        // bankNetwork.deposit(User1.getCard(), "password", 500);
        // bankNetwork.

        // myBank.deposit("TEST", 12333);
        // myBank.deposit("1233", 3333);
        // myBank.getBalance("TEST");
        // myBank.getBalance("1233");
    }
}