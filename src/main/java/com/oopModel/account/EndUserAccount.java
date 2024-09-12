package com.oopModel.account;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class
EndUserAccount {
    protected final String accountID;
    protected String username;
    protected String passwordHash;
    protected final byte[] salt;


    public EndUserAccount(String name, String password) throws NoSuchAlgorithmException {
        this.username = generateUsername(name); // username structure, eg: JohnSmith
        this.salt = generateSalt();
        this.passwordHash = createHash(password, true);
        this.accountID = createHash(name, false);
    }
    public EndUserAccount(String accountID, String username, String passwordHash, byte[] salt) {
        this.accountID = accountID;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public String getAccountID() {
        return accountID;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String name) {
        this.username = generateUsername(name);
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setNewPassword(String password) throws NoSuchAlgorithmException {
        this.passwordHash = createHash(password, true);
    }
    public byte[] getSalt() {
        return salt;
    }

    public String generateUsername(String name) {
        String username = null;
        for (int index=0;index<name.length();index++) {
            if (name.charAt(index) == ' ') {
                username = name.substring(0, index) + name.substring(index+1);
            }
        }
        return username;
    }

    //Parts of the methods generateSalt(), createHash() and compareToPassword are
    //borrowed code from: https://www.javainterviewpoint.com/java-salted-password-hashing/
    public byte[] generateSalt() {
        SecureRandom randomNum = new SecureRandom();
        byte[] saltBytes = new byte[5];
        randomNum.nextBytes(saltBytes);
        return saltBytes;
    }
    public String createHash(String itemToHash, boolean isPassword) throws NoSuchAlgorithmException {
        MessageDigest hashDigest = MessageDigest.getInstance("SHA-256");
        String hashedItem = null;
        if (isPassword) {hashDigest.update(salt);}

        byte[] hash = hashDigest.digest(itemToHash.getBytes(StandardCharsets.UTF_8));
        int stopHashingIndex = (isPassword) ? hash.length : 10;
        for (int index=0; index<stopHashingIndex; index++) {
            byte HashByte = hash[index];
            hashedItem += Integer.toHexString(Byte.toUnsignedInt(HashByte));
        }
        return (isPassword) ? hashedItem : hashedItem.substring(0, 10);
    }
    public boolean compareToPassword(String passWToCompare) throws NoSuchAlgorithmException {
        String passWToCompareHash = createHash(passWToCompare, true);
        boolean same = passWToCompareHash.equals(passwordHash);
        return same;
    }
}
