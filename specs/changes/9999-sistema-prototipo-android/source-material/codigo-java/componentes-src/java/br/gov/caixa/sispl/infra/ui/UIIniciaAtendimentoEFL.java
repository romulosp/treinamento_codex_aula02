package br.gov.caixa.sispl.infra.ui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.gov.caixa.sispl.dominio.AbstractMovimento;
import br.gov.caixa.sispl.dominio.CodigoErro;
import br.gov.caixa.sispl.dominio.MotivoCancelamento;
import br.gov.caixa.sispl.dominio.MovimentoLogRegistroPortador;
import br.gov.caixa.sispl.dominio.OfertaCliente;
import br.gov.caixa.sispl.dominio.Pagador;
import br.gov.caixa.sispl.dominio.Pagador.TipoPagadorEnum;
import br.gov.caixa.sispl.dominio.PortadorNaoJogo;
import br.gov.caixa.sispl.dominio.RegraCapturaEnum;
import br.gov.caixa.sispl.dominio.RegraNegocioException;
import br.gov.caixa.sispl.dominio.TipoMovimento;
import br.gov.caixa.sispl.dominio.financeiros.AtualizaOfertaCliente;
import br.gov.caixa.sispl.dominio.financeiros.IcOfertaOfertaEnum;
import br.gov.caixa.sispl.dominio.financeiros.ListaOfertaCliente;
import br.gov.caixa.sispl.dominio.financeiros.ValidadorCpfNis;
import br.gov.caixa.sispl.infra.atendimento.oferta.ModuloOfertaEnum;
import br.gov.caixa.sispl.infra.controle.AbstractControleMovimento;
import br.gov.caixa.sispl.infra.controle.ControleAtendimentoEFL;
import br.gov.caixa.sispl.infra.controle.EventoEFL;
import br.gov.caixa.sispl.infra.controle.GerenteEventosEFL;
import br.gov.caixa.sispl.infra.executor.ExecutorException;
import br.gov.caixa.sispl.infra.meiosdepagamento.uc.meiosdepagamento.cesta.CestaFactory;
import br.gov.caixa.sispl.infra.perifericos.GerenciaPerifericos;
import br.gov.caixa.sispl.infra.timeout.GerenteTimeoutUI;
import br.gov.caixa.sispl.infra.ui.atendimento.pnl.PnlColetaCPFOferta;
import br.gov.caixa.sispl.infra.ui.atendimento.pnl.PnlIniciaAtendimento;
import br.gov.caixa.sispl.infra.ui.atendimento.pnl.PnlLeituraSenhaCartao;
import br.gov.caixa.sispl.infra.ui.atendimento.pnl.PnlMensagemErroNegocial;
import br.gov.caixa.sispl.infra.ui.atendimento.pnl.PnlOfertaAutorizacao;
import br.gov.caixa.sispl.infra.ui.dlg.DlgModuloOfertas;
import br.gov.caixa.sispl.infra.ui.ofertacliente.IModuloOfertaListener;
import br.gov.caixa.sispl.infra.ui.ofertacliente.ProdutosOfertaDiposnivelEnum;
import br.gov.caixa.sispl.infra.ui.ofertacliente.SimulaOfertaEnum;
import br.gov.caixa.sispl.infra.ui.pnl.PnlMensagemOferta1;
import br.gov.caixa.sispl.infra.ui.pnl.PnlMensagemOferta2;
import br.gov.caixa.sispl.infra.ui.pnl.PnlMensagemOferta3;
import br.gov.caixa.sispl.infra.util.MatematicosUtil;
import br.gov.caixa.sispl.infra.util.PerifericoUtil;
import br.gov.caixa.sispl.infra.util.log.Log;
import br.gov.caixa.sispl.infra.util.log.LogFactory;
import br.gov.caixa.sispl.util.MensagemErro;
import br.gov.caixa.sispl.util.Messages;

public class UIIniciaAtendimentoEFL extends UIAbstractControle implements IModuloOfertaListener {

	private static final String GRLI0017 = "GRLI0017";

	private static final Log log = LogFactory.getLog(UIIniciaAtendimentoEFL.class);

	private static final String TXT_BTN_VOLTAR = "VOLTAR";
	private static final String TXT_BTN_DIGITAR_NOVAMENTE = "DIGITAR NOVAMENTE";
	protected static final String ATENDIMENTO_MENSAGEM_PORTADOR = "ATENDIMENTO_MENSAGEM_PORTADOR";
	private static final String ATENDIMENTO_MENSAGEM_MODULO_OFERTA_MANUAL = "ATENDIMENTO_MENSAGEM_MODULO_OFERTA_MANUAL";
	private static final String ESPACAMENTO = " ";
	private static final String ATENDIMENTO_MENSAGEM_PORTADOR_CONFIRMAR = "ATENDIMENTO_MENSAGEM_PORTADOR_CONFIRMAR";
	protected static final String[] PIN_OPERACAO_CANCELADA_CLIENTE = { "OPERACAO", "CANCELADA", "PELO CLIENTE" };

	private static final String AGUARDE_IMPRIMINDO = "AGUARDE, IMPRIMINDO...";

