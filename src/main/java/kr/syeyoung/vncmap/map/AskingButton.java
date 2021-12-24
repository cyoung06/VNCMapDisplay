package kr.syeyoung.vncmap.map;

import kr.syeyoung.vncmap.VncMap;
import lombok.Setter;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AskingButton  extends ClickerKnownButton {
    private String whatAsk;
    @Setter
    private BiConsumer<Player, String> callback;

    public AskingButton(String whatAsk, BiConsumer<Player, String> callback) {
        this.whatAsk = whatAsk;
        this.callback = callback;
    }

    @Override
    public void onActivate() {
        Player clicker = getLastClicker();
        if (!clicker.isOp()) return;
        Conversation conversation = new ConversationFactory(VncMap.getPlugin(VncMap.class)).withModality(true)
                .withEscapeSequence("quit")
                .withInitialSessionData(new HashMap() {{
                    put("callback", new Consumer<ConversationContext>() {
                        @Override
                        public void accept(ConversationContext conversationContext) {
                            String input = (String) conversationContext.getSessionData("curr");
                            callback.accept(clicker, input);
                        }
                    });
                }})
                .withFirstPrompt(new PromptInputPls(whatAsk))
                .buildConversation(clicker);
        clicker.beginConversation(conversation);
    }
}
