package br.gov.caixa.sispl.infra.ui.dlg;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import br.gov.caixa.sispl.dominio.RegraNegocioException;
import br.gov.caixa.sispl.dominio.TipoMovimento;
import br.gov.caixa.sispl.infra.controle.ControleMovimento;
import br.gov.caixa.sispl.infra.controle.FabricaControleMovimento;
import br.gov.caixa.sispl.infra.executor.ExecutorException;
import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.skin.component.textfield.TextFieldSkin;
import br.gov.caixa.sispl.infra.ui.DefaultUIManager;
import br.gov.caixa.sispl.infra.ui.EFLButton;
import br.gov.caixa.sispl.infra.ui.EFLFrame;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.infra.ui.EFLTextField;
import br.gov.caixa.sispl.infra.ui.EFLVirtualKeyboardCalculator;
import br.gov.caixa.sispl.infra.ui.SwingWorkerActionListener;
import br.gov.caixa.sispl.infra.util.Calculadora;
import br.gov.caixa.sispl.infra.util.ControleAcesso;
import br.gov.caixa.sispl.infra.util.VersaoEFL;
import br.gov.caixa.sispl.util.StringUtil;


/**
 * Dialog utilizado para exibição da calculadora da EFL.
 * @author p517563
 */

public class DlgCalculadora extends EFLDialog {
    private static final long serialVersionUID = -476361016403536107L;

    private DefaultUIManager defaultUIManager;

	private EFLPanel calculadoraPanel;
	private EFLTextField areaDados;
	private EFLButton backspace;
	private EFLButton teclaFechar;
	private EFLLabel labelCalculadora;
	private EFLButton teclaPix;

	private ArrayList tecladoNum;
	private Calculadora calculadora;

	private EFLVirtualKeyboardCalculator eflVirtualKeyboardKey;
	private EFLVirtualKeyboardCalculator backSpaceKeyboardKey;
	private EFLVirtualKeyboardCalculator pixKeyboardKey;

	private boolean ready;

	public String ultimoCliqueOperacao = "-10000";

	private static final char NO_OP = '=';
	private static final char PLUS = '+';
	private static final char SUBTRACT = '-';
	private static final char MULTIPLY = '*';
	private static final char DIVIDE = '/';

	/**
	 * Construtor DlgCalculadora
	 * 
	 * @param EFLFRame
	 *            Inicializa o Dialog
	 */
	public DlgCalculadora(EFLFrame parent, Calculadora calculadora) throws Exception {
		super(null, parent);
		this.calculadora = calculadora;
		defaultUIManager = new DefaultUIManager();
		setModal(true);
		ready = true;
		initialize();
		
		calculadora.operacaoCE("0");
	}

	/**
	 * @return Retorna a areaDados onde visualiza o valor digitado.
	 */
	private EFLTextField getAreaDados() {
		if (areaDados == null) {
			areaDados = new EFLTextField(defaultUIManager, TextFieldSkin.TEXT_LOGIN);
			areaDados.setBorder(new BevelBorder(BevelBorder.LOWERED));
			areaDados.setLocation(5, 50);
			areaDados.setSize(215, 40);
			areaDados.setHorizontalAlignment(SwingConstants.RIGHT);
			areaDados.setEnabled(false);
			areaDados.setText("0");
		}
		return areaDados;
	}

	/**
	 * @return Retorna o botão backspace.
	 */
	private EFLButton getBackspace() {
		if (backspace == null) {
			backSpaceKeyboardKey = new EFLVirtualKeyboardCalculator("BACK\nSPACE", ButtonSkin.BOTAO_TECLADO_4,
					KeyEvent.VK_BACK_SPACE, new Point(225, 45), new Dimension(52, 52));
			backspace = new EFLButton(defaultUIManager, backSpaceKeyboardKey.getKeySkin());
			backspace.setText(backSpaceKeyboardKey.getKeyLabel());
			backspace.setLocation(backSpaceKeyboardKey.getLocation());
			backspace.setSize(backSpaceKeyboardKey.getSize());
			backspace.addActionListener(new EFLVirtualOperatorActionListener(backSpaceKeyboardKey));
		}
		return backspace;
	}

