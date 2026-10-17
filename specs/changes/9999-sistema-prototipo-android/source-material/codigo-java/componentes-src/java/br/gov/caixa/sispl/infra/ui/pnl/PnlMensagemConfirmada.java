package br.gov.caixa.sispl.infra.ui.pnl;

import java.awt.event.ActionEvent;

import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.timeout.GerenteTimeoutUI;
import br.gov.caixa.sispl.infra.ui.EFLButton;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.infra.ui.SwingWorkerActionListener;
import br.gov.caixa.sispl.infra.ui.TypeCase;
import br.gov.caixa.sispl.infra.ui.UIAbstractControle;

/****
 * O OBJETIVO DESSE PAINEL E MELHORAR INTERAÇÃO ENTRE OPERADOR E SISTEMA. O
 * PAINEL APRESENTA UMA MENSAGEM CONFIRMADA. OPERADOR É OBRIGADO A DÁ O OKEY
 * CONFIRMANDO QUE LEU E A MENSAGEM DE RETORNO DO SISTEMA. INSTANCIA PAINEL
 * INFORMATIVO COM A MENSAGEM E ÍCONE/TITULO. A INFORMAÇÃO DEVE APRESENTAR O
 * RESULTADO DO PROCESSAMENTO/REQUISIÇÃO. APRESENTAR UM BOTAO DE OK(ENTENDI O
 * QUE FOI LIDO ). O BOTÃO DE OK EXECUTA O MÉTODO executarTeclaOKMsgConfirmada
 * DO UIAbstractControle QUE POR SUA VEZ EXECUTA OS MÉTODOS
 * apresentaMsg(mensagem) ; E fim(); A CLASSE(EXTENDIDA DE UIAbstractControle)
 * QUE NECESSITAR PODE SOBREESCREVER O MÉTODO executarTeclaOKMsgConfirmada
 * CONFORME SUA NECESSIDADE.
 * 
 * @author f595054
 *
 */
public class PnlMensagemConfirmada extends EFLPanel {

	private static final long serialVersionUID = 2007080901L;

	// Label que apresenta a titulo.
	private EFLLabel labelTituloPrincipal = null;

	private EFLPanel pnlTituloPrincipal = null;

	// Label que apresenta a mensagem.
	private EFLLabel labelMensagem = null;

	// Botao de Afirmação: Leva a listagem de Notificações
	private EFLButton teclaOK = null;

	private UIAbstractControle uiAbstractControle;

	private String mensagem;

	private String titulo;

	private boolean apresentarMensagemRodape;

	
	/***
	 * A CLASSE(EXTENDIDA DE UIAbstractControle) QUE NECESSITAR PODE SOBREESCREVER O
	 * MÉTODO executarTeclaOKMsgConfirmada CONFORME SUA NECESSIDADE.
	 * 
	 * @param ui
	 * @param mensagem
	 * @param titulo
	 */
	public PnlMensagemConfirmada(UIAbstractControle ui, String mensagem, String titulo) {
		super(PanelSkin.PANEL_EFL);
		GerenteTimeoutUI.getInstance().stop();
		this.uiAbstractControle = ui;
		this.mensagem = mensagem != null && !mensagem.isEmpty() ? mensagem.toUpperCase() : mensagem;
		this.titulo = titulo;
		initialize();
	}
	
	public PnlMensagemConfirmada(final UIAbstractControle ui, final String mensagem, final String titulo,
			final TypeCase typeCase) {
		super(PanelSkin.PANEL_EFL);
		GerenteTimeoutUI.getInstance().stop();
		this.uiAbstractControle = ui;

		this.mensagem = mensagem;

		this.titulo = titulo;
		initialize();
	}

	
	public PnlMensagemConfirmada(UIAbstractControle ui, String mensagem, String titulo,boolean apresentarMensagemRodape) {
		 this(ui, mensagem, titulo);
		 this.apresentarMensagemRodape = apresentarMensagemRodape;
	}
	
	
	public PnlMensagemConfirmada(UIAbstractControle ui) {
		this.uiAbstractControle = ui;
	}

