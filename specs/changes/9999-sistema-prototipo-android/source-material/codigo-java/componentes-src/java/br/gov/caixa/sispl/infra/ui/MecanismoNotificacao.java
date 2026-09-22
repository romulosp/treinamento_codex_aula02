package br.gov.caixa.sispl.infra.ui;

/**
 * Interface usada pelo menu para percorrer um lista de mecansimos de notificação.
 * @author Tiago de A. van den Berg
 * @see br.gov.caixa.sispl.infra.controle.ControleMenu
 */
public interface MecanismoNotificacao {
    /**
     * Retorna se o mecanismo possui uma notificação a ser lida pelo usuário.
     * @return <code>true</code> se houver uma notificação a ser lida, ou <code>false</code> caso contrário.
     */
    public boolean hasNotificacao();

    /**
     * Retorna se a notificação precisa ser lida obrigatoriamente, sem a intervenção do usuário
     * @return <code>true</code> se o usuário deve ler a notificação obrigatoriamente, ou <code>false</code> caso contrário.
     */
    public boolean isLeituraMandatoria();
    
    /**
     * Retorna se existe alguma pendencia com trataemnto especifico para o terminal.
     * @return <code>true</code> se existe pendencia com tratamento especifico
     */
    public boolean hasPendenciaTratamentoEspecifico();

    /**
     * Se o usuário desejar ler a notificação, esse método retorna o código de movimento de qual caso de uso deverá ser executado. 
     * @return o código de movimento de qual caso de uso deverá ser executado. 
     */
    public int getTipoMovimento();

    /**
     * Se o usuário desejar ler a notificação, esse método retorna a opção de um movimento do caso de uso que será executado. 
     * @returna opção de movimento do caso de uso que será executado. 
     */
    public int getOpcaoMovimento();

    /**
     * Retorna o titulo da mensagem que será mostrado ao usuário quando ele possuir notificações.
     * @return o título da mensagem mostrado ao usuário.
     */
    public String getTitulo();

    /**
     * Retorna a mensagem que será mostrada ao usuário quando ele possuir notificações.
     * @return a mensagem mostrada ao usuário.
     */
    public String getMensagem();
}
