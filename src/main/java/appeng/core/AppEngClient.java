/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import guideme.Guide;
import guideme.GuidesCommon;
import guideme.PageAnchor;
import guideme.compiler.TagCompiler;
import guideme.scene.ImplicitAnnotationStrategy;

import appeng.api.parts.CableRenderMode;
import appeng.blockentity.networking.CableBusTESR;
import appeng.client.EffectType;
import appeng.client.Hotkeys;
import appeng.client.commands.ClientCommands;
import appeng.client.gui.me.common.PendingCraftingJobs;
import appeng.client.gui.me.common.PinnedKeys;
import appeng.client.gui.style.StyleManager;
import appeng.client.guidebook.ConfigValueTagExtension;
import appeng.client.guidebook.PartAnnotationStrategy;
import appeng.client.render.StorageCellClientTooltipComponent;
import appeng.client.render.crafting.CraftingMonitorRenderer;
import appeng.client.render.crafting.MolecularAssemblerRenderer;
import appeng.client.render.effects.CraftingFx;
import appeng.client.render.effects.EnergyFx;
import appeng.client.render.effects.EnergyParticleData;
import appeng.client.render.effects.LightningArcFX;
import appeng.client.render.effects.LightningFX;
import appeng.client.render.effects.MatterCannonFX;
import appeng.client.render.effects.ParticleTypes;
import appeng.client.render.effects.VibrantFX;
import appeng.client.render.model.GlassBakedModel;
import appeng.client.render.overlay.OverlayManager;
import appeng.client.render.tesr.ChargerBlockEntityRenderer;
import appeng.client.render.tesr.ChestBlockEntityRenderer;
import appeng.client.render.tesr.CrankRenderer;
import appeng.client.render.tesr.DriveLedBlockEntityRenderer;
import appeng.client.render.tesr.InscriberTESR;
import appeng.client.render.tesr.SkyChestTESR;
import appeng.client.render.tesr.SkyStoneTankBlockEntityRenderer;
import appeng.core.definitions.AEAttachmentTypes;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEEntities;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.serverbound.MouseWheelPacket;
import appeng.core.network.serverbound.UpdateHoldingCtrlPacket;
import appeng.entity.TinyTNTPrimedRenderer;
import appeng.helpers.IMouseWheelItem;
import appeng.hooks.BlockAttackHook;
import appeng.hooks.RenderBlockOutlineHook;
import appeng.init.client.InitAdditionalModels;
import appeng.init.client.InitBlockColors;
import appeng.init.client.InitBuiltInModels;
import appeng.init.client.InitEntityLayerDefinitions;
import appeng.init.client.InitItemColors;
import appeng.init.client.InitItemModelsProperties;
import appeng.init.client.InitScreens;
import appeng.init.client.InitStackRenderHandlers;
import appeng.items.storage.StorageCellTooltipComponent;
import appeng.siteexport.AESiteExporter;
import appeng.spatial.SpatialStorageDimensionIds;
import appeng.spatial.SpatialStorageSkyProperties;
import appeng.util.Platform;

/**
 * Client-specific functionality.
 */
@Mod(value = AppEng.MOD_ID, dist = Dist.CLIENT)
public class AppEngClient extends AppEngBase {
    private static final Logger LOG = LoggerFactory.getLogger(AppEngClient.class);

    private static AppEngClient INSTANCE;

    /**
     * Last known cable render mode. Used to update all rendered blocks once at the end of the tick when the mode is
     * changed.
     */
    private CableRenderMode prevCableRenderMode = CableRenderMode.STANDARD;

    /**
     * This modifier key has to be held to activate mouse wheel items.
     */
    private static final KeyMapping MOUSE_WHEEL_ITEM_MODIFIER = new KeyMapping(
            "key.ae2.mouse_wheel_item_modifier", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM,
            InputConstants.KEY_LSHIFT, "key.ae2.category");

    private static final KeyMapping PART_PLACEMENT_OPPOSITE = new KeyMapping(
            "key.ae2.part_placement_opposite", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM,
            InputConstants.KEY_LCONTROL, "key.ae2.category");

    private final Guide guide;

    public AppEngClient(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);
        InitBuiltInModels.init();

