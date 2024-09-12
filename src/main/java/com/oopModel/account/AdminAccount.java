package com.oopModel.account;

import java.security.NoSuchAlgorithmException;

public class AdminAccount extends EndUserAccount {
    private String name;

    public AdminAccount(String name, String password) throws NoSuchAlgorithmException {
        super(name, password);
        this.name = name;
    }
    public AdminAccount(String accountID, String username, String passwordHash, byte[] salt, String name) {
        super(accountID, username, passwordHash, salt);
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
        this.username = generateUsername(name);
    }


}
