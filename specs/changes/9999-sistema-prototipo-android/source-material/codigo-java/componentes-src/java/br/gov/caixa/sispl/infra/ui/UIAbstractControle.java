/*
 * Caixa Econômica Federal SISPL - Arquivo : PnlMensagem.java Criaçao : 31/08/2002 Implementador: Alexandre Vieira Rebouças
 */
package br.gov.caixa.sispl.infra.ui;

import java.math.BigDecimal;

import br.gov.caixa.sispl.dominio.CodigoErro;
import br.gov.caixa.sispl.dominio.ImpressaoVO;
import br.gov.caixa.sispl.dominio.MotivoCancelamento;
import br.gov.caixa.sispl.dominio.Movimento;
import br.gov.caixa.sispl.dominio.OfertaCliente;
import br.gov.caixa.sispl.dominio.Pendencia;
import br.gov.caixa.sispl.dominio.RegraNegocioException;
import br.gov.caixa.sispl.dominio.TipoMovimento;
import br.gov.caixa.sispl.dominio.Usuario;
import br.gov.caixa.sispl.dominio.financeiros.FabricaDeValidadoresDeCartao;
import br.gov.caixa.sispl.dominio.financeiros.ListaOfertaCliente;
import br.gov.caixa.sispl.infra.controle.AbstractControleMovimento;
import br.gov.caixa.sispl.infra.controle.ControleMenu;
import br.gov.caixa.sispl.infra.controle.ControleMovimento;
import br.gov.caixa.sispl.infra.controle.ControleMovimentoException;
import br.gov.caixa.sispl.infra.controle.FabricaControleMovimento;
import br.gov.caixa.sispl.infra.controle.InfoMovimento;
import br.gov.caixa.sispl.infra.executor.ExecutorException;
import br.gov.caixa.sispl.infra.executor.GerenteTransacao;
import br.gov.caixa.sispl.infra.meiosdepagamento.uc.meiosdepagamento.enums.MeioDePagamentoOpcaoMovimentosEnum;
import br.gov.caixa.sispl.infra.meiosdepagamento.uc.meiosdepagamento.ui.IAtendimentoConfirmeListener;
import br.gov.caixa.sispl.infra.meiosdepagamento.uc.meiosdepagamento.ui.IMeiosDePagamentoListener;
import br.gov.caixa.sispl.infra.meiosdepagamento.uc.meiosdepagamento.ui.UIMeiosDePagamentoDefaultControle;
import br.gov.caixa.sispl.infra.perifericos.GerenciaPerifericos;
import br.gov.caixa.sispl.infra.perifericos.GerentePerifericosPadrao;
import br.gov.caixa.sispl.infra.perifericos.event.KeyboardListener;
import br.gov.caixa.sispl.infra.perifericos.servicos.display.mensagens.DefaultMessage;
import br.gov.caixa.sispl.infra.propriedades.SuportePropriedades;
import br.gov.caixa.sispl.infra.relatorio.RelatorioEFL;
import br.gov.caixa.sispl.infra.timeout.GerenteTimeoutUI;
import br.gov.caixa.sispl.infra.timeout.GerenteTimeoutUIListener;
import br.gov.caixa.sispl.infra.ui.atendimento.validadores.ValidadorCartao;
import br.gov.caixa.sispl.infra.ui.ofertacliente.IModuloOfertaListener;
import br.gov.caixa.sispl.infra.ui.pnl.PnlConfirmarResposta;
import br.gov.caixa.sispl.infra.ui.pnl.PnlMensagem;
import br.gov.caixa.sispl.infra.ui.pnl.PnlMensagemConfirmada;
import br.gov.caixa.sispl.infra.ui.pnl.PnlSenSup;
import br.gov.caixa.sispl.infra.util.AbstractCode;
import br.gov.caixa.sispl.infra.util.PerifericoUtil;
import br.gov.caixa.sispl.infra.util.log.Log;
import br.gov.caixa.sispl.infra.util.log.LogFactory;
import br.gov.caixa.sispl.util.MensagemErro;
import br.gov.caixa.sispl.util.Messages;

