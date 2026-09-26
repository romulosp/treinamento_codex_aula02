//Source file: C:\\FABRICA_view\\Infra\\src\\java\\br\\gov\\caixa\\sispl\\infra\\ui\\FabricaUI.java
package br.gov.caixa.sispl.infra.ui;

/**
 * Caixa Econômica Federal SISPL - Arquivo  : FabricaUI.java Criaçao  : 31/08/2002 Implementador: Alexandre Vieira Rebouças
 */
import br.gov.caixa.sispl.infra.controle.AbstractControleMovimento;
import br.gov.caixa.sispl.infra.perifericos.GerentePerifericosPadrao;
import br.gov.caixa.sispl.infra.propriedades.SuportePropriedades;

/**
 * Classe abstrata utilizada como fábrica de objetos UI. Ela é responsável por
 * identificar dinamicamente o tipo de objeto desejado pela classe solicitante,
 * sendo necessário para issso, que a classe que requisitante do serviço, siga
 * na sua definição a padronização de nomes estabelecido para o sistema SISPL.
 *
 * @version 1.0 31-Ago-2002
 * @author Alexandre Vieira Rebouças
 */
public abstract class FabricaUI {
	private static final String PREFIXO_CONTROLE = ".controle.Controle";
	private static final String PREFIXO_CONTROLE_ABECS = ".controle.abecs.Controle";
	private static final String SUFIXO = "Controle";
	private static final String PREFIXO_UI = ".ui.UI";
	private static final String PREFIXO_UI_ABECS = ".ui.abecs.UI";

	/**
	 * Instancia e retorna a referência para um objeto UI. É fundamental que a
	 * classe controladora recebida como parâmetro, siga, na sua definição, as
	 * regras de de nomeclatura para controladoras do sistema SISPL.
	 * 
	 * 
	 * @return UIEFL Interface genérica do pacote UI.
	 * 
	 * @throws UIException
	 * 
	 * @roseuid 3F1DE7190161
	 */
	// public static UIEFL getUI() throws UIException {
	// return null;
	// }

	/**
	 * Instancia e retorna a referência para um objeto UI. É fundamental que a
	 * classe controladora recebida como parâmetro, siga, na sua definição, as
	 * regras de de nomeclatura para controladoras do sistema SISPL.
	 * 
	 * @param controle
	 *            Classe controladora abstrata
	 * 
	 * @return UIEFL Interface genérica do pacote UI.
	 * 
	 * @throws UIException
	 * 
	 * @roseuid 3F1DEB8F028C
	 */
	public static UIEFL getUI(AbstractControleMovimento controle) throws UIException {
		UIAbstractControle objUIAbstCtrl = null;
		if (SuportePropriedades.getInstance().isPinpadABECS()) {
			try {
				objUIAbstCtrl = getUIAbecs(controle);
			} catch (Exception e) {
				objUIAbstCtrl = getUINaoAbecs(controle);
			}
		} else {
			objUIAbstCtrl = getUINaoAbecs(controle);
		}
		return (UIEFL) objUIAbstCtrl;
	}

	private static UIAbstractControle getUIAbecs(AbstractControleMovimento controle) throws UIException {

		// Captura o nome da classe controladora
		String nomeControle = controle.getClass().getName();

		// Pega o nome do caso de uso que pertence a controladora
		String nomeCasoUso = nomeControle
				.substring(nomeControle.indexOf(PREFIXO_CONTROLE_ABECS) + PREFIXO_CONTROLE_ABECS.length());

		// Monta o nome da classe UI a ser instanciada
		String nomeUI = nomeControle.substring(0, nomeControle.indexOf(PREFIXO_CONTROLE_ABECS)) + PREFIXO_UI_ABECS
				+ nomeCasoUso + SUFIXO;

		UIAbstractControle objUIAbstCtrl = null;

		// Instanciando classe UI
		try {
			objUIAbstCtrl = (UIAbstractControle) Class.forName(nomeUI).newInstance();
		} catch (ClassNotFoundException e) {
			throw new UIException("A classe " + nomeUI + " não foi encontrada");
		} catch (InstantiationException e) {
			throw new UIException("Não pode ser criada instância da classe " + nomeUI);
		} catch (IllegalAccessException e) {
			throw new UIException("Acesso ilegal a classe " + nomeUI);
		}

		objUIAbstCtrl.setControle(controle);
		return objUIAbstCtrl;
	}

	private static UIAbstractControle getUINaoAbecs(AbstractControleMovimento controle) throws UIException {

		// Captura o nome da classe controladora
		String nomeControle = controle.getClass().getName();

		// Pega o nome do caso de uso que pertence a controladora
		String nomeCasoUso = nomeControle.substring(nomeControle.indexOf(PREFIXO_CONTROLE) + PREFIXO_CONTROLE.length());

		// Monta o nome da classe UI a ser instanciada
		String nomeUI = nomeControle.substring(0, nomeControle.indexOf(PREFIXO_CONTROLE)) + PREFIXO_UI + nomeCasoUso
				+ SUFIXO;

		UIAbstractControle objUIAbstCtrl = null;

		// Instanciando classe UI
		try {
			objUIAbstCtrl = (UIAbstractControle) Class.forName(nomeUI).newInstance();
		} catch (ClassNotFoundException e) {
			throw new UIException("A classe " + nomeUI + " não foi encontrada");
		} catch (InstantiationException e) {
			throw new UIException("Não pode ser criada instância da classe " + nomeUI);
		} catch (IllegalAccessException e) {
			throw new UIException("Acesso ilegal a classe " + nomeUI);
		}

		objUIAbstCtrl.setControle(controle);
		return objUIAbstCtrl;
	}
}