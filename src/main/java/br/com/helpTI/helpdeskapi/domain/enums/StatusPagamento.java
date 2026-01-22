package br.com.helpTI.helpdeskapi.domain.enums;

public enum StatusPagamento {
    
    PENDENTE(0, "PENDENTE"),
    PAGO(1, "PAGO");

    private Integer codigo;
    private String descricao;

    private StatusPagamento(Integer codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    // Método para converter código numérico (do banco) para o Enum
    public static StatusPagamento toEnum(Integer cod) {
        if (cod == null) {
            return null;
        }

        for (StatusPagamento x : StatusPagamento.values()) {
            if (cod.equals(x.getCodigo())) {
                return x;
            }
        }

        throw new IllegalArgumentException("Status de pagamento inválido: " + cod);
    }
}