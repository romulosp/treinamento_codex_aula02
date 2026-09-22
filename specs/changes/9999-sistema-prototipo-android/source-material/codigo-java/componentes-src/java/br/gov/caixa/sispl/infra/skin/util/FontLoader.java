/*
 * Created on 02/06/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.gov.caixa.sispl.infra.skin.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * @author p532313
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FontLoader {
    // Tamanho default para o tamanho da fonte quando nao for setado
    private static int DEFAULT_FONT_SIZE = 12;

    private static FontLoader singleton = null;

    public static FontLoader getInstance() {
        if (singleton == null) {
            singleton = new FontLoader();
        }
        return singleton;
    }

    private HashMap hashMap = null;

    private FontLoader() {
        hashMap = new HashMap(500);
    }

    public Font getFont(String fontPath, int size) throws FontFormatException, IOException {
        String key = fontPath + "@" + String.valueOf(size);
        Font font = (Font) hashMap.get(key);
        if (font == null) {
            try {
                InputStream fontInputStream = this.getClass().getResourceAsStream(fontPath);
                if (size == 0) {
                    size = DEFAULT_FONT_SIZE;
                }
                font = Font.createFont(Font.TRUETYPE_FONT, fontInputStream).deriveFont((float)size);
                hashMap.put(key, font);
            } catch (Exception e) {
                font = new Font("Arial", 0, DEFAULT_FONT_SIZE);
            }
        }
        return font;
    }

}
