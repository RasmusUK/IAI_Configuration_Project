import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;

public class Chess25 implements IQueensLogic {
    private int size;
    private int[][] board;
    private BDD[][] BDDs;
    private BDDFactory fact;
    private BDD bdd;

    @Override
    public void initializeBoard(int size) {
        this.size = size;
        board = new int[size][size];
        BDDs = new BDD[size][size];
        fact = JFactory.init(2000000, 200000);
        fact.setVarNum((size * size));
        bdd = null;

        int counter = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                BDDs[i][j] = fact.ithVar(counter);
                counter++;
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (bdd == null) bdd = bind(i, j);
                else bdd = bdd.and(bind(i, j));
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
        bdd = restrict(column, row);
        updateBoard();
    }

    // Restricts the BDD based on entered column and row
    private BDD restrict(int column, int row) {
        int var = column * size + row;
        return bdd.restrict(fact.ithVar(var));
    }

    // Updates the board according to restrictions
    private void updateBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int status = board[i][j];
                if (status != 0) continue;
                board[i][j] = getUpdatedStatus(i, j);
            }
        }
    }

    // Returns the updated status of the cell
    private int getUpdatedStatus(int i, int j) {
        var temp = restrict(i, j);
        if (temp.isZero()) return -1;
        if (temp.isOne()) return 1;
        return 0;
    }

    // Returns a cells restrictions
    private BDD bind(int col, int row) {
        var tree = bindCol(null, col, row);
        tree = bindRow(tree, col, row);
        tree = bindDiagonals(tree, col, row);
        return BDDs[col][row].xor(tree);
    }

    // Returns cells vertical restrictions
    private BDD bindCol(BDD tree, int col, int row) {
        for (int i = 0; i < size; i++) tree = orWithoutDuplicationOfRoot(tree, col, row, col, i);
        return tree;
    }

    //Returns cells horizontal restrictions
    private BDD bindRow(BDD tree, int col, int row) {
        for (int i = 0; i < size; i++) tree = orWithoutDuplicationOfRoot(tree, col, row, i, row);
        return tree;
    }

    //Returns cells diagonal restrictions
    private BDD bindDiagonals(BDD tree, int col, int row) {
        var min = Math.min(col, row);
        var startCol = col - min;
        var startRow = row - min;
        while (startRow < (size - 1) && startCol < (size - 1)) {
            tree = orWithoutDuplicationOfRoot(tree, col, row, startCol, startRow);
            startCol++;
            startRow++;
        }
        startCol = col + row;
        startRow = 0;
        var delta = startCol - (size - 1);
        if (delta > 0) {
            startCol -= delta;
            startRow += delta;
        }
        while (startCol > -1 && startRow < (size - 1)) {
            tree = orWithoutDuplicationOfRoot(tree, col, row, startCol, startRow);
            startCol--;
            startRow++;
        }
        return tree;
    }

    //Adds a BDD as an or operation to the tree without duplicating the root
    private BDD orWithoutDuplicationOfRoot(BDD bdd, int rootCol, int rootRow, int col, int row) {
        if (rootCol != col || rootRow != row) {
            if (bdd == null) return BDDs[col][row];
            else return bdd.or(BDDs[col][row]);
        }
        return bdd;
    }
}