	public PnlMensagemConfirmada(UIAbstractControle ui, String mensagem, String titulo,final SwingWorkerActionListener swingWorkerActionListener) {
		this(ui, mensagem, titulo);
		getTeclaOK().addActionListener(new SwingWorkerActionListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformedInBackground(ActionEvent actionEvent) {
				getTeclaOK().setEnabled(false);
				swingWorkerActionListener.actionPerformed(actionEvent);
			}
		});
	}

	public PnlMensagemConfirmada(final UIAbstractControle ui, final String mensagem, final String titulo,
			final SwingWorkerActionListener swingWorkerActionListener, final TypeCase typeCase) {
		this(ui, mensagem, titulo, typeCase);
		getTeclaOK().addActionListener(new SwingWorkerActionListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformedInBackground(final ActionEvent actionEvent) {
				getTeclaOK().setEnabled(false);
				swingWorkerActionListener.actionPerformed(actionEvent);
			}
		});
	}
	
	public PnlMensagemConfirmada(UIAbstractControle ui, String mensagem, String titulo,final SwingWorkerActionListener swingWorkerActionListener,boolean apresentarMensagemRodape) {
		this(ui, mensagem, titulo, swingWorkerActionListener);
		this.apresentarMensagemRodape = apresentarMensagemRodape;
	}


	/**
	 * Inicializa componentes gráficos do Dialog
	 *
	 */
	public void initialize() {
		this.add(getPnlTituloPrincipal());
		this.add(getLabelMensagem());
		this.add(getTeclaOK());
	}

	protected EFLLabel getLabelTituloPrincipal() {
		if (labelTituloPrincipal == null) {
			labelTituloPrincipal = new EFLLabel(LabelSkin.LABEL_FAIXA_TITULO_PRINCIPAL, this.titulo, EFLLabel.RIGHT);
			labelTituloPrincipal.setLocation(0, 0);
			labelTituloPrincipal.setSize(750, 28);
		}
		return labelTituloPrincipal;
	}

	private EFLPanel getPnlTituloPrincipal() {
		if (pnlTituloPrincipal == null) {
			pnlTituloPrincipal = new EFLPanel(PanelSkin.PANEL_FAIXA_TITULO_PRINCIPAL);
			pnlTituloPrincipal.setLocation(0, 100);
			pnlTituloPrincipal.setSize(800, 28);
			pnlTituloPrincipal.setVisible(true);
			pnlTituloPrincipal.add(getLabelTituloPrincipal());
		}
		return pnlTituloPrincipal;
	}

	/**
	 * Método responsável por criar o Label que contém a pergunta a ser respondida.
	 * 
	 * @return EFLLabel Label que contém a pergunta a ser respondida.
	 */
	protected EFLLabel getLabelMensagem() {
		if (labelMensagem == null) {
			labelMensagem = new EFLLabel(LabelSkin.LABEL_TITULO_1_PIX, this.mensagem);
			labelMensagem.setHorizontalAlignment(EFLLabel.CENTER);
			labelMensagem.setSize(800, 300);
			labelMensagem.setLocation((800 - labelMensagem.getWidth()) / 2, (600 - labelMensagem.getHeight()) / 2);
		}
		return labelMensagem;

	}

	/**
	 * Método responsável por criar o Botao de Afirmação que leva a tela de Menu
	 * 
	 * @return EFLButton Botao que leva a tela de menu
	 */
	@SuppressWarnings("serial")
	private EFLButton getTeclaOK() {
		if (teclaOK == null) {
			teclaOK = new EFLButton(uiManager, ButtonSkin.BOTAO_CONFIRMAR, "Ok, entendi");
			teclaOK.setSize(80, 60);
			teclaOK.setLocation((800 - teclaOK.getWidth()) / 2,
					(600 - teclaOK.getHeight() + getLabelMensagem().getHeight()) / 2);
			teclaOK.setDefaultButton();
			teclaOK.addActionListener(new SwingWorkerActionListener() {
				public void actionPerformedInBackground(ActionEvent e) {
					teclaOK.setEnabled(false);
					teclaOK.repaint();
					if(apresentarMensagemRodape) {
						uiAbstractControle.executarTeclaOKMsgConfirmada(mensagem);
					}else {
						uiAbstractControle.executarTeclaOKMsgConfirmada();
					}
				}
			});
		}
		return teclaOK;
	}

}