/**
 * Classe abstrata, ancestral de todas as classes de controle de UI's do sistema
 * SISPL.
 *
 * @author Alexandre Vieira Rebouças
 */
public abstract class UIAbstractControle implements UIEFL, ControleMovimento, GerenteTimeoutUIListener {

	private static final Log log = LogFactory.getLog(UIAbstractControle.class);
	
	private static final String MSG_PINPAD_REMOVE_CARTAO = "RETIRE O CARTAO";

	private final EFLFrame eflFrame;

	/**
	 * dados abstratos do movimento de oferta originalizados pela infra
	 */
	private OfertaCliente ofertaCli;
	private ListaOfertaCliente listaOfertaCli;
	private IModuloOfertaListener ofertaListener;
	private boolean validaTrocaTipoConcurso = false;

	/**
	 * Referência para o objeto que efetiva as movimentações.
	 */
	private GerenteTransacao gerenteTransacao;
	private SuportePropriedades suportePropriedades;

	/** Listener utilizado na execução do último movimento */
	protected AbstractControleMovimento abstCtrlMov;
	private IAtendimentoConfirmeListener atendimentoListener;
	
	public String ATENDIMENTO_TITULO = Messages.getString("TITULO_CONTRATA_MEIOS_DE_PAGAMENTOS");

	public UIAbstractControle() {
		eflFrame = EFLFrame.getInstance();
	}

	public boolean isValidaTrocaTipoConcurso() {
		return validaTrocaTipoConcurso;
	}

	public void setValidaTrocaTipoConcurso(boolean validaTrocaTipoConcurso) {
		this.validaTrocaTipoConcurso = validaTrocaTipoConcurso;

	}
	/**
	 * Define um novo controle de movimento
	 * 
	 * @param controle
	 *            Novo controle de movimento
	 * @roseuid 3D70E92C0003
	 */
	public void setControle(final AbstractControleMovimento controle) {
		abstCtrlMov = controle;
	}

	/**
	 * Define uma nova tela a ser visualizada na EFL. Dispara a inicialização do
	 * timeout de tela.
	 * 
	 * @param panel
	 *            Nova tela a ser apresentada na EFL.
	 */
	public void setPanelVisualizado(final EFLPanel panel) {
		setPanelVisualizado(panel, this);
	}

	public void setPanelVisualizado(final EFLPanel panel, final GerenteTimeoutUIListener listener) {
		if (log.isDebugEnabled())
			log.debug("INTERFACE: TELA [" + panel.getClass().getName() + "]");
		this.getEFL().setPanel(panel);
		GerenteTimeoutUI.getInstance().start(listener, panel);
	}

	/**
	 * Apresenta uma mensagem na tela da EFL
	 * 
	 * @param mensagem
	 *            Mensagem a ser apresentada na tela da EFL
	 * @roseuid 3D63AA2903C9
	 */
	public void apresentaMsg(String mensagem) {
		if (mensagem == null) {
			mensagem = "";
		}

		if (mensagem.length() != 0) {
			log.debug("INTERFACE: MENSAGEM [" + mensagem + "]");
		}

		this.getEFL().setMensagem(mensagem);
	}

	/***
	 * A CLASSE(EXTENDIDA DE UIAbstractControle) QUE NECESSITAR PODE SOBREESCREVER O
	 * MÉTODO executarTeclaOKMsgConfirmada CONFORME SUA NECESSIDADE.
	 * 
	 * @param mensagem
	 */
	public void executarTeclaOKMsgConfirmada(final String mensagem) {
		apresentaMsg(mensagem);
		executarTeclaOKMsgConfirmada();
	}

	public void executarTeclaOKMsgConfirmada() {
		fim();
	}