	private boolean acaoRedirect;
	private AbstractControleMovimento abstractControleMovimento;
	private PnlMensagemErroNegocial pnlErroNegocial;
	private PnlIniciaAtendimento pnlIniciaAtendimento;
	private ControleAtendimentoEFL controle;
	private OfertaCliente ofertaSelecionada;
	private boolean solicitacaoRealizada = false;
	private boolean moduloCamapanhaFinalizada = false;
	private boolean isModuloOfertaAtivo = false;

	private boolean isModuloOfertaManual = false;
	private boolean isModuloOfertaAutomatica = false;
	private boolean isFluxoModuloOferta = false;

	private static final Integer OPCAO_LISTA_OFERTA = 90;

	public UIIniciaAtendimentoEFL() {
	}

	public UIIniciaAtendimentoEFL(AbstractControleMovimento abstractControleMovimento) {
		this.abstractControleMovimento = abstractControleMovimento;
		this.acaoRedirect = false;
		controle = new ControleAtendimentoEFL();
		prepararControle(abstractControleMovimento);
	}

	private void prepararControle(AbstractControleMovimento abstractControleMovimento) {
		controle.setGerenteTransacao(abstractControleMovimento.getGerenteTransacao());
		controle.setSuportePropriedades(abstractControleMovimento.getSuportePropriedades());
	}

	@Override
	public void init(int tipoMovimento, int opcaoMovimento) throws RegraNegocioException, ExecutorException {
		PerifericoUtil.desativarPerifericos();
		if (opcaoMovimento == OPCAO_LISTA_OFERTA) {
			isModuloOfertaManual = true;
			// setPanelVisualizado(gerarPnlInformativo(PnlColetaCPFOferta.TITULO_COLETA_CPF,MensagemErro.getString("GRLI0017"),
			// false));
			controle = new ControleAtendimentoEFL();
			preparaAntigoControleMovimento(controle);
			getControle().consultaParametrosFinanceiros();
			solicitarLeituraCartao();
		} else {
			mostrarPainelTelaPrincipal();
		}
	}

	public void verificarModuloOferta() throws ExecutorException, RegraNegocioException {
		AbstractMovimento abstractMovimento = controle.getAbstractMovimento();
		try {
			setModuloOfertaAutomatica(true);
			setModuloOfertaManual(false);
			getControle().setListaOfertaCliente(ModuloOfertaEnum.getListaOfertaCliente(abstractMovimento));
			getControle().consultarOfertaCliente();
			List<OfertaCliente> ofertas = getOfertasDisponibilizadas(abstractMovimento.getTipoMovimento());
			String cpfPesquisa = getListaOfertaCliente().getCpfPesquisa();
			if (cpfPesquisa != null && !cpfPesquisa.isEmpty() && ofertas != null && !ofertas.isEmpty()) {
				isModuloOfertaAtivo = true;
				mostrarAvisoOfertaCliente(ofertas);
			} else {
				log.error("CLIENTE SEM NENHUMA OFERTA ");
				setModuloCamapanhaFinalizada(true);
			}
		} catch (ExecutorException e) {
			if (e.getCodigoErro().equals(CodigoErro.ERRO_PR_OF_VALIDA_SENHA_INFRA_EXCEPTION)) {
				GerenciaPerifericos.getInstance().desativarPerifericos();
				GerenteTimeoutUI.getInstance().stop();
				apagaMsg();
				throw e;

			}
			log.debug("COD ERRO" + e.getCodigoErro());
			log.error("erro ao verificarModuloOferta - tipoMovimento " + abstractMovimento.getTipoMovimento());
			setModuloCamapanhaFinalizada(true);
		} catch (RegraNegocioException e) {
			if (e.getCodigoErro().equals(CodigoErro.ERRO_PR_OF_SENHA_NAO_CONFERE)
					|| e.getCodigoErro().equals(CodigoErro.ERRO_PR_OF_SENHA_NAO_EXISTE_NA_BASE)
					|| e.getCodigoErro().equals(CodigoErro.ERRO_PR_OF_SENHA_BLOQUEADA)
					|| e.getCodigoErro().equals(CodigoErro.ERRO_PR_OF_SENHA_CONTA)) {

				GerenciaPerifericos.getInstance().desativarPerifericos();
				GerenteTimeoutUI.getInstance().stop();
				apagaMsg();
				throw e;

			}
			log.error(e.getMessage());
			log.error("erro ao verificarModuloOferta - tipoMovimento " + abstractMovimento.getTipoMovimento());
			setModuloCamapanhaFinalizada(true);
		}
	}

	public void mostrarOfertaAutorizacaoMargem() {
		PnlOfertaAutorizacao pnl = new PnlOfertaAutorizacao(this, false);
		setPanelVisualizado(pnl);
	}

	public void mostrarOfertaAutorizacaoMargemProdutoOferta(OfertaCliente ofertaSelecionada) {
		this.ofertaSelecionada = ofertaSelecionada;
		PnlOfertaAutorizacao pnl = new PnlOfertaAutorizacao(this, true);
		setPanelVisualizado(pnl);
	}
	
