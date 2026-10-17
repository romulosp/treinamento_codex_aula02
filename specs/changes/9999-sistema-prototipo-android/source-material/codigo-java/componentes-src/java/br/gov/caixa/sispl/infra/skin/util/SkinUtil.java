package br.gov.caixa.sispl.infra.skin.util;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.SwingConstants;

/**
 * Classe responsável por definir o algoritimo de separação de linhas por quebras definidas.
 * @author p532313
 */
public class SkinUtil {
    public static final int TEXTBOX_BORDER = 10;

    /**
     * 
     * @param g
     * @param alignment
     * @param text
     * @param dimension
     */
    public static void alignText(Graphics g, int alignment, String text, double spacement, FontMetrics metricaFont, Dimension dimension) {

        //Verifica se há necessidade de escrever algo.
        if (text == null || text.equals("")) {
            return;
        }

        ArrayList listaLinhas = new ArrayList();

        StringTokenizer breakSeparatorTokenizer = new StringTokenizer(text, "\n", false);
        while (breakSeparatorTokenizer.hasMoreTokens()) {
            StringTokenizer spaceSeparatorTokenizer = new StringTokenizer(breakSeparatorTokenizer.nextToken(), " ", true);
            String linha = spaceSeparatorTokenizer.nextToken();
            do {
                if (spaceSeparatorTokenizer.hasMoreTokens()) {
                    String proximaPalavra = spaceSeparatorTokenizer.nextToken();
                    if (metricaFont.stringWidth(linha) + metricaFont.stringWidth(proximaPalavra) <= (dimension.getWidth() - TEXTBOX_BORDER)) {
                        linha += proximaPalavra;
                    } else {
                        listaLinhas.add(linha);
                        linha = proximaPalavra;
                    }
                }
                if (!spaceSeparatorTokenizer.hasMoreTokens()) {
                    listaLinhas.add(linha);
                }
            } while (spaceSeparatorTokenizer.hasMoreTokens());
        }

        //Desenha as linhas a serem impressas.
        //int firstLineTop = (int) (dimension.getHeight() + (metricaFont.getHeight() / 2) - (metricaFont.getHeight() * (listaLinhas.size() - 1))) / 2;
        int firstLineTop = (int) (dimension.getHeight() + (metricaFont.getAscent() - metricaFont.getDescent()) - (metricaFont.getHeight() * spacement * (listaLinhas.size() - 1))) / 2;
        //((dimension.getHeight() / 2) - rect.getCenterY()
        //int firstLineTop = (int) (dimension.getHeight()  / 2) + ((metricaFont.getHeight() - metricaFont.getLeading()) / 2);

        int countLinhas = 0;
        Iterator iter = listaLinhas.iterator();
        while (iter.hasNext()) { //Desenha todos os tokens presentes na List
            //Recupera a nova linha a imprimir
            String linhaAImprimir = (String)iter.next();

            //Define o alinhamento
            int textPosition = 0;
            if (alignment == SwingConstants.CENTER) {
                textPosition = ((int) dimension.getWidth() - metricaFont.stringWidth(linhaAImprimir)) / 2;
            } else if (alignment == SwingConstants.RIGHT) {
                textPosition = (int) dimension.getWidth() - metricaFont.stringWidth(linhaAImprimir);
            }
            //Desenha a linha.
            g.drawString(linhaAImprimir, textPosition, (int) (firstLineTop + ((double) countLinhas * (double) metricaFont.getHeight() * spacement)));
            //g.drawString(linhaAImprimir, textPosition, firstLineTop);
            //g.drawString(linhaAImprimir, textPosition, firstLineTop2);
            countLinhas++;
        }
    }
}
