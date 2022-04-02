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

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                BDD var = bdds[i][j];
                ArrayList<BDD> allBDDS = new ArrayList<>();
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

                for (int k = 0; k < size; k++) {
                    if(k == j) continue;
                    allBDDS.add(bdds[i][k]);
                }

                for (int k = 0; k < size; k++) {
                    if(k == i) continue;
                    allBDDS.add(bdds[k][j]);
                }


                BDD temp = null;
                for (BDD bdd: allBDDS) {
                    if(temp == null) temp = bdd.not();
                    else temp = temp.and(bdd.not());
                }
                var = var.imp(temp);

                if(bdd == null) bdd = var;
                else bdd = bdd.and(var);
            }
        }

        for (int i = 0; i < size; i++) {
            BDD temp = null;
            for (int j = 0; j < size; j++) {
                BDD var = bdds[i][j];
                if(temp == null) temp = var;
                else temp = temp.or(var);
            }
            if(bdd == null) bdd = temp;
            else bdd = bdd.and(temp);
        }

        for (int i = 0; i < size; i++) {
            BDD temp = null;
            for (int j = 0; j < size; j++) {
                BDD var = bdds[j][i];
                if(temp == null) temp = var;
                else temp = temp.or(var);
            }
            bdd = bdd.and(temp);
        }
        updateBoard();
    }

    @Override
    public int[][] getBoard() {
        return board;
    }

    @Override
    public void insertQueen(int column, int row) {
        board[column][row] = 1;
        bdd = restrictTrue(column,row);
        updateBoard();
    }

    private BDD restrictTrue(int column, int row){
        int var = column * size + row;
        return bdd.restrict(fact.ithVar(var));
    }
    private BDD restrictFalse(int column, int row){
        int var = column * size + row;
        return bdd.restrict(fact.nithVar(var));
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
        var temp = restrictTrue(i,j);
        if(temp.isZero())return -1;
        temp = restrictFalse(i,j);
        if(temp.isZero()) return 1;
        return 0;
    }
}
