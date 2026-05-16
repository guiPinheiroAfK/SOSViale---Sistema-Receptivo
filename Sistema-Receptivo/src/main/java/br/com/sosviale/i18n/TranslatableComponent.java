package br.com.sosviale.i18n;

import javax.swing.*;
import javax.swing.text.JTextComponent;

// listener que empurra translate(key) de novo quando lingua muda

public class TranslatableComponent implements LanguageManager.LanguageChangeListener {

    private final JComponent component;
    private final String translationKey;

    public TranslatableComponent(JComponent component, String translationKey) {
        this.component = component;
        this.translationKey = translationKey;
        LanguageManager.getInstance().addLanguageChangeListener(this);
        updateText();
    }

    private void updateText() {
        String text = LanguageManager.getInstance().translate(translationKey);
        if (component instanceof JLabel) {
            ((JLabel) component).setText(text);
        } else if (component instanceof JButton) {
            ((JButton) component).setText(text);
        } else if (component instanceof JTextComponent) {
            ((JTextComponent) component).setText(text);
        }
    }

    @Override
    public void onLanguageChanged(LanguageManager.Language newLanguage) {
        updateText();
    }
}
