package application.controller;

/**
 * The type Record.
 */
public class Record {
    /**
     * The Game id.
     */
    int game_id;
    /**
     * The Opponent.
     */
    String opponent;
    /**
     * The Result.
     */
    String result;

    /**
     * Gets game id.
     *
     * @return the game id
     */
    public int getGame_id() {
        return game_id;
    }

    /**
     * Sets game id.
     *
     * @param game_id the game id
     */
    public void setGame_id(int game_id) {
        this.game_id = game_id;
    }

    /**
     * Gets opponent.
     *
     * @return the opponent
     */
    public String getOpponent() {
        return opponent;
    }

    /**
     * Sets opponent.
     *
     * @param opponent the opponent
     */
    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }

    /**
     * Gets result.
     *
     * @return the result
     */
    public String getResult() {
        return result;
    }

    /**
     * Sets result.
     *
     * @param result the result
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "Record{" +
                "game_id=" + game_id +
                ", opponent='" + opponent + '\'' +
                ", result='" + result + '\'' +
                '}';
    }

    /**
     * Instantiates a new Record.
     *
     * @param id     the id
     * @param op     the op
     * @param result the result
     */
    public Record(int id, String op, String result) {
        this.game_id = id;
        this.opponent = op;
        this.result = result;
    }
}
