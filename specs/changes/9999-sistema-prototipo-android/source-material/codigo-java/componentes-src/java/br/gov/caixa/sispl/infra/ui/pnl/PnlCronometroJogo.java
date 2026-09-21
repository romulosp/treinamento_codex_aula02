/*
 * Caixa Econômica Federal SISPL - Arquivo: ControleRelogioJogos.java Criação: 26/07/2004 Implementador: Lellis Marçal Mesquita
 */

package br.gov.caixa.sispl.infra.ui.pnl;

import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import br.gov.caixa.sispl.infra.controle.RegistroTempoJogo;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.util.CronometroCotasVirtuais;
import br.gov.caixa.sispl.infra.util.CronometroJogo;

/**
 * Classe que constroi e mostra uma tabela com as modalidades de jogos que estão se encerrando, para que o operador da EFL possa saber
 * quando o jogo termina. Em cada modalidade de jogo é mostrado um cronômetro com o tempo decrescendo.
 */

public class PnlCronometroJogo extends JPanel {

	private static final long serialVersionUID = 2007100801L;

	private static transient final int LARGURA_CRONOMETRO = 160;
	private static transient final int ALTURA_CRONOMETRO = 21;
	private static transient final int CRONOMETRO_POR_COLUNA = 2; 
	private static transient final int CRONOMETRO_POR_LINHA = 2;
    private EFLLabel labelFechamentoMegaSena = null;
    private EFLLabel labelFechamentoQuina = null;
    private EFLLabel labelFechamentoLotofacil = null;
    private EFLLabel labelFechamentoLotomania = null;
    private EFLLabel labelFechamentoDuplaSena = null;
    private EFLLabel labelFechamentoLotogol = null;
    private EFLLabel labelFechamentoLoteca = null;
    private EFLLabel labelFechamentoEncalhe = null;
    private EFLLabel labelFechamentoTimemania = null;
    private EFLLabel labelFechamentoDiaDeSorte = null;
    private EFLLabel labelFechamentoSuperSete = null;
    private EFLLabel labelFechamentoMilionaria = null;
    private EFLLabel labelFechamentoMKP = null;
    
    public PnlCronometroJogo() {
        initialize();
    }

    private void initialize() {
        setOpaque(false);
        setLayout(null);
        setLocation(0, 50);
        //FIXME Deve arrendodar quantidade de cronometros para cima.
        setSize(LARGURA_CRONOMETRO * CRONOMETRO_POR_LINHA, ALTURA_CRONOMETRO * CRONOMETRO_POR_COLUNA);
        this.add(getLabelFechamentoMegaSena());
        this.add(getLabelFechamentoQuina());
        this.add(getLabelFechamentoLotofacil());
        this.add(getLabelFechamentoLotomania());
        this.add(getLabelFechamentoDuplaSena());
        this.add(getLabelFechamentoLoteca());
        this.add(getLabelFechamentoLotogol());
        this.add(getLabelFechamentoTimemania());
        this.add(getLabelFechamentoDiaDeSorte());
        this.add(getLabelFechamentoSuperSete());
        this.add(getLabelFechamentoMilionaria());
        this.add(getLabelFechamentoEncalheFederal());
        this.add(getLabelFechamentoMKP());
    }



