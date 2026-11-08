package ru.recoin.implement.features.modules.misc.browser;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import ru.recoin.common.util.logger.LoggerUtil;

/**
 * Knows when a text field is selected on a web page (search bar, login, comment box...): a tiny
 * script in the page reports focus changes through the console, and a display handler added to
 * Rinku's client reads them. Then the Browser can send the keyboard into the page by itself.
 */
final class FocusWatch {
    static final String PREFIX = "__DTX_FOCUS:";

    /** Injected into every page (idempotent). Reports 1 when an editable element has focus, else 0. */
    static final String SCRIPT = "(function(){if(window.__dtxFocus)return;window.__dtxFocus=1;"
            + "var NO=['button','checkbox','radio','submit','reset','file','image','range','color','hidden'];"
            + "function ed(e){if(!e)return false;if(e.isContentEditable)return true;var t=(e.tagName||'').toLowerCase();"
            + "if(t==='textarea')return !e.readOnly&&!e.disabled;"
            + "if(t==='input'){var y=(e.getAttribute('type')||'text').toLowerCase();return NO.indexOf(y)<0&&!e.readOnly&&!e.disabled;}return false;}"
            + "function cur(){var a=document.activeElement;while(a&&a.shadowRoot&&a.shadowRoot.activeElement)a=a.shadowRoot.activeElement;return a;}"
            + "var last=-1;function rep(force){var v=ed(cur())?1:0;if(v!==last||force){last=v;console.log('" + PREFIX + "'+v);}}"
            + "document.addEventListener('focusin',function(){rep(false);},true);"
            + "document.addEventListener('focusout',function(){setTimeout(function(){rep(false);},0);},true);"
            + "document.addEventListener('mousedown',function(){setTimeout(function(){rep(true);},60);},true);"
            + "rep(true);})();";

    /** Blurs the focused field (Esc: leave typing without the field grabbing the keyboard again). */
    static final String BLUR = "(function(){var a=document.activeElement;if(a&&a.blur)a.blur();})();";

    private static final List<BrowserPanel> PANELS = new CopyOnWriteArrayList<>();
    private static boolean installed;
    private static boolean failed;

    private FocusWatch() {
    }

    static void register(BrowserPanel p) {
        if (!PANELS.contains(p)) PANELS.add(p);
        FocusWatch.install();
    }

    static void unregister(BrowserPanel p) {
        PANELS.remove(p);
    }

    /** Adds the console listener to Rinku's client once Chromium is up. */
    static synchronized void install() {
        if (installed || failed || !BrowserBridge.ready()) return;
        try {
            Class<?> handlerType = Class.forName("org.cef.handler.CefDisplayHandler");
            Object client = Class.forName("de.keksuccino.rinku.Rinku").getMethod("getClient").invoke(null);
            InvocationHandler h = (proxy, method, args) -> {
                String name = method.getName();
                if (method.getDeclaringClass() == Object.class) {
                    return switch (name) {
                        case "equals" -> proxy == args[0];
                        case "hashCode" -> System.identityHashCode(proxy);
                        default -> "DettexFocusWatch";
                    };
                }
                if (name.equals("onConsoleMessage") && args != null && args.length >= 3 && args[2] instanceof String msg && msg.startsWith(PREFIX)) {
                    boolean editing = msg.endsWith("1");
                    boolean matched = false;
                    for (BrowserPanel p : PANELS) {
                        if (p.isBrowser(args[0])) {
                            p.setEditing(editing);
                            matched = true;
                        }
                    }
                    if (!matched) {
                        // could not tell the page by its browser object: give it to the screen clicked last
                        BrowserPanel recent = null;
                        for (BrowserPanel p : PANELS) {
                            if (recent == null || p.lastClick() > recent.lastClick()) recent = p;
                        }
                        if (recent != null && System.currentTimeMillis() - recent.lastClick() < 3000L) recent.setEditing(editing);
                    }
                    return true;   // handled: keep it out of the game log
                }
                Class<?> r = method.getReturnType();
                return r == boolean.class ? false : null;
            };
            Object proxy = Proxy.newProxyInstance(handlerType.getClassLoader(), new Class<?>[]{handlerType}, h);
            client.getClass().getMethod("addDisplayHandler", handlerType).invoke(client, proxy);
            installed = true;
        } catch (Throwable e) {
            failed = true;
            LoggerUtil.info("[Browser] text field detection unavailable: " + e);
        }
    }
}