	/***
	 * INSTANCIA PAINEL INFORMATIVO COM A MENSAGEM E ÍCONE/TITULO. A INFORMAÇÃO DEVE
	 * APRESENTAR O RESULTADO DO PROCESSAMENTO/REQUISIÇÃO.
	 * 
	 * @param ui
	 * @param mensagem
	 * @param titulo
	 */
	public void mostrarPaineltMensagemConfirmada(final UIAbstractControle ui, final String mensagem, final String titulo) {
		final PnlMensagemConfirmada pnl = new PnlMensagemConfirmada(ui, mensagem, titulo);
		setPanelVisualizado(pnl, null);
	}

	public void mostrarPaineltMensagemConfirmada(final UIAbstractControle ui, final String mensagem, final String titulo,
			final SwingWorkerActionListener swingWorkerActionListener) {
		final PnlMensagemConfirmada pnl = new PnlMensagemConfirmada(ui, mensagem, titulo, swingWorkerActionListener, false);
		setPanelVisualizado(pnl, null);
	}

	public void mostrarPaineltMensagemOriginalConfirmada(final UIAbstractControle ui, final String mensagem,
			final String titulo, final SwingWorkerActionListener swingWorkerActionListener) {
		final PnlMensagemConfirmada pnl = new PnlMensagemConfirmada(ui, mensagem, titulo, swingWorkerActionListener,
				TypeCase.ORIGINAL);
		setPanelVisualizado(pnl, null);
	}

	public void mensagemConfirmarResposta(final UIMenuControle ui, final ControleMenu controle, final InfoMovimento info, final String mensagem,
			final String titulo, final int tipoMovimento) {
		final PnlConfirmarResposta pnl = new PnlConfirmarResposta(ui, controle, info, mensagem, titulo, tipoMovimento);
		setPanelVisualizado(pnl, null);
	}

	public void mostrarPaineltMensagemConfirmada(final UIAbstractControle ui, final String mensagem, final String titulo,
			final boolean apresentarMensagemRodape) {
		final PnlMensagemConfirmada pnl = new PnlMensagemConfirmada(ui, mensagem, titulo, apresentarMensagemRodape);
		setPanelVisualizado(pnl, null);
	}

	public void apresentaMsgCodigo(final String codigo) {
		apresentaMsg(MensagemErro.getString(codigo));
	}

	/**
	 * Apresenta uma mensagem na tela da EFL.
	 * 
	 * @param RegraNegocioException
	 *            Exceção que contenha o código da Mensagem a ser apresentada na
	 *            tela da EFL
	 */
	public void apresentaMsg(final RegraNegocioException rne) {
		if (log.isDebugEnabled() || rne.getCodigoErro() != null)
			log.debug("INTERFACE: CODIGO ERRO[" + rne.getCodigoErro() + "]");
		apresentaMsg(rne.getMessage());
	}

	/**
	 * Apresenta uma mensagem na tela da EFL.
	 * 
	 * @param ExecutorException
	 *            Exceção que contenha o código da Mensagem a ser apresentada na
	 *            tela da EFL
	 */
	public void apresentaMsg(final ExecutorException ee) {
		if (log.isDebugEnabled() || ee.getCodigoErro() != null)
			log.debug("INTERFACE: CODIGO ERRO[" + ee.getCodigoErro() + "]");
		apresentaMsg(ee.getMessage());
	}

	/**
	 * Apaga a messagem da tela da EFL
	 */
	public void apagaMsg() {
		apresentaMsg((String) null);
	}

	/**
	 * Captura e retorna a instância da janela que está sendo visualizada na EFL
	 * 
	 * @return EFLFrame Instância da janela que está sendo visualizada na EFL
	 * @roseuid 3D7E793C036A
	 */
	protected EFLFrame getEFL() {
		return eflFrame;
	}

