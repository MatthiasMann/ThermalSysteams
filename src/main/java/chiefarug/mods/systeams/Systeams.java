package chiefarug.mods.systeams;

import chiefarug.mods.systeams.compat.mekanism.SysteamsMekanismCompat;
import chiefarug.mods.systeams.compat.pneumaticcraft.SysteamsPNCRCompat;
import chiefarug.mods.systeams.compat.thermal_extra.SysteamsThermalExtraCompat;
import cofh.core.client.event.CoreClientEvents;
import cofh.core.util.helpers.AugmentDataHelper;
import com.mojang.logging.LogUtils;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import mekanism.api.chemical.gas.IGasHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

import static cofh.lib.util.constants.NBTTags.TAG_AUGMENT_TYPE_DYNAMO;
import static net.minecraftforge.eventbus.api.EventPriority.LOW;
import static net.minecraftforge.eventbus.api.EventPriority.LOWEST;

@Mod("systeams")
@Mod.EventBusSubscriber(modid = Systeams.MODID)
public class Systeams {
    @SuppressWarnings("unused")
    public static final Logger LGGR = LogUtils.getLogger();
    public static final String MODID = "systeams";
    public static final ResourceLocation MODRL = new ResourceLocation(MODID, MODID);
    public static final String PNCR = "pneumaticcraft";
    public static final String MEKANISM = "mekanism";
    public static final String EXTRA = "thermal_extra";

    public static final Capability<IAirHandlerMachine> AIR_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IGasHandler> GAS_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public Systeams() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        SysteamsRegistry.init(bus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SysteamsConfig.spec, "systeams-server.toml");
        bus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(ConversionKitItem::fillDynamoMap));
        bus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> CoreClientEvents.addNamespace(MODID)));

        ModList mods = ModList.get();
        if (mods.isLoaded(PNCR))
            SysteamsPNCRCompat.unfoldPressurizedManifold(bus);
        if (mods.isLoaded(MEKANISM))
            SysteamsMekanismCompat.activateMechanisedManifold(bus);
        if (mods.isLoaded(EXTRA))
            SysteamsThermalExtraCompat.initializeExtraThermalization(bus);
    }

    /**
     * @param first The first tag
     * @param second The second tag. Will override any duplicate values in first
     * @return A new tag
     */
    @Nullable
    @Contract("null,null -> null;!null,_ -> new;_,!null -> new")
    public static CompoundTag mergeTags(@Nullable CompoundTag first, @Nullable CompoundTag second) {
        if (first == null && second == null) return null;
        if (first == null)
            return second.copy();
        else if (second == null)
            return first.copy();
        return first.copy().merge(second);
    }

    private static boolean first = true;
    @SubscribeEvent(priority = LOWEST)
    static void loginLogger(PlayerEvent.PlayerLoggedInEvent _e) {
        // if you recognise the quote, good job
        if (first) {
            LGGR.info("Welcome aboard Captain. All Systeams online");
            first = false;
        }
    }

    @SubscribeEvent(priority = LOW)
    static void supplementAugmentTooltips(ItemTooltipEvent event) {
        List<Component> tooltip = event.getToolTip();
        if (tooltip.isEmpty() || !SysteamsConfig.REPLACE_TOOLTIPS.get()) return;
        ItemStack stack = event.getItemStack();

        String augType = AugmentDataHelper.getAugmentType(stack);
        if (augType.equals(TAG_AUGMENT_TYPE_DYNAMO)) {
            replaceContents(tooltip);
        }
    }

    private static boolean replaceContents(List<Component> components) {
        for (int i = 0; i < components.size(); i++) {
            Component component = components.get(i);
            if (component.getContents() instanceof TranslatableContents translatableContents && translatableContents.getKey().equals("info.thermal.augment.type." + TAG_AUGMENT_TYPE_DYNAMO)) {
                MutableComponent replacement = Component.translatable("info.systeams.augment.type.DynamoBoiler", translatableContents.getArgs()).withStyle(component.getStyle());
                component.getSiblings().forEach(replacement::append);
                components.set(i, replacement);
                return true;
            } else {
                if (replaceContents(component.getSiblings())) return true;
            }
        }
        return false;
    }
}
