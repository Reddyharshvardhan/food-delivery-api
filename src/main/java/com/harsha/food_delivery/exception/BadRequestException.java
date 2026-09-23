package com.harsha.food_delivery.exception;

public class BadRequestException  extends  RuntimeException{

    public BadRequestException(String message){
        super(message);
    }
}
