package kr.syeyoung.vncmap.map;

import com.bergerkiller.bukkit.common.events.map.MapAction;
import com.bergerkiller.bukkit.common.events.map.MapClickEvent;
import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.widgets.MapWidget;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;

public class VncScreenWidget extends MapWidget implements MapClickListener {
    private VncWidget parent;
    private BufferedImage lastImg;
    public VncScreenWidget(VncWidget parent) {
        this.parent = parent;
        setFocusable(true);
        setClipParent(true);
    }

    public void onScreen(Image img) {
        lastImg = (BufferedImage) img;
        invalidate();
    }

    @Override
    public void onDraw() {
        super.onDraw();
        if (lastImg == null) return;
        this.view.clear();
        int width = Math.min(getWidth(), lastImg.getData().getWidth());
        int height = Math.min(getHeight(), lastImg.getData().getHeight());
        int realWidth = lastImg.getData().getWidth();
        int realHeight = lastImg.getData().getHeight();
        int[] arr = ((DataBufferInt)lastImg.getRaster().getDataBuffer()).getData();

        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                int idx = (y * realWidth + x) ;
                int buf = arr[idx];
                this.view.drawPixel(x,y, MapColorPalette.getColor((buf >> 16)&0xFF,(buf>>8)&0xFF,buf&0xFF));
            }
        }
    }

    private long lastClick = -1;
    private boolean sentRelease = true;
    private boolean dragging = false;

    @Override
    public void onTick() {
        if (!sentRelease && System.currentTimeMillis() > lastClick) {
            parent.getClient().updateMouseButton(MouseEvent.BUTTON1, false);
            parent.getClient().updateMouseButton(MouseEvent.BUTTON2, false);
            sentRelease = true;
            dragging = false;
        }
    }

    @Override
    public boolean onClick(MapClickEvent event) {
        int x = event.getX();
        int y = event.getY();
        if (lastClick < System.currentTimeMillis()) {
            parent.getClient().moveMouse(x, y);
            if (event.getAction() == MapAction.LEFT_CLICK) parent.getClient().updateMouseButton(MouseEvent.BUTTON1, true);
            else if (event.getAction() == MapAction.RIGHT_CLICK) parent.getClient().updateMouseButton(MouseEvent.BUTTON2, true);
            lastClick = System.currentTimeMillis() + 250;
            sentRelease = false;
            dragging = false;
        } else {
            parent.getClient().moveMouse(x, y);
            lastClick = System.currentTimeMillis() + 250;
            sentRelease = false;
            dragging = true;
        }
        return false;
    }
}