        this.registerClientCommands();

        modEventBus.addListener(this::registerClientTooltipComponents);
        modEventBus.addListener(this::registerParticleFactories);
        modEventBus.addListener(this::modelRegistryEventAdditionalModels);
        modEventBus.addListener(this::modelRegistryEvent);
        modEventBus.addListener(this::registerBlockColors);
        modEventBus.addListener(this::registerItemColors);
        modEventBus.addListener(this::registerEntityRenderers);
        modEventBus.addListener(this::registerEntityLayerDefinitions);
        modEventBus.addListener(this::registerHotkeys);
        modEventBus.addListener(this::registerDimensionSpecialEffects);
        modEventBus.addListener(InitScreens::init);
        modEventBus.addListener(this::enqueueImcMessages);

        BlockAttackHook.install();
        RenderBlockOutlineHook.install();
        guide = createGuide();

        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, (ClientTickEvent.Pre e) -> {
            updateCableRenderMode();
        });

        modEventBus.addListener(this::clientSetup);

        INSTANCE = this;

        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn evt) -> {
            PendingCraftingJobs.clearPendingJobs();
            PinnedKeys.clearPinnedKeys();
        });

        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> {
            tickPinnedKeys(Minecraft.getInstance());
            Hotkeys.checkHotkeys();
        });

        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mc, parent) -> new ConfigurationScreen(container, parent));
    }

    private void enqueueImcMessages(InterModEnqueueEvent event) {
        // Our new light-mode UI doesn't play nice with darkmodeeverywhere
        InterModComms.sendTo("darkmodeeverywhere", "dme-shaderblacklist", () -> "appeng.");
        InterModComms.sendTo("framedblocks", "add_ct_property", () -> GlassBakedModel.GLASS_STATE);
    }

    private void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(
                SpatialStorageDimensionIds.DIMENSION_TYPE_ID.location(),
                SpatialStorageSkyProperties.INSTANCE);
    }

    private void registerClientCommands() {
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent evt) -> {
            var dispatcher = evt.getDispatcher();

            LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("ae2client");
            if (AEConfig.instance().isDebugToolsEnabled()) {
                for (var commandBuilder : ClientCommands.DEBUG_COMMANDS) {
                    commandBuilder.build(builder);
                }
            }
            dispatcher.register(builder);
        });
    }

    private Guide createGuide() {

        return Guide.builder(AppEng.makeId("guide"))
                .folder("ae2guide")
                .extension(ImplicitAnnotationStrategy.EXTENSION_POINT, new PartAnnotationStrategy())
                .extension(TagCompiler.EXTENSION_POINT, new ConfigValueTagExtension())
                .build();
    }

    private void tickPinnedKeys(Minecraft minecraft) {
        // Only prune pinned keys when no screen is currently open
        if (minecraft.screen == null) {
            PinnedKeys.prune();
        }
    }

    @Override
    public Level getClientLevel() {
        return Minecraft.getInstance().level;
    }

    @Override
    public void registerHotkey(String id) {
        Hotkeys.registerHotkey(id);
    }

    private void registerHotkeys(RegisterKeyMappingsEvent e) {
        e.register(MOUSE_WHEEL_ITEM_MODIFIER);
        e.register(PART_PLACEMENT_OPPOSITE);
        Hotkeys.finalizeRegistration(e::register);
    }

    public static AppEngClient instance() {
        return Objects.requireNonNull(INSTANCE, "AppEngClient is not initialized");
    }

    public void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleTypes.CRAFTING, CraftingFx.Factory::new);
        event.registerSpriteSet(ParticleTypes.ENERGY, EnergyFx.Factory::new);
        event.registerSpriteSet(ParticleTypes.LIGHTNING_ARC, LightningArcFX.Factory::new);
        event.registerSpriteSet(ParticleTypes.LIGHTNING, LightningFX.Factory::new);
        event.registerSpriteSet(ParticleTypes.MATTER_CANNON, MatterCannonFX.Factory::new);
        event.registerSpriteSet(ParticleTypes.VIBRANT, VibrantFX.Factory::new);
    }

    public void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        InitBlockColors.init(event.getBlockColors());
    }

    public void registerItemColors(RegisterColorHandlersEvent.Item event) {
        InitItemColors.init(event);
    }

    private void registerClientTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(StorageCellTooltipComponent.class, StorageCellClientTooltipComponent::new);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            try {
                postClientSetup(minecraft);
            } catch (Throwable e) {
                LOG.error("AE2 failed postClientSetup", e);
                throw new RuntimeException(e);
            }
        });

        NeoForge.EVENT_BUS.addListener(this::wheelEvent);
        NeoForge.EVENT_BUS.addListener(this::ctrlEvent);
        NeoForge.EVENT_BUS.register(OverlayManager.getInstance());
    }

    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AEEntities.TINY_TNT_PRIMED.get(), TinyTNTPrimedRenderer::new);

        event.registerBlockEntityRenderer(AEBlockEntities.CRANK.get(), CrankRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.INSCRIBER.get(), InscriberTESR::new);
        event.registerBlockEntityRenderer(AEBlockEntities.SKY_CHEST.get(), SkyChestTESR::new);
        event.registerBlockEntityRenderer(AEBlockEntities.CHARGER.get(), ChargerBlockEntityRenderer.FACTORY);
        event.registerBlockEntityRenderer(AEBlockEntities.DRIVE.get(), DriveLedBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.ME_CHEST.get(), ChestBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.CRAFTING_MONITOR.get(), CraftingMonitorRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.MOLECULAR_ASSEMBLER.get(), MolecularAssemblerRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.CABLE_BUS.get(), CableBusTESR::new);
        event.registerBlockEntityRenderer(AEBlockEntities.SKY_STONE_TANK.get(), SkyStoneTankBlockEntityRenderer::new);
    }

    private void registerEntityLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        InitEntityLayerDefinitions.init((modelLayerLocation, layerDefinition) -> {
            event.registerLayerDefinition(modelLayerLocation, () -> layerDefinition);
        });
    }

    /**
     * Called when other mods have finished initializing and the client is now available.
     */
    private void postClientSetup(Minecraft minecraft) {
        StyleManager.initialize(minecraft.getResourceManager());
        InitStackRenderHandlers.init();

        // Only activate the site exporter when we're not running a release version, since it'll
        // replace blocks around spawn.
        if (!FMLLoader.isProduction()) {
            // Automatically run the export once the client has started and then exit
            if (Boolean.getBoolean("appeng.runGuideExportAndExit")) {
                Path outputFolder = Paths.get(System.getProperty("appeng.guideExportFolder"));

                new AESiteExporter(minecraft, outputFolder, guide)
                        .exportOnNextTickAndExit();
            }
        }
    }

    public void modelRegistryEventAdditionalModels(ModelEvent.RegisterAdditional event) {
        InitAdditionalModels.init(event);
    }

    public void modelRegistryEvent(RegisterGeometryLoaders event) {
        InitItemModelsProperties.init();
    }

    private void wheelEvent(final InputEvent.MouseScrollingEvent me) {
        if (me.getScrollDeltaY() == 0) {
            return;
        }

        final Minecraft mc = Minecraft.getInstance();
        final Player player = mc.player;
        if (MOUSE_WHEEL_ITEM_MODIFIER.isDown()) {
            var mainHand = player.getItemInHand(InteractionHand.MAIN_HAND)
                    .getItem() instanceof IMouseWheelItem;
            var offHand = player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof IMouseWheelItem;

            if (mainHand || offHand) {
                ServerboundPacket message = new MouseWheelPacket(me.getScrollDeltaY() > 0);
                PacketDistributor.sendToServer(message);
                me.setCanceled(true);
            }
        }
    }

    private void ctrlEvent(InputEvent.Key event) {
        if (event.getKey() == PART_PLACEMENT_OPPOSITE.getKey().getValue()) {
            var player = Minecraft.getInstance().player;

            if (player != null) {
                var isDown = event.getAction() == InputConstants.PRESS || event.getAction() == InputConstants.REPEAT;
                var previousIsDown = player.getData(AEAttachmentTypes.HOLDING_CTRL);
                if (previousIsDown != isDown) {
                    player.setData(AEAttachmentTypes.HOLDING_CTRL, isDown);
                    PacketDistributor.sendToServer(new UpdateHoldingCtrlPacket(isDown));
                }
            }
        }
    }

    public boolean shouldAddParticles(RandomSource r) {
        return switch (Minecraft.getInstance().options.particles().get()) {
            case ALL -> true;
            case DECREASED -> r.nextBoolean();
            case MINIMAL -> false;
        };
    }

    @Override
    public HitResult getCurrentMouseOver() {
        return Minecraft.getInstance().hitResult;
    }

    // FIXME: Instead of doing a custom packet and this dispatcher, we can use the
    // vanilla particle system
    @Override
    public void spawnEffect(EffectType effect, Level level, double posX, double posY,
            double posZ, Object o) {
        if (AEConfig.instance().isEnableEffects()) {
            switch (effect) {
                case Vibrant:
                    this.spawnVibrant(level, posX, posY, posZ);
                    return;
                case Energy:
                    this.spawnEnergy(level, posX, posY, posZ);
                    return;
                case Lightning:
                    this.spawnLightning(level, posX, posY, posZ);
                    return;
                default:
            }
        }
    }

    private void spawnVibrant(Level level, double x, double y, double z) {
        if (AppEngClient.instance().shouldAddParticles(level.getRandom())) {
            final double d0 = (level.getRandom().nextFloat() - 0.5F) * 0.26D;
            final double d1 = (level.getRandom().nextFloat() - 0.5F) * 0.26D;
            final double d2 = (level.getRandom().nextFloat() - 0.5F) * 0.26D;

            Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.VIBRANT, x + d0, y + d1, z + d2, 0.0D,
                    0.0D,
                    0.0D);
        }
    }

    private void spawnEnergy(Level level, double posX, double posY, double posZ) {
        var random = level.getRandom();
        final float x = (float) (Math.abs(random.nextInt()) % 100 * 0.01 - 0.5) * 0.7f;
        final float y = (float) (Math.abs(random.nextInt()) % 100 * 0.01 - 0.5) * 0.7f;
        final float z = (float) (Math.abs(random.nextInt()) % 100 * 0.01 - 0.5) * 0.7f;

        Minecraft.getInstance().particleEngine.createParticle(EnergyParticleData.FOR_BLOCK, posX + x, posY + y,
                posZ + z,
                -x * 0.1, -y * 0.1, -z * 0.1);
    }

    private void spawnLightning(Level level, double posX, double posY, double posZ) {
        Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.LIGHTNING, posX, posY + 0.3f, posZ, 0.0f,
                0.0f,
                0.0f);
    }

    private void updateCableRenderMode() {
        var currentMode = getCableRenderMode();

        // Handle changes to the cable-rendering mode
        if (currentMode == this.prevCableRenderMode) {
            return;
        }

        this.prevCableRenderMode = currentMode;

        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        // Invalidate all sections that contain a cable bus within view distance
        // This should asynchronously update the chunk meshes and as part of that use the new facade render mode
        var viewDistance = (int) Math.ceil(mc.levelRenderer.getLastViewDistance());
        ChunkPos.rangeClosed(mc.player.chunkPosition(), viewDistance).forEach(chunkPos -> {
            var chunk = mc.level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
            if (chunk != null) {
                for (var i = 0; i < chunk.getSectionsCount(); i++) {
                    var section = chunk.getSection(i);
                    if (section.maybeHas(state -> state.is(AEBlocks.CABLE_BUS.block()))) {
                        mc.levelRenderer.setSectionDirty(chunkPos.x, chunk.getSectionYFromSectionIndex(i), chunkPos.z);
                    }
                }
            }
        });
    }

    @Override
    public CableRenderMode getCableRenderMode() {
        if (Platform.isServer()) {
            return super.getCableRenderMode();
        }

        var mc = Minecraft.getInstance();
        if (mc.player == null) {
            return CableRenderMode.STANDARD;
        }

        return this.getCableRenderModeForPlayer(mc.player);
    }

    @Override
    public void openGuideAtAnchor(PageAnchor anchor) {
        GuidesCommon.openGuide(Minecraft.getInstance().player, guide.getId(), anchor);
    }

    public Guide getGuide() {
        return guide;
    }
}
