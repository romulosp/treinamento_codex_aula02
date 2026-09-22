package br.gov.caixa.sispl.infra.ui;

import br.gov.caixa.sispl.dominio.PortadorNaoJogo;
import br.gov.caixa.sispl.dominio.RegraNegocioException;
import br.gov.caixa.sispl.infra.executor.ExecutorException;

public interface IPortadorNaoJogo {
	void adicionarDadosPortador(PortadorNaoJogo portadorNaoJogo);

	public void atendimentoSolicite() throws RegraNegocioException, ExecutorException;

	public void atendimentoCancele(int motivo);
}
