package com.linearpast.sccore.core;

import java.util.concurrent.Callable;

@SuppressWarnings("UnusedReturnValue")
public interface IModLazyRun {
    default boolean testLoadedAndRun(Runnable runnable){
        if(testCondition()) runnable.run();
        else return false;
        return true;
    }

    default void testLoadedAndRun(Runnable runnable, Runnable elseRun){
        if(testCondition()) runnable.run();
        else elseRun.run();
    }

    default <T> T testLoadedAndCall(Callable<T> callable) {
        try {
            if(testCondition()) return callable.call();
        } catch (Exception ignored) {}
        return null;
    }

    default <T> T testLoadedAndCall(Callable<T> callable, Callable<T> elseCall) {
        try {
            if(testCondition()) return callable.call();
            else return elseCall.call();
        }catch(Exception ignored) {
            return null;
        }
    }

    boolean testCondition();
}
