package kr.syeyoung.vncmap;

import com.bergerkiller.bukkit.common.internal.CommonPlugin;
import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.map.MapSession;
import kr.syeyoung.vncmap.map.Keyboard;
import kr.syeyoung.vncmap.map.MapVnc;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Plugin(name = "VncMAp", version = "0.0.1")
@Description("Fun experiment")
@Author("syeyoung (cyoung06@naver.com)")
@Dependency("BKCommonLib")
@Commands({
        @Command(name="vncmap", desc="gives vnc display map"),
        @Command(name="keyclick", desc="how do you know it", permission = "op.op")
})
public class VncMap extends JavaPlugin {
    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    public static final Set<UUID> togglers = new HashSet<>();

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (command.getLabel().equals("vncmap")) {
            if (!sender.isOp()) {
                sender.sendMessage("§b[VNC Display] §cPermission Denied.");
                return true;
            }
            if (args.length == 0) {
                ((Player) sender).getInventory().addItem(MapDisplay.createMapItem(this, MapVnc.class));
                sender.sendMessage("§b[VNC Display] §fA map has been added to your inventory. Hang them to the wall using item frames to create web display.");
            } else if (args[0].equals("toggleControls")) {
                sender.sendMessage("§b[VNC Display] §fClick display to toggle controls (Disconnect / Keyboard / Copy)");
                togglers.add(((Player)sender).getUniqueId());
            }
        } else {
            if (args.length != 2) return true;
            if (!sender.isOp()) {
                sender.sendMessage("§b[VNC Display] §cPermission Denied.");
                return true;
            }
            Optional<MapSession> session = CommonPlugin.getInstance().getMapController().getInfo(UUID.fromString(args[0])).getSessions().stream().findFirst();
            if (!session.isPresent()) {
                sender.sendMessage("§cCan't find the webdisplay connected to this keyboard");
                return true;
            }
            MapSession mapSession = session.get();
            MapVnc browser= (MapVnc) mapSession.display;
            Keyboard keyboard = browser.getKeyboardByPlayer((Player) sender);
            getServer().getScheduler().runTaskAsynchronously(this, () -> {
                try {
                    keyboard.onKeyClick(Integer.parseInt(args[1]));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        return true;
    }
}
