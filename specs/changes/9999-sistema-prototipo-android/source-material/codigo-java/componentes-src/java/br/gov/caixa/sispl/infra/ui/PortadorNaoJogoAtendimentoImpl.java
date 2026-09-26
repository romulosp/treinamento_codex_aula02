package br.gov.caixa.sispl.infra.ui;

import br.gov.caixa.sispl.dominio.PortadorNaoJogo;
import br.gov.caixa.sispl.dominio.RegraNegocioException;
import br.gov.caixa.sispl.infra.executor.ExecutorException;
import br.gov.caixa.sispl.infra.meiosdepagamento.uc.meiosdepagamento.ui.UIMeiosDePagamentoControle;

public class PortadorNaoJogoAtendimentoImpl implements IPortadorNaoJogo {
	private UIMeiosDePagamentoControle uiMeiosDePagamentoControle;
	

	public PortadorNaoJogoAtendimentoImpl(UIMeiosDePagamentoControle uiMeiosDePagamentoControle) {
		super();
		this.uiMeiosDePagamentoControle = uiMeiosDePagamentoControle;
		
	}

	public UIMeiosDePagamentoControle getUiMeiosDePagamentoControle() {
		return uiMeiosDePagamentoControle;
	}

	public void setUiMeiosDePagamentoControle(UIMeiosDePagamentoControle uiMeiosDePagamentoControle) {
		this.uiMeiosDePagamentoControle = uiMeiosDePagamentoControle;
	}

	@Override
	public void adicionarDadosPortador(PortadorNaoJogo portadorNaoJogo) {
		this.uiMeiosDePagamentoControle.getControle().adicionarDadosPortador(portadorNaoJogo);

	}

	@Override
	public void atendimentoSolicite() throws RegraNegocioException, ExecutorException {
		this.uiMeiosDePagamentoControle.atendimentoSolicite();

	}

	@Override
	public void atendimentoCancele(int motivo) {
		this.uiMeiosDePagamentoControle.cancelaOperacao(motivo);

	}

}