	/**
     * atualiza o cronometro de acordo com o tempo definido no cronometroJogo.
     * 
     * @roseuid 3EB01381009E
     */
    public void atualizarCronometro(Map listaCronometros, int tipoRelogio) {
    	long currentTimeMillis = System.currentTimeMillis();
    	
    	long timeSlot = currentTimeMillis / 5000L;
    	int quantidadeCronometrosPorPagina = CRONOMETRO_POR_COLUNA * CRONOMETRO_POR_LINHA;
    	long quantidadeRelogios = listaCronometros.size(); 
    	long quantidadePaginas = ((quantidadeRelogios - 1) / quantidadeCronometrosPorPagina) + 1;
    	long paginaAtual = timeSlot % quantidadePaginas;
    	
        int posicaoTela = 0;
	    RegistroTempoJogo registroTempoMegaSena = (RegistroTempoJogo) listaCronometros.get("MEGA");
	    if (registroTempoMegaSena != null) {
	        getLabelFechamentoMegaSena().setLocation(
	        	((posicaoTela % quantidadeCronometrosPorPagina) % CRONOMETRO_POR_LINHA) * LARGURA_CRONOMETRO,
	            ((posicaoTela % quantidadeCronometrosPorPagina) / CRONOMETRO_POR_LINHA) * ALTURA_CRONOMETRO
	        );
	        getLabelFechamentoMegaSena().setText(
	            registroTempoMegaSena.getNumeroConcurso() + " - "
	            + CronometroJogo.getTempoRestanteHhMmSs(registroTempoMegaSena.getDataLimite())
	            + "  ");
	        boolean visible = (posicaoTela / quantidadeCronometrosPorPagina) == paginaAtual;
	        getLabelFechamentoMegaSena().setVisible(visible);
	        posicaoTela++;
	    } else {
	        getLabelFechamentoMegaSena().setVisible(false);
	    }
	
	    
	    RegistroTempoJogo registroTempoQuina = (RegistroTempoJogo) listaCronometros.get("QUINA");
	    if (registroTempoQuina != null) {
	        getLabelFechamentoQuina().setLocation(
	            	((posicaoTela % quantidadeCronometrosPorPagina) % CRONOMETRO_POR_LINHA) * LARGURA_CRONOMETRO,
	                ((posicaoTela % quantidadeCronometrosPorPagina) / CRONOMETRO_POR_LINHA) * ALTURA_CRONOMETRO
	            );
	        getLabelFechamentoQuina().setText(
	            registroTempoQuina.getNumeroConcurso() + " - "
	            + CronometroJogo.getTempoRestanteHhMmSs(registroTempoQuina.getDataLimite()) + "  ");
	        boolean visible = (posicaoTela / quantidadeCronometrosPorPagina) == paginaAtual;
	        getLabelFechamentoQuina().setVisible(visible);
	        posicaoTela++;
	    } else {
	        getLabelFechamentoQuina().setVisible(false);
	    }
	
	
	    RegistroTempoJogo registroTempoLotofacil = (RegistroTempoJogo) listaCronometros.get("LTFACIL");
	    if (registroTempoLotofacil != null) {
	        getLabelFechamentoLotofacil().setLocation(
	            	((posicaoTela % quantidadeCronometrosPorPagina) % CRONOMETRO_POR_LINHA) * LARGURA_CRONOMETRO,
	                ((posicaoTela % quantidadeCronometrosPorPagina) / CRONOMETRO_POR_LINHA) * ALTURA_CRONOMETRO
	            );
	        getLabelFechamentoLotofacil().setText(
	            registroTempoLotofacil.getNumeroConcurso() + " - "
	            + CronometroJogo.getTempoRestanteHhMmSs(registroTempoLotofacil.getDataLimite())
	            + "  ");
	        boolean visible = (posicaoTela / quantidadeCronometrosPorPagina) == paginaAtual;
	        getLabelFechamentoLotofacil().setVisible(visible);
	        posicaoTela++;
	    } else {
	        getLabelFechamentoLotofacil().setVisible(false);
	    }
	
	
	    RegistroTempoJogo registroTempoLotomania = (RegistroTempoJogo) listaCronometros.get("LTMANIA");
	    if (registroTempoLotomania != null) {
	        getLabelFechamentoLotomania().setLocation(
	            	((posicaoTela % quantidadeCronometrosPorPagina) % CRONOMETRO_POR_LINHA) * LARGURA_CRONOMETRO,
	                ((posicaoTela % quantidadeCronometrosPorPagina) / CRONOMETRO_POR_LINHA) * ALTURA_CRONOMETRO
	            );
	        getLabelFechamentoLotomania().setText(
	            registroTempoLotomania.getNumeroConcurso() + " - "
	            + CronometroJogo.getTempoRestanteHhMmSs(registroTempoLotomania.getDataLimite())
	            + "  ");
	        boolean visible = (posicaoTela / quantidadeCronometrosPorPagina) == paginaAtual;
	        getLabelFechamentoLotomania().setVisible(visible);
	        posicaoTela++;
	    } else {
	        getLabelFechamentoLotomania().setVisible(false);
	    }
	
	
	    RegistroTempoJogo registroTempoDuplaSena = (RegistroTempoJogo) listaCronometros.get("DUPLA");
	    if (registroTempoDuplaSena != null) {
	        getLabelFechamentoDuplaSena().setLocation(
	            	((posicaoTela % quantidadeCronometrosPorPagina) % CRONOMETRO_POR_LINHA) * LARGURA_CRONOMETRO,
	                ((posicaoTela % quantidadeCronometrosPorPagina) / CRONOMETRO_POR_LINHA) * ALTURA_CRONOMETRO
	            );
	        getLabelFechamentoDuplaSena().setText(
	            registroTempoDuplaSena.getNumeroConcurso() + " - "
	            + CronometroJogo.getTempoRestanteHhMmSs(registroTempoDuplaSena.getDataLimite())
	            + "  ");
	        boolean visible = (posicaoTela / quantidadeCronometrosPorPagina) == paginaAtual;
	        getLabelFechamentoDuplaSena().setVisible(visible);
	        posicaoTela++;
	    } else {
	        getLabelFechamentoDuplaSena().setVisible(false);
	    }
	
	    RegistroTempoJogo registroTempoLoteca = (RegistroTempoJogo) listaCronometros.get("LOTECA");
	    if (registroTempoLoteca == null) {
	    	registroTempoLoteca = (RegistroTempoJogo) listaCronometros.get("LOTECA ESP");
	    }
	    if (registroTempoLoteca != null) {
	        getLabelFechamentoLoteca().setLocation(
	            	((posicaoTela % quantidadeCronometrosPorPagina) % CRONOMETRO_POR_LINHA) * LARGURA_CRONOMETRO,
	                ((posicaoTela % quantidadeCronometrosPorPagina) / CRONOMETRO_POR_LINHA) * ALTURA_CRONOMETRO
	            );
	        getLabelFechamentoLoteca().setText(
	            registroTempoLoteca.getNumeroConcurso() + " - "
	            + CronometroJogo.getTempoRestanteHhMmSs(registroTempoLoteca.getDataLimite()) + "  ");
	        boolean visible = (posicaoTela / quantidadeCronometrosPorPagina) == paginaAtual;
	        getLabelFechamentoLoteca().setVisible(visible);
	        posicaoTela++;
	    } else {
	        getLabelFechamentoLoteca().setVisible(false);
	    }
	
	    RegistroTempoJogo registroTempoLotogol = (RegistroTempoJogo) listaCronometros.get("LOTOGOL");
	    if (registroTempoLotogol != null) {
	        getLabelFechamentoLotogol().setLocation(
	            	((posicaoTela % quantidadeCronometrosPorPagina) % CRONOMETRO_POR_LINHA) * LARGURA_CRONOMETRO,
	                ((posicaoTela % quantidadeCronometrosPorPagina) / CRONOMETRO_POR_LINHA) * ALTURA_CRONOMETRO
	            );
	        getLabelFechamentoLotogol().setText(
	            registroTempoLotogol.getNumeroConcurso() + " - "
	            + CronometroJogo.getTempoRestanteHhMmSs(registroTempoLotogol.getDataLimite())
	            + "  ");
	        boolean visible = (posicaoTela / quantidadeCronometrosPorPagina) == paginaAtual;
	        getLabelFechamentoLotogol().setVisible(visible);
	        posicaoTela++;
	    } else {
	        getLabelFechamentoLotogol().setVisible(false);
	    }
	
	    RegistroTempoJogo registroTempoTimemania = (RegistroTempoJogo) listaCronometros.get("TIMEMANIA");
	    if (registroTempoTimemania != null) {
	        getLabelFechamentoTimemania().setLocation(
	            	((posicaoTela % quantidadeCronometrosPorPagina) % CRONOMETRO_POR_LINHA) * LARGURA_CRONOMETRO,
	                ((posicaoTela % quantidadeCronometrosPorPagina) / CRONOMETRO_POR_LINHA) * ALTURA_CRONOMETRO
	            );
	        getLabelFechamentoTimemania().setText(
	        		registroTempoTimemania.getNumeroConcurso() + " - "
	                + CronometroJogo.getTempoRestanteHhMmSs(registroTempoTimemania.getDataLimite())
	                + "  ");
	        boolean visible = (posicaoTela / quantidadeCronometrosPorPagina) == paginaAtual;
	        getLabelFechamentoTimemania().setVisible(visible);
	        posicaoTela++;
	    } else {
	    	getLabelFechamentoTimemania().setVisible(false);
	    }
	    
	    RegistroTempoJogo registroTempoDiaDeSorte = (RegistroTempoJogo) listaCronometros.get("DIA DE SORTE");
	    if (registroTempoDiaDeSorte != null) {
	        getLabelFechamentoDiaDeSorte().setLocation(
	            	((posicaoTela % quantidadeCronometrosPorPagina) % CRONOMETRO_POR_LINHA) * LARGURA_CRONOMETRO,
	                ((posicaoTela % quantidadeCronometrosPorPagina) / CRONOMETRO_POR_LINHA) * ALTURA_CRONOMETRO
	            );
	        getLabelFechamentoDiaDeSorte().setText(
	        		registroTempoDiaDeSorte.getNumeroConcurso() + " - "
	                + CronometroJogo.getTempoRestanteHhMmSs(registroTempoDiaDeSorte.getDataLimite())
	                + "  ");
	        boolean visible = (posicaoTela / quantidadeCronometrosPorPagina) == paginaAtual;
	        getLabelFechamentoDiaDeSorte().setVisible(visible);
	        posicaoTela++;
	    } else {
	    	getLabelFechamentoDiaDeSorte().setVisible(false);
	    }
	    
	    
	    RegistroTempoJogo registroTempoSuperSete = (RegistroTempoJogo) listaCronometros.get("SUPER SETE");
	    if (registroTempoSuperSete != null) {
	        getLabelFechamentoSuperSete().setLocation(
	            	((posicaoTela % quantidadeCronometrosPorPagina) % CRONOMETRO_POR_LINHA) * LARGURA_CRONOMETRO,
	                ((posicaoTela % quantidadeCronometrosPorPagina) / CRONOMETRO_POR_LINHA) * ALTURA_CRONOMETRO
	            );
	        getLabelFechamentoSuperSete().setText(
	        		registroTempoSuperSete.getNumeroConcurso() + " - "
	                + CronometroJogo.getTempoRestanteHhMmSs(registroTempoSuperSete.getDataLimite())
	                + "  ");
	        boolean visible = (posicaoTela / quantidadeCronometrosPorPagina) == paginaAtual;
	        getLabelFechamentoSuperSete().setVisible(visible);
	        posicaoTela++;
	    } else {
	    	getLabelFechamentoSuperSete().setVisible(false);
	    }
	    
	    RegistroTempoJogo registroTempoMilionaria = (RegistroTempoJogo) listaCronometros.get("MILIONARIA");
	    if (registroTempoMilionaria != null) {
	        getLabelFechamentoMilionaria().setLocation(
	            	((posicaoTela % quantidadeCronometrosPorPagina) % CRONOMETRO_POR_LINHA) * LARGURA_CRONOMETRO,
	                ((posicaoTela % quantidadeCronometrosPorPagina) / CRONOMETRO_POR_LINHA) * ALTURA_CRONOMETRO
	            );
	        getLabelFechamentoMilionaria().setText(
	        		registroTempoMilionaria.getNumeroConcurso() + " - "
	                + CronometroJogo.getTempoRestanteHhMmSs(registroTempoMilionaria.getDataLimite())
	                + "  ");
	        boolean visible = (posicaoTela / quantidadeCronometrosPorPagina) == paginaAtual;
	        getLabelFechamentoMilionaria().setVisible(visible);
	        posicaoTela++;
	    } else {
	    	getLabelFechamentoMilionaria().setVisible(false);
	    }
	    
	    
	    RegistroTempoJogo registroTempoEncalheFederal = (RegistroTempoJogo) listaCronometros.get("ENCALHE");
	    if (registroTempoEncalheFederal != null) {
	        getLabelFechamentoEncalheFederal().setLocation(
	            	((posicaoTela % quantidadeCronometrosPorPagina) % CRONOMETRO_POR_LINHA) * LARGURA_CRONOMETRO,
	                ((posicaoTela % quantidadeCronometrosPorPagina) / CRONOMETRO_POR_LINHA) * ALTURA_CRONOMETRO
	            );
	        getLabelFechamentoEncalheFederal().setText(
	            CronometroJogo.getTempoRestanteHhMmSs(registroTempoEncalheFederal.getDataLimite())
	            + "  ");
	        boolean visible = (posicaoTela / quantidadeCronometrosPorPagina) == paginaAtual;
	        getLabelFechamentoEncalheFederal().setVisible(visible);
	    } else {
	        getLabelFechamentoEncalheFederal().setVisible(false);
	    }
        RegistroTempoJogo registroTempoMKP = (RegistroTempoJogo) listaCronometros.get("MARKETPLACE");
        if (registroTempoMKP != null) {
        	setSize(LARGURA_CRONOMETRO * CRONOMETRO_POR_LINHA + 160, ALTURA_CRONOMETRO * CRONOMETRO_POR_COLUNA + 21);
            getLabelFechamentoMKP().setLocation(
	            	((posicaoTela % quantidadeCronometrosPorPagina) % CRONOMETRO_POR_LINHA) * LARGURA_CRONOMETRO,
	                ((posicaoTela % quantidadeCronometrosPorPagina) / CRONOMETRO_POR_LINHA) * ALTURA_CRONOMETRO
	            );
            getLabelFechamentoMKP().setText(
            		CronometroCotasVirtuais.getTempoRestanteHhMmSs(registroTempoMKP.getDataLimite()));
		            
            boolean visible = (posicaoTela / quantidadeCronometrosPorPagina) == paginaAtual;
            getLabelFechamentoMKP().setVisible(visible);
            posicaoTela++;
        } else {
        	getLabelFechamentoMKP().setVisible(false);
        }
        
    }

