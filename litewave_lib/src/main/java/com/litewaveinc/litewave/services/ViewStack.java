package com.litewaveinc.litewave.services;

import android.content.Intent;

import java.util.Stack;

/**
 * Created by davidanderson on 11/3/15.
 */
public class ViewStack {

    public static Stack<Class<?>> parents= new Stack<Class<?>>();


    public static void push(Class intentClass) {
        parents.push(intentClass);
    }

    public static Class pop() {
        return parents.pop();
    }
}
