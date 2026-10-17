package br.gov.caixa.sispl.infra.ui;

import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.controle.monitoraefl.Servidor;

/**
 * Label utilizado na monitoração da rede na EFL. Este label é composto por uma
 * imagem, utilizada para indicar o status do serviço monitorado, e por um label
 * que contém o nome do serviço. Além disso, cada instância desta classe registra-se
 * com uma instância de <code>br.gov.caixa.sispl.infra.controle.monitoraefl.Servidor</code>,
 * que representa o serviço monitorado. Este registro e a monitoração são realizados
 * com base no padrão de projeto observer.
 *
 * @see	java.util.Observer
 * @see	java.util.Observable
 *
 * @author p543430	(Alexandre Gomes de Lima - alexlouco@zipmail.com.br)
 * @author <a href="http://www.paulojeronimo.com">Paulo Jerônimo</a>
 *
 */
public class MonitorLabel extends JComponent implements Observer {
    private static final long serialVersionUID = 1L;

    //private static final Log log = LogFactory.getLog(MonitorLabel.class);

    /**
     * Imagem que representa o status OK, ou seja, que o 
     * serviço é alcançável via rede. 
     */
    public static final String OK = "/azul/led_verde.png";

    /**
     * Imagem que representa o estado EM MONITORAÇÃO, ou seja, que 
     * está se verificando se o serviço é alcançável ou não.
     */
    public static final String EM_MONITORACAO = "/azul/led_amarelo.png";

    /**
     * Imagem que representa o estado de NÃO OK, ou seja, que o 
     * serviço não é alcançável via rede.
     */
    public static final String NOK = "/azul/led_vermelho.png";

    /** Label com a descrição do serviço monitorado */
    private EFLLabel textLabel;
    /** Label com a imagem que indica o status */
    //TODO : mudar para EFLLabel assim que esta classe implementar a utilização de Icons.
    private JLabel iconLabel;

    /**
     * Cria uma instância desta classe.
     * 
     * @param name		Nome do serviço monitorado.
     * @param servidor	Objeto que representa o host do serviço. Além disso, é com 
     * 					este objeto que a instância criada irá se registrar para receber 
     * 					notificações de alteração do status do serviço.
     */
    public MonitorLabel(String name, Servidor servidor) {
        super();

        textLabel = new EFLLabel(LabelSkin.LABEL_CABECALHO_EFL, name);
        iconLabel = new JLabel();

        // estado inicial (em monitoração)
        ImageIcon icon = new ImageIcon(getClass().getResource(EM_MONITORACAO));
        iconLabel.setIcon( icon );

        //define tamanho e posicionamento dos labels
        iconLabel.setSize(icon.getIconWidth(), icon.getIconHeight());
        iconLabel.setLocation(0, 5);
        textLabel.setLocation(iconLabel.getX() + iconLabel.getWidth() + 5, 0);

        add(iconLabel);
        add(textLabel);

        servidor.addObserver(this);
    }

    /**
     * Define o tamanho deste componente.
     * 
     * @param	width	Largura em pixels.
     * @param	height	Altura em pixels.
     */
    public void setSize(int width, int height) {
        //ajustando o tamando do label texto
        if ( width - iconLabel.getWidth() > 0 ) {
            textLabel.setSize(width - iconLabel.getWidth(), height);
        }
        //ajustando o tamanho deste componente
        super.setSize(width, height);
    }

    /**
     * Define o tamanho deste componente.
     * 
     * @param	dimension	Dimensões desejadas para o componente.
     */
    public void setSize(Dimension dimension) {
        setSize(dimension.width, dimension.height);
    }

    /**
     * Atualiza o status do serviço monitorado, modificando a imagem que indica 
     * este status.
     * 
     * @param	o	 	Uma instância de <code>br.gov.caixa.sispl.infra.controle.monitoraefl.Servidor</code>.
     * 					É deste objeto que se obterá o status do serviço monitorado.
     * @param	arg		Não é utilizado, podendo ser <code>null</code>.
     */
    public void update(Observable o, Object arg) {
        Servidor servidor = (Servidor) o;
        switch (servidor.getStatus()) {
        case Servidor.EM_MONITORACAO:
            iconLabel.setIcon(new ImageIcon(getClass().getResource(EM_MONITORACAO)));
            break;
        case Servidor.NOK:
            iconLabel.setIcon(new ImageIcon(getClass().getResource(NOK)));
            break;
        case Servidor.OK:
            iconLabel.setIcon(new ImageIcon(getClass().getResource(OK)));
            break;
        }
    }
}