package kr.syeyoung.vncmap.map;

import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.MapFont;
import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import com.bergerkiller.bukkit.common.resources.ResourceCategory;
import com.bergerkiller.bukkit.common.resources.ResourceKey;
import com.bergerkiller.generated.net.minecraft.resources.MinecraftKeyHandle;
import com.shinyhut.vernacular.client.VernacularClient;
import com.shinyhut.vernacular.client.VernacularConfig;
import com.shinyhut.vernacular.client.exceptions.VncException;
import com.shinyhut.vernacular.client.rendering.ColorDepth;
import com.shinyhut.vernacular.protocol.messages.Bell;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class VncWidget extends MapWidget {
    private MapVnc mapVnc;
    @Getter
    private VernacularClient client;

    private VncScreenWidget vncScreenWidget;

    private ControlWidget controlWidget;

    public VncWidget(MapVnc mapVnc, String host, int port) {
        this.mapVnc = mapVnc;
        VernacularConfig config = new VernacularConfig();
        client = new VernacularClient(config);

        config.setColorDepth(ColorDepth.BPP_16_TRUE);
        config.setErrorListener(this::onError);
        config.setUsernameSupplier(this::onUsername);
        config.setPasswordSupplier(this::onPassword);
        config.setBellListener(this::onBell);
        config.setRemoteClipboardListener(this::onClipboard);

        vncScreenWidget = new VncScreenWidget(this);
        config.setScreenUpdateListener(vncScreenWidget::onScreen);

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    client.start(host, port);
                } catch (Exception e) {
                    mapVnc.onDisconnect();
                }
            }).start();
        addWidget(vncScreenWidget);
        controlWidget = new ControlWidget(mapVnc, this);
        onControlVisibilityToggle();
    }

    @Override
    public void onAttached() {
        clearWidgets();
        addWidget(vncScreenWidget);
        onControlVisibilityToggle();
        if (askingButton != null)
            addWidget(askingButton);
    }

    String latestException;
    @Override
    public void onDraw() {
        this.view.fill(MapColorPalette.COLOR_WHITE);
        if (latestException == null) {
            Dimension textSize = this.view.calcFontSize(MapFont.MINECRAFT,  "Connecting...");
            this.view.draw(MapFont.MINECRAFT, (getWidth() - textSize.width)/2,getHeight()/3, MapColorPalette.COLOR_BLACK, "Connecting...");
        } else {
            int y = 20;
            for (String s : latestException.split("\n")) {
                this.view.draw(MapFont.MINECRAFT, 0,y, MapColorPalette.COLOR_RED, s);
                y += 8;
            }
        }
    }

    private void onError(VncException vncException) {
        clearWidgets();
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        PrintStream printWriter = new PrintStream(arrayOutputStream);
        vncException.printStackTrace(printWriter);
        printWriter.flush();
        latestException = arrayOutputStream.toString().replace("\t","    ");
        invalidate();

        ClickerKnownButton disconnect = new ClickerKnownButton() {
            {
                setText("Disconnect");
            }

            @Override
            public void onActivate() {
                client.stop();
                mapVnc.onDisconnect();
            }
        };
        ClickerKnownButton keepConnection = new ClickerKnownButton() {
            {
                setText("Keep Connection");
            }

            @Override
            public void onActivate() {
                latestException = null;
                VncWidget.this.onAttached();
            }
        };
        disconnect.setBounds(0,0,100,20);
        keepConnection.setBounds(105,0,150,20);
        addWidget(disconnect); addWidget(keepConnection);
    }

    private AskingButton askingButton;
    private String onUsername() {
        CompletableFuture<String> future = new CompletableFuture();

        askingButton = new AskingButton("Input Username", null);
        askingButton.setCallback((clicker, username) -> {
            VncWidget.this.removeWidget(askingButton);
            askingButton = null;
            future.complete(username);
        });
        askingButton.setText("Input Username");
        addWidget(askingButton);
        askingButton.setBounds(getWidth()/2-100,getHeight()/2-10,200, 20);

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
    private String onPassword() {
        CompletableFuture<String> future = new CompletableFuture();

        askingButton = new AskingButton("Input Password", null);
        askingButton.setCallback((clicker, password) -> {
            VncWidget.this.removeWidget(askingButton);
            askingButton = null;
            future.complete(password);
        });
        askingButton.setText("Input Password");
        addWidget(askingButton);
        askingButton.setBounds(getWidth()/2-100,getHeight()/2-10,200, 20);

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void onBell(Void v) {
        mapVnc.playSound(ResourceKey.fromMinecraftKey(ResourceCategory.sound_effect, MinecraftKeyHandle.createNew("minecraft", "entity.arrow.hit_player")));
    }

    private void onClipboard(String text) {
        Player pl = mapVnc.getLastInput();

        if (pl == null) return;
        TextComponent copyComp = new TextComponent("§b[VNC Display] §eClick to copy");
        copyComp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, text));
        pl.spigot().sendMessage(copyComp);
    }

    @Override
    public void onBoundsChanged() {
        vncScreenWidget.setBounds(0,0,getWidth(),getHeight());
        controlWidget.setBounds(getWidth()-100,0,100,60);
        if (askingButton != null)
            askingButton.setBounds(getWidth()/2-100,getHeight()/2-10,200, 20);
    }

    public void onControlVisibilityToggle() {
        if (mapVnc.isControlVisibility()) {
            addWidget(controlWidget);
        } else {
            removeWidget(controlWidget);
        }
    }
}
