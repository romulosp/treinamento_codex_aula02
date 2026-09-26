/*
 * Created on 02/06/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.gov.caixa.sispl.infra.skin.util;

import java.awt.Image;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import br.gov.caixa.sispl.infra.util.log.Log;
import br.gov.caixa.sispl.infra.util.log.LogFactory;

/**
 * @author p532313
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ImageLoader {

    private static final Log log = LogFactory.getLog(ImageLoader.class);
    private static ImageLoader singleton = null;

    public static ImageLoader getInstance() {
        if (singleton == null) {
            singleton = new ImageLoader();
        }
        return singleton;
    }

    private HashMap hashMap = null;

    private ImageLoader() {
        hashMap = new HashMap(3500);
    }

    public Image getImage(String imagePath) {
        Image image = (Image) hashMap.get(imagePath);
        if (image == null) {
            try {
                image = ImageIO.read(this.getClass().getResource(imagePath));
                hashMap.put(imagePath, image);
            } catch (IOException e) {
                log.error("Erro na imagem",e);
            }
        }
        return image;
    }
}
