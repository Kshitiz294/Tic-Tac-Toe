package kshitizgupta.bluetoothpractice;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


public class PlayFragment extends Fragment {

    private boolean is_O_turn=false;
    private char gameBoard[][] = new char[3][3];

    TextView nextTurnView;

    public static View view;

    AlertDialog dialog;
    AlertDialog.Builder builder;
    ProgressDialog progressDialog;

    public interface MessageInterface{
        void sendMessage(String string);
    }


    public void respondToMessage(String string) {

        if(string.charAt(0) == 'O'){
            newGameRequest(string);
        }else{
            int no= Integer.parseInt(string);
            int x= no%10;
            no= no/10;
            int y= no%10;
            gameBoard[x][y] = !is_O_turn ? 'O' : 'X';
            TableLayout T = (TableLayout) view.findViewById(R.id.tableLayout);
            if (T.getChildAt(y) instanceof TableRow){
                TableRow TR = (TableRow) T.getChildAt(y);
                if (TR.getChildAt(x) instanceof Button){
                    Button B = (Button) TR.getChildAt(x);
                    B.setTextColor(!is_O_turn ?
                            getResources().getColor(R.color.colorPrimary) :
                            getResources().getColor(R.color.colorRed));
                    B.setTextSize(20);
                    B.setText(!is_O_turn ? "O" : "X");
                    B.setEnabled(false);
                }
            }
            if(checkWin()){
                disableGameButtons();
                TextView nextTurnView = (TextView) view.findViewById(R.id.nextTurn);
                nextTurnView.setText("X's Turn");
            }else{
                enableGameButtons();
                nextTurnView.setText(is_O_turn ? "O's Turn" : "X's Turn");
            }
        }
    }

    public void send(String string){
        disableGameButtons();
        nextTurnView.setText(!is_O_turn ? "O's Turn" : "X's Turn");
        ((MessageInterface)getActivity()).sendMessage(string);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_single_player, container, false);

        nextTurnView = (TextView) PlayFragment.view.findViewById(R.id.nextTurn);
        resetButtons();
        setupOnClickListeners();
        if(Common.IS_SERVER) {
            is_O_turn= false;
            nextTurnView.setText(is_O_turn ? "O's Turn" : "X's Turn");
            enableGameButtons();
        }else if(Common.IS_CLIENT){
            is_O_turn=true;
            disableGameButtons();
            nextTurnView.setText(!is_O_turn ? "O's Turn" : "X's Turn");
        }

