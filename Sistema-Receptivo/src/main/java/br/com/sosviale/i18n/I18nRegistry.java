package br.com.sosviale.i18n;

import java.util.ArrayList;
import java.util.List;

// runnables de tela registrados aqui; language manager dispara tudo quando troca lingua

public final class I18nRegistry {

    private static final List<Runnable> refreshers = new ArrayList<>();
    private static boolean listenerAttached;

    private I18nRegistry() {}

    public static void register(Runnable refresher) {
        if (refresher == null) return;
        refreshers.add(refresher);
        attachListenerOnce();
        refresher.run();
    }

    // ouve LanguageManager uma vez e espalha pros refreshers

    private static void attachListenerOnce() {
        if (listenerAttached) return;
        listenerAttached = true;
        LanguageManager.getInstance().addLanguageChangeListener(lang -> {
            for (Runnable r : refreshers) {
                r.run();
            }
        });
    }
}