	/**
	 * @return Retorna o botão pix.
	 */
	private EFLButton getPix() {
		if (teclaPix == null) {

			pixKeyboardKey = new EFLVirtualKeyboardCalculator(null, ButtonSkin.BOTAO_PIX_CONFIRMAR, KeyEvent.VK_BACK_SPACE,
					new Point(5, 315), new Dimension(170, 60));

			teclaPix = new EFLButton(defaultUIManager, pixKeyboardKey.getKeySkin());
			teclaPix.setText(pixKeyboardKey.getKeyLabel());
			teclaPix.setLocation(pixKeyboardKey.getLocation());
			teclaPix.setSize(pixKeyboardKey.getSize());
			teclaPix.setAlignmentX(CENTER_ALIGNMENT);
			teclaPix.addActionListener(new EFLVirtualPixActionListener());
			teclaPix.setEnabled(false);

		}
		return teclaPix;
	}

	/**
	 * @return Retorna o botão fechar.
	 */
	private EFLButton getTeclaFechar() {
		if (teclaFechar == null) {
			teclaFechar = new EFLButton(defaultUIManager, ButtonSkin.BOTAO_CALCULADORA_FECHAR);
			teclaFechar.setLocation(255, 3);
			teclaFechar.setSize(28, 28);
			teclaFechar.setText("X");
			teclaFechar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
		}
		return teclaFechar;
	}

	/**
	 * @return Retorna o label superior da calculadora.
	 */
	private EFLLabel getLabel() {
		if (labelCalculadora == null) {
			labelCalculadora = new EFLLabel(LabelSkin.LABEL_TITULO_CALCULADORA);
			labelCalculadora.setLocation(3, 3);
			labelCalculadora.setSize(255, 28);
			labelCalculadora.setVisible(true);
		}
		return labelCalculadora;
	}

