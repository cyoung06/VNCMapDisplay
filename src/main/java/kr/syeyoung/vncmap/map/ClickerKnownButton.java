package kr.syeyoung.vncmap.map;

import com.bergerkiller.bukkit.common.events.map.MapClickEvent;
import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import com.bergerkiller.bukkit.common.map.widgets.MapWidgetButton;
import lombok.Getter;
import org.bukkit.entity.Player;

public abstract class ClickerKnownButton extends MapWidgetButton implements MapClickListener {
    public ClickerKnownButton() {
        super();
        setClipParent(true);
    }
    @Getter
    private Player lastClicker;
    @Override
    public boolean onClick(MapClickEvent event) {
        lastClicker = event.getPlayer();
        return true;
    }
}
