package main;

import java.util.List;

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
        bankA.createAcc(User1, "password");
        bankB.createAcc(User1, "password");
        bankA.createAcc(User2, "password");
        bankB.createAcc(User2, "password");

        System.out.println("User1 目前持有的卡片數量: " + User1.getCard().size());

        // Card cardA = User1.getCardByBank("BankA");

        // User1.setCard(User1.getCard());
        // User2.setCard(Card1);
        
        // atmA.checkBalance();
        // atmA.deposit(1234);
        // atmA.login("null");
        // atmA.deposit(1234);

        List<PassBook> passBooks = User1.getPassBookListByBank("BankA");
        System.out.println("User1 在 BankA 有 " + passBooks.size() + " 本存摺");

        atmA.insertCard(User1.getCardByAccount("BankA", passBooks.get(0).getAccID()));
        atmA.login("password");
        atmA.checkBalance();
        atmA.deposit(1000);
        atmA.checkBalance();
        atmA.ejectCard();
        
        passBooks.get(0).printHistory();

        // atmA.transfer("BankB", User1.getPassBookByAccount("BankB", passBooks.get(0).getAccID()), 100);
        // atmA.checkBalance();

        // atmB.insertCard(User1.getCardByBank("BankB"));
        // atmB.login("password");
        // atmB.checkBalance();

        // atmA.ejectCard();
        // // atmA.login("3333");
        // // atmB.ejectCard();
        // atmA.checkBalance();

        // User1.getPassBookByBank("bankA").getHistory();

        // bug
        // atmA.insertCard(User2.getCard());
        
        // atmA.ejectCard();


        // bankNetwork.deposit(User1.getCard(), "password", 500);
        // bankNetwork.

        // myBank.deposit("TEST", 12333);
        // myBank.deposit("1233", 3333);
        // myBank.getBalance("TEST");
        // myBank.getBalance("1233");
    }
}