	/**
	 * Retorna um array de botões
	 * 
	 * @return Returns tecladoNum.
	 */
	private ArrayList getTeclado() {

		if (tecladoNum == null) {
			tecladoNum = new ArrayList();
			tecladoNum.add(new EFLVirtualKeyboardCalculator("7", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_7,
					new Point(5, 100), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator("4", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_4,
					new Point(5, 150), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator("1", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_1,
					new Point(5, 200), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator("0", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_0,
					new Point(5, 255), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator("8", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_8,
					new Point(60, 100), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator("5", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_5,
					new Point(60, 150), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator("2", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_2,
					new Point(60, 200), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator("+/-", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_NUMBER_SIGN,
					new Point(60, 255), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator("9", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_9,
					new Point(115, 100), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator("6", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_6,
					new Point(115, 150), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator("3", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_3,
					new Point(115, 200), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator(",", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_COMMA,
					new Point(115, 255), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator("/", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_DIVIDE,
					new Point(170, 100), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator("x", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_MULTIPLY,
					new Point(170, 150), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator("-", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_SUBTRACT,
					new Point(170, 200), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator("+", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_ADD,
					new Point(170, 255), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator("CE", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_CLEAR,
					new Point(225, 100), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator("C", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_CLEAR,
					new Point(225, 150), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator("%", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_DIVIDE,
					new Point(225, 200), new Dimension(52, 52)));
			tecladoNum.add(new EFLVirtualKeyboardCalculator("=", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_EQUALS,
					new Point(225, 255), new Dimension(52, 52)));

		}
		return tecladoNum;
	}

	/**
	 * Cria o Painel que será usado como contentPane do Dialog.
	 * 
	 * @return EFLPanel Painel.
	 */
	private EFLPanel getPanel() {

		if (calculadoraPanel == null) {
			calculadoraPanel = new EFLPanel(PanelSkin.PANEL_CALCULADORA);
			calculadoraPanel.setSize(new Dimension(300, 600));
			calculadoraPanel.setLayout(null);
			calculadoraPanel.setBorder(new BevelBorder(BevelBorder.RAISED));

			// Adiciona o label e o botão fechar
			calculadoraPanel.add(getLabel());
			calculadoraPanel.add(getTeclaFechar());

			// Adiciona o campo textfield e o o botão de Back Space
			calculadoraPanel.add(getAreaDados());
			calculadoraPanel.add(getBackspace());

			// Adiciona os botões numéricos
			Iterator iteratorTeclas = getTeclado().iterator();
			while (iteratorTeclas.hasNext()) {
				eflVirtualKeyboardKey = (EFLVirtualKeyboardCalculator) iteratorTeclas.next();
				EFLButton teclaButton = new EFLButton(defaultUIManager, eflVirtualKeyboardKey.getKeySkin());
				teclaButton.setText(eflVirtualKeyboardKey.getKeyLabel());
				teclaButton.setLocation(eflVirtualKeyboardKey.getLocation());
				teclaButton.setSize(eflVirtualKeyboardKey.getSize());
				teclaButton.addActionListener(
						adicionaListener(eflVirtualKeyboardKey.getKeyLabel(), eflVirtualKeyboardKey));
				calculadoraPanel.add(teclaButton);
			}
			calculadoraPanel.setOpaque(false);
			calculadoraPanel.add(getPix());

		}
		return calculadoraPanel;
	}

	/**
	 * Inicializa componentes gráficos do Dialog possibilitando a visualização dos
	 * botões textfield e lebel da calculadora.
	 *
	 */
	public void initialize() {
		setUndecorated(true);
		setSize(285, 400);
		setLocation((getParent().getWidth() - this.getWidth()) / 2, (getParent().getHeight() - this.getHeight()) / 2);

		if (VersaoEFL.isDebian8OuSuperiorSingleton()) {
			addKeyListener();
		}

		EFLPanel panel = getPanel();

		setContentPane(panel);
		setVisible(false);

	}

	/*
	 * habilita ou desabilita a tecla do botao pix por logon
	 * 
	 */
	public void habilitarOuDesabilitarTeclaPixPorLogon() {
		if (teclaPix != null) {
			teclaPix.setEnabled(ControleAcesso.getLogon() != null 
					&& ControleAcesso.getLogon().isTipoUsuarioSelecionado());
		}
	}

	private void addKeyListener() {
		this.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// Empty method stub
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// Empty method stub
			}

			/**
			 * Formata valor calculadora.
			 * 
			 * @param valor
			 * @return String
			 */
			private String formataValorSaida(String valor) {
				double valorInicial = Double.parseDouble(valor.replaceAll(",", "."));

				DecimalFormatSymbols decimal = new DecimalFormatSymbols();
				decimal.setDecimalSeparator(',');

				DecimalFormat resultadoDecimal = new DecimalFormat("###0.#####E0", decimal);

				return resultadoDecimal.format(valorInicial);
			}

			/**
			 * Formata valor calculadora.
			 * 
			 * @param valor
			 * @return String
			 */
			private String formataValorEntrada(String valor) {
				double valorInicial = Double.parseDouble(valor.replaceAll(",", "."));

				DecimalFormatSymbols decimal = new DecimalFormatSymbols();
				decimal.setDecimalSeparator(',');
				DecimalFormat resultadoDecimal = new DecimalFormat(
						"######################################################################.#######################################################################",
						decimal);

				return resultadoDecimal.format(valorInicial);
			}

			/**
			 * Formata valor calculadora.
			 * 
			 * @param valor
			 * @return String
			 */
			private String validaDigito(String valor, String digito) {
				String resultadoFinal = null;

				if (valor.length() > 27) {
					resultadoFinal = formataValorSaida(valor + digito);
				} else {
					if (valor.indexOf('E') != -1) {
						String resultadoInicial = formataValorEntrada(valor);
						resultadoFinal = formataValorSaida(resultadoInicial + digito);
					} else {
						resultadoFinal = valor + digito;
					}
				}
				return resultadoFinal;
			}

			private void processarNumeros(KeyEvent e) {
				String code = String.valueOf(e.getKeyChar());
				if (ready) {
					if (!code.equalsIgnoreCase(",")) {
						areaDados.setText(code);
						ready = false;

					} else {
						areaDados.setText(validaDigito(areaDados.getText(), ""));
						ready = true;
					}
				} else {
					if (code.equalsIgnoreCase(",")) {
						if (areaDados.getText().indexOf(',') == -1 && !areaDados.getText().equalsIgnoreCase("")) {
							areaDados.setText(validaDigito(areaDados.getText(), code));
						} else {
							areaDados.setText(validaDigito(areaDados.getText(), ""));
						}
					} else {
						ultimoCliqueOperacao = eflVirtualKeyboardKey.getKeyLabel();
						areaDados.setText(validaDigito(areaDados.getText(), code));
					}
				}
			}

			private void processarOperacoes(KeyEvent e) {
				String resultado = null;
				int comando = e.getKeyCode();

				switch (comando) {
				case KeyEvent.VK_ADD:
					if (!ultimoCliqueOperacao.equals(Integer.toString(comando) )) {
						resultado = calculadora.somar(areaDados.getText());
					}
					break;
				case KeyEvent.VK_SUBTRACT:
					if (!ultimoCliqueOperacao.equals(Integer.toString(comando))){
						resultado = calculadora.subtrair(areaDados.getText());
					}
					break;
				case KeyEvent.VK_MULTIPLY:
					if (!ultimoCliqueOperacao.equals(Integer.toString(comando))){
						resultado = calculadora.multiplicar(areaDados.getText());
					}
					break;
				case KeyEvent.VK_DIVIDE:
					if (!ultimoCliqueOperacao.equals(Integer.toString(comando))){
						resultado = calculadora.dividir(areaDados.getText());
					}
					break;
				case KeyEvent.VK_ENTER:
					resultado = calculadora.operacaoIgualdade(areaDados.getText());
					break;
				case KeyEvent.VK_BACK_SPACE:
					resultado = calculadora.operacaoBackSpace(areaDados.getText());
					break;
				}
				ultimoCliqueOperacao = Integer.toString(comando);
				if (resultado != null) {
					areaDados.setText(resultado);
				}

				if ((comando != KeyEvent.VK_BACK_SPACE)
						|| (comando == KeyEvent.VK_BACK_SPACE && resultado.equals("0"))) {
					ready = true;
				} else {
					ready = calculadora.isReadyOperador();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				boolean isNumeros = e.getKeyCode() == KeyEvent.VK_NUMPAD1 || e.getKeyCode() == KeyEvent.VK_NUMPAD2
						|| e.getKeyCode() == KeyEvent.VK_NUMPAD3 || e.getKeyCode() == KeyEvent.VK_NUMPAD4
						|| e.getKeyCode() == KeyEvent.VK_NUMPAD5 || e.getKeyCode() == KeyEvent.VK_NUMPAD6
						|| e.getKeyCode() == KeyEvent.VK_NUMPAD7 || e.getKeyCode() == KeyEvent.VK_NUMPAD8
						|| e.getKeyCode() == KeyEvent.VK_NUMPAD9 || e.getKeyCode() == KeyEvent.VK_NUMPAD0
						|| e.getKeyCode() == KeyEvent.VK_1 || e.getKeyCode() == KeyEvent.VK_2
						|| e.getKeyCode() == KeyEvent.VK_3 || e.getKeyCode() == KeyEvent.VK_4
						|| e.getKeyCode() == KeyEvent.VK_5 || e.getKeyCode() == KeyEvent.VK_6
						|| e.getKeyCode() == KeyEvent.VK_7 || e.getKeyCode() == KeyEvent.VK_8
						|| e.getKeyCode() == KeyEvent.VK_9 || e.getKeyCode() == KeyEvent.VK_0
						|| e.getKeyCode() == KeyEvent.VK_SEPARATOR;

				boolean isOperacao = e.getKeyCode() == KeyEvent.VK_ADD || e.getKeyCode() == KeyEvent.VK_SUBTRACT
						|| e.getKeyCode() == KeyEvent.VK_MULTIPLY || e.getKeyCode() == KeyEvent.VK_DIVIDE
						|| e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_BACK_SPACE;

				if (isNumeros) {
					processarNumeros(e);
				} else if (isOperacao) {
					processarOperacoes(e);
				}
			}
		});
	}

	/**
	 * Adiciona o evento específico para o botão selecionado.
	 * 
	 * @return Returns SwingWorkerActionListener.
	 */
	private SwingWorkerActionListener adicionaListener(String tecla,
			EFLVirtualKeyboardCalculator eflVirtualKeyboardKey) {

		boolean isOK = (eflVirtualKeyboardKey.getKeyLabel().equals("1")
				|| eflVirtualKeyboardKey.getKeyLabel().equals("2") || eflVirtualKeyboardKey.getKeyLabel().equals("3")
				|| eflVirtualKeyboardKey.getKeyLabel().equals("4") || eflVirtualKeyboardKey.getKeyLabel().equals("5")
				|| eflVirtualKeyboardKey.getKeyLabel().equals("6") || eflVirtualKeyboardKey.getKeyLabel().equals("7")
				|| eflVirtualKeyboardKey.getKeyLabel().equals("8") || eflVirtualKeyboardKey.getKeyLabel().equals("9")
				|| eflVirtualKeyboardKey.getKeyLabel().equals("0") || eflVirtualKeyboardKey.getKeyLabel().equals(","));

		if (isOK) {
			return new EFLCalculatorActionListener(eflVirtualKeyboardKey);
		} else {
			return new EFLVirtualOperatorActionListener(eflVirtualKeyboardKey);
		}
	}

	/**
	 * Classe de eventos dos botões da calculadora da EFL
	 * 
	 * @author p529399
	 *
	 */
	private class EFLCalculatorActionListener extends SwingWorkerActionListener {

		private EFLVirtualKeyboardCalculator eflVirtualKeyboardKey = null;

		public EFLCalculatorActionListener(EFLVirtualKeyboardCalculator eflVirtualKeyboardKey) {
			this.eflVirtualKeyboardKey = eflVirtualKeyboardKey;
		}

		public void actionPerformedInBackground(ActionEvent actionEvent) {
			if (ready) {
				if (!eflVirtualKeyboardKey.getKeyLabel().equalsIgnoreCase(",")) {
					areaDados.setText(eflVirtualKeyboardKey.getKeyLabel());
					ready = false;

				} else {
					areaDados.setText(validaDigito(areaDados.getText(), ""));
					ready = true;
				}
			} else {
				if (eflVirtualKeyboardKey.getKeyLabel().equalsIgnoreCase(",")) {
					if (areaDados.getText().indexOf(',') == -1 && !areaDados.getText().equalsIgnoreCase("")) {
						areaDados.setText(validaDigito(areaDados.getText(), eflVirtualKeyboardKey.getKeyLabel()));
					} else {
						areaDados.setText(validaDigito(areaDados.getText(), ""));
					}
				} else {
					ultimoCliqueOperacao = eflVirtualKeyboardKey.getKeyLabel();
					areaDados.setText(validaDigito(areaDados.getText(), eflVirtualKeyboardKey.getKeyLabel()));

				}
			}
		}

		/**
		 * Formata valor calculadora.
		 * 
		 * @param valor
		 * @return String
		 */
		private String formataValorSaida(String valor) {

			String valorfinal = null;
			DecimalFormat resultadoDecimal = null;

			double valorInicial = Double.parseDouble(valor.replaceAll(",", "."));

			DecimalFormatSymbols decimal = new DecimalFormatSymbols();
			decimal.setDecimalSeparator(',');

			resultadoDecimal = new DecimalFormat("###0.#####E0", decimal);

			valorfinal = resultadoDecimal.format(valorInicial);

			return valorfinal;
		}

		/**
		 * Formata valor calculadora.
		 * 
		 * @param valor
		 * @return String
		 */
		private String formataValorEntrada(String valor) {

			String valorfinal = null;
			DecimalFormat resultadoDecimal = null;

			double valorInicial = Double.parseDouble(valor.replaceAll(",", "."));

			DecimalFormatSymbols decimal = new DecimalFormatSymbols();
			decimal.setDecimalSeparator(',');
			resultadoDecimal = new DecimalFormat(
					"######################################################################.#######################################################################",
					decimal);

			valorfinal = resultadoDecimal.format(valorInicial);

			return valorfinal;
		}

		/**
		 * Formata valor calculadora.
		 * 
		 * @param valor
		 * @return String
		 */
		private String validaDigito(String valor, String digito) {

			String resultadoFinal = null;
			String resultadoInicial = null;

			if (valor.length() > 27) {
				resultadoFinal = formataValorSaida(valor + digito);
			} else {
				if (valor.indexOf('E') != -1) {
					resultadoInicial = formataValorEntrada(valor);
					resultadoFinal = formataValorSaida(resultadoInicial + digito);
				} else {
					resultadoFinal = valor + digito;
				}
			}
			return resultadoFinal;
		}

	}

	/**
	 * Classe de eventos dos botões da calculadora da EFL
	 * 
	 * @author p529399
	 *
	 */
	private class EFLVirtualOperatorActionListener extends SwingWorkerActionListener {

		private EFLVirtualKeyboardCalculator eflVirtualKeyBoardKey;

		public EFLVirtualOperatorActionListener(EFLVirtualKeyboardCalculator eflVirtualKeyboardKey) {
			this.eflVirtualKeyBoardKey = eflVirtualKeyboardKey;
		}

		public void actionPerformedInBackground(ActionEvent actionEvent) {
			char operacao;
			String resultado = null;
			String resultadoDecimal = null;
			String comando = ((EFLButton) actionEvent.getSource()).getText();

			if (comando.equals("CE")) {
				operacao = '&';
			} else if (comando.equals("+/-")) {
				operacao = '#';
			} else if (comando.equalsIgnoreCase("BACK\nSPACE")) {
				operacao = '<';
			} else {
				operacao = comando.charAt(0);
			}

			switch (operacao) {
			case '+':
				if (!ultimoCliqueOperacao.equals(Character.toString(operacao))) {
					resultado = calculadora.somar(areaDados.getText());
				}
				break;
			case '-':
				if (!ultimoCliqueOperacao.equals(Character.toString(operacao))) {
					resultado = calculadora.subtrair(areaDados.getText());
				}
				break;
			case 'x':
				if (!ultimoCliqueOperacao.equals(Character.toString(operacao))) {
					resultado = calculadora.multiplicar(areaDados.getText());
				}
				break;
			case '/':
				if (!ultimoCliqueOperacao.equals(Character.toString(operacao))) {
					resultado = calculadora.dividir(areaDados.getText());
				}
				break;
			case '=':
				resultado = calculadora.operacaoIgualdade(areaDados.getText());
				break;
			case 'C':
				resultado = calculadora.operacaoC(areaDados.getText());
				break;
			case '#':
				resultado = calculadora.operacaoSinal(areaDados.getText());
				break;
			case '%':
				resultado = calculadora.operacaoPorcentagem(areaDados.getText());
				break;
			case '&':
				resultado = calculadora.operacaoCE(areaDados.getText());
				break;
			case '<':
				resultado = calculadora.operacaoBackSpace(areaDados.getText());
				break;
			}
			ultimoCliqueOperacao = Character.toString(operacao);
			if (resultado != null) {
				areaDados.setText(resultado);
			}
			if ((operacao != '#' && operacao != '<') || (operacao == '<' && resultado.equals("0"))) {
				ready = true;
			} else {
				ready = calculadora.isReadyOperador();

			}
		}
	}

	/**
	 * Classe representa eventos de click do botões Pix
	 * 
	 * @author f595054 Marcos Ferreira
	 *
	 */
	private class EFLVirtualPixActionListener extends SwingWorkerActionListener {

		private static final int CHAMADA_ESPECIAL_PIX = 99;
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		

		public void actionPerformedInBackground(final ActionEvent actionEvent) {
			try {
				teclaPix.setEnabled(false);
				ControleMovimento controleMovimento = FabricaControleMovimento.getInstance().getControleMovimento(TipoMovimento.GERA_QRCODE_DINAMICO_PIX_PAGAMENTO);
				try {
					controleMovimento.init(TipoMovimento.GERA_QRCODE_DINAMICO_PIX_PAGAMENTO, CHAMADA_ESPECIAL_PIX,getAreaDados().getText());
					setVisible(false);
				} catch (RegraNegocioException e) {
					e.printStackTrace();
				}
			} catch (ExecutorException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Limpa dados
	 */
	public void clear() {
		areaDados.setText(calculadora.operacaoC(StringUtil.EMPTY));
	}

	/**
	 * Método main para teste.
	 */
	// public static void main(String[] args) {
	// try {
	// EFLFrame frame = new EFLFrame("SISPL - Caixa Econômica Federal");
	// new CalculadoraTeste(frame);

	// frame.setVisible(true);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
}