	/**
	 * Dispara a finalização do caso de uso em reação ao evento de timeout de tela,
	 * caso o controle ainda não tenha sido finalizado.
	 * <p>
	 * Executa o método {@link #timeout()} antes de finalizar o UC, para dar tempo
	 * do caso de uso realizar alguma tarefa específica.
	 * 
	 * @return <code>true</code> se o timeout foi executado, ou <code>false</code>
	 *         caso contrário.
	 */
	public synchronized boolean notifyTimeout() {
		if (!isFinalizado()) {
			final GerenciaPerifericos gerencia = GerenciaPerifericos.getInstance();
			
			gerencia.desativarPerifericos();

			if (getEstado() == ESTADO_AGUARDANDO_CONFIRMACAO) {
				try {
					abstCtrlMov.cancela(abstCtrlMov.getMovimentoTransacionado(),
							new MotivoCancelamento(MotivoCancelamento.TIME_OUT_EFL));
				} catch (final ControleMovimentoException e) {
					log.error(e.getMessage(), e);
				}
			}

			apresentaMsg(MensagemErro.getString("TRNI0004"));
			timeout();
			fim();

			return true;
		}
		return false;
	}

	/**
	 * Método onde cada caso de uso pode especificar algum comportamento específico
	 * no evento de algum timeout.
	 */
	public synchronized void timeout() {
	}

	/**
	 * Delega recuperação de estado para o correspondente controle de movimento, uma
	 * vez que é o mesmo.
	 */
	public int getEstado() {
		return abstCtrlMov.getEstado();
	}

	/**
	 * Indica se essa instância da UIControle já foi finalizada.
	 */
	public synchronized boolean isFinalizado() {
		return getEstado() == ControleMovimento.ESTADO_FINALIZADO;
	}

	/**
	 * Dispara o fim do controle.
	 */
	public synchronized void fim() {
		abstCtrlMov.fim();
	}

	public void init(final int codigoMovimento, final boolean[][] matriz) throws RegraNegocioException, ExecutorException {
		init();
	}

	public void init(final int tipoMovimento, final int opcaoMovimento) throws RegraNegocioException, ExecutorException {
		init();
	}

	public void init(final int tipoMovimento, final String barCode) throws RegraNegocioException, ExecutorException {
		init();
	}

	public void init(final int tipoMovimento, final AbstractCode barcode) throws RegraNegocioException, ExecutorException {

		if (tipoMovimento == TipoMovimento.ARRECADACAO_CONVENIO_GENERICO_OUTROS_PAGAMENTOS
				|| tipoMovimento == TipoMovimento.ARRECADACAO_CONSULTA_CONVENIO_GENERICO_PAGAMENTO_PEC_10) {
			init(tipoMovimento, barcode.getBarcodeText());
		} else {
			init();
		}
	}
	
	@Override
	public void init(int tipoMovimento, AbstractCode barcode, BigDecimal valorFatura) throws RegraNegocioException, ExecutorException {
	    init(tipoMovimento, barcode, valorFatura);
	}	

	@Override
	public void init(final int tipoMovimento, final int opcaoMovimento, final Object param)
			throws RegraNegocioException, ExecutorException {
		init();
	}

	public void init() throws RegraNegocioException, ExecutorException {
		throw new ExecutorException(CodigoErro.ERRO_TF_GR_METODO_INIT_NAO_IMPLEMENTADO_02);
	}

	public void init(final Pendencia pendencia) throws RegraNegocioException, ExecutorException {
		throw new ExecutorException(CodigoErro.ERRO_TF_GR_METODO_INIT_NAO_IMPLEMENTADO_04);
	}

	public void setGerenteTransacao(final GerenteTransacao gerenteTransacao) {
		this.gerenteTransacao = gerenteTransacao;
	}

	public GerenteTransacao getGerenteTransacao() {
		return gerenteTransacao;
	}

	public void setSuportePropriedades(final SuportePropriedades suportePropriedades) {
		this.suportePropriedades = suportePropriedades;
	}

	public SuportePropriedades getSuportePropriedades() {
		return suportePropriedades;
	}