	public void mostrarOfertaImprimirFilipeta(){
		buscarOfertasClienteManual();

	}

	/**
	 * Imprimi autorizacao dados
	 */
	public void imprimirFilipeta(ControleAtendimentoEFL controle) {
		apresentaMsg(AGUARDE_IMPRIMINDO);
		getControle().imprimirFilipeta(controle);
		apagaMsg();
	}

	@SuppressWarnings("serial")
	public void buscarOfertasClienteManual() {
		GerenteTimeoutUI.getInstance().stop();

		setModuloOfertaAutomatica(false);
		setModuloOfertaManual(true);

		setPanelVisualizado(gerarPnlInformativo(PnlColetaCPFOferta.TITULO_COLETA_CPF,
				"Aguarde, será impressa filipeta com as ofertas disponíveis \n para o cliente ", false));

		try {
			getControle().getListaOfertaCliente().setOfertaManual(true);
			getControle().consultarOfertaCliente();
			List<OfertaCliente> ofertas = getOfertasDisponibilizadas(
					getControle().getListaOfertaCliente().getTipoMovimento());

			if (!ofertas.isEmpty()) {
				isModuloOfertaAtivo = true;
				imprimirFilipeta(getControle());
				if (isModuloOfertaManual) {
					mostrarOpcaoSimulacaoOferta(ofertas.get(0));
				} else {
					mostrarAvisoOfertaCliente(ofertas);
				}

			} else {
				log.error("CLIENTE SEM NENHUMA OFERTA ");
				setModuloCamapanhaFinalizada(true);
				mostrarPaineltMensagemConfirmada(this, "CLIENTE SEM NENHUMA OFERTA",
						PnlColetaCPFOferta.TITULO_COLETA_CPF);
			}
		} catch (ExecutorException e) {
			tratarErroExecutor(PnlColetaCPFOferta.TITULO_COLETA_CPF, e, new SwingWorkerActionListener() {
				@Override
				public void actionPerformedInBackground(ActionEvent actionEvent) {
					GerenciaPerifericos.getInstance().desativarPerifericos();
					apagaMsg();
					fim();
				}
			});

		} catch (RegraNegocioException e) {
			tratarErroNegocial(PnlColetaCPFOferta.TITULO_COLETA_CPF, e, new SwingWorkerActionListener() {
				@Override
				public void actionPerformedInBackground(ActionEvent actionEvent) {
					GerenciaPerifericos.getInstance().desativarPerifericos();
					apagaMsg();
					fim();
				}
			});
		}
	}

	private List<OfertaCliente> getOfertasDisponibilizadas(int tipoMovimentoPrimario) {
		ListaOfertaCliente listaOfertaClienteMovimento = getListaOfertaCliente();
		List<OfertaCliente> listaOfertas = listaOfertaClienteMovimento.getOfertas();

		List<OfertaCliente> listaOfertasExclusao = new ArrayList<OfertaCliente>();
		for (int i = 0; i < listaOfertas.size(); i++) {
			OfertaCliente ofertaCliente = listaOfertas.get(i);
			if (ofertaCliente.getNomeOferta() != null) {
				ofertaCliente.setNomeOferta(ofertaCliente.getNomeOferta().trim());
			}
			if (isRemoverTransacaoModuloOferta(tipoMovimentoPrimario, ofertaCliente)) {
				listaOfertasExclusao.add(ofertaCliente);
			}
		}
		listaOfertas.removeAll(listaOfertasExclusao);
		return listaOfertas;
	}

	private boolean isRemoverTransacaoModuloOferta(int tipoMovimentoPrimario, OfertaCliente ofertaCliente) {
		return ofertaCliente != null
				&& (ofertaCliente.getNuProduto() == tipoMovimentoPrimario
						|| isMovimentoPrimarioConsignadoProdutoEfetivaConsignado(tipoMovimentoPrimario, ofertaCliente)
						|| isMovimentoPrimarioRenovacaoConsignadoProdutoEfetivaConsignado(tipoMovimentoPrimario,
								ofertaCliente)
						|| isMovimentoPrimarioContrataCartaoProdutoContrataCartao(tipoMovimentoPrimario, ofertaCliente))
				|| !ProdutosOfertaDiposnivelEnum.isProdutoDisponivelEfl(ofertaCliente.getNuProduto());
	}

	private boolean isMovimentoPrimarioRenovacaoConsignadoProdutoEfetivaConsignado(int tipoMovimentoPrimario,
			OfertaCliente ofertaCliente) {
		return tipoMovimentoPrimario == TipoMovimento.CONSULTA_CREDITO_CONSIGNADO
				&& ofertaCliente.getNuProduto() == TipoMovimento.EFETIVA_CONTRATACAO_RENOVACAO_CONSIGNADO;
	}

	private boolean isMovimentoPrimarioConsignadoProdutoEfetivaConsignado(int tipoMovimentoPrimario,
			OfertaCliente ofertaCliente) {
		return tipoMovimentoPrimario == TipoMovimento.CONSULTA_CREDITO_CONSIGNADO
				&& ofertaCliente.getNuProduto() == TipoMovimento.EFETIVA_CONTRATACAO_CREDITO_CONSIGNADO;
	}