        Button new_game=(Button)view.findViewById(R.id.new_game);
        new_game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                builder = new AlertDialog.Builder(getActivity());
                builder.setCancelable(false)
                        .setMessage("Ask Opponent for New Game?")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                progressDialog= ProgressDialog.show(getActivity(),"","Waiting for opponent",true);
                                ((MessageInterface)getActivity()).sendMessage("OOO");
                            }
                        });
                dialog= builder.create();
                dialog.show();
            }
        });
        return view;
    }

    public void newGameRequest(String string){
        if(string.equals("OOO")){
            builder= new AlertDialog.Builder(getActivity());
            builder.setCancelable(false)
                    .setMessage("Opponent Requests a New Game")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            ((MessageInterface)getActivity()).sendMessage("O0");
                        }
                    })
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ((MessageInterface)getActivity()).sendMessage("O1");
                            newGame();
                        }
                    });
            dialog=builder.create();
            dialog.show();
        }else{
            progressDialog.dismiss();
            if(string.charAt(1) == '0'){
                Toast.makeText(getActivity(),"Opponent denied request",Toast.LENGTH_SHORT).show();
            }else if(string.charAt(1) == '1'){
                Toast.makeText(getActivity(),"Opponent accepted request",Toast.LENGTH_SHORT).show();
                newGame();
            }
        }
    }

    public void newGame(){

        is_O_turn = false;
        gameBoard = new char[3][3];
        resetButtons();
        if(Common.IS_SERVER) {
            is_O_turn= false;
            enableGameButtons();
            nextTurnView.setText(is_O_turn ? "O's Turn" : "X's Turn");
        }else if(Common.IS_CLIENT){
            is_O_turn=true;
            disableGameButtons();
            nextTurnView.setText(!is_O_turn ? "O's Turn" : "X's Turn");
        }
    }

    private void resetButtons() {
        TableLayout T = (TableLayout) view.findViewById(R.id.tableLayout);
        for (int y = 0; y < T.getChildCount(); y++) {
            if (T.getChildAt(y) instanceof TableRow) {
                TableRow R = (TableRow) T.getChildAt(y);
                for (int x = 0; x < R.getChildCount(); x++) {
                    if (R.getChildAt(x) instanceof Button) {
                        Button B = (Button) R.getChildAt(x);
                        B.setText("");
                        B.setEnabled(true);
                    }
                }
            }
        }

        TextView nextTurnView = (TextView) view.findViewById(R.id.nextTurn);
        nextTurnView.setText("X's Turn");
    }

    private boolean checkWin() {
        char winner = '\0';
        if (checkWinner(gameBoard, 3, 'X')) {
            winner = 'X';
        } else if (checkWinner(gameBoard, 3, 'O')) {
            winner = 'O';
        }

        if (winner == '\0') {
            return false; // nobody won
        } else {
            // display winner
            Toast.makeText(getActivity(), winner + " wins", Toast.LENGTH_LONG).show();
            return true;
        }
    }

    /**
     * This is a main algorithm for checking if a specific player has won.
     * @return true if the specified player has won
     *
     * Kindly have a look at the image (given after this section) for more details on this scction.
     */
    private boolean checkWinner(char[][] board, int size, char player) {
        //First we check all rows
        for (int x = 0; x < size; x++) {
            int total = 0;
            for (int y = 0; y < size; y++) {
                if (board[x][y] == player) {
                    total++;
                }
            }
            if (total >= size) {
                return true;
            }
        }

        //Then we check all the columns
        for (int y = 0; y < size; y++) {
            int total = 0;
            for (int x = 0; x < size; x++) {
                if (board[x][y] == player) {
                    total++;
                }
            }
            if (total >= size) {
                return true;
            }
        }

        /*
        * This part is checking diagonals (forward)
        * i.e. starting x & y from 0 and compare when x & y are same.
        */
        int total = 0;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (x == y && board[x][y] == player) {
                    total++;
                }
            }
        }
        if (total >= size) {
            return true;
        }

        /*
        * This part is checking diagonals (backward)
        * i.e. starting x & y from 0 and compare when x + y = boardsize - 1 (x=0, y=2 | x=0, y=1 | x=2, y=0).
        */
        total = 0;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (x + y == size - 1 && board[x][y] == player) {
                    total++;
                }
            }
        }
        if (total >= size) {
            return true;
        }

        return false;
    }

    /**
     * Disables all the buttons.
     */
    private void disableGameButtons() {
        TableLayout T = (TableLayout) view.findViewById(R.id.tableLayout);
        for (int y = 0; y < T.getChildCount(); y++) {
            if (T.getChildAt(y) instanceof TableRow) {
                TableRow R = (TableRow) T.getChildAt(y);
                for (int x = 0; x < R.getChildCount(); x++) {
                    if (R.getChildAt(x) instanceof Button) {
                        Button B = (Button) R.getChildAt(x);
                        B.setEnabled(false);
                    }
                }
            }
        }
    }

    private void enableGameButtons() {
        TableLayout T = (TableLayout) view.findViewById(R.id.tableLayout);
        for (int y = 0; y < T.getChildCount(); y++) {
            if (T.getChildAt(y) instanceof TableRow) {
                TableRow R = (TableRow) T.getChildAt(y);
                for (int x = 0; x < R.getChildCount(); x++) {
                    if (R.getChildAt(x) instanceof Button) {
                        Button B = (Button) R.getChildAt(x);
                        if(B.getText().equals("")) {
                            B.setEnabled(true);
                        }
                    }
                }
            }
        }
    }

    private void setupOnClickListeners() {
        TableLayout T = (TableLayout) view.findViewById(R.id.tableLayout);
        for (int y = 0; y < T.getChildCount(); y++) {
            if (T.getChildAt(y) instanceof TableRow) {
                TableRow R = (TableRow) T.getChildAt(y);
                for (int x = 0; x < R.getChildCount(); x++) {
                    View V = R.getChildAt(x); // In our case this will be each button on the grid
                    V.setOnClickListener(new PlayButtonOnClick(x, y));
                }
            }
        }
    }

    /**
     * Handles click event on the buttons
     */
    private class PlayButtonOnClick implements View.OnClickListener {

        private int x = 0;
        private int y = 0;

        public PlayButtonOnClick(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void onClick(View view) {
            if (view instanceof Button) {
                Button B = (Button) view;
                B.setTextColor(is_O_turn ?
                        getResources().getColor(R.color.colorPrimary) :
                        getResources().getColor(R.color.colorRed));
                B.setTextSize(20);
                gameBoard[x][y] = is_O_turn ? 'O' : 'X';
                B.setText(is_O_turn ? "O" : "X");
                B.setEnabled(false);


                nextTurnView.setText(is_O_turn ? "O's Turn" : "X's Turn");

                // check if anyone has won
                if (checkWin()) {
                    disableGameButtons();
                    nextTurnView.setText("X's Turn");
                }

                String sendString= ""+y+x;
                send(sendString);
            }
        }
    }
}
