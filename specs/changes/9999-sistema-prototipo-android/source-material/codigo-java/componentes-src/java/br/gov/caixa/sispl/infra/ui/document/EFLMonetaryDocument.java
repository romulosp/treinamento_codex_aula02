/*
 * Caixa Econômica Federal
 * SISPL -
 * Arquivo : EFLMonetaryDocument.java
 * Criaçao : 24/01/2006
 * Implementadores: Tiago de A. van den Berg, Luciano Bohnert
 */
package br.gov.caixa.sispl.infra.ui.document;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import br.gov.caixa.sispl.dominio.ConstantesParametros;
import br.gov.caixa.sispl.infra.propriedades.SuportePropriedades;
import br.gov.caixa.sispl.infra.util.log.Log;
import br.gov.caixa.sispl.infra.util.log.LogFactory;

/**
 * Classe responsável por restringir a entrada de texto em um textfield com valores monetários.
 *
 * @author p532313
 */
public class EFLMonetaryDocument implements EFLDocument {
    private static final Log log = LogFactory.getLog(EFLMonetaryDocument.class);

    // Constantes que representam a formatação da moeda.
    private static final char DIVISOR_CASAS_DECIMAIS = ',';
    private static final char SEPARADOR_MILHARES = '.';
    private static final char SINAL_NEGATIVO = '-';
    private static final String FORMATO_MONETARIO = "#,##0.00;#,##0.00 -";

    // Formatador da moeda usando as constantes mostradas acima.
    private DecimalFormat monetaryFormat;

    /**
     * Valor interno da reprensentação monetária. Atenção deve ser do tipo BigDecimal para evitar problemas de arrendondamento.
     * 
     * @see java.math.BigDecimal
     */
    private BigDecimal value;

    private int maxLength;

    /**
     * Cosntrutor padrão. Este construtor não restringe a quantidade de caracteres no campo.
     */
    public EFLMonetaryDocument() {
        value = new BigDecimal(0);
        maxLength = 0;

        DecimalFormatSymbols monetaryFormatSymbols = new DecimalFormatSymbols();
        //FIXME Rip out SuportePropriedades
        String currencySymbol = SuportePropriedades.getInstance().getParametro(ConstantesParametros.CODIGO_SIMBOLO_UNIDADE_MONETARIA);
        monetaryFormatSymbols.setCurrencySymbol(currencySymbol);
        monetaryFormatSymbols.setGroupingSeparator(SEPARADOR_MILHARES);
        monetaryFormatSymbols.setMinusSign(SINAL_NEGATIVO);
        monetaryFormatSymbols.setMonetaryDecimalSeparator(DIVISOR_CASAS_DECIMAIS);

        monetaryFormat = new DecimalFormat(FORMATO_MONETARIO, monetaryFormatSymbols);

    }

    /**
     * Construtor que restringe a quantidade de caracteres no campo.
     * 
     * @param length tamanho máximo do campo.
     */
    public EFLMonetaryDocument(int maxLength) {
        this();
        this.maxLength = maxLength;
    }

    /**
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#insertTextAtCursor(java.lang.String)
     */
    public void insertTextAtCursor(String keyText) {
        if ((keyText.length() == 1) && ((maxLength == 0) || (getMaskedText().length() < maxLength))) {
            char numberChar = keyText.charAt(0);
            // Evitar que caracteres alpha-numericos não sejam inseridos no campo.
            if ((numberChar >= '0') && (numberChar <= '9')) {
                // Utilizando BigDecimal para ShiftLeft o campo (x10) ...
                value = value.multiply(new BigDecimal(10));
                // ... e adicionar os centavos.
                value = value.add(new BigDecimal(new BigInteger(keyText), 2));
            }
        }
    }

    /**
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#deleteCharBeforeCursor()
     */
    public void deleteCharBeforeCursor() {
        // Divide o valor representado por 10 para fazer SHIFT RIGHT. ATENÇÃO: o modo de arredondamento tem
        // que ser ROUND_DOWN, senão não terá o comportamento esperado.
        value = value.divide(new BigDecimal(10), 2, BigDecimal.ROUND_DOWN);
    }

    /**
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getCursorPosition()
     */
    public int getCursorPosition() {
        // A posição do cursor é fixa no final do campo.
        return getMaskedText().length();
    }

    /**
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#setCursorPosition(int)
     */
    public void setCursorPosition(int cursorPosition) {
        // Não faz nada pois a posição do cursor é fixa no final do campo.
    }

    /**
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#setText(java.lang.String)
     */
    public void setText(String text) {
        // Transforma o texto na representacao interna.
        value = new BigDecimal(text);
    }

    /**
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getText()
     */
    public String getText() {
        return String.valueOf(value);
    }

    /**
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getMaskedText()
     */
    public String getMaskedText() {
        // Retorna a formatação mostrada no textfield.
        return monetaryFormat.format(value.doubleValue());
    }

    /**
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#isFinished()
     */
    public boolean isFinished() {
        return (maxLength != 0) && (getMaskedText().length() == maxLength);
    }

    /**
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getMinLength()
     */
    public int getMinLength() {
        // Quantidade minina de caracteres "0,00"
        return 4;
    }

    /**
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getMaxLength()
     */
    public int getMaxLength() {
        return maxLength;
    }
}
