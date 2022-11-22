package application.controller;

public class Record {
    int game_id;
    String opponent;
    String result;

    public int getGame_id() {
        return game_id;
    }

    public void setGame_id(int game_id) {
        this.game_id = game_id;
    }

    public String getOpponent() {
        return opponent;
    }

    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Record{" +
                "game_id=" + game_id +
                ", opponent='" + opponent + '\'' +
                ", result='" + result + '\'' +
                '}';
    }

    public Record(int id, String op, String result) {
        this.game_id = id;
        this.opponent = op;
        this.result = result;
    }
}
