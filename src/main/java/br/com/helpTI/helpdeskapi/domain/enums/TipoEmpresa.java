package br.com.helpTI.helpdeskapi.domain.enums;

public enum TipoEmpresa {
    MATRIZ,        // Nível 1: Vocês (Donos do Software - HelpTI)
    PRESTADORA,    // Nível 2: Empresas de TI parceiras (Seus clientes diretos)
    CLIENTE_FINAL  // Nível 3: Clientes da Prestadora (Empresas ou Pessoas que abrem chamados)
}