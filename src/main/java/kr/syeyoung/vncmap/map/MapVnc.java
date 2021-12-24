package kr.syeyoung.vncmap.map;

import com.bergerkiller.bukkit.common.events.map.MapClickEvent;
import com.bergerkiller.bukkit.common.events.map.MapStatusEvent;
import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.map.MapSessionMode;
import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import com.bergerkiller.bukkit.common.map.widgets.MapWidgetButton;
import com.shinyhut.vernacular.utils.KeySyms;
import kr.syeyoung.vncmap.VncMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;

import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.function.Consumer;

public class MapVnc extends MapDisplay {

    @Getter
    private Map<Player, Keyboard> keyboardMap = new HashMap<>();
    public Keyboard getKeyboardByPlayer(Player p) {
        return getKeyboardMap().get(p);
    }

    private MapWidgetButton connect = new AskingButton("Type Server IP", (clicker, input) -> {
        String[] split = input.split(":");
        String host = split[0];
        try {
            int port = split.length == 1 ? 5900 : Integer.parseInt(split[1]);
            connect(host, port);
        } catch (Exception e) {
            clicker.sendMessage("§b[VNC Display] §cInvalid Address");
            return;
        }
    });

    private VncWidget vncWidget;

    @Getter
    private Player lastInput;


    private void connect(String host, int port) {
        clearWidgets();
        vncWidget = new VncWidget(this, host, port);
        vncWidget.setBounds(0,0,getWidth(),getHeight());
        addWidget(vncWidget);
    }

    @Override
    public void onTick() {
        this.getBottomLayer().fill(MapColorPalette.COLOR_WHITE);
    }

    public void onDisconnect() {
        removeWidget(vncWidget);
        vncWidget = null;
        addWidget(connect);
    }

    @Override
    public void onAttached() {
        connect.setText("Connect");
        clearWidgets();

        setSessionMode(MapSessionMode.FOREVER);
        setGlobal(true);
        setUpdateWithoutViewers(true);

        if (vncWidget != null)
            addWidget(vncWidget);
        else
            addWidget(connect);
        resize();
    }

    private void resize() {
        if (vncWidget != null)
            vncWidget.setBounds(0,0,getWidth(),getHeight());
        connect.setBounds(getWidth()/2-50,getHeight()/2-10,100,20);
    }



    @Override
    public void onLeftClick(MapClickEvent event) {
        onClick(event);
    }

    @Override
    public void onRightClick(MapClickEvent event) {
        onClick(event);
    }

    @Getter @Setter
    private boolean controlVisibility = true;

    private void onClick(MapClickEvent event) {
        event.setCancelled(true);
        if (!event.getPlayer().isOp()) {
            return;
        }
        if (VncMap.togglers.contains(event.getPlayer().getUniqueId())) {
            VncMap.togglers.remove(event.getPlayer().getUniqueId());
            controlVisibility = !controlVisibility;
            event.getPlayer().sendMessage("§b[VNC Display] §fControl Visiblity has been toggled to "+controlVisibility);
            if (vncWidget != null) {
                vncWidget.onControlVisibilityToggle();
            }
            return;
        }
        lastInput = event.getPlayer();

        try {
            if ((lastTouch + 75) > System.currentTimeMillis()) {
                return;
            }
            lastTouch = System.currentTimeMillis();
            if (prioritized != null && System.currentTimeMillis() < priortizedBefore) {

                if (prioritized instanceof MapClickListener) {
                    ((MapClickListener) prioritized).onClick(event);
                }

                if (!prioritized.isFocused()) {
                    prioritized.focus();
                } else {
                    prioritized.activate();
                }

                return;
            }

            int x = event.getX();
            int y = event.getY();
            MapWidget theWidget = null;
            for (MapWidget widget : this.getWidgets()) {
                theWidget = findSpecificWidgetWithin(widget, x, y);
                if (theWidget != null) break;
            }


            if (theWidget == null) return;

            if (theWidget instanceof MapClickListener) {
                ((MapClickListener) theWidget).onClick(event);
            }

            if (!theWidget.isFocused()) {
                theWidget.focus();
            } else {
                theWidget.activate();
            }
        } catch (Exception e) {e.printStackTrace();}
    }

    private boolean withIn(MapWidget widget, int x, int y) {
        int childX = widget.getAbsoluteX();
        if (childX > x || x > (childX + widget.getWidth())) return false;
        int childY = widget.getAbsoluteY();
        if (childY > y || y > (childY + widget.getHeight())) return false;
        return true;
    }
    private MapWidget prioritized = null;
    private long priortizedBefore = System.currentTimeMillis();



    private long lastTouch = System.currentTimeMillis();

    @Override
    public void onStatusChanged(MapStatusEvent event) {
        super.onStatusChanged(event);

        if (event.getName().equals("PRIORITIZE")) {
            if (!(event.getArgument() instanceof Object[])) return;
            Object[] argument = (Object[]) event.getArgument();
            if (argument.length != 2) return;
            if (!(argument[0] instanceof MapWidget)) return;
            MapWidget widget = (MapWidget) argument[0];
            long time = (long) argument[1];

            this.prioritized = widget;
            this.priortizedBefore = time;
        }
    }

    private MapWidget findSpecificWidgetWithin(MapWidget widget, int x, int y) {
        if (!withIn(widget, x, y)) return null;
        List<MapWidget> widgetList = new ArrayList<>();
        widgetList.addAll(widget.getWidgets());
        Collections.reverse(widgetList);
        for (MapWidget children : widgetList) {
            if (withIn(children, x,y)) {
                MapWidget theWidget = findSpecificWidgetWithin(children, x,y);
                if (theWidget == null && children.isFocusable())
                    return children;
                else
                    return theWidget;
            }
        }
        return widget != null && widget.isFocusable() ? widget : null;
    }

    public void onChatKey(Player player, int keycode, boolean status) {
        lastInput = player;
        if (vncWidget == null) return;
        Optional<Integer> sym = KeySyms.forKeyCode(keycode);
        if (sym.isPresent()) keycode = sym.get();
        vncWidget.getClient().updateKey(keycode, status);
    }
}