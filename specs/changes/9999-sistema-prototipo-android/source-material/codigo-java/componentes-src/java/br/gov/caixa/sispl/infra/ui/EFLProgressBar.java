package br.gov.caixa.sispl.infra.ui;

import java.awt.Dimension;

import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;

/**
 * Barra de progresso da efl.
 *
 * Sobre o funcionamento:
 * O ProgressBar utiliza um sistema de progresso da seguinte forma:
 *  - Existe um nível de progresso atual e um nível de progresso máximo
 *  - A barra de progresso cresce utilizando uma proporcionalidade entre o progresso atual e máximo
 *  - Existem 3 formas de apresentação do texto (v1.0 - 18/09/2006):
 *    * Sem apresentação de texto
 *    * Em porcentagem (xx%)
 *    * Em passos (xx de yy)
 *  - A cada passo de um progresso deve-se chamar o método incrementaProgresso()
 *  - Não é preciso definir o tamanho da barra de progressos, ele já possui um tamanho padrão que é igual ao
 *    tamanho da barra de Status da EFL
 *
 * Observações importantes:
 *  - Não é permitido modificar o tamanho do ProgressBar após sua criação por isso o método setSize()
 *    lançará uma RuntimeException e o método setBounds() não funcionará corretamente.
 *
 * @author p543423 - Paulo Trigueiro Jr
 * @version 1.0 - 18/09/2006
 */
public class EFLProgressBar extends EFLPanel {
	private static final long serialVersionUID = 2007110801L;
	private EFLLabel lblBarraProgresso;
    private EFLLabel lblStatus;

    /** O valor atual que representa uma porcentagem do valor máximo */
    private int valor;
    
    /**Indica o valor máximo que representa 100% do progresso.*/
    private int valorMaximo;
    
    /**Indica uma borda para iniciar a barra de progresso de.*/
    private int bordas;

    /**
     * Construtor
     * @param valor progresso inicial
     * @param valorMaximo progresso máximo que poderá ser alcançado
     * @throws IllegalArgumentException Quando o progresso for menor ou igual a zero ou a altura indicada for menor que a mínima
     */
    public EFLProgressBar(int valor, int valorMaximo, int bordas) {
        super(PanelSkin.PANEL_PROGRESS_BAR);

        if (valor < 0) {
            throw new IllegalArgumentException("O valor deve ser maior ou igual a zero");
        } 

        if (valorMaximo <= 0) {
            throw new IllegalArgumentException("O valorMaximo deve ser maior que zero");
        } 

        if (valor > valorMaximo) {
            throw new IllegalArgumentException("O valornao pode ser maior que o valor maximo.");
        } 

        if (bordas < 0) {
            throw new IllegalArgumentException("As bordas devem ser maior ou igual a zero");
        } 

        this.valor = valor;
        this.valorMaximo = valorMaximo;
        this.bordas = bordas;
        
        initialize();
    }

    /**
     * Inicializa visualmente o componente
     */
    private void initialize() {
        this.add(getLblStatus());
        this.add(getLblBarraProgresso());
    }

    /**
     * Componente que preenche o progress bar
     * 
     * @return barra de progresso
     */
    private EFLLabel getLblBarraProgresso() {
        if (lblBarraProgresso == null) {
            lblBarraProgresso = new EFLLabel(LabelSkin.LABEL_TABELA_TITULO_2);
            lblBarraProgresso.setLocation(0 ,0);
            lblBarraProgresso.setSize(getWidth(), getHeight());
        }
        return lblBarraProgresso;
    }

    /**
     * Componente que mostra a parte escrita do progress bar
     * 
     * @return label de status
     */
    private EFLLabel getLblStatus() {
        if (lblStatus == null) {
            lblStatus = new EFLLabel(LabelSkin.LABEL_PROGRESS_BAR);
            lblStatus.setHorizontalAlignment(EFLLabel.CENTER);
            lblStatus.setSize(this.getSize());
            lblStatus.setLocation(0, 0);
            atualiza();
        }
        return lblStatus;
    }

    private void atualiza() {
        int percentual = (valor * 100) / valorMaximo;
        if((bordas * 2) > this.getWidth()) {
        	getLblBarraProgresso().setLocation(0, 0);
            getLblBarraProgresso().setSize((valor * this.getWidth()) / valorMaximo, this.getHeight());
        } else {
        	getLblBarraProgresso().setLocation(bordas, 0);
        	getLblBarraProgresso().setSize((valor * (this.getWidth() - (bordas * 2))) / valorMaximo, this.getHeight());
        }
        
        //atualiza a barra de progresso
        getLblStatus().setSize(this.getSize());
        getLblStatus().setText(percentual + "%");

        this.repaint();
	}

    /**
     * Retorna o passo em que o progresso está.
     * @return O progresso atual.
     */
    public int getValor() {
        return valor;
    }
    
    public void setValor(int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("O valor deve ser maior ou igual a zero");
        } 

        if (valor > valorMaximo) {
            throw new IllegalArgumentException("O valornao pode ser maior que o valor maximo.");
        } 
		this.valor = valor;
		atualiza();
	}

    /**
     * Retorna o passo em que o progresso está.
     * @return O progresso atual.
     */
    public int getValorMaximo() {
        return valorMaximo;
    }
    
    public void setValorMaximo(int valorMaximo) {
        if (valorMaximo <= 0) {
            throw new IllegalArgumentException("O valorMaximo deve ser maior que zero");
        } 

        if (valor > valorMaximo) {
            throw new IllegalArgumentException("O valornao pode ser maior que o valor maximo.");
        } 

        this.valorMaximo = valorMaximo;
    	atualiza();
	}
    
    public void setSize(Dimension d) {
    	super.setSize(d);
    	atualiza();
    }
    
    public void setSize(int width, int height) {
    	super.setSize(width, height);
    	atualiza();
    }

}