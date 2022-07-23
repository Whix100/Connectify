package appeng.integration.modules.jade.part;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import snownee.jade.api.ITooltip;

import appeng.api.networking.pathing.ControllerState;
import appeng.api.parts.IPart;
import appeng.core.localization.InGameTooltip;
import appeng.me.service.PathingService;
import appeng.parts.networking.IUsedChannelProvider;

/**
 * Shows the used and maximum channel count for a part that implements {@link IUsedChannelProvider}.
 */
public final class ChannelDataProvider implements IPartDataProvider {
    private static final String TAG_MAX_CHANNELS = "maxChannels";
    private static final String TAG_USED_CHANNELS = "usedChannels";
    private static final String TAG_ERROR = "channelError";

    @Override
    public void appendBodyTooltip(IPart part, CompoundTag partTag, ITooltip tooltip) {
        if (partTag.contains(TAG_ERROR, Tag.TAG_STRING)) {
            var error = ChannelError.valueOf(partTag.getString(TAG_ERROR));
            tooltip.add(error.text.text().withStyle(ChatFormatting.RED));
            return;
        }
        if (partTag.contains(TAG_MAX_CHANNELS, Tag.TAG_INT)) {
            var usedChannels = partTag.getInt(TAG_USED_CHANNELS);
            var maxChannels = partTag.getInt(TAG_MAX_CHANNELS);
            // Even in the maxChannels=0 case, we'll show as infinite
            if (maxChannels <= 0) {
                tooltip.add(InGameTooltip.Channels.text(usedChannels));
            } else {
                tooltip.add(InGameTooltip.ChannelsOf.text(usedChannels, maxChannels));
            }
        }
    }

    @Override
    public void appendServerData(ServerPlayer player, IPart part, CompoundTag partTag) {
        if (part instanceof IUsedChannelProvider usedChannelProvider) {
            var gridNode = part.getGridNode();
            if (gridNode != null) {
                var pathingService = (PathingService) gridNode.getGrid().getPathingService();
                if (pathingService.getControllerState() == ControllerState.NO_CONTROLLER) {
                    var adHocError = pathingService.getAdHocNetworkError();
                    if (adHocError != null) {
                        partTag.putString(TAG_ERROR, switch (adHocError) {
                            case NESTED_P2P_TUNNEL -> ChannelError.AD_HOC_NESTED_P2P_TUNNEL.name();
                            case TOO_MANY_CHANNELS -> ChannelError.AD_HOC_TOO_MANY_CHANNELS.name();
                        });
                        return;
                    }
                } else if (pathingService.getControllerState() == ControllerState.CONTROLLER_CONFLICT) {
                    partTag.putString(TAG_ERROR, ChannelError.CONTROLLER_CONFLICT.name());
                }
            }

            partTag.putInt(TAG_USED_CHANNELS, usedChannelProvider.getUsedChannelsInfo());
            partTag.putInt(TAG_MAX_CHANNELS, usedChannelProvider.getMaxChannelsInfo());
        }
    }

    enum ChannelError {
        AD_HOC_NESTED_P2P_TUNNEL(InGameTooltip.ErrorNestedP2PTunnel),
        AD_HOC_TOO_MANY_CHANNELS(InGameTooltip.ErrorTooManyChannels),
        CONTROLLER_CONFLICT(InGameTooltip.ErrorControllerConflict);

        final InGameTooltip text;

        ChannelError(InGameTooltip text) {
            this.text = text;
        }
    }
}
