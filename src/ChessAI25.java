import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;

import java.util.*;

public class ChessAI25 implements IQueensLogic{
    private int size;
    private int board[][];
    private BDD[][] bdds;
    private BDDFactory fact;
    private BDD bdd;

    @Override
    public void initializeBoard(int size) {
        this.size = size;
        board = new int[size][size];
        bdds = new BDD[size][size];
        fact = JFactory.init(2000000,200000);
        fact.setVarNum(size*size);


        int counter = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                bdds[i][j] = fact.ithVar(counter);
                counter++;
            }
        }

        bdd = null;
        /*
        for (int k = 0; k < size; k++) {
            BDD bddTemp = null;
            for (int i = 0; i < size; i++) {
                BDD var = bdds[k][i];
                for (int j = 0; j < size; j++) {
                    if(i == j) continue;
                    BDD local = bdds[k][j].not();
                    var = var.and(local);
                }
                fact.printTable(var);
                if(bddTemp == null) bddTemp = var;
                else bddTemp = bddTemp.or(var);
            }
            if(bdd == null) bdd = bddTemp;
            else bdd = bdd.and(bddTemp);
        }

        for (int k = 0; k < size; k++) {
            BDD bddTemp = null;
            for (int i = 0; i < size; i++) {
                BDD var = bdds[i][k];
                for (int j = 0; j < size; j++) {
                    if(i == j) continue;
                    BDD local = bdds[j][k].not();
                    var = var.and(local);
                }
                fact.printTable(var);
                if(bddTemp == null) bddTemp = var;
                else bddTemp = bddTemp.or(var);
            }
            if(bdd == null) bdd = bddTemp;
            else bdd = bdd.and(bddTemp);
        }*/
/*
        for (int i = 0; i < size; i++) {
            BDD bddTemp = null;
            for (int j = 0; j < size; j++) {
                BDD var = bdds[i][j];
                Set<BDD> allBDDS = new HashSet<>();
                allBDDS.add(var);
                int iTemp = i;
                int jTemp = j;
                while(true){
                    iTemp++;
                    jTemp++;
                    if(iTemp >= size || jTemp >= size) break;
                    allBDDS.add(bdds[iTemp][jTemp]);
                }
                iTemp = i;
                jTemp = j;

                while(true){
                    iTemp--;
                    jTemp++;
                    if(iTemp < 0 || jTemp >= size) break;
                    allBDDS.add(bdds[iTemp][jTemp]);
                }
                iTemp = i;
                jTemp = j;

                while(true){
                    iTemp--;
                    jTemp--;
                    if(iTemp <0 || jTemp < 0) break;
                    allBDDS.add(bdds[iTemp][jTemp]);
                }
                iTemp = i;
                jTemp = j;

                while(true){
                    iTemp++;
                    jTemp--;
                    if(iTemp >= size || jTemp < 0) break;
                    allBDDS.add(bdds[iTemp][jTemp]);
                }
                BDD BDDnew = null;
                for (var bddLocal: allBDDS) {

                    for (var bddNext: allBDDS) {
                        if(bddLocal == bddNext) continue;
                        bddLocal.and(bddNext.not());
                    }
                    if(BDDnew == null) BDDnew = bddLocal;
                    else BDDnew.or(bddLocal);
                }

                if(bddTemp == null) bddTemp = BDDnew;
                else bddTemp = bddTemp.or(BDDnew);
            }

            if(bdd == null) bdd = bddTemp;
            else bdd = bdd.and(bddTemp);
        }*/

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                BDD var = bdds[i][j];
                var min = Math.min(i,j);
                var startcol = Math.max(0,i-min);
                var startrow = Math.max(0,j-min);
                System.out.println("min: " + min + "\ncol: " + startcol +"\nstartrow: "+ startrow);
                while(startrow < (size -1) || startcol < (size -1)){
                    if(startcol <= size - 1 && startrow <= size -1) {
                        if (startcol != i || startrow != j) {
                            BDD local = bdds[startcol][startrow].not();
                            var = var.and(local);
                        }
                    }
                    startcol++;
                    startrow++;
                }
                startcol = i + j;
                startrow = 0;
                while (startcol > -1){
                    if(startcol <= size - 1 && startrow <= size -1){
                        if(startcol != i || startrow != j){
                            BDD local = bdds[startcol][startrow].not();
                            var = var.and(local);
                        }
                    }
                    startcol--;
                    startrow++;
                }
                bdd = var;
            }
        }
    }

    @Override
    public int[][] getBoard() {
        return board;
    }

    @Override
    public void insertQueen(int column, int row) {
        board[column][row] = 1;
        bdd = restrict(column,row);
        updateBoard();
    }

    private BDD restrict(int column, int row){
        int var = column * size + row;
        return bdd.restrict(fact.ithVar(var));
    }

    private void updateBoard(){
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int status = board[i][j];
                if(status != 0) continue;
                board[i][j] = getUpdatedStatus(i,j);
            }
        }
    }
    private int getUpdatedStatus(int i, int j){
        var temp = restrict(i,j);
        if(temp.isZero()) return -1;
        if(temp.isOne()) return 1;
        return 0;
    }
}