    public EFLLabel getLabelFechamentoMegaSena() {
        if (labelFechamentoMegaSena == null) {
            labelFechamentoMegaSena = new EFLLabel(LabelSkin.LABEL_HORA_ENCERRAMENTO_MEGASENA);
            labelFechamentoMegaSena.setSize(LARGURA_CRONOMETRO, 20);
            labelFechamentoMegaSena.setHorizontalAlignment(SwingConstants.RIGHT);
            labelFechamentoMegaSena.setOpaque(true);
            labelFechamentoMegaSena.setVisible(false);
        }
        return labelFechamentoMegaSena;
    }

    public EFLLabel getLabelFechamentoQuina() {
        if (labelFechamentoQuina == null) {
            labelFechamentoQuina = new EFLLabel(LabelSkin.LABEL_HORA_ENCERRAMENTO_QUINA);
            labelFechamentoQuina.setSize(LARGURA_CRONOMETRO, 20);
            labelFechamentoQuina.setHorizontalAlignment(SwingConstants.RIGHT);
            labelFechamentoQuina.setOpaque(true);
            labelFechamentoQuina.setVisible(false);
        }
        return labelFechamentoQuina;
    }

    public EFLLabel getLabelFechamentoLotofacil() {
        if (labelFechamentoLotofacil == null) {
            labelFechamentoLotofacil = new EFLLabel(LabelSkin.LABEL_HORA_ENCERRAMENTO_LOTOFACIL);
            labelFechamentoLotofacil.setSize(LARGURA_CRONOMETRO, 20);
            labelFechamentoLotofacil.setHorizontalAlignment(SwingConstants.RIGHT);
            labelFechamentoLotofacil.setOpaque(true);
            labelFechamentoLotofacil.setVisible(false);
        }
        return labelFechamentoLotofacil;
    }

