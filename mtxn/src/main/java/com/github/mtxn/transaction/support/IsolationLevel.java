package com.github.mtxn.transaction.support;


public enum IsolationLevel {
    DEFAULT("DEFAULT", -1),
    TRANSACTION_NONE("TRANSACTION_NONE", 0),
    TRANSACTION_READ_UNCOMMITTED("TRANSACTION_READ_UNCOMMITTED", 1),
    TRANSACTION_READ_COMMITTED("TRANSACTION_READ_COMMITTED", 2),
    TRANSACTION_REPEATABLE_READ("TRANSACTION_REPEATABLE_READ", 4),
    TRANSACTION_SERIALIZABLE("TRANSACTION_SERIALIZABLE", 8);
    private final int value;
    private final String name;

    IsolationLevel(String name, int value) {
        this.value = value;
        this.name = name;
    }


    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

}