	private boolean isMovimentoPrimarioContrataCartaoProdutoContrataCartao(int tipoMovimentoPrimario,
			OfertaCliente ofertaCliente) {
		return tipoMovimentoPrimario == TipoMovimento.CONSULTA_AVALIACAO_CARTAO
				&& ofertaCliente.getNuProduto() == TipoMovimento.CONTRATA_CARTAO_CREDITO;
	}

	public void atualizaSituacaoOfertaCliente(IcOfertaOfertaEnum icOfertaOfertaEnum, String cpf, int nuProduto)
			throws ExecutorException, RegraNegocioException {
		AtualizaOfertaCliente atualizaOfertaCliente = getControle().getAtualizaOfertaCliente();

		if (cpf != null) {
			atualizaOfertaCliente.setCpf(cpf);
		}

		atualizaOfertaCliente.setNuProduto(nuProduto);

		if (icOfertaOfertaEnum != null) {
			atualizaOfertaCliente.setIcOfertaProduto(icOfertaOfertaEnum.getIcOferta());
		}

		atualizaOfertaCliente.setSkipModuloOfertas(true);
		getControle().atualizaOfertaCliente();
	}

	public void consultarOfertaClienteCliente() {
		try {
			setPanelVisualizado(gerarPnlInformativo(MensagemErro.getString(GRLI0017), false));
			getControle().consultarDadosCliente();
			solicitacaoRealizada = true;
			exibirConfirmacaoDados();
		} catch (ExecutorException e) {

			log.error(e);
			proseguirTransacao();

		} catch (RegraNegocioException regra) {
			log.error(regra);
			proseguirTransacao();
		}
	}

	public void mostrarPainelTelaPrincipal() {
		pnlIniciaAtendimento = new PnlIniciaAtendimento(this);
		pnlIniciaAtendimento.setMensagem(Messages.getString(ATENDIMENTO_MENSAGEM_PORTADOR));
		setPanelVisualizado(pnlIniciaAtendimento);
	}

	public void mostrarPainelColetaCPF() {
		PnlColetaCPFOferta pnlColetaCPFOferta = new PnlColetaCPFOferta(this);
		pnlColetaCPFOferta.setMensagem(Messages.getString(ATENDIMENTO_MENSAGEM_MODULO_OFERTA_MANUAL));
		setPanelVisualizado(pnlColetaCPFOferta);
	}

	public void mostrarInfoSenha(String mensagemNegocial) throws ExecutorException {
		try {
			apagaMsg();
			@SuppressWarnings("serial")
			PnlMensagemErroNegocial pnlMensagemErroNegocialModuloOferta = gerarPnlInformativo("", mensagemNegocial,
					new SwingWorkerActionListener() {
						@Override
						public void actionPerformedInBackground(ActionEvent actionEvent) {
							cancelaOperacao(new MotivoCancelamento(MotivoCancelamento.SERVIDOR), true);
							finalizar();
						}
					});
			setPanelVisualizado(pnlMensagemErroNegocialModuloOferta);
			GerenteTimeoutUI.getInstance().stop();

		} catch (Exception ex) {
			throw new ExecutorException(CodigoErro.ERRO_TF_GR_CRIACAO_DIALOG_OFERTAS_CLIENTE, ex);
		}
	}

	public void mostrarAvisoOfertaCliente(List<OfertaCliente> listaOfertaClienteDisponivel) {
		apagaMsg();
		setPanelVisualizado(new PnlMensagemOferta1(this, listaOfertaClienteDisponivel));
	}

	public void mostrarOpcaoSimulacaoOferta(OfertaCliente ofertaSelecionada) {
		setPanelVisualizado(new PnlMensagemOferta2(this, ofertaSelecionada));
	}
	
	public void mostrarOpcaoSimulacaoOferta() {
		setPanelVisualizado(new PnlMensagemOferta2(this, this.ofertaSelecionada));
	}

	public void mostrarListaProdutoOferta(OfertaCliente ofertaSelecionada) {
		apagaMsg();
		ListaOfertaCliente listaOfertaCliente = getListaOfertaCliente();
		setPanelVisualizado(new PnlMensagemOferta3(this, listaOfertaCliente.getOfertas(), ofertaSelecionada));
	}

	public void mostrarOfertasCliente() throws ExecutorException {
		try {
			GerenciaPerifericos.getInstance().desativarPerifericos();
			GerenteTimeoutUI.getInstance().stop();
			new DlgModuloOfertas(EFLFrame.getInstance(), this, getListaOfertaCliente(), null);

		} catch (Exception ex) {
			throw new ExecutorException(CodigoErro.ERRO_TF_GR_CRIACAO_DIALOG_OFERTAS_CLIENTE, ex);
		}
	}

