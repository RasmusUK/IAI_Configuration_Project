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
        bdd = null;

        //Create all variables
        int counter = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                bdds[i][j] = fact.ithVar(counter);
                counter++;
            }
        }

        //Diagonal
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                BDD var = bdds[i][j];
                ArrayList<BDD> allBdds = new ArrayList<>();

                //Diagonal
                allBdds.addAll(getBdds(i,j,1,1));
                allBdds.addAll(getBdds(i,j,1,-1));
                allBdds.addAll(getBdds(i,j,-1,-1));
                allBdds.addAll(getBdds(i,j,-1,1));

                //Horizontal
                for (int k = 0; k < size; k++) {
                    if(k == j) continue;
                    allBdds.add(bdds[i][k]);
                }

                //Vertical
                for (int k = 0; k < size; k++) {
                    if(k == i) continue;
                    allBdds.add(bdds[k][j]);
                }

                //Right side of implies x -> not x1 and not x2 ...
                BDD temp = null;
                for (BDD bdd: allBdds) {
                    if(temp == null) temp = bdd.not();
                    else temp = temp.and(bdd.not());
                }
                //Do the actual implies
                var = var.imp(temp);

                //Add as and to bdd
                if(bdd == null) bdd = var;
                else bdd = bdd.and(var);
            }
        }

        //There must be one queen horizontal
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

        //There must be one queen vertical
        for (int i = 0; i < size; i++) {
            BDD temp = null;
            for (int j = 0; j < size; j++) {
                BDD var = bdds[j][i];
                if(temp == null) temp = var;
                else temp = temp.or(var);
            }
            bdd = bdd.and(temp);
        }
        //Update board to get start positions allowed if playing on smaller board - e.g. 6
        updateBoard();
    }

    //Get the diagonal bdd
    private ArrayList<BDD> getBdds(int i, int j, int iSign, int jSign){
        ArrayList<BDD> tempBdds = new ArrayList<>();
        while(true){
            i = i + iSign;
            j = j + jSign;
            if(i >= size || j >= size ||i <0 || j < 0) break;
            tempBdds.add(bdds[i][j]);
        }
        return tempBdds;
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

    //Do a true assignment
    private BDD restrictTrue(int column, int row){
        int var = column * size + row;
        return bdd.restrict(fact.ithVar(var));
    }

    //Do a false assignment
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

    //See where a queen cannot be or must be
    private int getUpdatedStatus(int i, int j){
        var temp = restrictTrue(i,j);
        if(temp.isZero())return -1;
        temp = restrictFalse(i,j);
        if(temp.isZero()) return 1;
        return 0;
    }
}
