package kr.syeyoung.vncmap.map;

import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import org.bukkit.entity.Player;

public class ControlWidget extends MapWidget {
    private VncWidget vncWidget;
    private MapVnc mapVnc;

    private ClickerKnownButton keyboard = new ClickerKnownButton() {
        {
            setText("Keyboard");
        }

        @Override
        public void onActivate() {
            if (!mapVnc.getKeyboardMap().containsKey(getLastClicker()))
                mapVnc.getKeyboardMap().put(getLastClicker(), new Keyboard(mapVnc,getDisplay().getMapInfo().getUniqueId(), getLastClicker()));
            mapVnc.getKeyboardMap().get(getLastClicker()).sendKeyboard();
        }
    };
    private ClickerKnownButton disconnect = new ClickerKnownButton() {
        {
            setText("Disconnect");
        }

        @Override
        public void onActivate() {
            vncWidget.getClient().stop();
            mapVnc.onDisconnect();
        }
    };
    private AskingButton copy = new AskingButton("What to Copy", this::copy) {
        {
            setText("Copy To VNC");
        }
    };

    public ControlWidget(MapVnc mapVnc, VncWidget vncWidget) {
        this.mapVnc = mapVnc;
        this.vncWidget =vncWidget;
    }

    private void copy(Player player, String text) {
        vncWidget.getClient().copyText(text);
        player.sendMessage("§b[VNC Display] §fCopied Text");
    }


    public ControlWidget() {
        addWidget(keyboard);
        addWidget(disconnect);
        addWidget(copy);
    }

    @Override
    public void onAttached() {
        clearWidgets();
        addWidget(keyboard);
        addWidget(disconnect);
        addWidget(copy);
    }

    @Override
    public void onBoundsChanged() {
        keyboard.setBounds(0, 0, getWidth(), 20);
        disconnect.setBounds(0, 20, getWidth(), 20);
        copy.setBounds(0, 40, getWidth(), 20);
    }
}