	public ListaOfertaCliente getListaOfertaCliente() {

		ListaOfertaCliente listaOfertaCliente = getControle().getListaOfertaCliente();
		List<OfertaCliente> ofertasDisponivelBanco = listaOfertaCliente.getOfertas();

		for (int i = 0; i < ofertasDisponivelBanco.size(); i++) {
			OfertaCliente ofertaCliente = ofertasDisponivelBanco.get(i);
			if (!ProdutosOfertaDiposnivelEnum.isProdutoDisponivelEfl(ofertaCliente.getNuProduto())) {
				ofertasDisponivelBanco.remove(i);
			}
		}
		return listaOfertaCliente;
	}

	public void consultarDadosCliente() {
		try {
			setPanelVisualizado(gerarPnlInformativo(MensagemErro.getString(GRLI0017), false));
			getControle().consultarDadosCliente();
			solicitacaoRealizada = true;
			exibirConfirmacaoDados();
		} catch (ExecutorException e) {
			apresentarMensagemErro(e);
		} catch (RegraNegocioException regra) {
			apresentarMensagemNegocialErro(regra.getMessage());
		}
	}

	protected void apresentarMensagemErro(ExecutorException executorException) {

		StringBuilder mensagemErro = getMensagemErroInfra(executorException);

		mostrarPaineltMensagemConfirmada(this, mensagemErro.toString(), PnlIniciaAtendimento.TITULO_INICIA_ATENDIMENTO,
				new SwingWorkerActionListener() {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformedInBackground(ActionEvent actionEvent) {
						cancelaOperacao(new MotivoCancelamento(MotivoCancelamento.SERVIDOR), true);
						finalizar();
					}

				});

	}

	private StringBuilder getMensagemErroInfra(ExecutorException executorException) {
		StringBuilder mensagemErro = new StringBuilder(
				MensagemErro.getString(executorException.getCodigoErro().getCodigoMensagem()) + " \n");
		mensagemErro.append("\nCódigo de erro [");
		mensagemErro.append(executorException.getCodigoErro().getCodigo() + "-");
		mensagemErro.append(executorException.getProperty("0"));
		mensagemErro.append("]");
		return mensagemErro;
	}

	public PnlMensagemErroNegocial gerarPnlInformativo(String mensagem, boolean isMostrarBtn) {
		PnlMensagemErroNegocial gerarPnlInformativo = gerarPnlInformativo(mensagem, null);
		gerarPnlInformativo.getBtnCancelar().setVisible(isMostrarBtn);
		gerarPnlInformativo.getBtnConfirmar().setVisible(isMostrarBtn);
		return gerarPnlInformativo;
	}

	public PnlMensagemErroNegocial gerarPnlInformativo(String titulo, String mensagem, boolean isMostrarBtn) {
		PnlMensagemErroNegocial gerarPnlInformativo = gerarPnlInformativo(titulo, mensagem, null);
		gerarPnlInformativo.getBtnCancelar().setVisible(isMostrarBtn);
		gerarPnlInformativo.getBtnConfirmar().setVisible(isMostrarBtn);
		return gerarPnlInformativo;
	}

	private static PnlMensagemErroNegocial gerarPnlInformativo(String mensagem,
			SwingWorkerActionListener swingWorkerActionListener) {
		SwingWorkerActionListener okAction = swingWorkerActionListener != null ? swingWorkerActionListener : null;
		PnlMensagemErroNegocial pnlMensagem = new PnlMensagemErroNegocial(
				PnlIniciaAtendimento.TITULO_INICIA_ATENDIMENTO, mensagem, null, okAction, false);
		pnlMensagem.getBtnCancelar().setEnabled(false);
		pnlMensagem.getBtnConfirmar().setEnabled(swingWorkerActionListener != null);
		return pnlMensagem;
	}

	private static PnlMensagemErroNegocial gerarPnlInformativo(String titulo, String mensagem,
			SwingWorkerActionListener swingWorkerActionListener) {
		SwingWorkerActionListener okAction = swingWorkerActionListener != null ? swingWorkerActionListener : null;
		PnlMensagemErroNegocial pnlMensagem = new PnlMensagemErroNegocial(titulo, mensagem, null, okAction, false);
		pnlMensagem.getBtnCancelar().setEnabled(false);
		pnlMensagem.getBtnConfirmar().setEnabled(swingWorkerActionListener != null);
		return pnlMensagem;
	}

	private static PnlMensagemErroNegocial gerarPnlInformativo(String mensagem, SwingWorkerActionListener acaoConfirmar,
			SwingWorkerActionListener acaoCancelar) {
		return new PnlMensagemErroNegocial(PnlIniciaAtendimento.TITULO_INICIA_ATENDIMENTO, mensagem, acaoCancelar,
				acaoConfirmar, false);
	}

	public void exibirConfirmacaoDados() {
		atualizarObjetos();
		setPanelVisualizado(pnlIniciaAtendimento);
	}

