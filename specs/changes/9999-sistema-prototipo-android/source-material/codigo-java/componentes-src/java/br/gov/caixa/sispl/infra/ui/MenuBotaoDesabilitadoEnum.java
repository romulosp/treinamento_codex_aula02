package br.gov.caixa.sispl.infra.ui;

import br.gov.caixa.sispl.dominio.ConstantesParametros;
import br.gov.caixa.sispl.dominio.TipoMovimento;
import br.gov.caixa.sispl.infra.propriedades.SuportePropriedades;

public enum MenuBotaoDesabilitadoEnum {

	AUX_BRASIL_CONSULTA_CLIENTE(TipoMovimento.AUX_BRASIL_CONSULTA_CLIENTE,
			ConstantesParametros.HABILITA_OPCAO_MENU_AUX_BRASIL, false,
			null), 
	AUX_BR_CONSULTA_CREDITO_CONSIGNADO(TipoMovimento.AUX_BR_CONSULTA_CREDITO_CONSIGNADO,
			ConstantesParametros.HABILITA_CREDITO_CONSIGNADO_AUX_BR_OPCAO_MENU, true,
			ConstantesParametros.HABILITA_CREDITO_CONSIGNADO_AUX_BR_TFL_PILOTO), 
	
	CDC(TipoMovimento.CDC_AUTENTICACAO_SENHA,
			ConstantesParametros.CDC_HABILITA_DESABILITA_POR_MENU, true,
			ConstantesParametros.CDC_HABILITA_DESABILITA_POR_TFL), 
	
	PAGAMENTO_CARTAO_CREDITO(TipoMovimento.PAGAMENTO_CARTAO_CREDITO_AVISTA_NAO_ABECS,
			ConstantesParametros.PAG_CARTAO_HABILITA_DESABILITA_ITEM_MENU, true,
			ConstantesParametros.PAG_CARTAO_HABILITA_DESABILITA_POR_TFL),
	
	PAGAMENTO_CARTAO_REL_POR_TERMINAL(TipoMovimento.PAGAMENTO_CARTAO_REL_POR_TERMINAL,
			ConstantesParametros.PAG_CARTAO_HABILITA_DESABILITA_ITEM_MENU, true,
			ConstantesParametros.PAG_CARTAO_HABILITA_DESABILITA_POR_TFL),
	PAGAMENTO_CARTAO_REL_DETALHAMENTO(TipoMovimento.PAGAMENTO_CARTAO_REL_DETALHAMENTO,
			ConstantesParametros.PAG_CARTAO_HABILITA_DESABILITA_ITEM_MENU, true,
			ConstantesParametros.PAG_CARTAO_HABILITA_DESABILITA_POR_TFL),
	HABILITA_DESABILITA_ATD_CAIXA_TEM(TipoMovimento.DEBITO_SEM_CARTAO,
			ConstantesParametros.HABILITA_DESABILITA_ATD_CAIXA_TEM_PAGAMENTO, false,
			null),
	ATD_CAIXA_TEM_RESET_CADASTRO(TipoMovimento.ATD_CAIXA_TEM_VINCULAR_NOVO_CELULAR_RESET_CADASTRO,
			ConstantesParametros.PARAMETRO_RESET_CADASTRO_CAIXA_TEM,false, null),
	PE_DE_MEIA(TipoMovimento.PE_DE_MEIA_CONSULTA_CLIENTE,
			ConstantesParametros.HABILITA_DESABILITA_CAD_SENHA_PE_DE_MEIA_CONSULTA, false,
			null);
	
	
	
	private static final String DESABILITADO = "0";
	private Integer tipoMovimento;
	private Integer parametroHabilitaMenu;
	private boolean validaTFLPiloto;
	private Integer parametroTFLPiloto;

	private MenuBotaoDesabilitadoEnum(final Integer tipoMovimento, final Integer parametroHabilitaMenu,
			final boolean validaTFLPiloto, final Integer parametroTFLPiloto) {
		this.tipoMovimento = tipoMovimento;
		this.parametroHabilitaMenu = parametroHabilitaMenu;
		this.validaTFLPiloto = validaTFLPiloto;
		this.parametroTFLPiloto = parametroTFLPiloto;

	}

	public static boolean isMenuBotaoDesabilitado(final Integer tipoMovimento) {

		for (final MenuBotaoDesabilitadoEnum itemMenu : MenuBotaoDesabilitadoEnum.values()) {
			if (itemMenu.getTipoMovimento().equals(tipoMovimento)) {
				if (DESABILITADO
						.equals(SuportePropriedades.getInstance().getParametro(itemMenu.getParametroHabilitaMenu()))) {
					
					return isConfiguraPiloto(itemMenu);
				} else {
					return false;
				}

			}

		}
		return false;
	}

	private static boolean isConfiguraPiloto(MenuBotaoDesabilitadoEnum itemMenu) {
		try {

			if (!itemMenu.isValidaTFLPiloto() || itemMenu.getParametroTFLPiloto() == null) {
				return true;
			}
			final String valorParametroTFLPiloto = SuportePropriedades.getInstance()
					.getParametro(itemMenu.getParametroTFLPiloto());

			if (valorParametroTFLPiloto == null || valorParametroTFLPiloto.equals(DESABILITADO)) {
				return true;
			}
			final int codLoterico = SuportePropriedades.getInstance().getCodLoterico();
			final int numTerminal = SuportePropriedades.getInstance().getNumTerminal();
			final String[] listaTFLPiloto = valorParametroTFLPiloto.split(";");
			for (final String tfl : listaTFLPiloto) {
				final String[] piloto = tfl.split(",");
				final int codLotericoPiloto = Integer.parseInt(piloto[0]);
				final int numTerminalPiloto = Integer.parseInt(piloto[1]);
				if (codLoterico == codLotericoPiloto && numTerminal == numTerminalPiloto) {
					return false;
				}

			}
			return true;

		} catch (final Exception e) {
			return true;
		}

	}

	public Integer getTipoMovimento() {
		return tipoMovimento;
	}

	public Integer getParametroHabilitaMenu() {
		return parametroHabilitaMenu;
	}

	public boolean isValidaTFLPiloto() {
		return validaTFLPiloto;
	}

	public Integer getParametroTFLPiloto() {
		return parametroTFLPiloto;
	}

}
