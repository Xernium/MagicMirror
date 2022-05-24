package me.fivepb.magicmirror;

public class QuestionResponsePair {

    private final String identifier;
    private final byte[] question;
    private final int transaction;

    private byte[] answer = new byte[0];

    public byte[] getAnswer() {
        return answer;
    }

    public void setAnswer(byte[] answer) {
        this.answer = answer;
    }

    public byte[] getQuestion() {
        return question;
    }

    public int getTransaction() {
        return transaction;
    }

    public String getIdentifier() {
        return identifier;
    }

    public QuestionResponsePair(String identifier, byte[] question, int transaction) {
        this.identifier = identifier;
        this.question = question;
        this.transaction = transaction;
    }
}
