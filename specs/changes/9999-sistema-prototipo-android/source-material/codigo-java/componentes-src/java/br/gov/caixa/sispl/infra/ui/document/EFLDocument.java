package br.gov.caixa.sispl.infra.ui.document;

public interface EFLDocument {

    public abstract void insertTextAtCursor(String keyText);

    public abstract void deleteCharBeforeCursor();

    public abstract int getCursorPosition();

    public abstract void setCursorPosition(int cursorPosition);

    public abstract void setText(String text);

    public abstract String getText();

    public abstract String getMaskedText();

    public abstract boolean isFinished();

    public abstract int getMinLength();

    public abstract int getMaxLength();

}