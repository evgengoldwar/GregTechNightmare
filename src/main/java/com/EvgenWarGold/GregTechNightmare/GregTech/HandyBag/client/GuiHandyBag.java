package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.ContainerHandyBag;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.HandyBagAdmin;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.network.HandyBagNetwork;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.network.MessageBagAction;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.network.MessageSelectCard;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.network.MessageSetBagMode;
import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemHandyBag;
import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemMemoryCard;
import com.EvgenWarGold.GregTechNightmare.GregTechNightmare;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiHandyBag extends GuiContainer {

    private static final ResourceLocation TEXTURE_REGULAR = new ResourceLocation(
        GregTechNightmare.RESOURCE_ROOT_ID,
        "textures/gui/handybag.0.png");
    private static final ResourceLocation TEXTURE_LARGE = new ResourceLocation(
        GregTechNightmare.RESOURCE_ROOT_ID,
        "textures/gui/handybag.1.png");
    private static final ResourceLocation TEXTURE_WIDGETS = new ResourceLocation(
        GregTechNightmare.RESOURCE_ROOT_ID,
        "textures/gui/handybag.widgets.png");
    private static final int ACTION_BUTTON_FIRST = 100;
    private static final int ACTION_BUTTON_LAST = 105;
    private static final int PICKUP_BUTTON = 110;
    private static final int RESTOCK_BUTTON = 111;
    private static final int LOCK_BUTTON = 112;
    private static final int BAUBLES_BUTTON = 113;
    private static final int BLOCK_SECTION_FIRST = 120;
    private final ContainerHandyBag container;
    private final EntityPlayer player;
    private float mouseX;
    private float mouseY;

    public GuiHandyBag(ContainerHandyBag container) {
        super(container);
        this.container = container;
        this.player = container.player;
        this.xSize = container.isLarge() ? 256 : 176;
        this.ySize = 256;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();
        int x = guiLeft + getBagStartX() + 2;
        int y = guiTop + 157;
        buttonList.add(new ActionButton(ACTION_BUTTON_FIRST, x, y, 0));
        buttonList.add(new ActionButton(ACTION_BUTTON_FIRST + 1, x + 18, y, 1));
        buttonList.add(new ActionButton(ACTION_BUTTON_FIRST + 2, x + 36, y, 2));
        buttonList.add(new ActionButton(ACTION_BUTTON_FIRST + 3, x + 108, y, 3));
        buttonList.add(new ActionButton(ACTION_BUTTON_FIRST + 4, x + 126, y, 4));
        buttonList.add(new ActionButton(ACTION_BUTTON_FIRST + 5, x + 144, y, 5));
        buttonList.add(new ModeButton(PICKUP_BUTTON, x + 49, y));
        buttonList.add(new ModeButton(RESTOCK_BUTTON, x + 69, y));
        buttonList.add(new ModeButton(LOCK_BUTTON, x + 89, y));
        if (Loader.isModLoaded("Baubles")) {
            int upperPanelOffset = container.isLarge() ? 40 : 0;
            int baublesX = guiLeft + upperPanelOffset + (useOldBaublesButton() ? 65 : 25);
            GuiButton baublesButton = createBaublesButton(baublesX, guiTop + 21);
            if (baublesButton != null) {
                buttonList.add(baublesButton);
            }
        }
        addSectionButtons();
    }

    @SuppressWarnings("unchecked")
    private void addSectionButtons() {
        int sideY = guiTop + 84;
        int centerY = guiTop + 91;
        if (container.isLarge()) {
            addSectionButtons(1, guiLeft + 8, sideY);
            addSectionButtons(0, guiLeft + 112, centerY);
            addSectionButtons(2, guiLeft + 228, sideY);
        } else {
            addSectionButtons(0, guiLeft + 70, centerY);
        }
    }

    @SuppressWarnings("unchecked")
    private void addSectionButtons(int section, int x, int y) {
        buttonList.add(new SectionButton(BLOCK_SECTION_FIRST + section, x, y, section));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawCardSelectorTooltip(mouseX, mouseY);
        drawButtonTooltip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.getTextureManager()
            .bindTexture(container.isLarge() ? TEXTURE_LARGE : TEXTURE_REGULAR);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        drawCardSelection(mouseX, mouseY);
        drawSwapSelection();
        drawPlayerModel(guiLeft + (container.isLarge() ? 88 : 48), guiTop + 82);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int xOffset = container.isLarge() ? 40 : 0;
        fontRendererObj.drawString(I18n.format("container.crafting"), xOffset + 97, 5, 0x404040);
        fontRendererObj.drawString(I18n.format("container.GTN_HandyBag.cards"), xOffset + 97, 59, 0x404040);
        fontRendererObj.drawString(I18n.format("container.GTN_HandyBag"), xOffset + 8, 90, 0x404040);
    }

    private void drawCardSelection(int mouseX, int mouseY) {
        int startX = guiLeft + getCardStartX();
        int slotY = guiTop + 69;
        int buttonY = guiTop + 87;
        int selected = container.getSelectedCard();

        int selectedX = startX + selected * 18;
        drawRect(selectedX - 1, slotY - 1, selectedX + 17, slotY, 0xFF4AA3D8);
        drawRect(selectedX - 1, slotY + 16, selectedX + 17, slotY + 17, 0xFF4AA3D8);
        drawRect(selectedX - 1, slotY, selectedX, slotY + 16, 0xFF4AA3D8);
        drawRect(selectedX + 16, slotY, selectedX + 17, slotY + 16, 0xFF4AA3D8);

        for (int i = 0; i < 4; i++) {
            int cellX = startX + i * 18;
            int buttonX = cellX + 3;
            boolean hasCard = container.data.getCard(i) != null;
            boolean hovered = mouseX >= cellX && mouseX < cellX + 18 && mouseY >= buttonY && mouseY < buttonY + 11;

            if (i == selected) {
                drawRect(buttonX + 1, buttonY + 1, buttonX + 9, buttonY + 9, 0xFF4AA3D8);
            } else if (hovered && hasCard) {
                drawRect(buttonX + 1, buttonY + 1, buttonX + 9, buttonY + 9, 0x704AA3D8);
            }

            String text = hasCard ? Integer.toString(i + 1) : "-";
            int color = i == selected ? 0xFFFFFF : hasCard ? 0x404040 : 0x888888;
            int textX = buttonX + 5 - fontRendererObj.getStringWidth(text) / 2;
            fontRendererObj.drawString(text, textX, buttonY + 1, color);
        }
    }

    private void drawSwapSelection() {
        int selected = container.getSelectedSwapSlot();
        if (selected < 0 || selected >= container.data.getSize()) return;
        Slot slot = container.getSlot(selected);
        int x = guiLeft + slot.xDisplayPosition;
        int y = guiTop + slot.yDisplayPosition;
        int color = 0xFFFFB84A;
        drawRect(x - 1, y - 1, x + 17, y, color);
        drawRect(x - 1, y + 16, x + 17, y + 17, color);
        drawRect(x - 1, y, x, y + 16, color);
        drawRect(x + 16, y, x + 17, y + 16, color);
    }

    private void drawCardSelectorTooltip(int mouseX, int mouseY) {
        int index = getHoveredCardButton(mouseX, mouseY);
        if (index < 0) return;

        List<String> lines = new ArrayList<String>();
        ItemStack card = container.data.getCard(index);
        lines.add(I18n.format("gtn.handybag.gui.card", index + 1));

        if (card == null) {
            lines.add(EnumChatFormatting.DARK_GRAY + I18n.format("gtn.handybag.gui.card_empty"));
        } else {
            lines.add(EnumChatFormatting.GRAY + card.getDisplayName());
            lines.add(
                EnumChatFormatting.GRAY + I18n.format("gtn.handybag.gui.card_limit", ItemMemoryCard.getCapacity(card)));
            int accessState = ItemMemoryCard.getAccessState(card);
            if (accessState == ItemMemoryCard.ACCESS_UNCLAIMED) {
                lines.add(EnumChatFormatting.GRAY + I18n.format("gtn.handybag.privacy.unclaimed"));
            } else {
                lines.add(
                    (accessState == ItemMemoryCard.ACCESS_PUBLIC ? EnumChatFormatting.GREEN : EnumChatFormatting.GOLD)
                        + I18n.format(
                            accessState == ItemMemoryCard.ACCESS_PUBLIC ? "gtn.handybag.privacy.public"
                                : "gtn.handybag.privacy.private"));
                lines.add(
                    EnumChatFormatting.GRAY
                        + I18n.format("gtn.handybag.privacy.owner", ItemMemoryCard.getOwnerName(card)));
                if (!ItemMemoryCard.canAccess(card, player)) {
                    lines.add(EnumChatFormatting.RED + I18n.format("gtn.handybag.privacy.no_access"));
                } else if (!ItemMemoryCard.isOwner(card, player) && HandyBagAdmin.canAdminister(player)) {
                    lines.add(EnumChatFormatting.AQUA + I18n.format("gtn.handybag.privacy.admin_access"));
                }
            }
            if (index == container.getSelectedCard()) {
                lines.add(EnumChatFormatting.AQUA + I18n.format("gtn.handybag.gui.card_selected"));
                lines.add(EnumChatFormatting.DARK_GRAY + I18n.format("gtn.handybag.privacy.toggle_hint"));
            } else {
                lines.add(EnumChatFormatting.YELLOW + I18n.format("gtn.handybag.gui.card_select"));
            }
        }

        drawHoveringText(lines, mouseX, mouseY, fontRendererObj);
    }

    private void drawButtonTooltip(int mouseX, int mouseY) {
        for (Object object : buttonList) {
            if (!(object instanceof GuiButton)) continue;
            GuiButton button = (GuiButton) object;
            if (!isMouseOver(button, mouseX, mouseY)) continue;
            List<String> lines = new ArrayList<String>();
            if (button.id >= ACTION_BUTTON_FIRST && button.id <= ACTION_BUTTON_LAST) {
                int action = button.id - ACTION_BUTTON_FIRST;
                lines.add(I18n.format("gtn.handybag.action." + action));
                lines.add(EnumChatFormatting.GRAY + I18n.format("gtn.handybag.action." + action + ".detail"));
                if (hasBlockedSections()) {
                    lines.add(EnumChatFormatting.DARK_GRAY + I18n.format("gtn.handybag.section.blocked_note"));
                }
            } else if (button.id == PICKUP_BUTTON) {
                int mode = container.getPickupMode();
                lines.add(I18n.format("gtn.handybag.mode.pickup"));
                lines.add(EnumChatFormatting.AQUA + I18n.format("gtn.handybag.pickup_mode." + mode));
                lines.add(EnumChatFormatting.GRAY + I18n.format("gtn.handybag.mode.pickup.click"));
                if (hasBlockedSections()) {
                    lines.add(EnumChatFormatting.DARK_GRAY + I18n.format("gtn.handybag.section.blocked_note"));
                }
            } else if (button.id == RESTOCK_BUTTON) {
                lines.add(I18n.format("gtn.handybag.mode.restock"));
                lines.add(
                    (container.isRestockEnabled() ? EnumChatFormatting.GREEN : EnumChatFormatting.DARK_GRAY) + I18n
                        .format(container.isRestockEnabled() ? "gtn.handybag.state.on" : "gtn.handybag.state.off"));
                lines.add(EnumChatFormatting.GRAY + I18n.format("gtn.handybag.mode.restock.click"));
                if (hasBlockedSections()) {
                    lines.add(EnumChatFormatting.DARK_GRAY + I18n.format("gtn.handybag.section.blocked_note"));
                }
            } else if (button.id == LOCK_BUTTON) {
                lines.add(I18n.format("gtn.handybag.mode.lock"));
                lines.add(
                    (container.isLocked() ? EnumChatFormatting.AQUA : EnumChatFormatting.DARK_GRAY)
                        + I18n.format(container.isLocked() ? "gtn.handybag.state.on" : "gtn.handybag.state.off"));
                lines.add(EnumChatFormatting.GRAY + I18n.format("gtn.handybag.mode.lock.click"));
                lines.add(EnumChatFormatting.GRAY + I18n.format("gtn.handybag.mode.lock.detail"));
            } else if (button.id >= BLOCK_SECTION_FIRST && button.id < BLOCK_SECTION_FIRST + 3) {
                int section = button.id - BLOCK_SECTION_FIRST;
                boolean blocked = container.isSectionQuickActionBlocked(section);
                lines.add(I18n.format("gtn.handybag.section.block"));
                lines.add(EnumChatFormatting.GRAY + I18n.format("gtn.handybag.section." + section));
                lines.add(
                    (blocked ? EnumChatFormatting.AQUA : EnumChatFormatting.DARK_GRAY)
                        + I18n.format(blocked ? "gtn.handybag.state.on" : "gtn.handybag.state.off"));
                lines.add(EnumChatFormatting.GRAY + I18n.format("gtn.handybag.section.block.detail"));
            }
            if (!lines.isEmpty()) drawHoveringText(lines, mouseX, mouseY, fontRendererObj);
            return;
        }
    }

    private boolean hasBlockedSections() {
        for (int i = 0; i < container.getSectionCount(); i++) {
            if (container.isSectionQuickActionBlocked(i)) return true;
        }
        return false;
    }

    private int getHoveredCardButton(int mouseX, int mouseY) {
        int startX = guiLeft + getCardStartX();
        int buttonY = guiTop + 87;
        if (mouseY < buttonY || mouseY >= buttonY + 11) return -1;
        for (int i = 0; i < 4; i++) {
            int x = startX + i * 18;
            if (mouseX >= x && mouseX < x + 18) return i;
        }
        return -1;
    }

    private int getCardStartX() {
        return container.isLarge() ? 138 : 98;
    }

    private int getBagStartX() {
        return container.isLarge() ? 48 : 8;
    }

    private static boolean isMouseOver(GuiButton button, int mouseX, int mouseY) {
        return button.visible && mouseX >= button.xPosition
            && mouseX < button.xPosition + button.width
            && mouseY >= button.yPosition
            && mouseY < button.yPosition + button.height;
    }

    private void drawPlayerModel(int x, int y) {
        GuiInventory.func_147046_a(x, y, 30, x - mouseX, y - 50 - mouseY, player);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0) {
            int index = getHoveredCardButton(mouseX, mouseY);
            if (index >= 0 && container.data.getCard(index) != null && index != container.getSelectedCard()) {
                container.selectCard(index);
                HandyBagNetwork.CHANNEL.sendToServer(new MessageSelectCard(index));
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id >= ACTION_BUTTON_FIRST && button.id <= ACTION_BUTTON_LAST) {
            HandyBagNetwork.CHANNEL.sendToServer(new MessageBagAction(button.id - ACTION_BUTTON_FIRST));
            return;
        }
        if (button.id == PICKUP_BUTTON) {
            int mode = (container.getPickupMode() + 1) % 3;
            container.setPickupMode(mode);
            HandyBagNetwork.CHANNEL.sendToServer(new MessageSetBagMode(MessageSetBagMode.MODE_PICKUP, mode));
            return;
        }
        if (button.id == RESTOCK_BUTTON) {
            boolean enabled = !container.isRestockEnabled();
            container.setRestockEnabled(enabled);
            HandyBagNetwork.CHANNEL
                .sendToServer(new MessageSetBagMode(MessageSetBagMode.MODE_RESTOCK, enabled ? 1 : 0));
            return;
        }
        if (button.id == LOCK_BUTTON) {
            boolean locked = !container.isLocked();
            container.setLocked(locked);
            HandyBagNetwork.CHANNEL.sendToServer(new MessageSetBagMode(MessageSetBagMode.MODE_LOCK, locked ? 1 : 0));
            return;
        }
        if (button.id == BAUBLES_BUTTON) {
            openBaubles();
            return;
        }
        if (button.id >= BLOCK_SECTION_FIRST && button.id < BLOCK_SECTION_FIRST + 3) {
            int section = button.id - BLOCK_SECTION_FIRST;
            boolean blocked = !container.isSectionQuickActionBlocked(section);
            container.setSectionQuickActionBlocked(section, blocked);
            HandyBagNetwork.CHANNEL.sendToServer(
                new MessageSetBagMode(
                    MessageSetBagMode.MODE_BLOCK_SECTION,
                    MessageSetBagMode.packSection(section, blocked)));
            return;
        }
    }

    private void openBaubles() {
        try {
            Class<?> packetClass = Class.forName("baubles.common.network.PacketOpenBaublesInventory");
            Object packet;
            try {
                packet = packetClass.getConstructor(EntityPlayer.class)
                    .newInstance(player);
            } catch (NoSuchMethodException ignored) {
                packet = packetClass.getDeclaredConstructor()
                    .newInstance();
            }
            Class<?> handlerClass = Class.forName("baubles.common.network.PacketHandler");
            Field instanceField = handlerClass.getField("INSTANCE");
            Object channel = instanceField.get(null);
            for (Method method : channel.getClass()
                .getMethods()) {
                if (method.getName()
                    .equals("sendToServer") && method.getParameterTypes().length == 1) {
                    method.invoke(channel, packet);
                    return;
                }
            }
        } catch (Exception ignored) {}
    }

    private boolean useOldBaublesButton() {
        try {
            Class<?> configClass = Class.forName("baubles.common.BaublesConfig");
            Field field = configClass.getField("useOldGuiButton");
            return field.getBoolean(null);
        } catch (Exception ignored) {
            return true;
        }
    }

    private GuiButton createBaublesButton(int x, int y) {
        try {
            Class<?> buttonClass = Class.forName("baubles.client.gui.GuiBaublesButton");
            return (GuiButton) buttonClass
                .getConstructor(int.class, int.class, int.class, int.class, int.class, String.class)
                .newInstance(BAUBLES_BUTTON, x, y, 10, 10, I18n.format("button.baubles"));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static class ActionButton extends GuiButton {

        private final int action;

        public ActionButton(int id, int x, int y, int action) {
            super(id, x, y, 12, 12, "");
            this.action = action;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (!visible) return;
            GL11.glColor4f(1F, 1F, 1F, 1F);
            mc.getTextureManager()
                .bindTexture(TEXTURE_WIDGETS);
            drawTexturedModalRect(xPosition, yPosition, 24, action * 12, 12, 12);
            if (isMouseOver(this, mouseX, mouseY)) {
                drawRect(xPosition, yPosition, xPosition + width, yPosition + height, 0x404AA3D8);
            }
        }
    }

    private class SectionButton extends GuiButton {

        private final int section;

        public SectionButton(int id, int x, int y, int section) {
            super(id, x, y, 10, 10, "");
            this.section = section;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (!visible) return;
            boolean active = container.isSectionQuickActionBlocked(section);
            int border = active ? 0xFF4AA3D8 : 0xFF777777;
            int background = isMouseOver(this, mouseX, mouseY) ? 0xFFBDBDBD : 0xFFA0A0A0;
            drawRect(xPosition, yPosition, xPosition + width, yPosition + height, border);
            drawRect(xPosition + 1, yPosition + 1, xPosition + width - 1, yPosition + height - 1, background);
            drawCenteredString(
                mc.fontRenderer,
                "Q",
                xPosition + width / 2,
                yPosition + 1,
                active ? 0xFFFFFF : 0x404040);
        }
    }

    private class ModeButton extends GuiButton {

        public ModeButton(int id, int x, int y) {
            super(id, x, y, 18, 12, "");
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (!visible) return;
            boolean active;
            String text;
            if (id == PICKUP_BUTTON) {
                int mode = container.getPickupMode();
                active = mode != ItemHandyBag.PICKUP_OFF;
                text = "P" + (mode == ItemHandyBag.PICKUP_OFF ? "-" : Integer.toString(mode));
            } else if (id == RESTOCK_BUTTON) {
                active = container.isRestockEnabled();
                text = active ? "R+" : "R-";
            } else {
                active = container.isLocked();
                text = active ? "L+" : "L-";
            }
            int border = active ? 0xFF4AA3D8 : 0xFF777777;
            int background = isMouseOver(this, mouseX, mouseY) ? 0xFFBDBDBD : 0xFFA0A0A0;
            drawRect(xPosition, yPosition, xPosition + width, yPosition + height, border);
            drawRect(xPosition + 1, yPosition + 1, xPosition + width - 1, yPosition + height - 1, background);
            int color = active ? 0xFFFFFF : 0x404040;
            drawCenteredString(mc.fontRenderer, text, xPosition + width / 2, yPosition + 2, color);
        }
    }
}
