package campominado;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CampoMinadoGUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("Campo Minado");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(400, 400);

                int faseEscolhida = escolherFase();

                CampoMinado campoMinado = new CampoMinado(faseEscolhida);
                JPanel panel = criarPainel(campoMinado, frame);

                frame.getContentPane().add(panel);
                frame.setVisible(true);
            }
        });
    }

    private static int escolherFase() {
        String[] opcoes = {"Fase 1 (Simples)", "Fase 2 (Complexa)"};
        int escolha = JOptionPane.showOptionDialog(
                null,
                "Escolha a fase:",
                "Escolha de Fase",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcoes,
                opcoes[0]
        );

        return escolha + 1;
    }

    private static JPanel criarPainel(CampoMinado campoMinado, JFrame frame) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(campoMinado.getLinhas(), campoMinado.getColunas()));

        for (int i = 0; i < campoMinado.getLinhas(); i++) {
            for (int j = 0; j < campoMinado.getColunas(); j++) {
                JButton button = new JButton();
                button.addActionListener(new BlocoClickListener(i, j, campoMinado, button, frame));
                panel.add(button);
            }
        }
        return panel;
    }
}

class BlocoClickListener implements ActionListener {
    private final int linha;
    private final int coluna;
    private final CampoMinado campoMinado;
    private final JButton button;
    private final JFrame frame;

    public BlocoClickListener(int linha, int coluna, CampoMinado campoMinado, JButton button, JFrame frame) {
        this.linha = linha;
        this.coluna = coluna;
        this.campoMinado = campoMinado;
        this.button = button;
        this.frame = frame;
    }

    public void actionPerformed(ActionEvent e) {
        Bloco blocoClicado = campoMinado.getBloco(linha, coluna);

        if (!blocoClicado.isAberto()) {
            campoMinado.abrirBloco(linha, coluna);

            if (blocoClicado.isBomba()) {
                revelarBombas();
                JOptionPane.showMessageDialog(frame, "Você perdeu!", "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
                reiniciarJogo();
            } else {
                atualizarInterface();
            }
        }
        button.setEnabled(false);
    }

    private void revelarBombas() {
        for (int i = 0; i < campoMinado.getLinhas(); i++) {
            for (int j = 0; j < campoMinado.getColunas(); j++) {
                Bloco bloco = campoMinado.getBloco(i, j);
                if (bloco.isBomba()) {
                    JButton bombaButton = getButton(i, j);
                    bombaButton.setIcon(new ImageIcon("C:\\Users\\lucas\\OneDrive\\Área de Trabalho\\POO\\Bomba.png"));
                }
            }
        }
    }

    private JButton getButton(int linha, int coluna) {
        Component[] components = button.getParent().getComponents();
        int index = linha * campoMinado.getColunas() + coluna;
        return (JButton) components[index];
    }

    private void atualizarInterface() {
        Bloco blocoClicado = campoMinado.getBloco(linha, coluna);

        if (blocoClicado.isBandeira()) {
            button.setIcon(new ImageIcon("C:\\Users\\lucas\\OneDrive\\Área de Trabalho\\POO\\Bandeira.png"));
        } else if (blocoClicado.getNumero() > 0) {
            button.setText(String.valueOf(blocoClicado.getNumero()));
        } else {
            button.setIcon(new ImageIcon("C:\\Users\\lucas\\OneDrive\\Área de Trabalho\\POO\\Bloco.png"));
        }
    }

    private void reiniciarJogo() {
        int resposta = JOptionPane.showConfirmDialog(frame, "Deseja jogar novamente?", "Reiniciar Jogo", JOptionPane.YES_NO_OPTION);
        if (resposta == JOptionPane.YES_OPTION) {
            frame.dispose();
            CampoMinadoGUI.main(new String[0]);
        } else {
            System.exit(0);
        }
    }
}

class CampoMinado {
    private static final int LINHAS = 8;
    private static final int COLUNAS = 8;
    private static final int BOMBAS = 10;

    private Bloco[][] tabuleiro;

    public CampoMinado(int fase) {
        inicializarTabuleiro();
        distribuirBombas();
        calcularNumeros();
        configurarFase(fase);
    }

    private void configurarFase(int fase) {
        try {
            String fileName = "fase" + fase + ".txt";
            Path filePath = Paths.get("src/campominado/", fileName);

            List<String> lines = Files.readAllLines(filePath);

            for (int i = 0; i < LINHAS; i++) {
                String[] values = lines.get(i).split(",");
                for (int j = 0; j < COLUNAS; j++) {
                    tabuleiro[i][j].setBomba(Boolean.parseBoolean(values[j].trim()));
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo de fase:");
            e.printStackTrace();
        }
    }



    private void inicializarTabuleiro() {
        tabuleiro = new Bloco[LINHAS][COLUNAS];
        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {
                tabuleiro[i][j] = new Bloco();
            }
        }
    }

    private void distribuirBombas() {
        Random random = new Random();
        int bombasColocadas = 0;

        while (bombasColocadas < BOMBAS) {
            int linha = random.nextInt(LINHAS);
            int coluna = random.nextInt(COLUNAS);

            if (!tabuleiro[linha][coluna].isBomba()) {
                tabuleiro[linha][coluna].setBomba(true);
                bombasColocadas++;
            }
        }
    }

    private void calcularNumeros() {
        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {
                if (!tabuleiro[i][j].isBomba()) {
                    int numero = contarBombasAoRedor(i, j);
                    tabuleiro[i][j].setNumero(numero);
                }
            }
        }
    }

    private int contarBombasAoRedor(int linha, int coluna) {
        int bombasAoRedor = 0;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int novaLinha = linha + i;
                int novaColuna = coluna + j;

                if (novaLinha >= 0 && novaLinha < LINHAS && novaColuna >= 0 && novaColuna < COLUNAS) {
                    if (tabuleiro[novaLinha][novaColuna].isBomba()) {
                        bombasAoRedor++;
                    }
                }
            }
        }

        return bombasAoRedor;
    }

    public int getLinhas() {
        return LINHAS;
    }

    public int getColunas() {
        return COLUNAS;
    }

    public Bloco getBloco(int linha, int coluna) {
        return tabuleiro[linha][coluna];
    }

    public void abrirBloco(int linha, int coluna) {
        Bloco bloco = tabuleiro[linha][coluna];
        bloco.setAberto(true);

        if (bloco.getNumero() == 0) {
            abrirBlocosAdjacentes(linha, coluna);
        }
    }

    private void abrirBlocosAdjacentes(int linha, int coluna) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int novaLinha = linha + i;
                int novaColuna = coluna + j;

                if (novaLinha >= 0 && novaLinha < LINHAS && novaColuna >= 0 && novaColuna < COLUNAS) {
                    Bloco blocoAdjacente = tabuleiro[novaLinha][novaColuna];

                    if (!blocoAdjacente.isAberto()) {
                        abrirBloco(novaLinha, novaColuna);
                    }
                }
            }
        }
    }
}

class Bloco {
    private boolean bomba;
    private boolean aberto;
    private boolean bandeira;
    private int numero;

    public Bloco() {
    }

    public boolean isBomba() {
        return bomba;
    }

    public void setBomba(boolean bomba) {
        this.bomba = bomba;
    }

    public boolean isAberto() {
        return aberto;
    }

    public void setAberto(boolean aberto) {
        this.aberto = aberto;
    }

    public boolean isBandeira() {
        return bandeira;
    }

    public void setBandeira(boolean bandeira) {
        this.bandeira = bandeira;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }
}