	public void atualizarObjetos() {
		pnlIniciaAtendimento.getLabelMensagemConfirmacao().setVisible(true);
		pnlIniciaAtendimento.getLabelMensagemConfirmacao().setText(getMensagemConfirmacao());
		Point location = pnlIniciaAtendimento.getLabelMensagemConfirmacao().getLocation();
		pnlIniciaAtendimento.getLabelMensagemConfirmacao().setLocation(location.x, location.y + 20);

		pnlIniciaAtendimento.getTeclaConfirmar().setVisible(true);
		pnlIniciaAtendimento.getTeclaConfirmar().setDefaultButton();
		pnlIniciaAtendimento.getTextCpf().setEnabled(false);
		pnlIniciaAtendimento.getBotaoConfirmaCpf().setVisible(false);
		pnlIniciaAtendimento.getLabelMsgErroNegocialCentral().setVisible(false);
		pnlIniciaAtendimento.getMensagem().setLocation(120, 120);
		pnlIniciaAtendimento.getEFLVirtualKeyboardNumeric().setVisible(false);
		pnlIniciaAtendimento.setMensagem(Messages.getString(ATENDIMENTO_MENSAGEM_PORTADOR_CONFIRMAR));
		pnlIniciaAtendimento.getTeclaCancelar().setText(TXT_BTN_VOLTAR);
		pnlIniciaAtendimento.getTeclaCancelar().addActionListener(new SwingWorkerActionListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformedInBackground(ActionEvent actionEvent) {
				cancelaOperacao(new MotivoCancelamento(MotivoCancelamento.OK), false);
				mostrarPainelTelaPrincipal();
			}
		});
		pnlIniciaAtendimento.repaint();
	}

	private String getMensagemConfirmacao() {
		PortadorNaoJogo portadorNaoJogo = getControle().getPortadorNaoJogo();
		String nome = portadorNaoJogo.getNome();
		StringBuilder mensagemConfirmacao = new StringBuilder(
				"\n\n" + Messages.getString("ATENDIMENTO_NOME_CLIENTE") + ESPACAMENTO + nome + "\n \n");

		String nomeDaMae = portadorNaoJogo.getNomeDaMae();
		if (nomeDaMae != null && !nomeDaMae.isEmpty()) {
			mensagemConfirmacao.append(
					"\n\n" + Messages.getString("ATENDIMENTO_LABEL_NOME_MAE") + ESPACAMENTO + nomeDaMae + "\n \n");
		}

		String dataNascimento = portadorNaoJogo.getDataNascimento();
		if (dataNascimento != null && !dataNascimento.isEmpty()) {
			mensagemConfirmacao
					.append(Messages.getString("ATENDIMENTO_LABEL_DATA_NASC") + ESPACAMENTO + dataNascimento + "\n \n");
		}
		return mensagemConfirmacao.toString();
	}

	@SuppressWarnings("serial")
	public void apresentarMensagemNegocialErro(String mensagemNegocial) {

		pnlErroNegocial = gerarPnlInformativo(mensagemNegocial, new SwingWorkerActionListener() {

			@Override
			public void actionPerformedInBackground(ActionEvent actionEvent) {
				pnlErroNegocial.getBtnConfirmar().setEnabled(false);
				cancelaOperacao(new MotivoCancelamento(MotivoCancelamento.OK), true);
				mostrarPainelTelaPrincipal();
			}
		}, new SwingWorkerActionListener() {

			@Override
			public void actionPerformedInBackground(ActionEvent actionEvent) {
				pnlErroNegocial.getBtnCancelar().setEnabled(false);
				cancelaOperacao(new MotivoCancelamento(MotivoCancelamento.OPERADOR), true);
				finalizar();
			}
		});

		pnlErroNegocial.getBtnConfirmar().setText(TXT_BTN_DIGITAR_NOVAMENTE);
		setPanelVisualizado(pnlErroNegocial);
	}

	/**
	 * Método responsável por definir o fluxo ao cliente apos ser pressionada tecla
	 * cancelar pelo cliente/operador
	 */
	public void cancelaOperacao(MotivoCancelamento motivoCancelamento, boolean isServidor) {
		PerifericoUtil.getGerentePerifericos().clearMessage();
		int motivo = motivoCancelamento.getCodigoMotivo();
		if (MotivoCancelamento.OPERADOR == motivo) {
			apresentaMsg(Messages.getString("ATENDIMENTO_TRANSACAO_CANCELADA_PELO_OPERADOR"));
			PerifericoUtil.aprensentarMensagemCancelamentoPinpad(PerifericoUtil.ALIGN_CENTER);
		}
		if (MotivoCancelamento.NAO_AUTORIZADO == motivo) {
			apresentaMsg(Messages.getString("PRAN002"));
			PerifericoUtil.aprensentarMensagemNaoAutorizadaPinpad(PerifericoUtil.ALIGN_CENTER);
		}
		if (MotivoCancelamento.TIME_OUT_EFL == motivo) {
			apresentaMsg(MensagemErro.getString("TRNI0004"));
			PerifericoUtil.aprensentarMensagemTimeoutPinpad();
		}
		if (isServidor) {
			getControle().cancelarDadosPortadorBrasileiro(motivoCancelamento.getCodigoMotivo());
		}

		verificarCancelamentoModuloOfertas();

	}

	private void verificarCancelamentoModuloOfertas() {
		if (isModuloOfertaManual) {
			fim();
		}else if(isModuloOfertaAutomatica || isFluxoModuloOferta) {
			try {
				atualizaSituacaoOfertaCliente(IcOfertaOfertaEnum.IC_NAO_FINALIZADA, getOfertaCli().getCpf(), getOfertaCli().getNuProduto());
			} catch (Exception e) {
				log.error("Erro ao atualizar situação oferta");
			}	
		}
	}

	public void finalizar() {
		try {
			GerenteEventosEFL.getInstance().fireEFLEvent(this, EventoEFL.EVT_EFL_UC_ENCERRADO);
		} catch (ExecutorException e) {
			log.error("Erro ao tentar finalizar ",e);
		}
	}

	public void proseguirFluxoPagador(Pagador pagador) {
		try {
			if (TipoPagadorEnum.BRASILEIRO.equals(pagador.getTipoPagador())) {
				String cpf = pagador.getDocumento();
				validarCPF(cpf);
			}
			setPanelVisualizado(gerarPnlInformativo(MensagemErro.getString(GRLI0017), false));
			abstractControleMovimento = null;

			PortadorNaoJogo portadorNaoJogo = getControle().getPortadorNaoJogo();
			portadorNaoJogo.getRegrasCaptura().add(RegraCapturaEnum.PIX);
			getControle().getAbstractMovimento().setPortadorNaoJogo(portadorNaoJogo);
			CestaFactory.getInstancePortadorNaoJogoListener().adicionarDadosPortador(portadorNaoJogo);
		} catch (RegraNegocioException rne) {
			apresentaMsg(rne);
		}
	}
	
	public void proseguirFluxoPagadorModuloOferta() {
		setPanelVisualizado(gerarPnlInformativo(MensagemErro.getString(GRLI0017), false));
		abstractControleMovimento = null;
	}

	@Override
	public synchronized boolean notifyTimeout() {
		if (isModuloOfertaAtivo) {
			tratarOfertaNaoFinalizada(getOfertaCli());
		} else {
			cancelarTimeOut();
		}
		return false;
	}

	private void cancelarTimeOut() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				PerifericoUtil.aprensentarMensagemTimeoutPinpad();
				cancelaOperacao(new MotivoCancelamento(MotivoCancelamento.TIME_OUT_EFL), solicitacaoRealizada);
				finalizar();

			}
		}).start();
	}

	protected void validarCPF(String cpf) throws RegraNegocioException {
		ValidadorCpfNis validadorCpfNis = new ValidadorCpfNis();
		validadorCpfNis.validateCpf(cpf);
	}

	public boolean isAcaoRedirect() {
		return acaoRedirect;
	}

	public synchronized AbstractControleMovimento getAbstractControleMovimento() {
		return abstractControleMovimento;
	}

	@Override
	public synchronized void timeout() {
		log.debug("MODULO CAMAPANHA FINALIZADA - TIMEOUT");
		this.abstractControleMovimento = null;
	}

	public ControleAtendimentoEFL getControle() {
		return controle;
	}

	public void setControle(ControleAtendimentoEFL controle) {
		this.controle = controle;
	}

	public void setMovimentoLog(List<MovimentoLogRegistroPortador> movimentoLog) {
		controle.setMovimentoLog(movimentoLog);
	}

	public void setNomePrograma(String nomePrograma) {
		controle.setNomePrograma(nomePrograma);
	}

	public void setTipoRegistro(Integer tipoRegistro) {
		controle.setTipoRegistro(tipoRegistro);
	}

	public void setAbstractMovimento(AbstractMovimento abstractMovimento) {
		controle.setAbstractMovimento(abstractMovimento);
	}

	public void mostrarAguardeProcessando() {
		setPanelVisualizado(
				gerarPnlInformativo(DlgModuloOfertas.TITULO_MODULO_OFERTAS, MensagemErro.getString(GRLI0017), false));
	}

	/*
	 * metodo de retorno do modulo ofertas
	 * 
	 */

	@Override
	public void tratarRejeitouOferta(OfertaCliente ofertaCliente) {
		log.debug("tratarRejeitouOferta");
		addHistorico(IcOfertaOfertaEnum.IC_REJEITOU_OFERTA, ofertaCliente);
		finalizarOfertaManual(ofertaCliente);
	}

	@Override
	public void tratarOfertaContratada(OfertaCliente ofertaCliente) {
		log.debug("tratarOfertaContratada");
		addHistorico(IcOfertaOfertaEnum.IC_OFERTA_CONTRADADA, ofertaCliente);
		finalizarOfertaManual(ofertaCliente);
	}

	public void simularOferta(OfertaCliente ofertaCliente) {
		mostrarAguardeProcessando();
		PerifericoUtil.desativarPerifericos();
		SimulaOfertaEnum.getSimulador(ofertaCliente.getNuProduto()).simular(ofertaCliente,
				getControle().getListaOfertaCliente(), this);
	}

	@Override
	public void tratarOfertaDepois(OfertaCliente ofertaCliente) {
		log.debug("tratarOfertaDepois");
		addHistorico(IcOfertaOfertaEnum.IC_OFERTAR_DEPOIS, ofertaCliente);
		finalizarOfertaManual(ofertaCliente);
	}

	private void finalizarOfertaManual(OfertaCliente ofertaCliente) {
		if (isModuloOfertaManual) {
			fim();
		}
	}

	@Override
	public void tratarOfertaNaoFinalizada(OfertaCliente ofertaCliente) {
		log.debug("tratarOfertaNaoContradado");
		addHistorico(IcOfertaOfertaEnum.IC_NAO_FINALIZADA, ofertaCliente);
		finalizarOfertaManual(ofertaCliente);
	}

	@Override
	public void tratarErro(OfertaCliente ofertaCliente, Exception e) {
		log.error("tratar erro ", e);
		proseguirTransacao();
	}

	public void proseguirTransacao() {
		GerenteTimeoutUI.getInstance().restart();
		PerifericoUtil.desativarPerifericos();
		setModuloCamapanhaFinalizada(true);
		log.debug("PROSEGUIR TRANSACAO - MODULO CAMAPANHA FINALIZADA");
		if (isModuloOfertaManual) {
			fim();
		}
	}

	private void addHistorico(IcOfertaOfertaEnum icOfertaOfertaEnum, OfertaCliente ofertaCliente) {
		try {
			setPanelVisualizado(gerarPnlInformativo(DlgModuloOfertas.TITULO_MODULO_OFERTAS,
					MensagemErro.getString(GRLI0017), false), null);
			atualizaSituacaoOfertaCliente(icOfertaOfertaEnum, ofertaCliente.getCpf(), ofertaCliente.getNuProduto());
		} catch (ExecutorException e) {
			log.error("erro tratarOfertaContratada ExecutorException", e);
		} catch (RegraNegocioException e) {
			log.error("erro tratarOfertaContratada RegraNegocioException", e);
		} finally {
			proseguirTransacao();
		}
	}

	@SuppressWarnings("serial")
	@Override
	public void tratarErroNegocial(final OfertaCliente ofertaCliente, final String mensagemNegocial,
			String tituloFuncionalidade) {
		final UIIniciaAtendimentoEFL ui = this;
		ui.apagaMsg();
		PnlMensagemErroNegocial pnlMensagemErroNegocialModuloOferta = gerarPnlInformativo(tituloFuncionalidade,
				mensagemNegocial, new SwingWorkerActionListener() {
					@Override
					public void actionPerformedInBackground(ActionEvent actionEvent) {
						PerifericoUtil.aprensentarMensagemCancelamentoPinpad(PerifericoUtil.ALIGN_CENTER);
						ui.tratarOfertaNaoFinalizada(ofertaCliente);
					}
				});
		setPanelVisualizado(pnlMensagemErroNegocialModuloOferta);
		GerenteTimeoutUI.getInstance().stop();
	}

	public synchronized boolean isModuloCamapanhaFinalizada() {
		return moduloCamapanhaFinalizada;
	}

	public synchronized void setModuloCamapanhaFinalizada(boolean moduloCamapanhaFinalizada) {
		this.moduloCamapanhaFinalizada = moduloCamapanhaFinalizada;
	}

	public void solicitarLeituraCartao() {
		Date date = new Date();
		int numeroAleatorioControle = Integer.parseInt(MatematicosUtil.calculaNumeroAleatorio());
		getListaOfertaCliente().setDataHoraSenha(date);
		getListaOfertaCliente().setNuControle(numeroAleatorioControle);

		PnlLeituraSenhaCartao pnlLeituraSenhaCartao = new PnlLeituraSenhaCartao(this, numeroAleatorioControle, date);
		setPanelVisualizado(pnlLeituraSenhaCartao);
	}

	public boolean isModuloOfertaManual() {
		return isModuloOfertaManual;
	}

	public void setModuloOfertaManual(boolean isModuloOfertaManual) {
		this.isModuloOfertaManual = isModuloOfertaManual;
	}

	public boolean isModuloOfertaAutomatica() {
		return isModuloOfertaAutomatica;
	}

	public void setModuloOfertaAutomatica(boolean isModuloOfertaAutomatica) {
		this.isModuloOfertaAutomatica = isModuloOfertaAutomatica;
	}

	/**
	 * UTIL - Cancelamento efetuado pelo cliente
	 */
	public synchronized void cancelaCliente() {
		log.debug("MODULO OFERTA: CANCELAMENTO [CLIENTE]");
		apresentaMsg(MensagemErro.getString("ACSI0004"));
		PerifericoUtil.aprensentarMensagensPinpad(PIN_OPERACAO_CANCELADA_CLIENTE);
		fim();
	}

	protected void setPnlIniciaAtendimento(PnlIniciaAtendimento pnlIniciaAtendimento) {
		this.pnlIniciaAtendimento = pnlIniciaAtendimento;
	}

	public void setSolicitacaoRealizada(boolean solicitacaoRealizada) {
		this.solicitacaoRealizada = solicitacaoRealizada;
	}
	
	public boolean isFluxoModuloOferta() {
		return isFluxoModuloOferta;
	}

	public void setFluxoModuloOferta(boolean isFluxoModuloOferta) {
		this.isFluxoModuloOferta = isFluxoModuloOferta;
	}

}
