/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.jchat.core.server;

/**
 *
 * @author info
 */
public class AuthTimeOutException extends Exception {

    /**
     * Creates a new instance of <code>AuthTimeOutException</code> without
     * detail message.
     */
    public AuthTimeOutException() {
    }

    /**
     * Constructs an instance of <code>AuthTimeOutException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public AuthTimeOutException(String msg) {
        super("AuthTimeOutException:" + msg);
    }
}

/*
public class MyArrayDataException extends Exception {


    public MyArrayDataException() {
    }

    /**
     * Constructs an instance of <code>MyArrayDataException</code> with the
     * specified detail message.
     *
     * @param row - строка с "неправильным" элементом
     * @param col - колонка с "неправильным" элементом
     * @param msg the detail message.
     */
/*
    public MyArrayDataException(int row, int col, String msg) {
        super("Array Data Exception. Element:"+"["+row+"]"+"["+col+"] is not an Integer. "+"Element:"+msg);
    }
}

*/