	protected void preparaAntigoControleMovimento(final AbstractControleMovimento antigoControleMovimento) {
		antigoControleMovimento.setGerenteTransacao(gerenteTransacao);
		antigoControleMovimento.setSuportePropriedades(suportePropriedades);
		setControle(antigoControleMovimento);
	}

	/**
	 * Método responsável por inserir as informações do Autorizador no movimento.
	 * Este método somente é utilizado nos casos de uso que incluem o Caso de Uso
	 * "Efetua Validação de Usuário na TFL"
	 * 
	 * @param login
	 * @param senha
	 */
	public void setSupervisor(final String login, final String senha) {
		final Usuario supervisor = new Usuario();
		supervisor.setMatricula(login);
		supervisor.setSenha(senha);
		abstCtrlMov.getMovimentoAutorizador().setAutorizador(supervisor);
	}

	/**
	 * Mostra o painel para ser digitado o código do Supervisor
	 */
	protected void mostraPainelSenhaSupervisor(final UIAbstractControle ui) {
		final PnlSenSup pnlSenhaSupervisor = new PnlSenSup(ui, false);
		setPanelVisualizado(pnlSenhaSupervisor);
	}

	/**
	 * Mostra o painel para ser digitado o código do Proprietario
	 */
	protected void mostraPainelSenhaProprietario(final UIAbstractControle ui) {
		final PnlSenSup pnlSenhaSupervisor = new PnlSenSup(ui, true);
		setPanelVisualizado(pnlSenhaSupervisor);
	}

	/**
	 *  Foi criado para ser utilizado nas
	 * situações onde se utiliza o ExecutorServidorValidaAutorizador.
	 * 
	 */
	public void concluir() {
	}

