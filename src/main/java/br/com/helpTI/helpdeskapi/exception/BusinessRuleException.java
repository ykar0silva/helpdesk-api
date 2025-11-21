package br.com.helpTI.helpdeskapi.exception;

// Classe simples de exceção para regras de negócio (o que o Service está lançando)
public class BusinessRuleException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BusinessRuleException(String msg) {
        super(msg);
    }
}