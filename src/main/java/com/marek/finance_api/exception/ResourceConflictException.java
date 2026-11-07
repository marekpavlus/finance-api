package com.marek.finance_api.exception;

public class ResourceConflictException extends RuntimeException
{
  public ResourceConflictException(String message)
  {
    super(message);
  }
}