	/**
	 * Método que faz a verificação se existe alguma impressão a ser realizada para
	 * transação efetivada com sucesso.
	 * 
	 * @param movimento
	 * @return
	 */
	protected boolean isImpressaoEfetivadaHabilitada(final Movimento movimento) {
		if (movimento.getImpressao()[RelatorioEFL.VIA1_IMPRIME].equals(ImpressaoVO.SIM)
				|| movimento.getImpressao()[RelatorioEFL.VIA2_IMPRIME].equals(ImpressaoVO.SIM)
				|| movimento.getImpressao()[RelatorioEFL.VIA3_IMPRIME].equals(ImpressaoVO.SIM)) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * Método que apresenta as seguintes mensagens para finalização de Caso de Uso
	 * com Sucesso.
	 * <p>
	 * <P>
	 * PinPad: TRANSAÇÃO FINALIZADA
	 * <P>
	 * Rodapé TFL: TRANSAÇÃO EFETUADA COM SUCESSO.
	 * 
	 * <p>
	 * Este método apresenta as mensagens faz uma pausa de 2 segundos e depois limpa
	 * as mensagens. Fica então padronizado que após a finalização com sucesso de um
	 * UC, apresente estas mensagens e depois limpe.
	 */
	protected void apresentaMsgFinalizado() {
		GerenciaPerifericos.getInstance().clearDisplayPinPad();
		GerenciaPerifericos.getInstance().displayPinPadDefault("TRANSACAO\nFINALIZADA");
		apresentaMsg(MensagemErro.getString("GRLI0024"));
		(new Thread(new Runnable() {
			public void run() {

				try {
					Thread.sleep(2500);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
				GerenciaPerifericos.getInstance().clearDisplayPinPad();
				GerenciaPerifericos.getInstance().displayPinPad(3, 0,
						SuportePropriedades.getInstance().getMensagemCaixa());
				apresentaMsg("");
			}
		})).start();
	}

	/**
	 * Método que apresenta as seguintes mensagens para finalização de Caso de Uso
	 * com Sucesso.
	 * <p>
	 * <P>
	 * PinPad: TRANSAÇÃO EFETUADA COM SUCESSO
	 * <P>
	 * Rodapé TFL: TRANSAÇÃO EFETIVADA.
	 * 
	 * <p>
	 * Este método apresenta as mensagens faz uma pausa de 2 segundos e depois limpa
	 * as mensagens. Fica então padronizado que após a finalização com sucesso de um
	 * UC, apresente estas mensagens e depois limpe.
	 */
	protected void apresentaMsgFinalizacaoSucesso() {
		GerenciaPerifericos.getInstance().clearDisplayPinPad();
		GerenciaPerifericos.getInstance().displayPinPadDefault("TRANSACAO\nEFETIVADA\nCOM SUCESSO");
		apresentaMsg(MensagemErro.getString("GRLI0024"));
		(new Thread(new Runnable() {
			public void run() {

				try {
					Thread.sleep(2500);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
				GerenciaPerifericos.getInstance().clearDisplayPinPad();
				GerenciaPerifericos.getInstance().displayPinPad(3, 0,
						SuportePropriedades.getInstance().getMensagemCaixa());
				apresentaMsg("");
			}
		})).start();
	}
	
	/**
	 * Método que apresenta mensagens na TFL e no PinPad para realizar a impressão
	 * de comprovante
	 * <p>
	 * <p>
	 * Rodapé TFL: AGUARDE IMPRIMINDO...
	 * <p>
	 * PinPad: AGUARDE COMPROVANTE
	 */
	protected void apresentaMsgImpressao() {
		apresentaMsg(MensagemErro.getString("GRLI0001"));
		GerenciaPerifericos.getInstance().clearDisplayPinPad();
		GerenciaPerifericos.getInstance().displayPinPadDefault("AGUARDE\nCOMPROVANTE");
	}

	public OfertaCliente getOfertaCli() {
		return ofertaCli;
	}

	public void setOfertaCli(final OfertaCliente ofertaCli) {
		this.ofertaCli = ofertaCli;
	}

	public ListaOfertaCliente getListaOfertaCli() {
		return listaOfertaCli;
	}

	public void setListaOfertaCli(final ListaOfertaCliente listaOfertaCli) {
		this.listaOfertaCli = listaOfertaCli;
	}

	public IModuloOfertaListener getOfertaListener() {
		return ofertaListener;
	}

	public void setOfertaListener(final IModuloOfertaListener ofertaListener) {
		this.ofertaListener = ofertaListener;
	}

	public void tratarMensagemConfirmacao(final String titulo, final String mensagem,
			final SwingWorkerActionListener erroExecutorListener) {
		mostrarPaineltMensagemConfirmada(this, mensagem, titulo, erroExecutorListener);
	}

	public void tratarErroExecutor(final String titulo, final ExecutorException e,
			final SwingWorkerActionListener erroExecutorListener) {
		mostrarPaineltMensagemConfirmada(this, e.getMessage(), titulo, erroExecutorListener);
	}

	public void tratarErroNegocial(final String titulo, final RegraNegocioException regra,
			final SwingWorkerActionListener erroNegocialListener) {
		mostrarPaineltMensagemConfirmada(this, regra.getMessage(), titulo, erroNegocialListener);
	}

	public void exibirTelaAtendimento() {
		exibirTelaAtendimentoMeiosDePagamento(MeioDePagamentoOpcaoMovimentosEnum.ATENDIMENTO_DIRETO.getOpcaoMovimento(),
				null);
	}

	public void exibirTelaAtendimentoOrquestrarSolicitacao() {
		exibirTelaAtendimentoMeiosDePagamento(MeioDePagamentoOpcaoMovimentosEnum.PADRAO.getOpcaoMovimento(), null);
	}

	public void cancelamentoMeiosDePagamento() {
		exibirTelaAtendimentoMeiosDePagamento(
				MeioDePagamentoOpcaoMovimentosEnum.CHAMAR_COM_CANCELAMENTO.getOpcaoMovimento(), null);
	}

	public void exibirTelaAtendimentoConfirmacaoNovoAtendimento() {
		exibirTelaAtendimentoMeiosDePagamento(MeioDePagamentoOpcaoMovimentosEnum.NOVO_ATENDIMENTO.getOpcaoMovimento(),
				null);
	}

	private void exibirTelaAtendimentoMeiosDePagamento(final int opcaoMovimento, final Object listener) {

		try {
			final ControleMovimento controle = FabricaControleMovimento.getInstance()
					.getControleMovimento(TipoMovimento.MEIOS_DE_PAGAMENTO);
			if (listener == null)
				controle.init(TipoMovimento.MEIOS_DE_PAGAMENTO, opcaoMovimento);
			else
				controle.init(TipoMovimento.MEIOS_DE_PAGAMENTO, opcaoMovimento, listener);
		} catch (final RegraNegocioException e) {
			log.error(e.getMessage());
			apresentaMsg(e);

		} catch (final ExecutorException e) {
			log.error(e.getMessage());
			apresentaMsg(e);

		}

	}

	public IAtendimentoConfirmeListener getAtendimentoListener() {
		if (this.atendimentoListener == null) {
			try {
				this.atendimentoListener = new UIMeiosDePagamentoDefaultControle();
			} catch (final ExecutorException e) {
				log.error(e.getMessage(), e);
			}
		}

		return atendimentoListener;
	}

	public void setAtendimentoListener(final IAtendimentoConfirmeListener atendimentoListener) {
		this.atendimentoListener = atendimentoListener;
	}

	public void adicionarItemNaCesta(final Movimento consulta, final IMeiosDePagamentoListener listener)
			throws RegraNegocioException {
		try {
			final Movimento consultaCopia = (Movimento) consulta.clone();
			this.abstCtrlMov.adicionarItemNaCesta(consultaCopia, listener);
		} catch (final CloneNotSupportedException e) {
			log.error(e.getMessage(), e);
		}

	}

	
	public void mostrarMensagemPinpad(String[] mensagensPinpad) {
		GerenciaPerifericos.getInstance().displayPinPad(new DefaultMessage(PerifericoUtil.getMensagemPin(mensagensPinpad)));
	}
	
	public void confirmaCancelaPinpad(KeyboardListener keyboardListener, String mensagemPinpad, int timeout) {
		GerenciaPerifericos.getInstance().readKey(keyboardListener, timeout,mensagemPinpad);
	}
	
	public void mostraRetireCartao(String tituloPainel) {
		final PnlMensagem pnlMensagem = new PnlMensagem(tituloPainel, MensagemErro.getString("CRTI0010"), null,
				null, false);
		setPanelVisualizado(pnlMensagem);
		verificaRemocaoCartaoChip(new String[] { MSG_PINPAD_REMOVE_CARTAO });
	}
	
	
	
	public void verificaRemocaoCartaoChip() {
		PerifericoUtil.aprensentarMensagensPinpad(new String[] { MSG_PINPAD_REMOVE_CARTAO });
		GerentePerifericosPadrao.getInstance().verificaRemocaoCartaoChip(this);
	}
	
	public void verificaRemocaoCartaoChip(String[] mensagem) {
		PerifericoUtil.aprensentarMensagensPinpad(mensagem);
		GerentePerifericosPadrao.getInstance().verificaRemocaoCartaoChip(this);
	}

	public boolean isCartaoComChip(String trilhaCartao) {
		ValidadorCartao vcs = new ValidadorCartao();
		return vcs.possuiChip(trilhaCartao);
	}
	
	public boolean isCartaoCaixa(String trilhaCartao) {
		ValidadorCartao vcs = new ValidadorCartao();
		return vcs.isValido(trilhaCartao);
	}
	
	public boolean isCartaoBNB(String trilha2) {
		return FabricaDeValidadoresDeCartao.getInstance().getValidadorTrilhaCartaoBNB().isValido(trilha2) ||
				isCartaoBNBVisa(trilha2);
	}
	
	public boolean isCartaoBNBVisa(String trilha2) {
		return FabricaDeValidadoresDeCartao.getInstance().getValidadorTrilhaCartaoBNBVisa().isValido(trilha2);
	}

}