    public EFLLabel getLabelFechamentoLotomania() {
        if (labelFechamentoLotomania == null) {
            labelFechamentoLotomania = new EFLLabel(LabelSkin.LABEL_HORA_ENCERRAMENTO_LOTOMANIA);
            labelFechamentoLotomania.setSize(LARGURA_CRONOMETRO, 20);
            labelFechamentoLotomania.setHorizontalAlignment(SwingConstants.RIGHT);
            labelFechamentoLotomania.setOpaque(true);
            labelFechamentoLotomania.setVisible(false);
        }
        return labelFechamentoLotomania;
    }

    public EFLLabel getLabelFechamentoDuplaSena() {
        if (labelFechamentoDuplaSena == null) {
            labelFechamentoDuplaSena = new EFLLabel(LabelSkin.LABEL_HORA_ENCERRAMENTO_DUPLASENA);
            labelFechamentoDuplaSena.setSize(LARGURA_CRONOMETRO, 20);
            labelFechamentoDuplaSena.setHorizontalAlignment(SwingConstants.RIGHT);
            labelFechamentoDuplaSena.setOpaque(true);
            labelFechamentoDuplaSena.setVisible(false);
        }
        return labelFechamentoDuplaSena;
    }

    public EFLLabel getLabelFechamentoLoteca() {
        if (labelFechamentoLoteca == null) {
            labelFechamentoLoteca = new EFLLabel(LabelSkin.LABEL_HORA_ENCERRAMENTO_LOTECA);
            labelFechamentoLoteca.setSize(LARGURA_CRONOMETRO, 20);
            labelFechamentoLoteca.setHorizontalAlignment(SwingConstants.RIGHT);
            labelFechamentoLoteca.setOpaque(true);
            labelFechamentoLoteca.setVisible(false);
        }
        return labelFechamentoLoteca;
    }

