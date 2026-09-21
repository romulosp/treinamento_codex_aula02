package br.gov.caixa.sispl.infra.ui;

/**
 * Interface para tratar os eventos do report
 *
 */
public interface EFLReportEventListener {
	
	/**
	 * Quando um elemento é selecionado
	 * @param index
	 */
	void onSelected(int index);
}
