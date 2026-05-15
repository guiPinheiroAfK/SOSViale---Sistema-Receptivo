package br.com.sosviale.i18n;

import java.util.ArrayList;
import java.util.List;

/*
 * Registra painéis para atualização automática ao trocar o idioma.
 */
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
