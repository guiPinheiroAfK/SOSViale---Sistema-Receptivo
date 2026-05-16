package br.com.sosviale.i18n;

import javax.swing.*;
import javax.swing.text.JTextComponent;

/*
 * TranslatableComponent - Wrapper para componentes que precisam ser traduzidos
 */
public class TranslatableComponent implements LanguageManager.LanguageChangeListener {

    private final JComponent component;
    private final String translationKey;
    private String translationKeyAlt;

    public TranslatableComponent(JComponent component, String translationKey) {
        this.component = component;
        this.translationKey = translationKey;
        LanguageManager.getInstance().addLanguageChangeListener(this);
        updateText();
    }

    public TranslatableComponent(JComponent component, String translationKey, String altKey) {
        this.component = component;
        this.translationKey = translationKey;
        this.translationKeyAlt = altKey;
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

    public void destroy() {
        LanguageManager.getInstance().removeLanguageChangeListener(this);
    }
}