package br.com.brunoxkk0.servercore.api.task;

public interface ReturnableTask <T> extends IRunnableTask, IProgress{

    T getResult();

    boolean isResultReady();

}