    public EFLLabel getLabelFechamentoLotogol() {
        if (labelFechamentoLotogol == null) {
            labelFechamentoLotogol = new EFLLabel(LabelSkin.LABEL_HORA_ENCERRAMENTO_LOTOGOL);
            labelFechamentoLotogol.setSize(LARGURA_CRONOMETRO, 20);
            labelFechamentoLotogol.setHorizontalAlignment(SwingConstants.RIGHT);
            labelFechamentoLotogol.setOpaque(true);
            labelFechamentoLotogol.setVisible(false);
        }
        return labelFechamentoLotogol;
    }

    public EFLLabel getLabelFechamentoEncalheFederal() {
        if (labelFechamentoEncalhe == null) {
            labelFechamentoEncalhe = new EFLLabel(LabelSkin.LABEL_HORA_ENCERRAMENTO_FEDERAL);
            labelFechamentoEncalhe.setSize(LARGURA_CRONOMETRO, 20);
            labelFechamentoEncalhe.setHorizontalAlignment(SwingConstants.RIGHT);
            labelFechamentoEncalhe.setOpaque(true);
            labelFechamentoEncalhe.setVisible(false);
        }
        return labelFechamentoEncalhe;
    }

    public EFLLabel getLabelFechamentoTimemania() {
        if (labelFechamentoTimemania == null) {
        	labelFechamentoTimemania = new EFLLabel(LabelSkin.LABEL_HORA_ENCERRAMENTO_TIMEMANIA);
        	labelFechamentoTimemania.setSize(LARGURA_CRONOMETRO, 20);
        	labelFechamentoTimemania.setHorizontalAlignment(SwingConstants.RIGHT);
        	labelFechamentoTimemania.setOpaque(true);
        	labelFechamentoTimemania.setVisible(false);
        }
        return labelFechamentoTimemania;
    }
    
    public EFLLabel getLabelFechamentoDiaDeSorte() {
        if (labelFechamentoDiaDeSorte == null) {
        	labelFechamentoDiaDeSorte = new EFLLabel(LabelSkin.LABEL_HORA_ENCERRAMENTO_DIA_DE_SORTE);
        	labelFechamentoDiaDeSorte.setSize(LARGURA_CRONOMETRO, 20);
        	labelFechamentoDiaDeSorte.setHorizontalAlignment(SwingConstants.RIGHT);
        	labelFechamentoDiaDeSorte.setOpaque(true);
        	labelFechamentoDiaDeSorte.setVisible(false);
        }
        return labelFechamentoDiaDeSorte;
    }

    public EFLLabel getLabelFechamentoSuperSete() {
        if (labelFechamentoSuperSete == null) {
        	labelFechamentoSuperSete = new EFLLabel(LabelSkin.LABEL_HORA_ENCERRAMENTO_SUPER_SETE);
        	labelFechamentoSuperSete.setSize(LARGURA_CRONOMETRO, 20);
        	labelFechamentoSuperSete.setHorizontalAlignment(SwingConstants.RIGHT);
        	labelFechamentoSuperSete.setOpaque(true);
        	labelFechamentoSuperSete.setVisible(false);
        }
        return labelFechamentoSuperSete;
    }
    
    public EFLLabel  getLabelFechamentoMilionaria() {
        if (labelFechamentoMilionaria == null) {
        	labelFechamentoMilionaria = new EFLLabel(LabelSkin.LABEL_HORA_ENCERRAMENTO_MILIONARIA);
        	labelFechamentoMilionaria.setSize(LARGURA_CRONOMETRO, 20);
        	labelFechamentoMilionaria.setHorizontalAlignment(SwingConstants.RIGHT);
        	labelFechamentoMilionaria.setOpaque(true);
        	labelFechamentoMilionaria.setVisible(false);
        }
        return labelFechamentoMilionaria;
	}
    public EFLLabel  getLabelFechamentoMKP() {
        if (labelFechamentoMKP == null) {
        	labelFechamentoMKP = new EFLLabel(LabelSkin.LABEL_HORA_ENCERRAMENTO_MARKETPLACE);
        	labelFechamentoMKP.setSize(LARGURA_CRONOMETRO, 20);
        	labelFechamentoMKP.setHorizontalAlignment(SwingConstants.RIGHT);
        	labelFechamentoMKP.setOpaque(true);
        	labelFechamentoMKP.setVisible(false);
        }
        return labelFechamentoMKP;
	}
}
